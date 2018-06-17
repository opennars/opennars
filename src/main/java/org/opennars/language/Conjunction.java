/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.language;

import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.main.MiscFlags;

import java.util.*;

import static java.lang.System.arraycopy;

/**
 * Conjunction of statements
 */
public class Conjunction extends CompoundTerm {

    public final int temporalOrder;
    public final boolean isSpatial;

    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     * @param order
     * @param normalized
     */
    //avoids re-calculates of conv rectangle
    protected Conjunction(final Term[] arg, final int order, final boolean normalized, final boolean spatial, final CompoundTerm.ConvRectangle rect) {
        super(arg);
        this.isSpatial = spatial;
        temporalOrder = order;
        this.index_variable = rect.index_variable;
        this.term_indices = rect.term_indices;
        init(this.term);
    }
    protected Conjunction(final Term[] arg, final int order, final boolean normalized, final boolean spatial) {
        super(arg);
        this.isSpatial = spatial;
        temporalOrder = order;
        init(this.term);
        //update imagination space if it exsists (also type checking the operations):
        if(arg[0].imagination != null) {
            this.imagination=arg[0].imagination.ConstructSpace(this);
        }
    }

    @Override public Term clone(final Term[] t) {
        return make(t, temporalOrder, isSpatial);
    }

    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Conjunction clone() {
        return new Conjunction(term, temporalOrder, isNormalized(), isSpatial);
    }
    
    
    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        switch (temporalOrder) {
            case TemporalRules.ORDER_FORWARD:
                if(isSpatial) {
                    return NativeOperator.SPATIAL;
                } else {
                    return NativeOperator.SEQUENCE;
                }
            case TemporalRules.ORDER_CONCURRENT:
                return NativeOperator.PARALLEL;
            default:
                return NativeOperator.CONJUNCTION;
        }
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return temporalOrder != TemporalRules.ORDER_FORWARD;
    }

    /**
     * Try to make a new compound from a list of term. Called by StringParser.
     *
     * @return the Term generated from the arguments
     * @param argList the list of arguments
     */
    final public static Term make(final Term[] argList) {
        return make(argList, TemporalRules.ORDER_NONE);
    }

    public static boolean isConjunctionAndHasSameOrder(final Term t, final int order) {
        if(t instanceof Conjunction) {
            final Conjunction c=(Conjunction) t;
            return c.getTemporalOrder() == order;
        }
        return false;
    }
    
    public static Term[] flatten(final Term[] args, final int order, final boolean isSpatial) { //flatten only same order!
        //determine how many there are with same order
        int sz=0;
        for (final Term a : args) {
            if (isConjunctionAndHasSameOrder(a, order) && isSpatial == ((Conjunction) a).isSpatial) {
                sz += ((Conjunction) a).term.length;
            } else {
                sz += 1;
            }
        }
        final Term[] ret=new Term[sz];
        int k=0;
        for (final Term a : args) {
            if (isConjunctionAndHasSameOrder(a, order) && isSpatial == ((Conjunction) a).isSpatial) {
                final Conjunction c = ((Conjunction) a);
                for (final Term t : c.term) {
                    ret[k] = t;
                    k++;
                }
            } else {
                ret[k] = a;
                k++;
            }
        }
        return ret;
    }
    
    public static String PositiveIntString(final int value) {
        if(value == 0) {
            return "";
        } else {
            return "+"+String.valueOf(value);
        }
    }
    public static Term UpdateRelativeIndices(final int minX, final int minY, final int minsX, final int minsY, final Term term) {
        if(term instanceof CompoundTerm) {
            final CompoundTerm ct = ((CompoundTerm)term);
            for(int i=0;i<ct.term.length;i++) {
                ct.term[i]=UpdateRelativeIndices(minX, minY, minsX, minsY, ct.term[i]);
            }
            return ct;
        } else {
            if(term.term_indices != null) {
                //term indices remain the same, but representation changes
                String s = term.index_variable;
                final int relativeSizeX = term.term_indices[0]-minsX;
                final int relativeSizeY = term.term_indices[1]-minsY;
                final int relativePositionX = term.term_indices[2]-minX;
                final int relativePositionY = term.term_indices[3]-minY;
                
                s+="[i" + PositiveIntString(relativeSizeX)+
                   ",j" + PositiveIntString(relativeSizeY);
                s+=",k" + PositiveIntString(relativePositionX);
                s+=",l" + PositiveIntString(relativePositionY)+"]";
                final Term ret = Term.get(s);
                ret.term_indices = term.term_indices;
                ret.index_variable = term.index_variable;
                return ret;
            } else {
                return term; //another atomic term
            }
        }
    }
    
    /**
     * @param components The components
     * @return The components sequence with summed intervals
     * for transforming (&/,a,+1,+1) to (&/,a,+2)
     */
    public static Term[] simplifyIntervals(Term[] components) {
        List<Term> ret = new ArrayList<Term>();
        for(int i=0;i<components.length;) {
            if(components[i] instanceof Interval) {
                // add up next ones
                long ival = 0;
                for(;i<components.length && components[i] instanceof Interval; i++) {
                    ival += ((Interval)components[i]).time;
                }
                ret.add(new Interval(ival));
            }
            else {
                ret.add(components[i]);
                i++;
            }
        }
        return ret.toArray(new Term[0]);
    }
    
    /**
     * Try to make a new compound from a list of term. Called by StringParser.
     *
     * @param temporalOrder The temporal order among term
     * @param argList the list of arguments
     * @return the Term generated from the arguments, or null if not possible
     */
    final public static Term make(final Term[] argList, final int temporalOrder) {
        return make(argList, temporalOrder, false);
    }
    final public static Term make(final Term[] argList, final int temporalOrder, final boolean spatial) {
        if (MiscFlags.DEBUG) {  Terms.verifyNonNull(argList);}
        
        if (argList.length == 0) {
            return null;
        }                         // special case: single component
        if (argList.length == 1) {
            return argList[0];
        }                         // special case: single component
        
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            final Term[] newArgList = spatial ? argList : simplifyIntervals(flatten(argList, temporalOrder, spatial));
            
            if(newArgList.length == 1) {
                return newArgList[0];
            }
            return new Conjunction(newArgList, temporalOrder, false, spatial);
            
        } 
        else {
            
            // sort/merge arguments
            final NavigableSet<Term> set = new TreeSet<>();
            final Term[] flattened = flatten(argList, temporalOrder, spatial);
            final ConvRectangle rect = UpdateConvRectangle(flattened);
            for (final Term t : flattened) {
                if(!(t instanceof Interval)) { //intervals only for seqs
                    if(t.term_indices == null || rect.term_indices == null) {
                        set.add(t);
                    } 
                    else 
                    if(t instanceof CompoundTerm)
                    {   
                        final Term updated = UpdateRelativeIndices(rect.term_indices[2], rect.term_indices[3], rect.term_indices[4], rect.term_indices[5], t.cloneDeep());
                        set.add(updated);
                    }
                }
            }
            
            if (set.size() == 1) {
                return set.first();
            }
            
            return new Conjunction(set.toArray(new Term[0]), temporalOrder, false, spatial, rect);
        }
    }

    final public static Term make(final Term prefix, final Interval suffix, final int temporalOrder) {
        final Term[] t = new Term[1+1];
        int i = 0;
        t[i++] = prefix;
        t[i++] = suffix;
        return make(t, temporalOrder);        
    }
    
    final public static Term make(final Term prefix, final Interval ival, final Term suffix, final int temporalOrder) {
        final Term[] t = new Term[1+2];
        int i = 0;
        t[i++] = prefix;
        t[i++] = ival;
        t[i++] = suffix;
        return make(t, temporalOrder);        
    }
    
    /**    
     *
     * @param set a set of Term as term
     * @return the Term generated from the arguments
     */
    final private static Term make(final Collection<Term> set, final int temporalOrder, final boolean spatial) {
        final Term[] argument = set.toArray(new Term[0]);
        return make(argument, temporalOrder, spatial);
    }

    @Override
    protected CharSequence makeName() {
        return makeCompoundName( operator(),  term);
    }

    
    // overload this method by term type?
    /**
     * Try to make a new compound from two term. Called by the inference rules.
     *
     * @param term1 The first component
     * @param term2 The second component
     * @return A compound generated or a term it reduced to
     */
    final public static Term make(final Term term1, final Term term2) {
        return make(term1, term2, TemporalRules.ORDER_NONE);
    }

    final public static Term make(final Term term1, final Term term2, final int temporalOrder) {
        return make(term1, term2, temporalOrder, false);
    }
    final public static Term make(final Term term1, final Term term2, final int temporalOrder, final boolean spatial) {
        if (temporalOrder == TemporalRules.ORDER_FORWARD) {
            
            final Term[] components;
            
            if ((term1 instanceof Conjunction) && (term1.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                
                final CompoundTerm cterm1 = (CompoundTerm) term1;
                
                final List<Term> list = new ArrayList<>(cterm1.size());
                cterm1.addTermsTo(list);
                        
                if ((term2 instanceof Conjunction) && 
                        cterm1.getIsSpatial() == term2.getIsSpatial() &&
                        term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD) { 
                    // (&/,(&/,P,Q),(&/,R,S)) = (&/,P,Q,R,S)
                    ((CompoundTerm) term2).addTermsTo(list);
                } 
                else {
                    // (&,(&,P,Q),R) = (&,P,Q,R)
                    list.add(term2);
                }
                
                components = list.toArray(new Term[0]);
                
            } else if ((term2 instanceof Conjunction) && (term2.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
                final CompoundTerm cterm2 = (CompoundTerm) term2;
                components = new Term[((CompoundTerm) term2).size() + 1];
                components[0] = term1;
                arraycopy(cterm2.term, 0, components, 1, cterm2.size());
            } else {
                components = new Term[] { term1, term2 };
            }
            return make(components, temporalOrder, spatial);
            
        } else {
            
            final List<Term> set = new ArrayList();
            if (term1 instanceof Conjunction) {                
                ((CompoundTerm) term1).addTermsTo(set);
                if (term2 instanceof Conjunction) {                    
                    // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                    ((CompoundTerm) term2).addTermsTo(set);
                } 
                else {
                    // (&,(&,P,Q),R) = (&,P,Q,R)
                    set.add(term2);
                }                          
                
            } else if (term2 instanceof Conjunction) {
                ((CompoundTerm) term2).addTermsTo(set);
                set.add(term1);                              // (&,R,(&,P,Q)) = (&,P,Q,R)
            } else {                
                set.add(term1);
                set.add(term2);
            }
            
            return make(set, temporalOrder, spatial);
        }
    }

    @Override
    public int getTemporalOrder() {
        return temporalOrder;
    }
    
    @Override
    public boolean getIsSpatial() {
        return isSpatial;
    }
}

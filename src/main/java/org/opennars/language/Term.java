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

import org.apache.commons.lang3.StringUtils;
import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.io.Texts;
import org.opennars.main.MiscFlags;
import org.opennars.operator.ImaginationSpace;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.storage.Memory;

import java.io.Serializable;
import java.util.*;
//import org.opennars.util.sort.SortedList;

/**
 * Term is the basic component of Narsese, and the object of processing in NARS.
 * <p>
 * A Term may have an associated Concept containing relations with other Terms.
 * It is not linked in the Term, because a Concept may be forgot while the Term
 * exists. Multiple objects may represent the same Term.
 */
public class Term implements AbstractTerm, Serializable {
    public ImaginationSpace imagination;
    private static final Map<CharSequence,Term> atoms = new HashMap();

    final public static Term SELF = SetExt.make(Term.get("SELF"));
    final public static Term SEQ_SPATIAL = Term.get("#");
    final public static Term SEQ_TEMPORAL = Term.get("&/");

    final public static boolean isSelf(final Term t) {
        return SELF.equals(t);
    }

    public NativeOperator operator() {
        return NativeOperator.ATOM;
    }
    
    public boolean isHigherOrderStatement() { //==> <=>
        return (this instanceof Equivalence) || (this instanceof Implication);
    }
    
    public boolean isExecutable(final Memory mem) {
        //don't allow ^want and ^believe to be active/have an effect, 
        //which means its only used as monitor
        final boolean isOp=this instanceof Operation;
        if(isOp) {
            final Operator op=((Operation)this).getOperator(); //the following part may be refactored after we
            //know more about how the NAL9 concepts should really interact together:
            /*if(op.equals(mem.getOperator("^want")) || op.equals(mem.getOperator("^believe"))) {
                return false;
            }*/
        }
        return isOp;
    }


    public interface TermVisitor {
        void visit(Term t, Term superterm);
    }
    
    
    
    protected CharSequence name = null;
    
    /**
     * Default constructor that build an internal Term
     */
    protected Term() {
    }

    /**
     * Constructor with a given name
     *
     * @param name A String as the name of the Term
     */
    public Term(final CharSequence name) {
        setName(name);
    }
    
    /** gets the atomic term given a name */
    public final static Term get(final CharSequence name) {
        Term x = atoms.get(name); //only
        if (x != null && !x.toString().endsWith("]")) { //return only if it isn't an index term
            return x;
        }

        final String namestr = name.toString();
        //p[s,i,j]
        int[] term_indices = null;
        String before_indices_str = null;
        if(namestr.endsWith("]") && namestr.contains("[")) { //simple check, failing for most terms
            String indices_str = namestr.split("\\[")[1].split("\\]")[0];
            before_indices_str = namestr.split("\\[")[0];
            String[] inds = indices_str.split(",");
            if(inds.length == 2) { //only position info given
                indices_str="1,1,"+indices_str;
                inds = indices_str.split(",");
            }
            term_indices = new int[inds.length];
            for(int i=0;i<inds.length;i++) {
                if(StringUtils.isNumeric(inds[i]))
                    term_indices[i] = Integer.valueOf(inds[i]);
                else {
                    term_indices = null;
                    break;
                }
            }
        }
        
        CharSequence name2 = name;
        if(term_indices != null) { //only on conceptual level not
            name2 = before_indices_str + "[i,j,k,l]";
        }
        x = new Term(name2);
        x.term_indices = term_indices;
        x.index_variable = before_indices_str;
        atoms.put(name2, x);
        
        return x;
    }
    
    /** gets the atomic term of an integer */
    public final static Term get(final int i) {
        return get(Integer.toString(i));
    }
    

    /**
     * Reporting the name of the current Term.
     *
     * @return The name of the term as a String
     */
    @Override
    public CharSequence name() {
        return name;
    }
    
    public int[] term_indices = null;
    public String index_variable = "";

    /**
     * Make a new Term with the same name.
     *
     * @return The new Term
     */
    @Override
    public Term clone() {
        //avoids setName and its intern(); the string will already be intern:
        final Term t = new Term();
        if(term_indices != null) {
            t.term_indices = term_indices.clone();
            t.index_variable = index_variable;
        }
        t.name = name();
        t.imagination = imagination;
        return t;
    }
    
    public Term cloneDeep() {
        return clone();
    }

    /**
     * Equal terms have identical name, though not necessarily the same
     * reference.
     *
     * @return Whether the two Terms are equal
     * @param that The Term to be compared with the current Term
     */
    @Override
    public boolean equals(final Object that) {
        if (that == this) return true;
        if (getClass() != this.getClass()) return false; //optimization, if complexity is different they cant be equal
        return this.getComplexity() == ((Term) that).getComplexity() && name().equals(((Term)that).name());
    }

    /**
     * Produce a hash code for the term
     *
     * @return An integer hash code
     */
    @Override
    public int hashCode() {
        return name().hashCode();
    }

    /**
     * Check whether the current Term can name a Concept.
     * isConstant means if the term contains free variable
     * True if:
     *   has zero variables, or
     *   uses several instances of the same variable
     * False if it uses one instance of a variable ("free" like a "free radical" in chemistry).
     * Therefore it may be considered Constant, yet actually contain variables.
     * 
     * @return A Term is constant by default
     */
    @Override
    public boolean isConstant() {        
        return true;
    }
    
    public int getTemporalOrder() {
        return TemporalRules.ORDER_NONE;
    }
    
    public boolean getIsSpatial() {
        return false;
    }

    public void recurseTerms(final TermVisitor v, final Term parent) {
        v.visit(this, parent);
        if (this instanceof CompoundTerm) {            
            for (final Term t : ((CompoundTerm)this).term) {
                t.recurseTerms(v, this);
            }
        }
    }
    
    public void recurseSubtermsContainingVariables(final TermVisitor v) {
        recurseTerms(v, null);
    }
    
    public void recurseSubtermsContainingVariables(final TermVisitor v, final Term parent) {
        if (!hasVar()) return;
        v.visit(this, parent);
        if (this instanceof CompoundTerm) {
            for (final Term t : ((CompoundTerm)this).term) {
                t.recurseSubtermsContainingVariables(v, this);
            }
        }
    }
         
    /**
     * The syntactic complexity, for constant atomic Term, is 1.
     *
     * @return The complexity of the term, an integer
     */
    public short getComplexity() {
        return 1;
    }

    /** only method that should modify Term.name. also caches hashcode 
     * @return whether the name was changed
     */
    protected void setName(final CharSequence newName) {
        this.name = newName;
    }
    
    /**
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     */
    @Override
    public int compareTo(final AbstractTerm that) {
        if (that==this) return 0;
        
        if (MiscFlags.TERM_ELEMENT_EQUIVALENCY) {
            if (!getClass().equals(that.getClass())) {
                //differnt class, use class as ordering
                return getClass().getSimpleName().compareTo(that.getClass().getSimpleName());
            }
            else {
                //same class, compare by name()
                return Texts.compareTo(name(), that.name());
            }

        }
        else {
            //previously: Orders among terms: variable < atomic < compound
            if ((that instanceof Variable) && (getClass()!=Variable.class))
                return 1;
            else if ((this instanceof Variable) && (that.getClass()!=Variable.class))
                return -1;
            return Texts.compareTo(name(), that.name());            
        }
    }

    
    
    public int containedTemporalRelations() {
        return 0;
    }
    
    /**
     * Recursively check if a compound contains a term
     *
     * @param target The term to be searched
     * @return Whether the two have the same content
     */
    public boolean containsTermRecursively(final Term target) {
        if(target==null) {
            return false;
        }
        return equals(target);
    }

    /** whether this contains a term in its components. */
    public boolean containsTerm(final Term target) {
        return equals(target);
    }

    /**
     * The same as getName by default, used in display only.
     *
     * @return The name of the term as a String
     */
    @Override
    public final String toString() {
        return name().toString();
    }

    /** Creates a quote-escaped term from a string. Useful for an atomic term that is meant to contain a message as its name */
    public static Term text(final String t) {
        return Term.get(Texts.escape('"' + t + '"').toString());
    }


    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    @Override public boolean hasVar() {
        return false;
    }
    
    public boolean hasVar(final char type) {
        switch (type) {
            case Symbols.VAR_DEPENDENT: return hasVarDep();
            case Symbols.VAR_INDEPENDENT: return hasVarIndep();
            case Symbols.VAR_QUERY: return hasVarQuery();
        }
        throw new IllegalStateException("Invalid variable type: " + type);
    }
    
    public boolean hasVarIndep() {
        return false;
    }
    
    public boolean hasInterval() {
        return false;
    }

    public boolean hasVarDep() {
        return false;
    }

    public boolean hasVarQuery() {
        return false;
    }

    public static NavigableSet<Term> toSortedSet(final Term... arg) {
        //use toSortedSetArray where possible
        final NavigableSet<Term> t = new TreeSet();
        Collections.addAll(t, arg);
        return t;        
    }
    
    public final static Term[] EmptyTermArray = new Term[0];
    
    public static Term[] toSortedSetArray(final Term... arg) {
        switch (arg.length) {
            case 0: return EmptyTermArray;                
            case 1: return new Term[] { arg[0] };
            case 2: 
                final Term a = arg[0];
                final Term b = arg[1];
                final int c = a.compareTo(b);

                if (MiscFlags.DEBUG) {
                    //verify consistency of compareTo() and equals()
                    final boolean equal = a.equals(b);
                    if ((equal && (c!=0)) || (!equal && (c==0))) {
                        throw new IllegalStateException("invalid order: " + a + " = " + b);
                    }
                }

                if (c < 0) return new Term[] { a, b };
                else if (c > 0) return new Term[] { b, a };
                else if (c == 0) return new Term[] { a }; //equal
                
        }
        
        //TODO fast sorted array for arg.length == 3

        //terms > 2:        
        final NavigableSet<Term> s = new TreeSet();
        //SortedList<Term> s = new SortedList(arg.length);
        //s.setAllowDuplicate(false);

        Collections.addAll(s, arg);
        
        return s.toArray(new Term[0]);
    }

    /** performs a thorough check of the validity of a term (by cloneDeep it) to see if it's valid */
    public static boolean valid(final Term content) {
        final Term cloned = content.cloneDeep();
        return cloned != null;
    }

    public boolean subjectOrPredicateIsIndependentVar() {
        if(this instanceof Statement) {
            final Statement cont=(Statement)this;
            if(cont.getSubject() instanceof Variable) {
                final Variable v=(Variable) cont.getSubject();
                if(v.hasVarIndep()) {
                    return true;
                }
            }
            if(cont.getPredicate()instanceof Variable) {
                final Variable v=(Variable) cont.getPredicate();
                return v.hasVarIndep();
            }
        }
        return false;
    }
}

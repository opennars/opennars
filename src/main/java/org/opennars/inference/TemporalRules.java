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
package org.opennars.inference;

import org.opennars.control.DerivationContext;
import org.opennars.control.TemporalInferenceControl;
import org.opennars.entity.*;
import org.opennars.io.Symbols;
import org.opennars.language.*;
import org.opennars.operator.Operation;

import java.util.*;

/**
 *
 * @author peiwang
 */
public class TemporalRules {

    public static final int ORDER_NONE = 2;
    public static final int ORDER_FORWARD = 1;
    public static final int ORDER_CONCURRENT = 0;
    public static final int ORDER_BACKWARD = -1;
    public static final int ORDER_INVALID = -2;

    public final static int reverseOrder(final int order) {
        if (order == ORDER_NONE) {
            return ORDER_NONE;
        } else {
            return -order;
        }
    }

    public final static boolean matchingOrder(final Sentence a, final Sentence b) {
        return matchingOrder(a.getTemporalOrder(), b.getTemporalOrder());
    }
    
    public final static boolean matchingOrder(final int order1, final int order2) {
        return (order1 == order2) || (order1 == ORDER_NONE) || (order2 == ORDER_NONE);
    }

    public final static int dedExeOrder(final int order1, final int order2) {
        int order = ORDER_INVALID;
        if ((order1 == order2) || (order2 == TemporalRules.ORDER_NONE)) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = order2;
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            order = order1;
        }
        return order;
    }

    public final static int abdIndComOrder(final int order1, final int order2) {
        int order = ORDER_INVALID;
        if (order2 == TemporalRules.ORDER_NONE) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = reverseOrder(order2);
        } else if ((order2 == TemporalRules.ORDER_CONCURRENT) || (order1 == -order2)) {
            order = order1;
        }
        return order;
    }

    public final static int analogyOrder(final int order1, final int order2, final int figure) {
        int order = ORDER_INVALID;
        if ((order2 == TemporalRules.ORDER_NONE) || (order2 == TemporalRules.ORDER_CONCURRENT)) {
            order = order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = (figure < 20) ? order2 : reverseOrder(order2);
        } else if (order1 == order2) {
            if ((figure == 12) || (figure == 21)) {
                order = order1;
            }
        } else if ((order1 == -order2)) {
            if ((figure == 11) || (figure == 22)) {
                order = order1;
            }
        }
        return order;
    }

    public static final int resemblanceOrder(final int order1, final int order2, final int figure) {
        int order = ORDER_INVALID;
        final int order1Reverse = reverseOrder(order1);
        
        if ((order2 == TemporalRules.ORDER_NONE)) {
            order = (figure > 20) ? order1 : order1Reverse; // switch when 11 or 12
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = (figure % 10 == 1) ? order2 : reverseOrder(order2); // switch when 12 or 22
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            order = (figure > 20) ? order1 : order1Reverse; // switch when 11 or 12
        } else if (order1 == order2) {
            order = (figure == 21) ? order1 : -order1;
        }
        return order;
    }

    public static final int composeOrder(final int order1, final int order2) {
        int order = ORDER_INVALID;
        if (order2 == TemporalRules.ORDER_NONE) {
            order = order1;
        } else if (order1 == TemporalRules.ORDER_NONE) {
            order = order2;
        } else if (order1 == order2) {
            order = order1;
        }
        return order;
    }
    
    /** whether temporal induction can generate a task by avoiding producing wrong terms; only one temporal operator is allowed */
    public final static boolean tooMuchTemporalStatements(final Term t) {
        return (t == null) || (t.containedTemporalRelations() > 1);
    }
    
    /** whether a term can be used in temoralInduction(,,) */
    protected static boolean termForTemporalInduction(final Term t) {
        return (t instanceof Inheritance) || (t instanceof Similarity);
    }
    
    //TODO maybe split &/ case into own function
    public static List<Task> temporalInduction(final Sentence s1, final Sentence s2, final org.opennars.control.DerivationContext nal, final boolean SucceedingEventsInduction, final boolean addToMemory, final boolean allowSequence) {
        
        if ((s1.truth==null) || (s2.truth==null) || s1.punctuation!=Symbols.JUDGMENT_MARK || s2.punctuation!=Symbols.JUDGMENT_MARK
                || s1.isEternal() || s2.isEternal())
            return Collections.emptyList();
        
        Term t1 = s1.term;
        Term t2 = s2.term;
               
        final boolean deriveSequenceOnly = (!addToMemory) || Statement.invalidStatement(t1, t2, true);
        if (Statement.invalidStatement(t1, t2, false))
            return Collections.emptyList();
        
        Term t11=null;
        Term t22=null;
        
        if (!deriveSequenceOnly && termForTemporalInduction(t1) && termForTemporalInduction(t2)) {
            
            final Statement ss1 = (Statement) t1;
            final Statement ss2 = (Statement) t2;

            final Variable var1 = new Variable("$0");
            final Variable var2 = new Variable("$1");
            
            if(ss2.containsTermRecursively(ss1.getSubject())) {
                final Map<Term,Term> subs=new HashMap();
                subs.put(ss1.getSubject(), var1);
                if(ss2.containsTermRecursively(ss1.getPredicate())) {
                    subs.put(ss1.getPredicate(), var2);
                }
                t11=ss1.applySubstitute(subs);
                t22=ss2.applySubstitute(subs);
            }
            
            if(ss1.containsTermRecursively(ss2.getSubject())) {
                final Map<Term,Term> subs=new HashMap();
                subs.put(ss2.getSubject(), var1);
                if(ss1.containsTermRecursively(ss2.getPredicate())) {
                    subs.put(ss2.getPredicate(), var2);
                }
                t11=ss1.applySubstitute(subs);
                t22=ss2.applySubstitute(subs);
            }
            
            //allow also temporal induction on operator arguments:
            if(ss2 instanceof Operation ^ ss1 instanceof Operation) {
                if(ss2 instanceof Operation && !(ss2.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
                    final Term comp=ss1.getSubject();
                    final Term ss2_term = ss2.getSubject();
                    final boolean applicableVariableType = !(comp instanceof Variable && comp.hasVarIndep());
                    
                    if(ss2_term instanceof Product) {
                        final Product ss2_prod=(Product) ss2_term;
                        
                        if(applicableVariableType && Terms.contains(ss2_prod.term, comp)) { //only if there is one and it isnt a variable already
                            
                            final Term[] ars = ss2_prod.cloneTermsReplacing(comp, var1);
                            t11 = Statement.make(ss1, var1, ss1.getPredicate());
                            final Operation op=(Operation) Operation.make(
                                    new Product(ars), 
                                    ss2.getPredicate()
                            );
                            t22 = op;
                        }
                    }
                }
                if(ss1 instanceof Operation && !(ss1.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
                    final Term comp=ss2.getSubject();
                    final Term ss1_term = ss1.getSubject();
                    
                    final boolean applicableVariableType = !(comp instanceof Variable && comp.hasVarIndep());
                    
                    if(ss1_term instanceof Product) {
                        final Product ss1_prod=(Product) ss1_term;
                                               
                        if(applicableVariableType && Terms.contains(ss1_prod.term, comp)) { //only if there is one and it isnt a variable already
                            
                            final Term[] ars = ss1_prod.cloneTermsReplacing(comp, var1);
                            t22 = Statement.make(ss2, var1, ss2.getPredicate());
                            final Operation op=(Operation) Operation.make(
                                    new Product(ars), 
                                    ss1.getPredicate()
                            );
                            t11 = op;
                        }
                    }
                }
            }
        }

        final int durationCycles = nal.narParameters.DURATION;
        final long time1 = s1.getOccurenceTime();
        final long time2 = s2.getOccurenceTime();
        final long timeDiff = time2 - time1;
        Interval interval=null;
        
        if (!concurrent(time1, time2, durationCycles)) {
            
            interval = new Interval(Math.abs(timeDiff));
            
            if (timeDiff > 0) {
                t1 = Conjunction.make(t1, interval, ORDER_FORWARD);
                if(t11!=null) {
                    t11 = Conjunction.make(t11, interval, ORDER_FORWARD);
                }
            } else {
                t2 = Conjunction.make(t2, interval, ORDER_FORWARD);
                if(t22!=null) {
                    t22 = Conjunction.make(t22, interval, ORDER_FORWARD);
                }
            }
        }
        final int order = order(timeDiff, durationCycles);
        final TruthValue givenTruth1 = s1.truth;
        TruthValue givenTruth2 = s2.truth;
        
        //This code adds a penalty for large time distance (TODO probably revise)
        final Sentence s3 = s2.projection(s1.getOccurenceTime(), nal.time.time(), nal.memory);
        givenTruth2 = s3.truth; 
        
        //Truth and priority calculations
        final TruthValue truth1 = TruthFunctions.induction(givenTruth1, givenTruth2, nal.narParameters);
        final TruthValue truth2 = TruthFunctions.induction(givenTruth2, givenTruth1, nal.narParameters);
        final TruthValue truth3 = TruthFunctions.comparison(givenTruth1, givenTruth2, nal.narParameters);
        final TruthValue truth4 = TruthFunctions.intersection(givenTruth1, givenTruth2, nal.narParameters);
        final BudgetValue budget1 = BudgetFunctions.forward(truth1, nal);
        final BudgetValue budget2 = BudgetFunctions.forward(truth2, nal);
        final BudgetValue budget3 = BudgetFunctions.forward(truth3, nal);
        final BudgetValue budget4 = BudgetFunctions.forward(truth4, nal); //this one is sequence in sequenceBag, no need to reduce here
        
        final Statement statement1 = Implication.make(t1, t2, order);
        final Statement statement2 = Implication.make(t2, t1, reverseOrder(order));
        final Statement statement3 = Equivalence.make(t1, t2, order);
        Term statement4 = null;
        switch (order) {
            case TemporalRules.ORDER_FORWARD:
                statement4 = Conjunction.make(t1, interval, s2.term, order);
                break;
            case TemporalRules.ORDER_BACKWARD:
                statement4 = Conjunction.make(s2.term, interval, t1, reverseOrder(order));
                break;
            default:
                statement4 = Conjunction.make(t1, s2.term, order);
                break;
        }
        
        //maybe this way is also the more flexible and intelligent way to introduce variables for the case above
        //TODO: rethink this for 1.6.3
        //"Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
        if(!deriveSequenceOnly && statement2!=null) { //there is no general form
            //ok then it may be the (&/ =/> case which 
            //is discussed here: https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
            final Statement st=statement2;
            if(st.getPredicate() instanceof Inheritance && (st.getSubject() instanceof Conjunction || st.getSubject() instanceof Operation)) {
                final Term precon= st.getSubject();
                final Inheritance consequence=(Inheritance) st.getPredicate();
                final Term pred=consequence.getPredicate();
                final Term sub=consequence.getSubject();
                //look if subject is contained in precon:
                final boolean SubsSub=precon.containsTermRecursively(sub);
                final boolean SubsPred=precon.containsTermRecursively(pred);
                final Variable v1=new Variable("$91");
                final Variable v2=new Variable("$92");
                final Map<Term,Term> app= new HashMap<>();
                if(SubsSub || SubsPred) {
                    if(SubsSub)
                        app.put(sub, v1);
                    if(SubsPred)
                        app.put(pred,v2);
                    final Term res= statement2.applySubstitute(app);
                    if(res!=null) { //ok we applied it, all we have to do now is to use it
                        t22=((Statement)res).getSubject();
                        t11=((Statement)res).getPredicate();
                    }
                }
             }
        }
        
        final List<Task> derivations= new ArrayList<>();
        if (!deriveSequenceOnly ) {
            if (t11!=null && t22!=null) {
                final Statement statement11 = Implication.make(t11, t22, order);
                final Statement statement22 = Implication.make(t22, t11, reverseOrder(order));
                final Statement statement33 = Equivalence.make(t11, t22, order);
                appendConclusion(nal, truth1, budget1, statement11, derivations);
                appendConclusion(nal, truth2, budget2, statement22, derivations);
                appendConclusion(nal, truth3, budget3, statement33, derivations);
            }

            appendConclusion(nal, truth1, budget1, statement1, derivations);
            appendConclusion(nal, truth2, budget2, statement2, derivations);
            appendConclusion(nal, truth3, budget3, statement3, derivations);
        }

        if(!tooMuchTemporalStatements(statement4)) {
            if(!allowSequence) {
                return derivations;
            }
            final List<Task> tl=nal.doublePremiseTask(statement4, truth4, budget4,true, false, addToMemory);
            if(tl!=null) {
                for(final Task t : tl) {
                    //fill sequenceTask buffer due to the new derived sequence
                    if(addToMemory &&
                            t.sentence.isJudgment() &&
                            !t.sentence.isEternal() && 
                            t.sentence.term instanceof Conjunction && 
                            t.sentence.term.getTemporalOrder() != TemporalRules.ORDER_NONE &&
                            t.sentence.term.getTemporalOrder() != TemporalRules.ORDER_INVALID) {
                        TemporalInferenceControl.addToSequenceTasks(nal, t);
                    }

                    derivations.add(t);
                }
            }
        }

        return derivations;
    }

    private static void appendConclusion(DerivationContext nal, TruthValue truth1, BudgetValue budget1, Statement statement1, List<Task> success) {
        if(!tooMuchTemporalStatements(statement1)) {
            final List<Task> t=nal.doublePremiseTask(statement1, truth1, budget1, true, false);
            if(t!=null) {
                success.addAll(t);
            }
        }
    }

    public static int order(final long timeDiff, final int durationCycles) {
        final int halfDuration = durationCycles/2;
        if (timeDiff > halfDuration) {
            return ORDER_FORWARD;
        } else if (timeDiff < -halfDuration) {
            return ORDER_BACKWARD;
        } else {
            return ORDER_CONCURRENT;
        }
    }
    /** if (relative) event B after (stationary) event A then order=forward;
     *                event B before       then order=backward
     *                occur at the same time, relative to duration: order = concurrent
     */
    public static int order(final long a, final long b, final int durationCycles) {        
        if ((a == Stamp.ETERNAL) || (b == Stamp.ETERNAL))
            throw new IllegalStateException("order() does not compare ETERNAL times");
        
        return order(b - a, durationCycles);
    }
    
    public static boolean concurrent(final long a, final long b, final int durationCycles) {        
        //since Stamp.ETERNAL is Integer.MIN_VALUE, 
        //avoid any overflow errors by checking eternal first
        
        if (a == Stamp.ETERNAL) {
            //if both are eternal, consider concurrent.  this is consistent with the original
            //method of calculation which compared equivalent integer values only
            return (b == Stamp.ETERNAL);
        }
        else if (b == Stamp.ETERNAL) {
            return false; //a==b was compared above
        }
        else {        
            return order(a, b, durationCycles) == ORDER_CONCURRENT;
        }
    }
    
}

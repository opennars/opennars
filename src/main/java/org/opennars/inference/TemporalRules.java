/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
 * @author Pei Wang
 * @author Patrick Hammer
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
        
        final int durationCycles = nal.narParameters.DURATION;
        final long time1 = s1.getOccurenceTime();
        final long time2 = s2.getOccurenceTime();
        final long timeDiff = time2 - time1;
        Interval interval=null;
        
        if (!concurrent(time1, time2, durationCycles)) {
            interval = new Interval(Math.abs(timeDiff));
            if (timeDiff > 0) {
                t1 = Conjunction.make(t1, interval, ORDER_FORWARD);
            } else {
                t2 = Conjunction.make(t2, interval, ORDER_FORWARD);
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
        
        List<Term> t11s = new ArrayList<>();
        List<Term> t22s = new ArrayList<>();
        //"Perception Variable Introduction Rule" - https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE
        if(!deriveSequenceOnly && statement2!=null) {
            for(boolean subjectIntro : new boolean[]{true, false}) {
                Set<Term> ress = CompositionalRules.introduceVariables(nal, statement2, subjectIntro);
                for(Term res : ress) { //ok we applied it, all we have to do now is to use it
                    t11s.add(((Statement)res).getPredicate());
                    t22s.add(((Statement)res).getSubject());
                }
            }
        }
        
        final List<Task> derivations= new ArrayList<>();
        if (!deriveSequenceOnly ) {
            for(int i=0; i<t11s.size(); i++) {
                Term t11 = t11s.get(i);
                Term t22 = t22s.get(i);
                final Statement statement11 = Implication.make(t11, t22, order);
                final Statement statement22 = Implication.make(t22, t11, reverseOrder(order));
                final Statement statement33 = Equivalence.make(t11, t22, order);
                appendConclusion(nal, truth1.clone(), budget1.clone(), statement11, derivations);
                appendConclusion(nal, truth2.clone(), budget2.clone(), statement22, derivations);
                appendConclusion(nal, truth3.clone(), budget3.clone(), statement33, derivations);
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

/*
 * Copyright (C) 2014 peiwang
 *
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
package nars.nal.nal7;


import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.nal.*;
import nars.nal.stamp.Stamp;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal8.Operation;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Variable;
import nars.operate.mental.Mental;

import java.util.List;

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
        
        if (order == ORDER_INVALID) {
            throw new RuntimeException("ORDER_INVALID not handled here");
        }
        
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
        if ((order1 == order2) || (order2 == TemporalRules.ORDER_NONE)) {
            return order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            return order2;
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            return order1;
        }
        return ORDER_INVALID;
    }

    public final static int abdIndComOrder(final int order1, final int order2) {
        if (order2 == TemporalRules.ORDER_NONE) {
            return order1;
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            return reverseOrder(order2);
        } else if ((order2 == TemporalRules.ORDER_CONCURRENT) || (order1 == -order2)) {
            return order1;
        }
        return ORDER_INVALID;
    }

    public final static int analogyOrder(final int order1, final int order2, final int figure) {

        if ((order2 == TemporalRules.ORDER_NONE) || (order2 == TemporalRules.ORDER_CONCURRENT)) {
            return order1;
        }
        else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            return (figure < 20) ? order2 : reverseOrder(order2);
        }
        else if (order1 == order2) {
            if ((figure == 12) || (figure == 21)) {
                return order1;
            }
        }
        else if ((order1 == -order2)) {
            if ((figure == 11) || (figure == 22)) {
                return order1;
            }
        }
        return ORDER_INVALID;
    }

    public static final int resemblanceOrder(final int order1, final int order2, final int figure) {
        if ((order2 == TemporalRules.ORDER_NONE)) {
            return (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            return (figure % 10 == 1) ? order2 : reverseOrder(order2); // switch when 12 or 22
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            return (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if (order1 == order2) {
            return (figure == 21) ? order1 : -order1;
        }
        return ORDER_INVALID;
    }

    public static final int composeOrder(final int order1, final int order2) {
        if (order2 == TemporalRules.ORDER_NONE) {
            return order1;
        } else if (order1 == TemporalRules.ORDER_NONE) {
            return order2;
        } else if (order1 == order2) {
            return order1;
        }
        return ORDER_INVALID;
    }
    
    /** whether temporal induction can generate a task by avoiding producing wrong terms; only one temporal operate is allowed */
    public final static boolean tooMuchTemporalStatements(final Term t, int maxTemporalRelations) {
        return (t == null) || (t.containedTemporalRelations() > maxTemporalRelations);
    }

    //is input or by the system triggered operation
    public static boolean isInputOrTriggeredOperation(final Task newEvent, Memory mem) {
        if (newEvent.isInput()) return true;
        if (containsMentalOperator(newEvent)) return true;
        if (newEvent.getCause()!=null) return true;
        return false;
    }


    public static boolean containsMentalOperator(final Task t) {
        return containsMentalOperator(t.getTerm(), true);
        /*
        if(!(t.sentence.term instanceof Operation))
            return false;

        Operation o= (Operation)t.sentence.term;
        return (o.getOperator() instanceof Mental);
        */
    }

    public static boolean containsMentalOperator(Term t, boolean recurse) {
        if (t instanceof Operation) {
            Operation o= (Operation)t;
            if (o.getOperator() instanceof Mental) return true;
        }
        if ((recurse) && (t instanceof Compound)) {
            for (Term s : ((Compound)t)) {
                if (containsMentalOperator(s, true)) return true;
            }
        }

        return false;
    }


    public static long applyExpectationOffset(final Memory memory, final Term temporalStatement, final long occurrenceTime) {
        if (occurrenceTime==Stamp.ETERNAL) return Stamp.ETERNAL;

        if(temporalStatement!=null && temporalStatement instanceof Implication) {
            Implication imp=(Implication) temporalStatement;
            if(imp.getSubject() instanceof Conjunction && imp.getTemporalOrder()==TemporalRules.ORDER_FORWARD)  {
                Conjunction conj=(Conjunction) imp.getSubject();
                if(conj.term[conj.term.length-1] instanceof Interval) {
                    Interval intv=(Interval) conj.term[conj.term.length-1];
                    long time_offset=intv.durationCycles(memory);
                    return (occurrenceTime+time_offset);
                }
            }
        }
        return Stamp.ETERNAL;
    }



    /** whether a term can be used in temoralInduction(,,) */
    protected static boolean termForTemporalInduction(final Term t) {
        /*
                //if(t1 instanceof Operation && t2 instanceof Operation) {
        //   return; //maybe too restrictive
        //}
        if(((t1 instanceof Implication || t1 instanceof Equivalence) && t1.getTemporalOrder()!=TemporalRules.ORDER_NONE) ||
           ((t2 instanceof Implication || t2 instanceof Equivalence) && t2.getTemporalOrder()!=TemporalRules.ORDER_NONE)) {
            return; //better, if this is fullfilled, there would be more than one temporal operate in the statement, return
        }
        */

        return (t instanceof Inheritance) || (t instanceof Similarity);
    }

    /*
    public static void applyExpectationOffset(Memory memory, Term temporalStatement, Stamp stamp) {
        if(temporalStatement!=null && temporalStatement instanceof Implication) {
            Implication imp=(Implication) temporalStatement;
            if(imp.getSubject() instanceof Conjunction && imp.getTemporalOrder()==TemporalRules.ORDER_FORWARD)  {
                Conjunction conj=(Conjunction) imp.getSubject();
                if(conj.term[conj.term.length-1] instanceof Interval) {
                    Interval intv=(Interval) conj.term[conj.term.length-1];
                    long time_offset=intv.durationCycles(memory);
                    stamp.setOccurrenceTime(stamp.getOccurrenceTime()+time_offset);
                }
            }
        }
    }*/



    public static void temporalInduction(final Sentence s1, final Sentence s2, NAL.StampBuilder stamp, final NAL nal) {
        temporalInduction(s1, s2, stamp, nal, nal.getCurrentTask());
    }

    final static Variable var1 = new Variable("$0");

    public static void temporalInduction(final Sentence s1, final Sentence s2, NAL.StampBuilder stamp, final NAL nal, Task subbedTask) {
        
        if ((s1.truth==null) || (s2.truth==null))
            return;
        
        Term t1 = s1.term;
        Term t2 = s2.term;
                
        if (Statement.invalidStatement(t1, t2))
            return;
        
        Term t11=null;
        Term t22=null;
        
        if (termForTemporalInduction(t1) && termForTemporalInduction(t2)) {
            
            Statement ss1 = (Statement) t1;
            Statement ss2 = (Statement) t2;


            final Variable var2 = var1;

            if (ss1.getSubject().equals(ss2.getSubject())) {
                t11 = Terms.makeStatement(ss1, var1, ss1.getPredicate());
                t22 = Terms.makeStatement(ss2, var2, ss2.getPredicate());
            } else if (ss1.getPredicate().equals(ss2.getPredicate())) {
                t11 = Terms.makeStatement(ss1, ss1.getSubject(), var1);
                t22 = Terms.makeStatement(ss2, ss2.getSubject(), var2);
            }
            //allow also temporal induction on operate arguments:
            if(ss2 instanceof Operation ^ ss1 instanceof Operation) {
                if(ss2 instanceof Operation && !(ss2.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
                    Term comp=ss1.getSubject();
                    Term ss2_term = ss2.getSubject();
                    
                    boolean applicableVariableType = !(comp instanceof Variable && comp.hasVarIndep());
                    
                    if(ss2_term instanceof Product) {
                        Product ss2_prod=(Product) ss2_term;
                        
                        if(applicableVariableType && Terms.contains(ss2_prod.term, comp)) { //only if there is one and it isnt a variable already
                            Term[] ars = ss2_prod.cloneTermsReplacing(comp, var1);

                            t11 = Terms.makeStatement(ss1, var1, ss1.getPredicate());
                            
                            Operation op=(Operation) Operation.make(
                                    new Product(ars), 
                                    ss2.getPredicate()
                            );
                            
                            t22 = op;
                        }
                    }
                }
                if(ss1 instanceof Operation && !(ss1.getSubject() instanceof Variable)) {//it is an operation, let's look if one of the arguments is same as the subject of the other term
                    Term comp=ss2.getSubject();
                    Term ss1_term = ss1.getSubject();
                    
                    boolean applicableVariableType = !(comp instanceof Variable && comp.hasVarIndep());
                    
                    if(ss1_term instanceof Product) {
                        Product ss1_prod=(Product) ss1_term;
                                               
                        if(applicableVariableType && Terms.contains(ss1_prod.term, comp)) { //only if there is one and it isnt a variable already
                            
                            Term[] ars = ss1_prod.cloneTermsReplacing(comp, var1);
                            

                            t22 = Terms.makeStatement(ss2, var1, ss2.getPredicate());
                            
                            Operation op=(Operation) Operation.make(
                                    new Product(ars), 
                                    ss1.getPredicate()
                            );
                            
                            t11 = op;
                        }
                    }
                }
            }
        }

        
        final Interval.AtomicDuration duration = nal.memory.param.duration;
        int durationCycles = duration.get();
        
        long time1 = s1.getOccurrenceTime();
        long time2 = s2.getOccurrenceTime();
        
        final long timeDiff;
        if ((time1 ==Stamp.ETERNAL) || (time2 == Stamp.ETERNAL))
            timeDiff = 0;
        else
            timeDiff = time2 - time1;

        if (timeDiff != 0 && !concurrent(time1, time2, durationCycles)) {

            List<Interval> interval = Interval.intervalSequence(Math.abs(timeDiff), Global.TEMPORAL_INTERVAL_PRECISION, nal.memory);

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


        int order = order(timeDiff, durationCycles);

        TruthValue givenTruth1 = s1.truth;
        TruthValue givenTruth2 = s2.truth;
        TruthValue truth1 = TruthFunctions.induction(givenTruth1, givenTruth2);
        TruthValue truth2 = TruthFunctions.induction(givenTruth2, givenTruth1);
        TruthValue truth3 = TruthFunctions.comparison(givenTruth1, givenTruth2);
        Budget budget1 = BudgetFunctions.forward(truth1, nal);
        Budget budget2 = BudgetFunctions.forward(truth2, nal);
        Budget budget3 = BudgetFunctions.forward(truth3, nal);
        Statement statement1 = Implication.make(t1, t2, order);
        Statement statement2 = Implication.make(t2, t1, reverseOrder(order));
        Statement statement3 = Equivalence.make(t1, t2, order);


        final int inductionLimit = nal.memory.param.temporalRelationsMax.get();

        if(t11!=null && t22!=null) {
            Statement statement11 = Implication.make(t11, t22, order);
            Statement statement22 = Implication.make(t22, t11, reverseOrder(order));
            Statement statement33 = Equivalence.make(t11, t22, order);
            if(!tooMuchTemporalStatements(statement11,inductionLimit)) {
                boolean t = nal.doublePremiseTask(statement11, truth1, budget1, stamp, true, subbedTask, false);
            }
            if(!tooMuchTemporalStatements(statement22,inductionLimit)) {
               boolean t = nal.doublePremiseTask(statement22, truth2, budget2, stamp, true, subbedTask, false);
            }
            if(!tooMuchTemporalStatements(statement33,inductionLimit)) {
               boolean t = nal.doublePremiseTask(statement33, truth3, budget3, stamp, true, subbedTask, false);
            }
        }
        if(!tooMuchTemporalStatements(statement1,inductionLimit)) {
            boolean t = nal.doublePremiseTask(statement1, truth1, budget1, stamp, true, subbedTask, false);
        }
        if(!tooMuchTemporalStatements(statement2,inductionLimit)) {
            boolean t = nal.doublePremiseTask(statement2, truth2, budget2, stamp, true, subbedTask, false); //=/> only to  keep graph simple for now
        }
        if(!tooMuchTemporalStatements(statement3,inductionLimit)) {
            boolean t = nal.doublePremiseTask(statement3, truth3, budget3, stamp, true, subbedTask, false);
        }

    }
    
    /**
     * Evaluate the quality of the judgment as a solution to a problem
     *
     * @param problem A goal or question
     * @param solution The solution to be evaluated
     * @return The quality of the judgment as the solution
     */
    public static float solutionQuality(final Sentence problem, final Sentence solution, Memory memory) {

        return solution.projectionTruthQuality(problem.getOccurrenceTime(), memory.time(), problem.hasQueryVar());

//        if (!matchingOrder(problem, solution)) {
//            return 0.0F;
//        }

//        TruthValue truth;
//        if (ptime!=solution.getOccurenceTime())
//            truth = solution.projectionTruth(ptime, memory.time());
//        else
//            truth = solution.truth;
//
//        if (problem.hasQueryVar()) {
//            return truth.getExpectation() / solution.term.getComplexity();
//        } else {
//            return truth.getConfidence();
//        }

    }


    /* ----- Functions used both in direct and indirect processing of tasks ----- */
    /**
     * Evaluate the quality of a belief as a solution to a problem, then reward
     * the belief and de-prioritize the problem
     *
     * @param problem The problem (question or goal) to be solved
     * @param solution The belief as solution
     * @param task The task to be immediately processed, or null for continued
     * process
     * @return The budget for the new task which is the belief activated, if
     * necessary
     */
    public static Budget solutionEval(final Sentence problem, final Sentence solution, Task task, final NAL nal) {
        Budget budget = null;
        boolean feedbackToLinks = false;
        /*if (task == null) {
            task = nal.getCurrentTask();
            feedbackToLinks = true;
        }*/
        boolean judgmentTask = task.sentence.isJudgment();
        final float quality = TemporalRules.solutionQuality(problem, solution, nal.memory);
        if (judgmentTask) {
            task.incPriority(quality);
        } else {
            float taskPriority = task.getPriority();
            budget = new Budget(UtilityFunctions.or(taskPriority, quality), task.getDurability(), BudgetFunctions.truthToQuality(solution.truth));
            task.setPriority(Math.min(1 - quality, taskPriority));
        }
        /*
        if (feedbackToLinks) {
            TaskLink tLink = nal.getCurrentTaskLink();
            tLink.setPriority(Math.min(1 - quality, tLink.getPriority()));
            TermLink bLink = nal.getCurrentBeliefLink();
            bLink.incPriority(quality);
        }*/
        return budget;
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
            throw new RuntimeException("order() does not compare ETERNAL times");
        
        return order(b - a, durationCycles);
    }

    public static boolean concurrent(Sentence a, Sentence b, final int durationCycles) {
        return concurrent(a.getOccurrenceTime(), b.getOccurrenceTime(), durationCycles);
    }

    /** whether two times are concurrent with respect ao a specific duration ("present moment") # of cycles */
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

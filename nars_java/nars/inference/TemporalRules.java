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
package nars.inference;


import java.util.ArrayList;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.entity.TruthValue;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Equivalence;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.Interval;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Variable;
import nars.storage.Memory;

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

    public static int reverseOrder(int order) {
        if (order == ORDER_NONE) {
            return ORDER_NONE;
        } else {
            return -order;
        }
    }

    public static boolean matchingOrder(int order1, int order2) {
        return (order1 == order2) || (order1 == ORDER_NONE) || (order2 == ORDER_NONE);
    }

    public static int dedExeOrder(int order1, int order2) {
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

    public static int abdIndComOrder(int order1, int order2) {
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

    public static int analogyOrder(int order1, int order2, int figure) {
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

    public static int resemblanceOrder(int order1, int order2, int figure) {
        int order = ORDER_INVALID;
        if ((order2 == TemporalRules.ORDER_NONE)) {
            order = (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if ((order1 == TemporalRules.ORDER_NONE) || (order1 == TemporalRules.ORDER_CONCURRENT)) {
            order = (figure % 10 == 1) ? order2 : reverseOrder(order2); // switch when 12 or 22
        } else if (order2 == TemporalRules.ORDER_CONCURRENT) {
            order = (figure > 20) ? order1 : reverseOrder(order1); // switch when 11 or 12
        } else if (order1 == order2) {
            order = (figure == 21) ? order1 : -order1;
        }
        return order;
    }

    public static int composeOrder(int order1, int order2) {
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

    public static void temporalInduction(final Sentence s1, final Sentence s2, final Memory memory) {
        if ((s1.truth==null) || (s2.truth==null))
            return;
        
        Term t1 = s1.cloneContent();
        Term t2 = s2.cloneContent();
        if (Statement.invalidStatement(t1, t2)) {
            return;
        }
        long time1 = s1.getOccurenceTime();
        long time2 = s2.getOccurenceTime();
        long timeDiff = time2 - time1;
        Interval interval = null;
        if (Math.abs(timeDiff) > Parameters.DURATION) {
            interval = new Interval(Math.abs(timeDiff));
            if (timeDiff > 0) {
                t1 = Conjunction.make(t1, interval, ORDER_FORWARD, memory);
            } else {
                t2 = Conjunction.make(t2, interval, ORDER_FORWARD, memory);
            }
        }
        int order;
        if (timeDiff > Parameters.DURATION) {
            order = TemporalRules.ORDER_FORWARD;
        } else if (timeDiff < -Parameters.DURATION) {
            order = TemporalRules.ORDER_BACKWARD;
        } else {
            order = TemporalRules.ORDER_CONCURRENT;
        }
        TruthValue givenTruth1 = s1.truth;
        TruthValue givenTruth2 = s2.truth;
        TruthValue truth1 = TruthFunctions.abduction(givenTruth1, givenTruth2);
        TruthValue truth2 = TruthFunctions.abduction(givenTruth2, givenTruth1);
        TruthValue truth3 = TruthFunctions.comparison(givenTruth1, givenTruth2);
        BudgetValue budget1 = BudgetFunctions.forward(truth1, memory);
        BudgetValue budget2 = BudgetFunctions.forward(truth2, memory);
        BudgetValue budget3 = BudgetFunctions.forward(truth3, memory);
        Statement statement1 = Implication.make(t1, t2, order, memory);
        Statement statement2 = Implication.make(t2, t1, reverseOrder(order), memory);
        Statement statement3 = Equivalence.make(t1, t2, order, memory);
        memory.doublePremiseTask(statement1, truth1, budget1);
        memory.doublePremiseTask(statement2, truth2, budget2);
        memory.doublePremiseTask(statement3, truth3, budget3);
    }

    public static void temporalIndCom(Sentence judg1, Sentence judg2, Memory memory) {
        Term term1 = judg1.content;
        Term term2 = judg2.content;
        if ((term1 instanceof CompoundTerm) && ((CompoundTerm) term1).containsTerm(term2)) {
            return;
        }

        if ((term2 instanceof CompoundTerm) && ((CompoundTerm) term2).containsTerm(term1)) {
            return;
        }

        if ((term1 instanceof Inheritance) && (term2 instanceof Inheritance)) {
            Statement s1 = (Statement) term1;
            Statement s2 = (Statement) term2;

            Variable var1 = new Variable("$0");
            Variable var2 = var1;
            if (s1.getSubject().equals(s2.getSubject())) {
                term1 = Statement.make(s1, var1, s1.getPredicate(), memory);
                Statement.make(Symbols.NativeOperator.PRODUCT, var1, s1.getPredicate(), memory);
                term2 = Statement.make(s2, var2, s2.getPredicate(), memory);
            } else if (s1.getPredicate().equals(s2.getPredicate())) {
                term1 = Statement.make(s1, s1.getSubject(), var1, memory);
                term2 = Statement.make(s2, s2.getSubject(), var2, memory);
            }

        } else { // to generalize
            Term condition;
            if (term1 instanceof Implication) {
                condition = ((Implication) term1).getSubject();
                if (condition.equals(term2)) {
                    return;
                }

                if ((condition instanceof Conjunction) && ((Conjunction) condition).containsTerm(term2)) {
                    return;
                }

            } else if (term2 instanceof Implication) {
                condition = ((Implication) term2).getSubject();
                if (condition.equals(term1)) {
                    return;
                }

                if ((condition instanceof Conjunction) && ((Conjunction) condition).containsTerm(term1)) {
                    return;
                }
            }
        }
        long time1 = judg1.getOccurenceTime();
        long time2 = judg2.getOccurenceTime();
        int distance = (int) (time2 - time1);
        int order=TemporalRules.ORDER_NONE;
        int order2=TemporalRules.ORDER_NONE;
        
        if(distance>0) {
            order=TemporalRules.ORDER_FORWARD;
            order2=TemporalRules.ORDER_BACKWARD;
        }
        if(distance<0) {
            order=TemporalRules.ORDER_BACKWARD;
            order2=TemporalRules.ORDER_FORWARD;
        }
        
        if (Math.abs(distance) > 0) {
            long deltat = Math.abs(distance);
            Interval delta=new Interval(deltat);
            
            Term[] argument = new Term[2];
            argument[0]=term2;
            argument[1]=delta;

            term2 = Conjunction.make(argument, TemporalRules.ORDER_FORWARD, memory);
        }

        Statement statement1 = Implication.make(term1, term2, order, memory);
        Statement statement2 = Implication.make(term2, term1, order2, memory);
        Statement statement3 = Equivalence.make(term1, term2, order, memory);
        TruthValue value1 = judg1.truth;
        TruthValue value2 = judg2.truth;
        TruthValue truth1 = TruthFunctions.induction(value1, value2);
        TruthValue truth2 = TruthFunctions.induction(value2, value1);
        TruthValue truth3 = TruthFunctions.comparison(value1, value2);
        /*BudgetValue budget1 = BudgetFunctions.temporalIndCom(task1.budget, task2.budget, truth1);
        BudgetValue budget2 = BudgetFunctions.temporalIndCom(task1.budget, task2.budget, truth2);
        BudgetValue budget3 = BudgetFunctions.temporalIndCom(task1.budget, task2.budget, truth3);*/
        BudgetValue budget1=BudgetFunctions.compoundForward(truth1, statement1, memory);
        BudgetValue budget2=BudgetFunctions.compoundForward(truth2, statement2, memory);
        BudgetValue budget3=BudgetFunctions.compoundForward(truth3, statement3, memory);
        /*memory.doublePremiseTask(budget1, statement1, truth1, judg1, judg2);
        memory.doublePremiseTask(budget2, statement2, truth2, judg2, judg1);
        memory.doublePremiseTask(budget3, statement3, truth3, judg1, judg2);*/
        memory.doublePremiseTask(statement1, truth1, budget1);
        memory.doublePremiseTask(statement2, truth2, budget2);
        memory.doublePremiseTask(statement3, truth3, budget3);
    }
    
    /**
     * Evaluate the quality of the judgment as a solution to a problem
     *
     * @param problem A goal or question
     * @param solution The solution to be evaluated
     * @return The quality of the judgment as the solution
     */
    public static float solutionQuality(final Sentence problem, final Sentence solution, Memory memory) {
        if (!TemporalRules.matchingOrder(problem.getTemporalOrder(), solution.getTemporalOrder())) {
            return 0.0F;
        }
        TruthValue truth = solution.truth;
        if (problem.getOccurenceTime() != solution.getOccurenceTime()) {
            Sentence cloned = solution.projection(problem.getOccurenceTime(), memory.getTime());
            truth = cloned.truth;
        }
        if (problem.containQueryVar()) {
            return truth.getExpectation() / solution.content.getComplexity();
        } else {
            return truth.getConfidence();
        }
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
    public static BudgetValue solutionEval(final Sentence problem, final Sentence solution, Task task, final Memory memory) {
        BudgetValue budget = null;
        boolean feedbackToLinks = false;
        if (task == null) {
            task = memory.getCurrentTask();
            feedbackToLinks = true;
        }
        boolean judgmentTask = task.sentence.isJudgment();
        final float quality = TemporalRules.solutionQuality(problem, solution, memory);
        if (judgmentTask) {
            task.incPriority(quality);
        } else {
            float taskPriority = task.getPriority();
            budget = new BudgetValue(UtilityFunctions.or(taskPriority, quality), task.getDurability(), BudgetFunctions.truthToQuality(solution.truth));
            task.setPriority(Math.min(1 - quality, taskPriority));
        }
        if (feedbackToLinks) {
            TaskLink tLink = memory.getCurrentTaskLink();
            tLink.setPriority(Math.min(1 - quality, tLink.getPriority()));
            TermLink bLink = memory.getCurrentBeliefLink();
            bLink.incPriority(quality);
        }
        return budget;
    }
}

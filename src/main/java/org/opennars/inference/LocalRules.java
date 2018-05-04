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

import java.util.ArrayList;
import org.opennars.main.Parameters;
import org.opennars.io.events.Events.Answer;
import org.opennars.io.events.Events.Unsolved;
import org.opennars.storage.Memory;
import org.opennars.control.DerivationContext;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TaskLink;
import org.opennars.entity.TermLink;
import org.opennars.entity.TruthValue;
import static org.opennars.inference.TemporalRules.matchingOrder;
import static org.opennars.inference.TemporalRules.reverseOrder;
import static org.opennars.inference.TruthFunctions.temporalProjection;
import org.opennars.io.events.OutputHandler;
import org.opennars.io.Symbols;
import org.opennars.language.CompoundTerm;
import static org.opennars.language.CompoundTerm.extractIntervals;
import static org.opennars.language.CompoundTerm.replaceIntervals;
import org.opennars.language.Equivalence;
import org.opennars.language.Inheritance;
import org.opennars.language.Similarity;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import org.opennars.language.Variables;


/**
 * Directly process a task by a oldBelief, with only two Terms in both. In
 * matching, the new task is compared with an existing direct Task in that
 * Concept, to carry out:
 * <p>
 *   revision: between judgments or goals on non-overlapping evidence; 
 *   satisfy: between a Sentence and a Question/Goal; 
 *   merge: between items of the same type and stamp; 
 *   conversion: between different inheritance relations.
 */
public class LocalRules {

    /* -------------------- same contents -------------------- */
    /**
     * The task and belief have the same content
     * <p>
     * called in RuleTables.reason
     *
     * @param task The task
     * @param belief The belief
     * @param memory Reference to the memory
     */
    public static boolean match(final Task task, final Sentence belief, final DerivationContext nal) {
        Sentence sentence = task.sentence;
        
        if (sentence.isJudgment()) {
            if (revisible(sentence, belief)) {
                return revision(sentence, belief, true, nal);
            }
        } else {
            if (matchingOrder(sentence, belief)) {
                Term[] u = new Term[] { sentence.term, belief.term };
                if (Variables.unify(Symbols.VAR_QUERY, u)) {
                    trySolution(belief, task, nal, true);
                }
            }
        }
        return false;
    }

    /**
     * Check whether two sentences can be used in revision
     *
     * @param s1 The first sentence
     * @param s2 The second sentence
     * @return If revision is possible between the two sentences
     */
    public static boolean revisible(final Sentence s1, final Sentence s2) {
        if(!s1.isEternal() && !s2.isEternal() && Math.abs(s1.getOccurenceTime() - s2.getOccurenceTime()) > Parameters.REVISION_MAX_OCCURRENCE_DISTANCE) {
            return false;
        }
        if(s1.term.term_indices != null && s2.term.term_indices != null) {
            for(int i=0;i<s1.term.term_indices.length;i++) {
                if(s1.term.term_indices[i] != s2.term.term_indices[i]) {
                    return false;
                }
            }
        }
        return (s1.getRevisible() && 
                matchingOrder(s1.getTemporalOrder(), s2.getTemporalOrder()) &&
                CompoundTerm.replaceIntervals(s1.term).equals(CompoundTerm.replaceIntervals(s2.term)) &&
                !Stamp.baseOverlap(s1.stamp.evidentialBase, s2.stamp.evidentialBase));
    }

    /**
     * Belief revision
     * <p>
     * called from Concept.reviseTable and match
     *
     * @param newBelief The new belief in task
     * @param oldBelief The previous belief with the same content
     * @param feedbackToLinks Whether to send feedback to the links
     * @param memory Reference to the memory
     */
    public static boolean revision(final Sentence newBelief, final Sentence oldBelief, final boolean feedbackToLinks, final DerivationContext nal) {
        if (newBelief.term==null) { 
            return false;
        }
        
        newBelief.stamp.alreadyAnticipatedNegConfirmation = oldBelief.stamp.alreadyAnticipatedNegConfirmation;
        TruthValue newTruth = newBelief.truth.clone();
        TruthValue oldTruth = oldBelief.truth;
        boolean useNewBeliefTerm = false;
        
        if(newBelief.getTerm().hasInterval()) {
            Term cterm = replaceIntervals(newBelief.getTerm());
            Concept c = nal.memory.concept(cterm);
            ArrayList<Long> ivalOld = extractIntervals(nal.memory, oldBelief.getTerm());
            if(c.recent_intervals.size() == 0) {
                for(Long l : ivalOld) {
                    c.recent_intervals.add((float) l);
                }
            }
            ArrayList<Long> ivalNew = extractIntervals(nal.memory, newBelief.getTerm());
            for(int i=0;i<ivalNew.size();i++) {
                float Inbetween = (c.recent_intervals.get(i)+ivalNew.get(i)) / 2.0f; //vote as one new entry, turtle style
                float speed = 1.0f / (float) (Parameters.INTERVAL_ADAPT_SPEED*(1.0f-newBelief.getTruth().getExpectation())); //less truth expectation, slower
                c.recent_intervals.set(i,c.recent_intervals.get(i)+speed*(Inbetween - c.recent_intervals.get(i)));
            }
            long AbsDiffSumNew = 0;
            for(int i=0;i<ivalNew.size();i++) {
                AbsDiffSumNew += Math.abs(ivalNew.get(i) - c.recent_intervals.get(i));
            }
            long AbsDiffSumOld = 0;
            for(int i=0;i<ivalNew.size();i++) {
                AbsDiffSumOld += Math.abs(ivalOld.get(i) - c.recent_intervals.get(i));
            }
            long AbsDiffSum = 0;
            for(int i=0;i<ivalNew.size();i++) {
                AbsDiffSum += Math.abs(ivalNew.get(i) - ivalOld.get(i));
            }
            float a = temporalProjection(0, AbsDiffSum, 0); //re-project, and it's safe:
                                                            //we won't count more confidence than
                                                            //when the second premise would have been shifted
                                                            //to the necessary time in the first place
                                                            //to build the hypothesis newBelief encodes
            newTruth.setConfidence(newTruth.getConfidence()*a);
            useNewBeliefTerm = AbsDiffSumNew < AbsDiffSumOld;
        }
        
        TruthValue truth = TruthFunctions.revision(newTruth, oldTruth);
        BudgetValue budget = BudgetFunctions.revise(newTruth, oldTruth, truth, feedbackToLinks, nal);
        
        if (budget.aboveThreshold()) {
            if (nal.doublePremiseTaskRevised(useNewBeliefTerm ? newBelief.term : oldBelief.term, truth, budget)) {
                //nal.mem().logic.BELIEF_REVISION.commit();
                return true;
            }
        }
        
        return false;
    }


    /**
     * Check if a Sentence provide a better answer to a Question or Goal
     *
     * @param belief The proposed answer
     * @param task The task to be processed
     * @param memory Reference to the memory
     */
    public static boolean trySolution(Sentence belief, final Task task, final DerivationContext nal, boolean report) {
        Sentence problem = task.sentence;
        Memory memory = nal.mem();
        Sentence oldBest = task.getBestSolution();
        
        if (oldBest != null) {
            boolean rateByConfidence = oldBest.getTerm().equals(belief.getTerm());
            float newQ = solutionQuality(rateByConfidence, task, belief, memory);
            float oldQ = solutionQuality(rateByConfidence, task, oldBest, memory);
            if (oldQ >= newQ) {
                if (problem.isGoal()) {
                    memory.emotion.adjustSatisfaction(oldQ, task.getPriority(),nal);
                }
                memory.emit(Unsolved.class, task, belief, "Lower quality");               
                return false;
            }
        }
        task.setBestSolution(memory,belief);
        //memory.logic.SOLUTION_BEST.commit(task.getPriority());
        
        BudgetValue budget = solutionEval(task, belief, task, nal);
        if ((budget != null) && budget.aboveThreshold()) {                       
            
            //Solution Activated
            if(task.sentence.punctuation==Symbols.QUESTION_MARK || task.sentence.punctuation==Symbols.QUEST_MARK) {
                if(task.isInput() && report) { //only show input tasks as solutions
                    memory.emit(Answer.class, task, belief); 
                } else {
                    memory.emit(OutputHandler.class, task, belief);   //solution to quests and questions can be always showed   
                }
            } else {
                memory.emit(OutputHandler.class, task, belief);   //goal things only show silence related 
            }

            nal.addTask(nal.getCurrentTask(), budget, belief, task.getParentBelief());
            return true;
        }
        else {
            memory.emit(Unsolved.class, task, belief, "Insufficient budget");
        }
        return false;
    }
    
    /**
     * Evaluate the quality of the judgment as a solution to a problem
     *
     * @param problem A goal or question
     * @param solution The solution to be evaluated
     * @return The quality of the judgment as the solution
     */
    public static float solutionQuality(boolean rateByConfidence, final Task probT, final Sentence solution, Memory memory) {
        Sentence problem = probT.sentence;
        
        if ((probT.sentence.punctuation != solution.punctuation && solution.term.hasVarQuery()) || !matchingOrder(problem.getTemporalOrder(), solution.getTemporalOrder())) {
            return 0.0F;
        }
        
        TruthValue truth = solution.truth;
        if (problem.getOccurenceTime()!=solution.getOccurenceTime()) {
            truth = solution.projectionTruth(problem.getOccurenceTime(), memory.time());            
        }
        
        //when the solutions are comparable, we have to use confidence!! else truth expectation.
        //this way negative evidence can update the solution instead of getting ignored due to lower truth expectation.
        //so the previous handling to let whether the problem has query vars decide was wrong.
        if (!rateByConfidence) {
            return (float) (truth.getExpectation() / Math.sqrt(Math.sqrt(Math.sqrt(solution.term.getComplexity()*Parameters.COMPLEXITY_UNIT))));
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
    public static BudgetValue solutionEval(final Task problem, final Sentence solution, Task task, final org.opennars.control.DerivationContext nal) {
        if(problem.sentence.punctuation != solution.punctuation && solution.term.hasVarQuery()) {
            return null;
        }
        BudgetValue budget = null;
        boolean feedbackToLinks = false;
        if (task == null) {
            task = nal.getCurrentTask();
            feedbackToLinks = true;
        }
        boolean judgmentTask = task.sentence.isJudgment();
        boolean rateByConfidence = problem.getTerm().hasVarQuery(); //here its whether its a what or where question for budget adjustment
        final float quality = solutionQuality(rateByConfidence, problem, solution, nal.mem());
        
        if (problem.sentence.isGoal()) {
            nal.memory.emotion.adjustSatisfaction(quality, task.getPriority(), nal);
        }
        
        if (judgmentTask) {
            task.incPriority(quality);
        } else {
            float taskPriority = task.getPriority(); //+goal satisfication is a matter of degree - https://groups.google.com/forum/#!topic/open-nars/ZfCM416Dx1M
            budget = new BudgetValue(UtilityFunctions.or(taskPriority, quality), task.getDurability(), BudgetFunctions.truthToQuality(solution.truth));
            task.setPriority(Math.min(1 - quality, taskPriority));
        }
        if (feedbackToLinks) {
            TaskLink tLink = nal.getCurrentTaskLink();
            tLink.setPriority(Math.min(1 - quality, tLink.getPriority()));
            TermLink bLink = nal.getCurrentBeliefLink();
            bLink.incPriority(quality);
        }
        return budget;
    }


    /* -------------------- same terms, difference relations -------------------- */
    /**
     * The task and belief match reversely
     *
     * @param nal Reference to the memory
     */
    public static void matchReverse(final DerivationContext nal) {
        Task task = nal.getCurrentTask();
        Sentence belief = nal.getCurrentBelief();
        Sentence sentence = task.sentence;
        if (matchingOrder(sentence.getTemporalOrder(), reverseOrder(belief.getTemporalOrder()))) {
            if (sentence.isJudgment()) {
                inferToSym(sentence, belief, nal);
            } else {
                conversion(nal);
            }
        }
    }

    /**
     * Inheritance/Implication matches Similarity/Equivalence
     *
     * @param asym A Inheritance/Implication sentence
     * @param sym A Similarity/Equivalence sentence
     * @param figure location of the shared term
     * @param nal Reference to the memory
     */
    public static void matchAsymSym(final Sentence asym, final Sentence sym, int figure, final DerivationContext nal) {
        if (nal.getCurrentTask().sentence.isJudgment()) {
            inferToAsym(asym, sym, nal);
        } else {
            convertRelation(nal);
        }
    }

    /* -------------------- two-premise inference rules -------------------- */
    /**
     * {<S --> P>, <P --> S} |- <S <-> p> Produce Similarity/Equivalence from a
     * pair of reversed Inheritance/Implication
     *
     * @param judgment1 The first premise
     * @param judgment2 The second premise
     * @param nal Reference to the memory
     */
    private static void inferToSym(Sentence judgment1, Sentence judgment2, DerivationContext nal) {
        Statement s1 = (Statement) judgment1.term;
        Term t1 = s1.getSubject();
        Term t2 = s1.getPredicate();
        Term content;
        if (s1 instanceof Inheritance) {
            content = Similarity.make(t1, t2);
        } else {
            content = Equivalence.make(t1, t2, s1.getTemporalOrder());
        }
        TruthValue value1 = judgment1.truth;
        TruthValue value2 = judgment2.truth;
        TruthValue truth = TruthFunctions.intersection(value1, value2);
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,false, false); //(allow overlap) but not needed here, isn't detachment
    }

    /**
     * {<S <-> P>, <P --> S>} |- <S --> P> Produce an Inheritance/Implication
     * from a Similarity/Equivalence and a reversed Inheritance/Implication
     *
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param nal Reference to the memory
     */
    private static void inferToAsym(Sentence asym, Sentence sym, DerivationContext nal) {
        Statement statement = (Statement) asym.term;
        Term sub = statement.getPredicate();
        Term pre = statement.getSubject();
        
        Statement content = Statement.make(statement, sub, pre, statement.getTemporalOrder());
        if (content == null) return;
        
        TruthValue truth = TruthFunctions.reduceConjunction(sym.truth, asym.truth);
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,false, false);
    }

    /* -------------------- one-premise inference rules -------------------- */
    /**
     * {<P --> S>} |- <S --> P> Produce an Inheritance/Implication from a
     * reversed Inheritance/Implication
     *
     * @param nal Reference to the memory
     */
    private static void conversion(final DerivationContext nal) {
        TruthValue truth = TruthFunctions.conversion(nal.getCurrentBelief().truth);
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
        convertedJudgment(truth, budget, nal);
    }

    /**
     * {<S --> P>} |- <S <-> P> {<S <-> P>} |- <S --> P> Switch between
     * Inheritance/Implication and Similarity/Equivalence
     *
     * @param nal Reference to the memory
     */
    private static void convertRelation(final DerivationContext nal) {
        TruthValue truth = nal.getCurrentBelief().truth;
        if (((CompoundTerm) nal.getCurrentTask().getTerm()).isCommutative()) {
            truth = TruthFunctions.abduction(truth, 1.0f);
        } else {
            truth = TruthFunctions.deduction(truth, 1.0f);
        }
        BudgetValue budget = BudgetFunctions.forward(truth, nal);
        convertedJudgment(truth, budget, nal);
    }

    /**
     * Convert judgment into different relation
     * <p>
     * called in MatchingRules
     *
     * @param budget The budget value of the new task
     * @param truth The truth value of the new task
     * @param nal Reference to the memory
     */
    private static void convertedJudgment(final TruthValue newTruth, final BudgetValue newBudget, final DerivationContext nal) {
        Statement content = (Statement) nal.getCurrentTask().getTerm();
        Statement beliefContent = (Statement) nal.getCurrentBelief().term;
        int order = TemporalRules.reverseOrder(beliefContent.getTemporalOrder());
        final Term subjT = content.getSubject();
        final Term predT = content.getPredicate();
        final Term subjB = beliefContent.getSubject();
        final Term predB = beliefContent.getPredicate();
        Term otherTerm;
        if (subjT.hasVarQuery()) {
            otherTerm = (predT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, otherTerm, predT, order);
        }
        if (predT.hasVarQuery()) {
            otherTerm = (subjT.equals(subjB)) ? predB : subjB;
            content = Statement.make(content, subjT, otherTerm, order);
        }
        
        if (content == null) { 
            return;
        }
        
        nal.singlePremiseTask(content, Symbols.JUDGMENT_MARK, newTruth, newBudget);
    }

    
}

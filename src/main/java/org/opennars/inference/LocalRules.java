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
import org.opennars.entity.*;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.io.events.Events.Answer;
import org.opennars.io.events.Events.Unsolved;
import org.opennars.io.events.OutputHandler;
import org.opennars.language.*;
import org.opennars.storage.Memory;
import java.util.List;
import static org.opennars.inference.TemporalRules.matchingOrder;
import static org.opennars.inference.TemporalRules.reverseOrder;
import static org.opennars.inference.TruthFunctions.temporalProjection;
import static org.opennars.language.CompoundTerm.extractIntervals;
import static org.opennars.language.CompoundTerm.replaceIntervals;
import org.opennars.main.Parameters;

/**
 * Directly process a task by a oldBelief, with only two Terms in both. In
 * matching, the new task is compared with an existing direct Task in that
 * Concept, to carry out:
 * <p>
 *   revision: between judgments or goals on non-overlapping evidence;<br>
 *   satisfy: between a Sentence and a Question/Goal; <br>
 *   merge: between items of the same type and stamp; <br>
 *   conversion: between different inheritance relations.<br>
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class LocalRules {

    /* -------------------- same contents -------------------- */
    /**
     * The task and belief have the same content
     *
     * @param task The task
     * @param belief The belief
     */
    // called in RuleTables.reason
    public static boolean match(final Task task, final Sentence belief, Concept beliefConcept, final DerivationContext nal) {
        final Sentence sentence = task.sentence;
        
        if (sentence.isJudgment()) {
            if (revisible(sentence, belief, nal.narParameters)) {
                return revision(sentence, belief, beliefConcept, true, nal);
            }
        } else {
            if (matchingOrder(sentence, belief)) {
                final Term[] u = new Term[] { sentence.term, belief.term };
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
    public static boolean revisible(final Sentence s1, final Sentence s2, Parameters narParameters) {
        if(!s1.isEternal() && !s2.isEternal() && Math.abs(s1.getOccurenceTime() - s2.getOccurenceTime()) > narParameters.REVISION_MAX_OCCURRENCE_DISTANCE) {
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
     *
     * Summarizes the evidence of two beliefs with same content.
     * called from processGoal and processJudgment and LocalRules.match
     * 
     * @param newBelief The new belief in task
     * @param oldBelief The previous belief with the same content
     * @param feedbackToLinks Whether to send feedback to the links
     */
    public static boolean revision(final Sentence newBelief, final Sentence oldBelief, final Concept beliefConcept, final boolean feedbackToLinks, final DerivationContext nal) {
        if (newBelief.term==null) { 
            return false;
        }
        
        newBelief.stamp.alreadyAnticipatedNegConfirmation = oldBelief.stamp.alreadyAnticipatedNegConfirmation;
        final TruthValue newTruth = newBelief.truth.clone();
        final TruthValue oldTruth = oldBelief.truth;
        boolean useNewBeliefTerm = intervalProjection(nal, newBelief.getTerm(), oldBelief.getTerm(), beliefConcept, newTruth);
        
        final TruthValue truth = TruthFunctions.revision(newTruth, oldTruth, nal.narParameters);
        final BudgetValue budget = BudgetFunctions.revise(newTruth, oldTruth, truth, feedbackToLinks, nal);
        
        if (budget.aboveThreshold()) {
            return nal.doublePremiseTaskRevised(useNewBeliefTerm ? newBelief.term : oldBelief.term, truth, budget);
        }
        
        return false;
    }

    /**
     * Interval projection
     * 
     * Decides to use whether to use old or new term dependent on which one is more usual,
     * also discounting the truth confidence according to the interval difference.
     * called by Revision
     * 
     * @param nal
     * @param newBeliefTerm
     * @param oldBeliefTerm
     * @param beliefConcept
     * @param newTruth
     * @return 
     */
    public static boolean intervalProjection(final DerivationContext nal, final Term newBeliefTerm, final Term oldBeliefTerm, final Concept beliefConcept, final TruthValue newTruth) {
        boolean useNewBeliefTerm = false;
        if(newBeliefTerm.hasInterval()) {    
            final List<Long> ivalOld = extractIntervals(nal.memory, oldBeliefTerm);
            final List<Long> ivalNew = extractIntervals(nal.memory, newBeliefTerm);
            long AbsDiffSumNew = 0;
            long AbsDiffSumOld = 0;
            List<Float> recent_ivals = beliefConcept.recent_intervals;
            synchronized(recent_ivals){
                if(recent_ivals.isEmpty()) {
                    for(final Long l : ivalOld) {
                        recent_ivals.add((float) l);
                    }
                }
                for(int i=0;i<ivalNew.size();i++) {
                    final float Inbetween = (recent_ivals.get(i)+ivalNew.get(i)) / 2.0f; //vote as one new entry, turtle style
                    final float speed = 1.0f / (nal.narParameters.INTERVAL_ADAPT_SPEED*(1.0f-newTruth.getExpectation())); //less truth expectation, slower
                    recent_ivals.set(i,recent_ivals.get(i)+speed*(Inbetween - recent_ivals.get(i)));
                }
                
                for(int i=0;i<ivalNew.size();i++) {
                    AbsDiffSumNew += Math.abs(ivalNew.get(i) - recent_ivals.get(i));
                }
                
                for(int i=0;i<ivalNew.size();i++) {
                    AbsDiffSumOld += Math.abs(ivalOld.get(i) - recent_ivals.get(i));
                }
            }
            long AbsDiffSum = 0;
            for(int i=0;i<ivalNew.size();i++) {
                AbsDiffSum += Math.abs(ivalNew.get(i) - ivalOld.get(i));
            }
            final float a = temporalProjection(0, AbsDiffSum, 0, nal.memory.narParameters); //re-project, and it's safe:
            //we won't count more confidence than
            //when the second premise would have been shifted
            //to the necessary time in the first place
            //to build the hypothesis newBelief encodes
            newTruth.setConfidence(newTruth.getConfidence()*a);
            useNewBeliefTerm = AbsDiffSumNew < AbsDiffSumOld;
        }
        return useNewBeliefTerm;
    }


    /**
     * Check if a Sentence provide a better answer to a Question or Goal
     *
     * @param belief The proposed answer
     * @param task The task to be processed
     */
    public static boolean trySolution(final Sentence belief, final Task task, final DerivationContext nal, final boolean report) {
        final Sentence problem = task.sentence;
        final Memory memory = nal.mem();
        final Sentence oldBest = task.getBestSolution();
        
        if (oldBest != null) {
            final boolean rateByConfidence = oldBest.getTerm().equals(belief.getTerm());
            final float newQ = solutionQuality(rateByConfidence, task, belief, memory, nal.time);
            final float oldQ = solutionQuality(rateByConfidence, task, oldBest, memory, nal.time);
            if (oldQ >= newQ) {
                if (problem.isGoal() && memory.emotion != null) {
                    memory.emotion.adjustSatisfaction(oldQ, task.getPriority(), nal);
                }
                memory.emit(Unsolved.class, task, belief, "Lower quality");               
                return false;
            }
        }
        task.setBestSolution(memory, belief, nal.time);
        //memory.logic.SOLUTION_BEST.commit(task.getPriority());
        
        final BudgetValue budget = solutionEval(task, belief, task, nal);
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
     * @param probT A goal or question
     * @param solution The solution to be evaluated
     * @return The quality of the judgment as the solution
     */
    public static float solutionQuality(final boolean rateByConfidence, final Task probT, final Sentence solution, final Memory memory, final Timable time) {
        final Sentence problem = probT.sentence;
        
        if ((probT.sentence.punctuation != solution.punctuation && solution.term.hasVarQuery()) || !matchingOrder(problem.getTemporalOrder(), solution.getTemporalOrder())) {
            return 0.0F;
        }
        
        TruthValue truth = solution.truth;
        if (problem.getOccurenceTime()!=solution.getOccurenceTime()) {
            truth = solution.projectionTruth(problem.getOccurenceTime(), time.time(), memory);
        }
        
        //when the solutions are comparable, we have to use confidence!! else truth expectation.
        //this way negative evidence can update the solution instead of getting ignored due to lower truth expectation.
        //so the previous handling to let whether the problem has query vars decide was wrong.
        if (!rateByConfidence) {
            /*
             * just some function that decreases quality of solution if it is complex, and increases if it has a high truth expecation
             */
            
            return (float) (truth.getExpectation() / Math.sqrt(Math.sqrt(Math.sqrt(solution.term.getComplexity()*memory.narParameters.COMPLEXITY_UNIT))));
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
     * @param task The task to be immediately processed, or null for continued process
     * @return The budget for the new task which is the belief activated, if necessary
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
        final boolean judgmentTask = task.sentence.isJudgment();
        final boolean rateByConfidence = problem.getTerm().hasVarQuery(); //here its whether its a what or where question for budget adjustment
        final float quality = solutionQuality(rateByConfidence, problem, solution, nal.mem(), nal.time);
        
        if (problem.sentence.isGoal() && nal.memory.emotion != null) {
            nal.memory.emotion.adjustSatisfaction(quality, task.getPriority(), nal);
        }
        
        if (judgmentTask) {
            task.incPriority(quality);
        } else {
            final float taskPriority = task.getPriority(); //+goal satisfication is a matter of degree - https://groups.google.com/forum/#!topic/open-nars/ZfCM416Dx1M
            budget = new BudgetValue(UtilityFunctions.or(taskPriority, quality), task.getDurability(), BudgetFunctions.truthToQuality(solution.truth), nal.narParameters);
            task.setPriority(Math.min(1 - quality, taskPriority));
        }
        if (feedbackToLinks) {
            final TaskLink tLink = nal.getCurrentTaskLink();
            tLink.setPriority(Math.min(1 - quality, tLink.getPriority()));
            final TermLink bLink = nal.getCurrentBeliefLink();
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
        final Task task = nal.getCurrentTask();
        final Sentence belief = nal.getCurrentBelief();
        final Sentence sentence = task.sentence;
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
    public static void matchAsymSym(final Sentence asym, final Sentence sym, final int figure, final DerivationContext nal) {
        if (nal.getCurrentTask().sentence.isJudgment()) {
            inferToAsym(asym, sym, nal);
        } else {
            convertRelation(nal);
        }
    }

    /* -------------------- two-premise inference rules -------------------- */
    /**
     * Produce Similarity/Equivalence from a pair of reversed Inheritance/Implication
     * <br>
     * {&lt;S --&gt; P&gt;, &lt;P --&gt; S} |- &lt;S &lt;-&gt; p&gt;
     *
     * @param judgment1 The first premise
     * @param judgment2 The second premise
     * @param nal Reference to the memory
     */
    private static void inferToSym(final Sentence judgment1, final Sentence judgment2, final DerivationContext nal) {
        final Statement s1 = (Statement) judgment1.term;
        final Term t1 = s1.getSubject();
        final Term t2 = s1.getPredicate();
        final Term content;
        if (s1 instanceof Inheritance) {
            content = Similarity.make(t1, t2);
        } else {
            content = Equivalence.make(t1, t2, s1.getTemporalOrder());
        }
        final TruthValue value1 = judgment1.truth;
        final TruthValue value2 = judgment2.truth;
        final TruthValue truth = TruthFunctions.intersection(value1, value2, nal.narParameters);
        final BudgetValue budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,false, false); //(allow overlap) but not needed here, isn't detachment
    }

    /**
     * Produce an Inheritance/Implication from a Similarity/Equivalence and a reversed Inheritance/Implication
     * <br>
     * {&lt;S &lt;-&gt; P&gt;, &lt;P --&gt; S&gt;} |- &lt;S --&gt; P&gt;
     *
     * @param asym The asymmetric premise
     * @param sym The symmetric premise
     * @param nal Reference to the memory
     */
    private static void inferToAsym(final Sentence asym, final Sentence sym, final DerivationContext nal) {
        final Statement statement = (Statement) asym.term;
        final Term sub = statement.getPredicate();
        final Term pre = statement.getSubject();
        
        final Statement content = Statement.make(statement, sub, pre, statement.getTemporalOrder());
        if (content == null) return;
        
        final TruthValue truth = TruthFunctions.reduceConjunction(sym.truth, asym.truth, nal.narParameters);
        final BudgetValue budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,false, false);
    }

    /* -------------------- one-premise inference rules -------------------- */
    /**
     * Produce an Inheritance/Implication from a reversed Inheritance/Implication
     * <br>
     * {&lt;P --&gt; S&gt;} |- &lt;S --&gt; P&gt;
     *
     * @param nal Reference to the memory
     */
    private static void conversion(final DerivationContext nal) {
        final TruthValue truth = TruthFunctions.conversion(nal.getCurrentBelief().truth, nal.narParameters);
        final BudgetValue budget = BudgetFunctions.forward(truth, nal);
        convertedJudgment(truth, budget, nal);
    }

    /**
     * Switch between Inheritance/Implication and Similarity/Equivalence
     * <br>
     * {&lt;S --&gt; P&gt;} |- &lt;S &lt;-&gt; P&gt; {&lt;S &lt;-&gt; P&gt;} |- &lt;S --&gt; P&gt;
     *
     * @param nal Reference to the memory
     */
    private static void convertRelation(final DerivationContext nal) {
        TruthValue truth = nal.getCurrentBelief().truth;
        if (((CompoundTerm) nal.getCurrentTask().getTerm()).isCommutative()) {
            truth = TruthFunctions.abduction(truth, 1.0f, nal.narParameters);
        } else {
            truth = TruthFunctions.deduction(truth, 1.0f, nal.narParameters);
        }
        final BudgetValue budget = BudgetFunctions.forward(truth, nal);
        convertedJudgment(truth, budget, nal);
    }

    /**
     * Convert judgment into different relation
     * <p>
     * called in MatchingRules
     *
     * @param newBudget The budget value of the new task
     * @param newTruth The truth value of the new task
     * @param nal Reference to the memory
     */
    private static void convertedJudgment(final TruthValue newTruth, final BudgetValue newBudget, final DerivationContext nal) {
        Statement content = (Statement) nal.getCurrentTask().getTerm();
        final Statement beliefContent = (Statement) nal.getCurrentBelief().term;
        final int order = TemporalRules.reverseOrder(beliefContent.getTemporalOrder());
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

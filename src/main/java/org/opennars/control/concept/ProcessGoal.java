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
package org.opennars.control.concept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opennars.control.DerivationContext;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Stamp.BaseEntry;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.inference.LocalRules;
import static org.opennars.inference.LocalRules.revisible;
import static org.opennars.inference.LocalRules.revision;
import static org.opennars.inference.LocalRules.trySolution;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.io.Symbols;
import org.opennars.io.events.Events;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Conjunction;
import org.opennars.language.Equivalence;
import org.opennars.language.Implication;
import org.opennars.language.Interval;
import org.opennars.language.Product;
import org.opennars.language.Term;
import org.opennars.language.Variable;
import org.opennars.language.Variables;
import org.opennars.main.MiscFlags;
import org.opennars.operator.FunctionOperator;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.plugin.mental.InternalExperience;

/**
 *
 * @author Patrick Hammer
 */
public class ProcessGoal {
    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it, potentially executing in case of an operation goal
     *
     * @param concept The concept of the goal
     * @param nal The derivation context
     * @param task The goal task to be processed
     */
    protected static void processGoal(final Concept concept, final DerivationContext nal, final Task task) {
        final Sentence goal = task.sentence;
        final Task oldGoalT = concept.selectCandidate(task, concept.desires, nal.time); // revise with the existing desire values
        Sentence oldGoal = null;
        final Stamp newStamp = goal.stamp;
        if (oldGoalT != null) {
            oldGoal = oldGoalT.sentence;
            final Stamp oldStamp = oldGoal.stamp;
            if (newStamp.equals(oldStamp,false,false,true)) {
                return; // duplicate
            }
        }

        Task beliefT = null;
        if(task.aboveThreshold()) {
            beliefT = concept.selectCandidate(task, concept.beliefs, nal.time);

            for (final Task iQuest : concept.quests ) {
                trySolution(task.sentence, iQuest, nal, true);
            }

            // check if the Goal is already satisfied
            if (beliefT != null) {
                // check if the Goal is already satisfied (manipulate budget)
                trySolution(beliefT.sentence, task, nal, true);
            }
        }

        if (oldGoalT != null && revisible(goal, oldGoal, nal.narParameters)) {
            final Stamp oldStamp = oldGoal.stamp;
            nal.setTheNewStamp(newStamp, oldStamp, nal.time.time());
            final Sentence projectedGoal = oldGoal.projection(task.sentence.getOccurenceTime(), newStamp.getOccurrenceTime(), concept.memory);
            if (projectedGoal!=null) {
                nal.setCurrentBelief(projectedGoal);
                final boolean wasRevised = revision(task.sentence, projectedGoal, concept, false, nal);
                if (wasRevised) {
                    /* It was revised, so there is a new task for which this method will be called
                     * with higher/lower desire.
                     * We return because it is not allowed to go on directly due to decision making.
                     * see https://groups.google.com/forum/#!topic/open-nars/lQD0no2ovx4
                     */
                    return;
                }
            }
        }

        final Stamp s2=goal.stamp.clone();
        s2.setOccurrenceTime(nal.time.time());
        if(s2.after(task.sentence.stamp, nal.narParameters.DURATION)) {
            // this task is not up to date we have to project it first

            final Sentence projGoal = task.sentence.projection(nal.time.time(), nal.narParameters.DURATION, nal.memory);
            if(projGoal!=null && projGoal.truth.getExpectation() > nal.narParameters.DECISION_THRESHOLD) {

                // keep goal updated
                nal.singlePremiseTask(projGoal, task.budget.clone());

                // we don't return here, allowing "roundtrips now", relevant for executing multiple steps of learned implication chains
            }
        }


        if (!task.aboveThreshold()) {
            return;
        } 

        double AntiSatisfaction = 0.5f; // we dont know anything about that goal yet
        if (beliefT != null) {
            final Sentence belief = beliefT.sentence;
            final Sentence projectedBelief = belief.projection(task.sentence.getOccurenceTime(), nal.narParameters.DURATION, nal.memory);
            AntiSatisfaction = task.sentence.truth.getExpDifAbs(projectedBelief.truth);
        }

        task.setPriority(task.getPriority()* (float)AntiSatisfaction);
        if (!task.aboveThreshold()) {
            return;
        }

        final TruthValue T=goal.truth.clone();
        final double Satisfaction=1.0-AntiSatisfaction;
        T.setFrequency((float) (T.getFrequency()-Satisfaction)); //decrease frequency according to satisfaction value
        final boolean isFullfilled = AntiSatisfaction < nal.narParameters.SATISFACTION_TRESHOLD;
        final Sentence projectedGoal = goal.projection(nal.time.time(), nal.time.time(), nal.memory);
        if (!(projectedGoal != null && task.aboveThreshold() && !isFullfilled)) {
            return;
        }
        bestReactionForGoal(concept, nal, projectedGoal, task);
        questionFromGoal(task, nal);
        concept.addToTable(task, false, concept.desires, nal.narParameters.CONCEPT_GOALS_MAX, Events.ConceptGoalAdd.class, Events.ConceptGoalRemove.class);
        InternalExperience.InternalExperienceFromTask(concept.memory, task, false, nal.time);
        if(!(task.sentence.getTerm() instanceof Operation)) {
            return;
        }
        processOperationGoal(projectedGoal, nal, concept, oldGoalT, task);
    }

    /**
     * To process an operation for potential execution
     * only called by processGoal
     * 
     * @param projectedGoal The current goal
     * @param nal The derivation context
     * @param concept The concept of the current goal
     * @param oldGoalT The best goal in the goal table
     */
    protected static void processOperationGoal(final Sentence projectedGoal, final DerivationContext nal, final Concept concept, final Task oldGoalT, final Task task) {
        if(projectedGoal.truth.getExpectation() > nal.narParameters.DECISION_THRESHOLD) {
            //see whether the goal evidence is fully included in the old goal, if yes don't execute
            //as execution for this reason already happened (or did not since there was evidence against it)
            final Set<BaseEntry> oldEvidence = new HashSet<>();
            boolean Subset=false;
            if(oldGoalT != null) {
                Subset = true;
                for(final BaseEntry l: oldGoalT.sentence.stamp.evidentialBase) {
                    oldEvidence.add(l);
                }
                for(final BaseEntry l: task.sentence.stamp.evidentialBase) {
                    if(!oldEvidence.contains(l)) {
                        Subset = false;
                        break;
                    }
                }
            }
            if(!Subset && !executeOperation(nal, task)) {
                concept.memory.emit(Events.UnexecutableGoal.class, task, concept, nal);
                return; //it was made true by itself
            }
        }
    }
    
    /**
     * Generate &lt;?how =/&gt; g&gt;? question for g! goal.
     * only called by processGoal
     *
     * @param task the task for which the question should be processed
     * @param nal The derivation context
     */    
    public static void questionFromGoal(final Task task, final DerivationContext nal) {
        if(nal.narParameters.QUESTION_GENERATION_ON_DECISION_MAKING || nal.narParameters.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
            //ok, how can we achieve it? add a question of whether it is fullfilled
            final List<Term> qu= new ArrayList<>();
            if(nal.narParameters.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
                if(!(task.sentence.term instanceof Equivalence) && !(task.sentence.term instanceof Implication)) {
                    final Variable how=new Variable("?how");
                    //Implication imp=Implication.make(how, task.sentence.term, TemporalRules.ORDER_CONCURRENT);
                    final Implication imp2=Implication.make(how, task.sentence.term, TemporalRules.ORDER_FORWARD);
                    //qu.add(imp);
                    if(!(task.sentence.term instanceof Operation)) {
                        qu.add(imp2);
                    }
                }
            }
            if(nal.narParameters.QUESTION_GENERATION_ON_DECISION_MAKING) {
                qu.add(task.sentence.term);
            }
            for(final Term q : qu) {
                if(q!=null) {
                    final Stamp st = new Stamp(task.sentence.stamp, nal.time.time());
                    st.setOccurrenceTime(task.sentence.getOccurenceTime()); //set tense of question to goal tense
                    final Sentence s = new Sentence(
                        q,
                        Symbols.QUESTION_MARK,
                        null,
                        st);

                    if(s!=null) {
                        final BudgetValue budget=new BudgetValue(task.getPriority()*nal.narParameters.CURIOSITY_DESIRE_PRIORITY_MUL,
                                                                 task.getDurability()*nal.narParameters.CURIOSITY_DESIRE_DURABILITY_MUL,
                                                                 1, nal.narParameters);
                        nal.singlePremiseTask(s, budget);
                    }
                }
            }
        }
    }
    
    private static class ExecutablePrecondition {
        public Operation bestop = null;
        public float bestop_truthexp = 0.0f;
        public TruthValue bestop_truth = null;
        public Task executable_precond = null;
        public long mintime = -1;
        public long maxtime = -1;
        public Map<Term,Term> substitution;
    }
    
    /**
    * When a goal is processed, use the best memorized reaction
    * that is applicable to the current context (recent events) in case that it exists.
    * This is a special case of the choice rule and allows certain behaviors to be automated.
    * 
    * @param concept The concept of the goal to realize
    * @param nal The derivation context
    * @param projectedGoal The current goal
    * @param task The goal task
    */
    protected static void bestReactionForGoal(final Concept concept, final DerivationContext nal, final Sentence projectedGoal, final Task task) {
        //1. if there is no solution known yet, pull up variable based preconditions from component concepts without replacing them
        Map<Term, Integer> ret = (projectedGoal.getTerm()).countTermRecursively(null);
        for(Term t : ret.keySet()) {
            final Concept get_concept = nal.memory.concept(t); //the concept to pull preconditions from
            if(get_concept == null || get_concept == concept) { //target concept does not exist or is the same as the goal concept
                continue;
            }
            //pull variable based preconditions from component concepts
            synchronized(get_concept) {
                for(Task precon : get_concept.general_executable_preconditions) {
                    //check whether the conclusion matches
                    if(Variables.findSubstitute(Symbols.VAR_INDEPENDENT, ((Implication)precon.sentence.term).getPredicate(), projectedGoal.term, new HashMap<>(), new HashMap<>())) {
                        ProcessJudgment.addToTargetConceptsPreconditions(precon, nal, concept); //it matches, so add to this concept!
                    }
                }
            }
        }
        //2. For the more specific hypotheses first and then the general
        for(List<Task> table : new List[] {concept.executable_preconditions, concept.general_executable_preconditions}) {
            //3. Apply choice rule, using the highest truth expectation solution
            ExecutablePrecondition bestOpWithMeta = calcBestExecutablePrecondition(nal, concept, projectedGoal, table);
            //4. And executing it, also forming an expectation about the result
            if(executePrecondition(nal, bestOpWithMeta, concept, projectedGoal, task)) {
                return; //don't try the other table as a specific solution was already used
            }
        }
    }
       
    /**
     * Search for the best precondition that best matches recent events, and is most successful in leading to goal fulfilment
     * 
     * @param nal The derivation context
     * @param concept The goal concept
     * @param projectedGoal The goal projected to the current time
     * @param execPreconditions The procedural hypotheses with the executable preconditions
     * @return 
     */
    private static ExecutablePrecondition calcBestExecutablePrecondition(final DerivationContext nal, final Concept concept, final Sentence projectedGoal, List<Task> execPreconditions) {
        ExecutablePrecondition result = new ExecutablePrecondition();
        for(final Task t: execPreconditions) {
            final Term[] prec = ((Conjunction) ((Implication) t.getTerm()).getSubject()).term;
            final Term[] newprec = new Term[prec.length-3];
            System.arraycopy(prec, 0, newprec, 0, prec.length - 3);
            final long add_tolerance = (long) (((Interval)prec[prec.length-1]).time*nal.narParameters.ANTICIPATION_TOLERANCE);
            result.mintime = nal.time.time();
            result.maxtime = nal.time.time() + add_tolerance;
            final Operation op = (Operation) prec[prec.length-2];
            final Term precondition = Conjunction.make(newprec,TemporalRules.ORDER_FORWARD);
            final Concept preconc = nal.memory.concept(precondition);
            long newesttime = -1;
            Task bestsofar = null;
            if(preconc == null) {
                continue;
            }
            //ok we can look now how much it is fullfilled
            //check recent events in event bag
            Map<Term,Term> subsBest = new HashMap<>();
            synchronized(concept.memory.seq_current) {
                for(final Task p : concept.memory.seq_current) {
                    Map<Term,Term> subs = new HashMap<>();
                    if(p.sentence.isJudgment() && !p.sentence.isEternal() && p.sentence.getOccurenceTime() > newesttime && p.sentence.getOccurenceTime() <= nal.time.time()) {
                        boolean preconditionMatches = Variables.findSubstitute(Symbols.VAR_INDEPENDENT, 
                                    CompoundTerm.replaceIntervals(precondition), 
                                    CompoundTerm.replaceIntervals(p.sentence.term), subs, new HashMap<>());
                        boolean conclusionMatches = Variables.findSubstitute(Symbols.VAR_INDEPENDENT, 
                                    CompoundTerm.replaceIntervals(((Implication) t.getTerm()).getPredicate()), 
                                    CompoundTerm.replaceIntervals(projectedGoal.getTerm()), subs, new HashMap<>());
                        if(preconditionMatches && conclusionMatches){
                            newesttime = p.sentence.getOccurenceTime();
                            //Apply interval penalty for interval differences in the precondition
                            Task pNew = new Task(p.sentence.clone(), p.budget.clone(), p.isInput() ? Task.EnumType.INPUT : Task.EnumType.DERIVED);
                            LocalRules.intervalProjection(nal, pNew.sentence.term, precondition, preconc, pNew.sentence.truth);
                            bestsofar = pNew;
                            subsBest = subs;
                        }
                    }
                }
            }
            if(bestsofar == null) {
                continue;
            }
            //ok now we can take the desire value:
            final TruthValue A = projectedGoal.getTruth();
            //and the truth of the hypothesis:
            final TruthValue Hyp = t.sentence.truth;
            //overlap will almost never happen, but to make sure
            if(Stamp.baseOverlap(projectedGoal.stamp.evidentialBase, t.sentence.stamp.evidentialBase) ||
               Stamp.baseOverlap(bestsofar.sentence.stamp.evidentialBase, t.sentence.stamp.evidentialBase) ||
               Stamp.baseOverlap(projectedGoal.stamp.evidentialBase, bestsofar.sentence.stamp.evidentialBase)) {
                continue;
            }
            //and the truth of the precondition:
            final Sentence projectedPrecon = bestsofar.sentence.projection(nal.time.time() /*- distance*/, nal.time.time(), concept.memory);
            if(projectedPrecon.isEternal()) {
                continue; //projection wasn't better than eternalization, too long in the past
            }
            final TruthValue precon = projectedPrecon.truth;
            //and derive the conjunction of the left side:
            final TruthValue leftside = TruthFunctions.desireDed(A, Hyp, concept.memory.narParameters);
            //in order to derive the operator desire value:
            final TruthValue opdesire = TruthFunctions.desireDed(precon, leftside, concept.memory.narParameters);
            final float expecdesire = opdesire.getExpectation();
            if(expecdesire > result.bestop_truthexp) {
                result.bestop = (Operation) ((CompoundTerm)op).applySubstitute(subsBest);
                result.bestop_truthexp = expecdesire;
                result.bestop_truth = opdesire;
                result.executable_precond = t;
                result.substitution = subsBest;
            }
        }
        return result;
    }
    
    /**
     * Execute the operation suggested by the most applicable precondition
     * 
     * @param nal The derivation context
     * @param precon The procedural hypothesis leading to goal
     * @param concept The concept of the goal
     * @param projectedGoal The goal projected to the current time
     * @param task The goal task
     */
    private static boolean executePrecondition(final DerivationContext nal, ExecutablePrecondition precon, final Concept concept, final Sentence projectedGoal, final Task task) {
        if(precon.bestop != null && precon.bestop_truthexp > nal.narParameters.DECISION_THRESHOLD /*&& Math.random() < bestop_truthexp */) {
            final Sentence createdSentence = new Sentence(
                precon.bestop,
                Symbols.JUDGMENT_MARK,
                precon.bestop_truth,
                projectedGoal.stamp);
            final Task t = new Task(createdSentence,
                                    new BudgetValue(1.0f,1.0f,1.0f, nal.narParameters),
                                    Task.EnumType.DERIVED);
            //System.out.println("used " +t.getTerm().toString() + String.valueOf(memory.randomNumber.nextInt()));
            if(!task.sentence.stamp.evidenceIsCyclic()) {
                if(!executeOperation(nal, t)) { //this task is just used as dummy
                    concept.memory.emit(Events.UnexecutableGoal.class, task, concept, nal);
                    return false;
                }
                ProcessAnticipation.anticipate(nal, precon.executable_precond.sentence, precon.executable_precond.budget, precon.mintime, precon.maxtime, 2, precon.substitution);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Entry point for all potentially executable operation tasks.
     * Returns true if the Task has a Term which can be executed
     * 
     * @param nal The derivation concept
     * @param t The operation goal task
     */
    public static boolean executeOperation(final DerivationContext nal, final Task t) {        
        final Term content = t.getTerm();
        if(!(nal.memory.allowExecution) || !(content instanceof Operation)) {
            return false;
        }  
        final Operation op=(Operation)content;
        final Operator oper = op.getOperator();
        final Product prod = (Product) op.getSubject();
        final Term arg = prod.term[0];
        if(oper instanceof FunctionOperator) {
            for(int i=0;i<prod.term.length-1;i++) { //except last one, the output arg
                if(prod.term[i].hasVarDep() || prod.term[i].hasVarIndep()) {
                    return false;
                }
            }
        } else {
            if(content.hasVarDep() || content.hasVarIndep()) {
                return false;
            }
        }
        if(!arg.equals(Term.SELF)) { //will be deprecated in the future
            return false;
        }

        op.setTask(t);
        if(!oper.call(op, nal.memory, nal.time)) {
            return false;
        }
        if (MiscFlags.DEBUG) {
            System.out.println(t.toStringLong());
        }
        return true;
    }
}

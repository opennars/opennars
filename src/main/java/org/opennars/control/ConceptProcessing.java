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
package org.opennars.control;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.opennars.main.Parameters;
import org.opennars.entity.*;
import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.io.events.OutputHandler;
import org.opennars.io.Symbols;
import org.opennars.language.*;
import org.opennars.operator.Operation;
import org.opennars.plugin.mental.InternalExperience;
import org.opennars.io.events.Events;
import static org.opennars.inference.LocalRules.revisible;
import static org.opennars.inference.LocalRules.revision;
import static org.opennars.inference.LocalRules.trySolution;
import org.opennars.operator.FunctionOperator;
import org.opennars.operator.Operator;
import org.opennars.operator.mental.Anticipate;
import org.opennars.operator.mental.Believe;
import org.opennars.operator.mental.Want;
import org.opennars.operator.mental.Evaluate;
import org.opennars.operator.mental.Wonder;

public class ConceptProcessing {
    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @param task The task to be processed
     * @return whether it was processed
     */
    public static boolean processTask(Concept concept, final DerivationContext nal, final Task task) {
        if(task.isInput()) {
            if(task.sentence.isJudgment() && !task.sentence.isEternal() && task.sentence.term instanceof Operation) {
                Operation op = (Operation) task.sentence.term;
                Operator o = (Operator) op.getPredicate();
                //only consider these mental ops an operation to track when executed not already when generated as internal event
                if(!(o instanceof Believe) && !(o instanceof Want) && !(o instanceof Wonder)
                        && !(o instanceof Evaluate) && !(o instanceof Anticipate)) {
                    TemporalInferenceControl.NewOperationFrame(nal.memory, task);
                }
            }
            concept.observable = true;
        }

        char type = task.sentence.punctuation;
        switch (type) {
            case Symbols.JUDGMENT_MARK:
                //memory.logic.JUDGMENT_PROCESS.commit();
                processJudgment(concept, nal, task);
                break;
            case Symbols.GOAL_MARK:
                //memory.logic.GOAL_PROCESS.commit();
                processGoal(concept, nal, task, true);
                break;
            case Symbols.QUESTION_MARK:
            case Symbols.QUEST_MARK:
                //memory.logic.QUESTION_PROCESS.commit();
                processQuestion(concept, nal, task);
                break;
            default:
                return false;
        }

        maintainDisappointedAnticipations(concept);

        if (task.aboveThreshold()) {    // still need to be processed
            //memory.logic.LINK_TO_TASK.commit();
            concept.linkToTask(task,nal);
        }

        return true;
    }

    /**
     * To accept a new judgment as belief, and check for revisions and solutions
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected static void processJudgment(Concept concept, final DerivationContext nal, final Task task) {
        final Sentence judg = task.sentence;

        boolean satisfiesAnticipation =
            task.isInput() &&
            !task.sentence.isEternal() &&
            concept.negConfirmation != null &&
            task.sentence.getOccurenceTime() > concept.negConfirm_abort_mintime;
        if(satisfiesAnticipation) {
            if(task.sentence.truth.getExpectation() > Parameters.DEFAULT_CONFIRMATION_EXPECTATION) {
                if(((Statement) concept.negConfirmation.sentence.term).getPredicate().equals(task.sentence.getTerm())) {
                    nal.memory.emit(OutputHandler.CONFIRM.class, ((Statement)concept.negConfirmation.sentence.term).getPredicate());
                    concept.negConfirmation = null; // confirmed
                }
            }
        }

        final Task oldBeliefT = concept.selectCandidate(task, concept.beliefs);   // only revise with the strongest -- how about projection?
        Sentence oldBelief = null;
        boolean wasRevised = false;
        if (oldBeliefT != null) {
            oldBelief = oldBeliefT.sentence;
            final Stamp newStamp = judg.stamp;
            final Stamp oldStamp = oldBelief.stamp;       //when table is full, the latter check is especially important too
            if (newStamp.equals(oldStamp,false,false,true)) {
                //if (task.getParentTask() != null && task.getParentTask().sentence.isJudgment()) {
                ////task.budget.decPriority(0);    // duplicated task
                //}   //// else: activated belief

                concept.memory.removeTask(task, "Duplicated");
                return;
            } else if (revisible(judg, oldBelief)) {

                nal.setTheNewStamp(newStamp, oldStamp, concept.memory.time());
                Sentence projectedBelief = oldBelief.projection(concept.memory.time(), newStamp.getOccurrenceTime());
                if (projectedBelief!=null) {
                    if (projectedBelief.getOccurenceTime()!=oldBelief.getOccurenceTime()) {
                        // nal.singlePremiseTask(projectedBelief, task.budget);
                    }
                    nal.setCurrentBelief(projectedBelief);
                    wasRevised = revision(judg, projectedBelief, false, nal);
                }
            }
        }
        if (task.aboveThreshold()) {
            int nnq = concept.questions.size();
            for (int i = 0; i < nnq; i++) {
                trySolution(judg, concept.questions.get(i), nal, true);
            }
            
            int nng = concept.desires.size();
            for (int i = 0; i < nng; i++) {
                trySolution(judg, concept.desires.get(i), nal, true);
            }

            concept.addToTable(task, false, concept.beliefs, Parameters.CONCEPT_BELIEFS_MAX, Events.ConceptBeliefAdd.class, Events.ConceptBeliefRemove.class);
            
            //if taskLink predicts this concept then add to predictive
            Task target = task;
            Term term = target.getTerm();
            if(//target.isObservablePrediction() &&
                    target.sentence.isEternal() &&
                            term instanceof Implication &&
                            !term.hasVarIndep())  //Might be relaxed in the future!!
            {

                Implication imp = (Implication) term;
                if(imp.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                    //also it has to be enactable, meaning the last entry of the sequence before the interval is an operation:
                    Term subj = imp.getSubject();
                    Term pred = imp.getPredicate();
                    Concept pred_conc = nal.memory.concept(pred);
                    if(pred_conc != null /*&& !(pred instanceof Operation)*/ && (subj instanceof Conjunction)) {
                        Conjunction conj = (Conjunction) subj;
                        if(!conj.isSpatial && conj.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                                conj.term.length >= 4 && conj.term.length%2 == 0 &&
                                conj.term[conj.term.length-1] instanceof Interval &&
                                conj.term[conj.term.length-2] instanceof Operation) {

                            //we do not add the target, instead the strongest belief in the target concept
                            if(concept.beliefs.size() > 0) {
                                Task strongest_target = null; //beliefs.get(0);
                                //get the first eternal:
                                for(Task t : concept.beliefs) {
                                    if(t.sentence.isEternal()) {
                                        strongest_target = t;
                                        break;
                                    }
                                }

                                int a = pred_conc.executable_preconditions.size();

                                //at first we have to remove the last one with same content from table
                                int i_delete = -1;
                                for(int i=0; i < pred_conc.executable_preconditions.size(); i++) {
                                    if(CompoundTerm.replaceIntervals(pred_conc.executable_preconditions.get(i).getTerm()).equals(
                                            CompoundTerm.replaceIntervals(strongest_target.getTerm()))) {
                                        i_delete = i; //even these with same term but different intervals are removed here
                                        break;
                                    }
                                }
                                if(i_delete != -1) {
                                    pred_conc.executable_preconditions.remove(i_delete);
                                }

                                Term[] prec = ((Conjunction) ((Implication) strongest_target.getTerm()).getSubject()).term;
                                for(int i=0;i<prec.length-2;i++) {
                                    if(prec[i] instanceof Operation) { //don't react to precondition with an operation before the last
                                        return; //for now, these can be decomposed into smaller such statements anyway
                                    }
                                }

                                //this way the strongest confident result of this content is put into table but the table ranked according to truth expectation
                                pred_conc.addToTable(strongest_target, true, pred_conc.executable_preconditions, Parameters.CONCEPT_BELIEFS_MAX, Events.EnactableExplainationAdd.class, Events.EnactableExplainationRemove.class);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it, potentially executing in case of an operation goal
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected static boolean processGoal(Concept concept, final DerivationContext nal, final Task task, boolean shortcut) {

        final Sentence goal = task.sentence;
        final Task oldGoalT = concept.selectCandidate(task, concept.desires); // revise with the existing desire values
        Sentence oldGoal = null;
        final Stamp newStamp = goal.stamp;
        if (oldGoalT != null) {
            oldGoal = oldGoalT.sentence;
            final Stamp oldStamp = oldGoal.stamp;

            if (newStamp.equals(oldStamp,false,false,true)) {
                return false; // duplicate
            }
        }
        Task beliefT = null;

        if(task.aboveThreshold()) {
            beliefT = concept.selectCandidate(task, concept.beliefs); // check if the Goal is already satisfied
            int nnq = concept.quests.size();
            for (int i = 0; i < nnq; i++) {
                trySolution(task.sentence, concept.quests.get(i), nal, true);
            }
            if (beliefT != null) { 
                trySolution(beliefT.sentence, task, nal, true); // check if the Goal is already satisfied (manipulate budget)
            }
        }
        if (oldGoalT != null) {
            if (revisible(goal, oldGoal)) {
                final Stamp oldStamp = oldGoal.stamp;
                nal.setTheNewStamp(newStamp, oldStamp, concept.memory.time());

                Sentence projectedGoal = oldGoal.projection(task.sentence.getOccurenceTime(), newStamp.getOccurrenceTime());
                if (projectedGoal!=null) {
                    // if (goal.after(oldGoal, nal.memory.param.duration.get())) { //no need to project the old goal, it will be projected if selected anyway now
                    // nal.singlePremiseTask(projectedGoal, task.budget);
                    //return;
                    // }
                    nal.setCurrentBelief(projectedGoal);
                    boolean successOfRevision=revision(task.sentence, projectedGoal, false, nal);
                    if(successOfRevision) { // it is revised, so there is a new task for which this function will be called
                        return false; // with higher/lower desire
                    } //it is not allowed to go on directly due to decision making https://groups.google.com/forum/#!topic/open-nars/lQD0no2ovx4
                }
            }
        }

        Stamp s2=goal.stamp.clone();
        s2.setOccurrenceTime(concept.memory.time());
        if(s2.after(task.sentence.stamp, Parameters.DURATION)) { //this task is not up to date we have to project it first
            Sentence projGoal = task.sentence.projection(concept.memory.time(), Parameters.DURATION);
            if(projGoal!=null && projGoal.truth.getExpectation() > nal.memory.param.decisionThreshold.get()) {
                nal.singlePremiseTask(projGoal, task.budget.clone()); //keep goal updated
                // return false; //outcommented, allowing "roundtrips now", relevant for executing multiple steps of learned implication chains
            }
        }

        if (task.aboveThreshold()) {

            double AntiSatisfaction = 0.5f; //we dont know anything about that goal yet, so we pursue it to remember it because its maximally unsatisfied
            if (beliefT != null) {
                Sentence belief = beliefT.sentence;
                Sentence projectedBelief = belief.projection(task.sentence.getOccurenceTime(), Parameters.DURATION);
                AntiSatisfaction = task.sentence.truth.getExpDifAbs(projectedBelief.truth);
            }

            double Satisfaction=1.0-AntiSatisfaction;
            task.setPriority(task.getPriority()* (float)AntiSatisfaction);
            if (!task.aboveThreshold()) {
                return false;
            }
            TruthValue T=goal.truth.clone();

            T.setFrequency((float) (T.getFrequency()-Satisfaction)); //decrease frequency according to satisfaction value

            boolean fullfilled = AntiSatisfaction < Parameters.SATISFACTION_TRESHOLD;

            Sentence projectedGoal = goal.projection(nal.memory.time(),nal.memory.time());

            if (projectedGoal != null && task.aboveThreshold() && !fullfilled) {

                bestReactionForGoal(concept, nal, projectedGoal, task);

                questionFromGoal(task, nal);

                concept.addToTable(task, false, concept.desires, Parameters.CONCEPT_GOALS_MAX, Events.ConceptGoalAdd.class, Events.ConceptGoalRemove.class);

                InternalExperience.InternalExperienceFromTask(concept.memory,task,false);

                if(projectedGoal.truth.getExpectation() > nal.memory.param.decisionThreshold.get() && nal.memory.time() >= concept.memory.decisionBlock) {
                    //see whether the goal evidence is fully included in the old goal, if yes don't execute
                    //as execution for this reason already happened (or did not since there was evidence against it)
                    HashSet<Long> oldEvidence = new HashSet<Long>();
                    boolean Subset=false;
                    if(oldGoalT != null) {
                        Subset = true;
                        for(Long l: oldGoalT.sentence.stamp.evidentialBase) {
                            oldEvidence.add(l);
                        }
                        for(Long l: task.sentence.stamp.evidentialBase) {
                            if(!oldEvidence.contains(l)) {
                                Subset = false;
                                break;
                            }
                        }
                    }
                    if(!Subset && !executeDecision(nal, task)) {
                        concept.memory.emit(Events.UnexecutableGoal.class, task, concept, nal);
                        return true; //it was made true by itself
                    }
                }
                return false;
            }
            return fullfilled;
        }
        return false;
    }
    
    public static void questionFromGoal(final Task task, final DerivationContext nal) {
        if(Parameters.QUESTION_GENERATION_ON_DECISION_MAKING || Parameters.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
            //ok, how can we achieve it? add a question of whether it is fullfilled
            ArrayList<Term> qu=new ArrayList<Term>();
            if(Parameters.HOW_QUESTION_GENERATION_ON_DECISION_MAKING) {
                if(!(task.sentence.term instanceof Equivalence) && !(task.sentence.term instanceof Implication)) {
                    Variable how=new Variable("?how");
                    //Implication imp=Implication.make(how, task.sentence.term, TemporalRules.ORDER_CONCURRENT);
                    Implication imp2=Implication.make(how, task.sentence.term, TemporalRules.ORDER_FORWARD);
                    //qu.add(imp);
                    if(!(task.sentence.term instanceof Operation)) {
                        qu.add(imp2);
                    }
                }
            }
            if(Parameters.QUESTION_GENERATION_ON_DECISION_MAKING) {
                qu.add(task.sentence.term);
            }
            for(Term q : qu) {
                if(q!=null) {
                    Stamp st = new Stamp(task.sentence.stamp,nal.memory.time());
                    st.setOccurrenceTime(task.sentence.getOccurenceTime()); //set tense of question to goal tense
                    Sentence s = new Sentence(
                        q,
                        Symbols.QUESTION_MARK,
                        null,
                        st);

                    if(s!=null) {
                        BudgetValue budget=new BudgetValue(task.getPriority()*Parameters.CURIOSITY_DESIRE_PRIORITY_MUL,task.getDurability()*Parameters.CURIOSITY_DESIRE_DURABILITY_MUL,1);
                        nal.singlePremiseTask(s, budget);
                    }
                }
            }
        }
    }


    /**
     * To answer a question by existing beliefs
     *
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected static void processQuestion(Concept concept, final DerivationContext nal, final Task task) {

        Task quesTask = task;
        boolean newQuestion = true;
        
        List<Task> questions = concept.questions;
        if(task.sentence.punctuation == Symbols.QUEST_MARK) {
            questions = concept.quests;
        }
        for (final Task t : questions) {
            if (t.sentence.term.equals(quesTask.sentence.term)) {
                quesTask = t;
                newQuestion = false;
                break;
            }
        }

        if (newQuestion) {
            if (questions.size() + 1 > Parameters.CONCEPT_QUESTIONS_MAX) {
                Task removed = questions.remove(0);    // FIFO
                concept.memory.event.emit(Events.ConceptQuestionRemove.class, concept, removed);
            }

            questions.add(task);
            concept.memory.event.emit(Events.ConceptQuestionAdd.class, concept, task);
        }

        Sentence ques = quesTask.sentence;
        final Task newAnswerT = (ques.isQuestion())
                ? concept.selectCandidate(quesTask, concept.beliefs)
                : concept.selectCandidate(quesTask, concept.desires);

        if (newAnswerT != null) {
            trySolution(newAnswerT.sentence, task, nal, true);
        }
        else if(task.isInput() && !quesTask.getTerm().hasVarQuery() && quesTask.getBestSolution() != null) { // show previously found solution anyway in case of input
            concept.memory.emit(Events.Answer.class, quesTask, quesTask.getBestSolution());
        }
    }

    /**
    * When a goal is processed, use the best memorized reaction
    * that is applicable to the current context (recent events) in case that it exists.
    * This is a special case of the choice rule and allows certain behaviors to be automated.
    */
    protected static void bestReactionForGoal(Concept concept, final DerivationContext nal, Sentence projectedGoal, final Task task) {
        try{
            Operation bestop = null;
            float bestop_truthexp = 0.0f;
            TruthValue bestop_truth = null;
            Task executable_precond = null;
            long mintime = -1;
            long maxtime = -1;
            for(Task t: concept.executable_preconditions) {
                Term[] prec = ((Conjunction) ((Implication) t.getTerm()).getSubject()).term;
                Term[] newprec = new Term[prec.length-3];
                System.arraycopy(prec, 0, newprec, 0, prec.length - 3);

                long add_tolerance = (long) (((Interval)prec[prec.length-1]).time*Parameters.ANTICIPATION_TOLERANCE);
                mintime = nal.memory.time();
                maxtime = nal.memory.time() + add_tolerance;

                Operation op = (Operation) prec[prec.length-2];
                Term precondition = Conjunction.make(newprec,TemporalRules.ORDER_FORWARD);

                Concept preconc = nal.memory.concept(precondition);
                long newesttime = -1;
                Task bestsofar = null;
                if(preconc != null) { //ok we can look now how much it is fullfilled

                    //check recent events in event bag
                    for(Task p : concept.memory.seq_current) {
                        if(p.sentence.term.equals(preconc.term) && p.sentence.isJudgment() && !p.sentence.isEternal() && p.sentence.getOccurenceTime() > newesttime  && p.sentence.getOccurenceTime() <= concept.memory.time()) {
                            newesttime = p.sentence.getOccurenceTime();
                            bestsofar = p; //we use the newest for now
                        }
                    }
                    if(bestsofar == null) {
                        continue;
                    }
                    //ok now we can take the desire value:
                    TruthValue A = projectedGoal.getTruth();
                    //and the truth of the hypothesis:
                    TruthValue Hyp = t.sentence.truth;
                    //overlap will almost never happen, but to make sure
                    if(Stamp.baseOverlap(projectedGoal.stamp.evidentialBase, t.sentence.stamp.evidentialBase)) {
                        continue; //base overlap
                    }
                    if(Stamp.baseOverlap(bestsofar.sentence.stamp.evidentialBase, t.sentence.stamp.evidentialBase)) {
                        continue; //base overlap
                    }
                    if(Stamp.baseOverlap(projectedGoal.stamp.evidentialBase, bestsofar.sentence.stamp.evidentialBase)) {
                        continue; //base overlap
                    }
                    //and the truth of the precondition:
                    Sentence projectedPrecon = bestsofar.sentence.projection(concept.memory.time() /*- distance*/, concept.memory.time());

                    if(projectedPrecon.isEternal()) {
                        continue; //projection wasn't better than eternalization, too long in the past
                    }
                    //debug start
                    //long timeA = memory.time();
                    //long timeOLD = bestsofar.sentence.stamp.getOccurrenceTime();
                    //long timeNEW = projectedPrecon.stamp.getOccurrenceTime();
                    //debug end
                    TruthValue precon = projectedPrecon.truth;
                    //and derive the conjunction of the left side:
                    TruthValue leftside = TruthFunctions.desireDed(A, Hyp);
                    //in order to derive the operator desire value:
                    TruthValue opdesire = TruthFunctions.desireDed(precon, leftside);

                    float expecdesire = opdesire.getExpectation();
                    if(expecdesire > bestop_truthexp) {
                        bestop = op;
                        bestop_truthexp = expecdesire;
                        bestop_truth = opdesire;
                        executable_precond = t;
                    }
                }
            }

            if(bestop != null && bestop_truthexp > concept.memory.param.decisionThreshold.get() /*&& Math.random() < bestop_truthexp */) {
                
                Sentence createdSentence = new Sentence(
                        bestop,
                        Symbols.JUDGMENT_MARK,
                        bestop_truth,
                        projectedGoal.stamp);

                Task t = new Task(createdSentence, 
                                  new BudgetValue(1.0f,1.0f,1.0f),
                                  false);
                //System.out.println("used " +t.getTerm().toString() + String.valueOf(memory.randomNumber.nextInt()));
                if(!task.sentence.stamp.evidenceIsCyclic()) {
                    if(!executeDecision(nal, t)) { //this task is just used as dummy
                        concept.memory.emit(Events.UnexecutableGoal.class, task, concept, nal);
                    } else {
                        concept.memory.decisionBlock = concept.memory.time() + Parameters.AUTOMATIC_DECISION_USUAL_DECISION_BLOCK_CYCLES;
                        generatePotentialNegConfirmation(nal, executable_precond.sentence, executable_precond.budget, mintime, maxtime, 2);
                    }
                }
            }
        } catch(Exception ex) {
            System.out.println("Failure in operation choice rule, analyze!");
        }
    }

    public static void generatePotentialNegConfirmation(DerivationContext nal, Sentence mainSentence, BudgetValue budget, long mintime, long maxtime, float priority) {
        //derivation was successful and it was a judgment event
        
        try { //that was predicted by an eternal belief that shifted time
        Stamp stamp = new Stamp(nal.memory);
        stamp.setOccurrenceTime(Stamp.ETERNAL);
        //long serial = stamp.evidentialBase[0];
        Sentence s = new Sentence(
            mainSentence.term,
            mainSentence.punctuation,
            new TruthValue(0.0f, 0.0f),
            stamp);

        //s.producedByTemporalInduction = true; //also here to not go into sequence buffer
        Task t = new Task(s, new BudgetValue(0.99f,0.1f,0.1f), false); //Budget for one-time processing
        Concept c = nal.memory.concept(((Statement) mainSentence.term).getPredicate()); //put into consequence concept
        if(c != null /*&& mintime > nal.memory.time()*/ && c.observable && mainSentence.getTerm() instanceof Statement && ((Statement)mainSentence.getTerm()).getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            if(c.negConfirmation == null || priority > c.negConfirmationPriority /*|| t.getPriority() > c.negConfirmation.getPriority() */) {
                c.negConfirmation = t;
                c.negConfirmationPriority = priority;
                c.negConfirm_abort_maxtime = maxtime;
                c.negConfirm_abort_mintime = mintime;
                
                if(c.negConfirmation.sentence.term instanceof Implication) {
                    Implication imp = (Implication) c.negConfirmation.sentence.term;
                    Concept ctarget = nal.memory.concept(imp.getPredicate());
                    if(ctarget != null && ctarget.getPriority()>=InternalExperience.MINIMUM_CONCEPT_PRIORITY_TO_CREATE_ANTICIPATION) {
                        ((Anticipate)c.memory.getOperator("^anticipate")).anticipationFeedback(imp.getPredicate(), null, c.memory);
                    }
                }
                
                nal.memory.emit(OutputHandler.ANTICIPATE.class,((Statement) c.negConfirmation.sentence.term).getPredicate()); //disappoint/confirm printed anyway
            }
       }
        }catch(Exception ex) {
            System.out.println("problem in anticipation handling");
        }
    }

    /**
     * Entry point for all potentially executable tasks.
     * Returns true if the Task has a Term which can be executed
     */
    public static boolean executeDecision(DerivationContext nal, final Task t) {
        //if (isDesired()) 
        if(nal.memory.allowExecution)
        {
            
            Term content = t.getTerm();

            if(content instanceof Operation) {

                Operation op=(Operation)content;
                Operator oper = op.getOperator();
                Product prod = (Product) op.getSubject();
                Term arg = prod.term[0];
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
                if(!oper.call(op, nal.memory)) {
                    return false;
                }
                System.out.println(t.toStringLong());
                //this.memory.sequenceTasks = new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE);
                return true;
            }
        }
        return false;
    }

    public static void maintainDisappointedAnticipations(Concept concept) {
        //here we can check the expiration of the feedback:
        if(concept.negConfirmation == null || concept.memory.time() <= concept.negConfirm_abort_maxtime) {
            return;
        }

        //at first search beliefs for input tasks:
        boolean cancelled = false;
        for(TaskLink tl : concept.taskLinks) { //search for input in tasklinks (beliefs alone can not take temporality into account as the eternals will win)
            Task t = tl.targetTask;
            if(t!= null && t.sentence.isJudgment() && t.isInput() && !t.sentence.isEternal() && t.sentence.truth.getExpectation() > Parameters.DEFAULT_CONFIRMATION_EXPECTATION &&
                    CompoundTerm.replaceIntervals(t.sentence.term).equals(CompoundTerm.replaceIntervals(concept.getTerm()))) {
                if(t.sentence.getOccurenceTime() >= concept.negConfirm_abort_mintime && t.sentence.getOccurenceTime() <= concept.negConfirm_abort_maxtime) {
                    cancelled = true;
                    break;
                }
            }
        }

        if(cancelled) {
            concept.memory.emit(OutputHandler.CONFIRM.class,((Statement) concept.negConfirmation.sentence.term).getPredicate());
            concept.negConfirmation = null; //confirmed
            return;
        }
        
        Term T = ((Statement)concept.negConfirmation.getTerm()).getPredicate();
        Sentence s1 = new Sentence(T, Symbols.JUDGMENT_MARK, new TruthValue(0.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE),
                        new Stamp(concept.memory));
        Sentence s2 = new Sentence(Negation.make(T), Symbols.JUDGMENT_MARK, new TruthValue(1.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE),
                        new Stamp(concept.memory));
        Task negated1 = new Task(s1,concept.negConfirmation.getBudget().clone(),true);
        Task negated2 = new Task(s2,concept.negConfirmation.getBudget().clone(),true);
        concept.memory.inputTask(negated1, false); //disappointed
        concept.memory.inputTask(negated2, false); //disappointed
        concept.memory.emit(OutputHandler.DISAPPOINT.class,((Statement) concept.negConfirmation.sentence.term).getPredicate());
        concept.negConfirmation = null;
    }
    
    public static void ProcessWhatQuestionAnswer(Concept concept, Task t, DerivationContext nal) {
        if(!t.sentence.term.hasVarQuery() && t.sentence.isJudgment() || t.sentence.isGoal()) { //ok query var, search
            for(TaskLink quess: concept.taskLinks) {
                Task ques = quess.getTarget();
                if(((ques.sentence.isQuestion() && t.sentence.isJudgment()) ||
                    (ques.sentence.isGoal()     && t.sentence.isJudgment()) ||
                    (ques.sentence.isQuest()    && t.sentence.isGoal())) && ques.getTerm().hasVarQuery()) {
                    boolean newAnswer = false;
                    Term[] u = new Term[] { ques.getTerm(), t.getTerm() };
                    if(ques.sentence.term.hasVarQuery() && !t.getTerm().hasVarQuery() && Variables.unify(Symbols.VAR_QUERY, u)) {
                        Concept c = nal.memory.concept(t.getTerm());
                        List<Task> answers = ques.sentence.isQuest() ? c.desires : c.beliefs;
                        if(c != null && answers.size() > 0) {
                            final Task taskAnswer = answers.get(0);
                            if(taskAnswer!=null) {
                                newAnswer |= trySolution(taskAnswer.sentence, ques, nal, false); //order important here
                            }
                        }
                    }
                    if(newAnswer && ques.isInput()) {
                       nal.memory.emit(Events.Answer.class, ques, ques.getBestSolution());
                    }
                }
            }
        }
    }

    public static void ProcessWhatQuestion(Concept concept, Task ques, DerivationContext nal) {
        if(!(ques.sentence.isJudgment()) && ques.getTerm().hasVarQuery()) { //ok query var, search
            boolean newAnswer = false;
            
            for(TaskLink t : concept.taskLinks) {
                
                Term[] u = new Term[] { ques.getTerm(), t.getTerm() };
                if(!t.getTerm().hasVarQuery() && Variables.unify(Symbols.VAR_QUERY, u)) {
                    Concept c = nal.memory.concept(t.getTerm());
                    List<Task> answers = ques.sentence.isQuest() ? c.desires : c.beliefs;
                    if(c != null && answers.size() > 0) {
                        final Task taskAnswer = answers.get(0);
                        if(taskAnswer!=null) {
                            newAnswer |= trySolution(taskAnswer.sentence, ques, nal, false); //order important here
                        }
                    }
                }
            }
            if(newAnswer && ques.isInput()) {
                nal.memory.emit(Events.Answer.class, ques, ques.getBestSolution());
            }
        }
    }
}

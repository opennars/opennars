package nars.control;

import java.util.ArrayList;
import nars.config.Parameters;
import nars.entity.*;
import nars.inference.SyllogisticRules;
import nars.inference.TemporalRules;
import nars.inference.TruthFunctions;
import nars.io.Output;
import nars.io.Symbols;
import nars.language.*;
import nars.operator.Operation;
import nars.plugin.mental.InternalExperience;
import nars.util.Events;

import static nars.inference.LocalRules.revisible;
import static nars.inference.LocalRules.revision;
import static nars.inference.LocalRules.trySolution;
import nars.operator.Operator;

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
                    nal.memory.emit(Output.CONFIRM.class, ((Statement)concept.negConfirmation.sentence.term).getPredicate());
                    concept.negConfirmation = null; // confirmed
                }
            }
        }

        final Task oldBeliefT = concept.selectCandidate(task, concept.beliefs, true);   // only revise with the strongest -- how about projection?
        Sentence oldBelief = null;
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
                    revision(judg, projectedBelief, false, nal);
                }
            }
        }
        if (task.aboveThreshold()) {
            int nnq = concept.questions.size();
            for (int i = 0; i < nnq; i++) {
                trySolution(judg, concept.questions.get(i), nal, true);
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
                                    if(CompoundTerm.cloneDeepReplaceIntervals(pred_conc.executable_preconditions.get(i).getTerm()).equals(
                                            CompoundTerm.cloneDeepReplaceIntervals(strongest_target.getTerm()))) {
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
        final Task oldGoalT = concept.selectCandidate(task, concept.desires, true); // revise with the existing desire values
        Sentence oldGoal = null;

        if (oldGoalT != null) {
            oldGoal = oldGoalT.sentence;
            final Stamp newStamp = goal.stamp;
            final Stamp oldStamp = oldGoal.stamp;


            if (newStamp.equals(oldStamp,false,false,true)) {
                return false; // duplicate
            }
            if (revisible(goal, oldGoal)) {

                nal.setTheNewStamp(newStamp, oldStamp, concept.memory.time());

                Sentence projectedGoal = oldGoal.projection(task.sentence.getOccurenceTime(), newStamp.getOccurrenceTime());
                if (projectedGoal!=null) {
                    // if (goal.after(oldGoal, nal.memory.param.duration.get())) { //no need to project the old goal, it will be projected if selected anyway now
                    // nal.singlePremiseTask(projectedGoal, task.budget);
                    //return;
                    // }
                    nal.setCurrentBelief(projectedGoal);
                    if(!(task.sentence.term instanceof Operation)) {
                        boolean successOfRevision=revision(task.sentence, projectedGoal, false, nal);
                        if(successOfRevision) { // it is revised, so there is a new task for which this function will be called
                            return false; // with higher/lower desire
                        } //it is not allowed to go on directly due to decision making https://groups.google.com/forum/#!topic/open-nars/lQD0no2ovx4
                    }
                }
            }
        }

        Stamp s2=goal.stamp.clone();
        s2.setOccurrenceTime(concept.memory.time());
        if(s2.after(task.sentence.stamp, nal.memory.param.duration.get())) { //this task is not up to date we have to project it first
            Sentence projGoal = task.sentence.projection(concept.memory.time(), nal.memory.param.duration.get());
            if(projGoal!=null && projGoal.truth.getExpectation() > nal.memory.param.decisionThreshold.get()) {
                nal.singlePremiseTask(projGoal, task.budget.clone()); //keep goal updated
                // return false; //outcommented, allowing "roundtrips now", relevant for executing multiple steps of learned implication chains
            }
        }

        if (task.aboveThreshold()) {

            final Task beliefT = concept.selectCandidate(task, concept.beliefs, false); // check if the Goal is already satisfied

            double AntiSatisfaction = 0.5f; //we dont know anything about that goal yet, so we pursue it to remember it because its maximally unsatisfied
            if (beliefT != null) {
                Sentence belief = beliefT.sentence;
                Sentence projectedBelief = belief.projection(task.sentence.getOccurenceTime(), nal.memory.param.duration.get());
                trySolution(projectedBelief, task, nal, true); // check if the Goal is already satisfied (manipulate budget)
                AntiSatisfaction = task.sentence.truth.getExpDifAbs(belief.truth);
            }

            double Satisfaction=1.0-AntiSatisfaction;
            TruthValue T=goal.truth.clone();

            T.setFrequency((float) (T.getFrequency()-Satisfaction)); //decrease frequency according to satisfaction value

            boolean fullfilled = AntiSatisfaction < Parameters.SATISFACTION_TRESHOLD;

            Sentence projectedGoal = goal.projection(nal.memory.time(),nal.memory.time());

            if (projectedGoal != null && task.aboveThreshold() && !fullfilled) {

                bestReactionForGoal(concept, nal, projectedGoal, task);

                questionFromGoal(task, nal);

                concept.addToTable(task, false, concept.desires, Parameters.CONCEPT_GOALS_MAX, Events.ConceptGoalAdd.class, Events.ConceptGoalRemove.class);

                InternalExperience.InternalExperienceFromTask(concept.memory,task,false);

                if(projectedGoal.truth.getExpectation() > nal.memory.param.decisionThreshold.get() && nal.memory.time() >= concept.memory.decisionBlock && !executeDecision(nal, task)) {
                    concept.memory.emit(Events.UnexecutableGoal.class, task, concept, nal);
                    return true; //it was made true by itself
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
        for (final Task t : concept.questions) {
            if (t.sentence.equalsContent(quesTask.sentence)) {
                quesTask = t;
                newQuestion = false;
                break;
            }
        }

        if (newQuestion) {
            if (concept.questions.size() + 1 > Parameters.CONCEPT_QUESTIONS_MAX) {
                Task removed = concept.questions.remove(0);    // FIFO
                concept.memory.event.emit(Events.ConceptQuestionRemove.class, concept, removed);
            }

            concept.questions.add(task);
            concept.memory.event.emit(Events.ConceptQuestionAdd.class, concept, task);
        }

        Sentence ques = quesTask.sentence;
        final Task newAnswerT = (ques.isQuestion())
                ? concept.selectCandidate(quesTask, concept.beliefs, false)
                : concept.selectCandidate(quesTask, concept.desires, false);

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
            //long distance = -1;
            long mintime = -1;
            long maxtime = -1;
            for(Task t: concept.executable_preconditions) {
                Term[] prec = ((Conjunction) ((Implication) t.getTerm()).getSubject()).term;
                Term[] newprec = new Term[prec.length-3];
                System.arraycopy(prec, 0, newprec, 0, prec.length - 3);

                //distance = Interval.magnitudeToTime(((Interval)prec[prec.length-1]).magnitude, nal.memory.param.duration);
                mintime = nal.memory.time() + Interval.magnitudeToTime(((Interval)prec[prec.length-1]).magnitude-1, nal.memory.param.duration);
                maxtime = nal.memory.time() + Interval.magnitudeToTime(((Interval)prec[prec.length-1]).magnitude+2, nal.memory.param.duration);

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

                Task t = new Task(createdSentence, new BudgetValue(1.0f,1.0f,1.0f));
                //System.out.println("used " +t.getTerm().toString() + String.valueOf(memory.randomNumber.nextInt()));
                if(!task.sentence.stamp.evidenceIsCyclic()) {
                    if(!executeDecision(nal, t)) { //this task is just used as dummy
                        concept.memory.emit(Events.UnexecutableGoal.class, task, concept, nal);
                    } else {
                        concept.memory.decisionBlock = concept.memory.time() + Parameters.AUTOMATIC_DECISION_USUAL_DECISION_BLOCK_CYCLES;
                        SyllogisticRules.generatePotentialNegConfirmation(nal, executable_precond.sentence, executable_precond.budget, mintime, maxtime, 2);
                    }
                }
            }
        } catch(Exception ex) {
            System.out.println("Failure in operation choice rule, analyze!");
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

            if(content instanceof Operation && !content.hasVarDep() && !content.hasVarIndep()) {

                Operation op=(Operation)content;
                Operator oper = op.getOperator();
                Product prod = (Product) op.getSubject();
                Term arg = prod.term[0];
                if(!arg.equals(Term.SELF)) { //will be deprecated in the future
                    return false;
                }

                op.setTask(t);
                if(!oper.call(op, nal.memory)) {
                    return false;
                }
                TemporalInferenceControl.NewOperationFrame(nal.memory, t);
                
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
                    CompoundTerm.cloneDeepReplaceIntervals(t.sentence.term).equals(CompoundTerm.cloneDeepReplaceIntervals(concept.getTerm()))) {
                if(t.sentence.getOccurenceTime() >= concept.negConfirm_abort_mintime && t.sentence.getOccurenceTime() <= concept.negConfirm_abort_maxtime) {
                    cancelled = true;
                    break;
                }
            }
        }

        if(cancelled) {
            concept.memory.emit(Output.CONFIRM.class,((Statement) concept.negConfirmation.sentence.term).getPredicate());
            concept.negConfirmation = null; //confirmed
            return;
        }

        concept.memory.inputTask(concept.negConfirmation, false); //disappointed
        //if(this.negConfirmationPriority >= 2) {
        //    System.out.println(this.negConfirmation.sentence.term);
        //}
        concept.memory.emit(Output.DISAPPOINT.class,((Statement) concept.negConfirmation.sentence.term).getPredicate());
        concept.negConfirmation = null;
    }
    
    public static void ProcessWhatQuestionAnswer(Concept concept, Task t, DerivationContext nal) {
        Task ques;
        if(t.sentence.isJudgment()) { //ok query var, search
            for(TaskLink quess: concept.taskLinks) {
                ques = quess.getTarget();
                if((ques.sentence.isQuestion() || ques.sentence.isQuest()) && ques.getTerm().hasVarQuery()) {
                    boolean newAnswer = false;
                    Term[] u = new Term[] { ques.getTerm(), t.getTerm() };
                    if(!t.getTerm().hasVarQuery() && Variables.unify(Symbols.VAR_QUERY, u)) {
                        Concept c = nal.memory.concept(t.getTerm());
                        if(c != null && ques.sentence.isQuestion() && c.beliefs.size() > 0) {
                            final Task taskAnswer = c.beliefs.get(0);
                            if(taskAnswer!=null) {
                                newAnswer |= trySolution(taskAnswer.sentence, ques, nal, false); //order important here
                            }
                        }
                        if(c != null && ques.sentence.isQuest() &&  c.desires.size() > 0) {
                            final Task taskAnswer = c.desires.get(0);
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
        if((ques.sentence.isQuestion() || ques.sentence.isQuest()) && ques.getTerm().hasVarQuery()) { //ok query var, search
            boolean newAnswer = false;
            
            for(TaskLink t : concept.taskLinks) {
                
                Term[] u = new Term[] { ques.getTerm(), t.getTerm() };
                if(!t.getTerm().hasVarQuery() && Variables.unify(Symbols.VAR_QUERY, u)) {
                    Concept c = nal.memory.concept(t.getTerm());
                    if(c != null && ques.sentence.isQuestion() && c.beliefs.size() > 0) {
                        final Task taskAnswer = c.beliefs.get(0);
                        if(taskAnswer!=null) {
                            newAnswer |= trySolution(taskAnswer.sentence, ques, nal, false); //order important here
                        }
                    }
                    if(c != null && ques.sentence.isQuest() &&  c.desires.size() > 0) {
                        final Task taskAnswer = c.desires.get(0);
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

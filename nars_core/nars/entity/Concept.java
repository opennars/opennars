/*
 * Concept.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nars.inference.TruthFunctions;
import nars.util.Events.BeliefSelect;
import nars.util.Events.ConceptBeliefAdd;
import nars.util.Events.ConceptBeliefRemove;
import nars.util.Events.ConceptGoalAdd;
import nars.util.Events.ConceptGoalRemove;
import nars.util.Events.ConceptQuestionAdd;
import nars.util.Events.ConceptQuestionRemove;
import nars.util.Events.TaskLinkAdd;
import nars.util.Events.TaskLinkRemove;
import nars.util.Events.TermLinkAdd;
import nars.util.Events.TermLinkRemove;
import nars.util.Events.UnexecutableGoal;
import nars.storage.Memory;
import nars.io.NARConsole;
import nars.config.Parameters;
import nars.control.DerivationContext;
import static nars.inference.BudgetFunctions.distributeAmongLinks;
import static nars.inference.BudgetFunctions.rankBelief;
import static nars.inference.LocalRules.revisible;
import static nars.inference.LocalRules.revision;
import static nars.inference.LocalRules.trySolution;
import nars.inference.SyllogisticRules;
import nars.inference.TemporalRules;
import static nars.inference.TemporalRules.solutionQuality;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import nars.language.CompoundTerm;
import nars.language.Equivalence;
import nars.language.Implication;
import nars.language.Term;
import nars.language.Variable;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.plugin.mental.InternalExperience;
import nars.storage.Bag;
import nars.storage.LevelBag;
import nars.language.Conjunction;
import nars.language.Interval;
import nars.util.Events.EnactableExplainationAdd;
import nars.util.Events.EnactableExplainationRemove;
import static nars.inference.UtilityFunctions.or;
import nars.language.Variables;
import nars.operator.mental.Anticipate;
import nars.util.Events;

public class Concept extends Item<Term> {

    
    /**
     * The term is the unique ID of the concept
     */
    public final Term term;

    /**
     * Task links for indirect processing
     */
    public final Bag<TaskLink,Task> taskLinks;

    /**
     * Term links between the term and its components and compounds; beliefs
     */
    public final Bag<TermLink,TermLink> termLinks;

    /**
     * Link templates of TermLink, only in concepts with CompoundTerm Templates
     * are used to improve the efficiency of TermLink building
     */
    public List<TermLink> termLinkTemplates;

    /**
     * Pending Question directly asked about the term
     *
     * Note: since this is iterated frequently, an array should be used. To
     * avoid iterator allocation, use .get(n) in a for-loop
     */
    public final List<Task> questions;

    
    /**
     * Pending Quests to be answered by new desire values
     */
    public final ArrayList<Task> quests;

    /**
     * Judgments directly made about the term Use ArrayList because of access
     * and insertion in the middle
     */
    public final ArrayList<Task> beliefs;
    public final ArrayList<Task> executable_preconditions;

    /**
     * Desire values on the term, similar to the above one
     */
    public final ArrayList<Task> desires;

    /**
     * Reference to the memory to which the Concept belongs
     */
    public final Memory memory;
    /**
     * The display window
     */

    //public final ArrayList<ArrayList<Long>> evidentalDiscountBases=new ArrayList<ArrayList<Long>>();

    /* ---------- constructor and initialization ---------- */
    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param tm A term corresponding to the concept
     * @param memory A reference to the memory
     */
    public Concept(final BudgetValue b, final Term tm, final Memory memory) {
        super(b);        
        
        this.term = tm;
        this.memory = memory;

        this.questions = new ArrayList<>();
        this.beliefs = new ArrayList<>();
        this.executable_preconditions = new ArrayList<>();
        this.quests = new ArrayList<>();
        this.desires = new ArrayList<>();

        this.taskLinks = new LevelBag<>(Parameters.TASK_LINK_BAG_LEVELS, Parameters.TASK_LINK_BAG_SIZE);
        this.termLinks = new LevelBag<>(Parameters.TERM_LINK_BAG_LEVELS, Parameters.TERM_LINK_BAG_SIZE);
                
        if (tm instanceof CompoundTerm) {
            this.termLinkTemplates = ((CompoundTerm) tm).prepareComponentLinks();
        } else {
            this.termLinkTemplates = null;
        }

    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Concept)) return false;
        return ((Concept)obj).name().equals(name());
    }

    @Override public int hashCode() { return name().hashCode();     }

    
    @Override
    public Term name() {
        return term;
    }

    /* ---------- direct processing of tasks ---------- */
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
    public boolean observable = false;
    public boolean directProcess(final DerivationContext nal, final Task task) {
        if(task.isInput()) {
            observable = true;
        }
        char type = task.sentence.punctuation;
        switch (type) {
            case Symbols.JUDGMENT_MARK:
                //memory.logic.JUDGMENT_PROCESS.commit();
                processJudgment(nal, task);
                break;
            case Symbols.GOAL_MARK:
                //memory.logic.GOAL_PROCESS.commit();
                processGoal(nal, task, true);
                break;
            case Symbols.QUESTION_MARK:
            case Symbols.QUEST_MARK:
                //memory.logic.QUESTION_PROCESS.commit();
                processQuestion(nal, task);
                break;
            default:
                return false;
        }

        if (task.aboveThreshold()) {    // still need to be processed
            //memory.logic.LINK_TO_TASK.commit();
            linkToTask(task,nal);
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
    protected void processJudgment(final DerivationContext nal, final Task task) {
        final Sentence judg = task.sentence;
        final Task oldBeliefT = selectCandidate(task, beliefs, true);   // only revise with the strongest -- how about projection?
        Sentence oldBelief = null;
        if (oldBeliefT != null) {
            oldBelief = oldBeliefT.sentence;
            final Stamp newStamp = judg.stamp;
            final Stamp oldStamp = oldBelief.stamp;       //when table is full, the latter check is especially important too
            if (newStamp.equals(oldStamp,false,true,true,false) && task.sentence.truth.equals(oldBelief.truth)) {
                //if (task.getParentTask() != null && task.getParentTask().sentence.isJudgment()) {
                    ////task.budget.decPriority(0);    // duplicated task
                //}   //// else: activated belief
                
                memory.removeTask(task, "Duplicated");                
                return;
            } else if (revisible(judg, oldBelief)) {
                
                nal.setTheNewStamp(newStamp, oldStamp, memory.time());
                Sentence projectedBelief = oldBelief.projection(memory.time(), newStamp.getOccurrenceTime());
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
            int nnq = questions.size();       
            for (int i = 0; i < nnq; i++) {                
                trySolution(judg, questions.get(i), nal, true);
            }
            
        addToTable(task, false, beliefs, Parameters.CONCEPT_BELIEFS_MAX, ConceptBeliefAdd.class, ConceptBeliefRemove.class);
            
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
                if(pred_conc != null && !(pred instanceof Operation) && (subj instanceof Conjunction)) {
                    Conjunction conj = (Conjunction) subj;
                    if(conj.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                            conj.term.length >= 4 && conj.term.length%2 == 0 &&
                            conj.term[conj.term.length-1] instanceof Interval && 
                            conj.term[conj.term.length-2] instanceof Operation) {
                        
                        //we do not add the target, instead the strongest belief in the target concept
                        if(beliefs.size() > 0) {
                            Task strongest_target = null; //beliefs.get(0);
                            //get the first eternal:
                            for(Task t : beliefs) {
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
                            pred_conc.addToTable(strongest_target, true, pred_conc.executable_preconditions, Parameters.CONCEPT_BELIEFS_MAX, EnactableExplainationAdd.class, EnactableExplainationRemove.class);
                        }
                    }
                }
            }
        }
        }
    }

    protected void addToTable(final Task task, final boolean rankTruthExpectation, final ArrayList<Task> table, final int max, final Class eventAdd, final Class eventRemove, final Object... extraEventArguments) {
        
        int preSize = table.size();
        Task removedT;
        Sentence removed = null;
        removedT = addToTable(task, table, max, rankTruthExpectation);
        if(removedT != null) {
            removed=removedT.sentence;
        }

        if (removed != null) {
            memory.event.emit(eventRemove, this, removed, task, extraEventArguments);
        }
        if ((preSize != table.size()) || (removed != null)) {
            memory.event.emit(eventAdd, this, task, extraEventArguments);
        }
    }

    /**
     * whether a concept's desire exceeds decision threshold
     */
    public boolean isDesired() {
        TruthValue desire=this.getDesire();
        if(desire==null) {
            return false;
        }
        return desire.getExpectation() > memory.param.decisionThreshold.get();
    }

    /**
     * Entry point for all potentially executable tasks.
     * Returns true if the Task has a Term which can be executed
     */
    public boolean executeDecision(final Task t) {

        //if (isDesired()) 
        if(memory.allowExecution)
        {
            
            Term content = t.getTerm();

            if(content instanceof Operation && !content.hasVarDep() && !content.hasVarIndep()) {

                Operation op=(Operation)content;
                Operator oper = op.getOperator();

                op.setTask(t);
                if(!oper.call(op, memory)) {
                    return false;
                }
                
                this.memory.lastDecision = t;
                //depriorize everything related to the previous decisions:
                successfulOperationHandler(this.memory);
                
                //this.memory.sequenceTasks = new LevelBag<>(Parameters.SEQUENCE_BAG_LEVELS, Parameters.SEQUENCE_BAG_SIZE);
                return true;
            }
        }
        return false;
    }
    
    public static void successfulOperationHandler(Memory memory) {
        //multiple versions are necessary, but we do not allow duplicates
        for(Task s : memory.sequenceTasks) {
            
            if(memory.lastDecision != null && (s.getTerm() instanceof Operation)) {
                if(!s.getTerm().equals(memory.lastDecision.getTerm())) {
                    s.setPriority(s.getPriority()*Parameters.CONSIDER_NEW_OPERATION_BIAS);
                    continue; //depriorized already, we can look at the next now
                }
            }
            if(memory.lastDecision != null && (s.getTerm() instanceof Conjunction)) {
                Conjunction seq = (Conjunction) s.getTerm();
                if(seq.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                    for(Term w : seq.term) {
                        if((w instanceof Operation) && !w.equals(memory.lastDecision.getTerm())) {
                            s.setPriority(s.getPriority()*Parameters.CONSIDER_NEW_OPERATION_BIAS);
                            break; //break because just penalty once, not for each term ^^
                        }
                    }
                }
            }
        }
    }
    
    /**
     * To accept a new goal, and check for revisions and realization, then
     * decide whether to actively pursue it
     *
     * @param judg The judgment to be accepted
     * @param task The task to be processed
     * @return Whether to continue the processing of the task
     */
    protected boolean processGoal(final DerivationContext nal, final Task task, boolean shortcut) {        
        
        final Sentence goal = task.sentence;
        final Task oldGoalT = selectCandidate(task, desires, true); // revise with the existing desire values
        Sentence oldGoal = null;
        
        if (oldGoalT != null) {
            oldGoal = oldGoalT.sentence;
            final Stamp newStamp = goal.stamp;
            final Stamp oldStamp = oldGoal.stamp;
            
            
            if (newStamp.equals(oldStamp,false,true,true,false)) {
                return false; // duplicate
            }
            if (revisible(goal, oldGoal)) {
                
                nal.setTheNewStamp(newStamp, oldStamp, memory.time());
                
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
        s2.setOccurrenceTime(memory.time());
        if(s2.after(task.sentence.stamp, nal.memory.param.duration.get())) { //this task is not up to date we have to project it first
            Sentence projectedGoal = task.sentence.projection(memory.time(), nal.memory.param.duration.get());
            if(projectedGoal!=null) {
                nal.singlePremiseTask(projectedGoal, task.budget.clone()); //it has to be projected
                return false;
            }
        }
        
        if (task.aboveThreshold()) {

            final Task beliefT = selectCandidate(task, beliefs, false); // check if the Goal is already satisfied

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
            
            if (projectedGoal != null && task.aboveThreshold() && !fullfilled && projectedGoal.truth.getExpectation() > nal.memory.param.decisionThreshold.get()) {

                try{
                Operation bestop = null;
                float bestop_truthexp = 0.0f;
                TruthValue bestop_truth = null;
                Task executable_precond = null;
                //long distance = -1;
                for(Task t: this.executable_preconditions) {
                    Term[] prec = ((Conjunction) ((Implication) t.getTerm()).getSubject()).term;
                    Term[] newprec = new Term[prec.length-3];
                    for(int i=0;i<prec.length-3;i++) { //skip the last part: interval, operator, interval
                        newprec[i] = prec[i];
                    }
                    
                    //distance = Interval.magnitudeToTime(((Interval)prec[prec.length-1]).magnitude, nal.memory.param.duration);
                    Operation op = (Operation) prec[prec.length-2];
                    Term precondition = Conjunction.make(newprec,TemporalRules.ORDER_FORWARD);

                    Concept preconc = nal.memory.concept(precondition);
                    long newesttime = -1;
                    Task bestsofar = null;
                    if(preconc != null) { //ok we can look now how much it is fullfilled
                        
                        //check recent events in event bag
                        for(Task p : this.memory.sequenceTasks) {
                            if(p.sentence.term.equals(preconc.term) && p.sentence.isJudgment() && !p.sentence.isEternal() && p.sentence.getOccurenceTime() > newesttime  && p.sentence.getOccurenceTime() <= memory.time()) {
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
                        Sentence projectedPrecon = bestsofar.sentence.projection(memory.time() /*- distance*/, memory.time());
                        
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

                if(bestop != null && bestop_truthexp > memory.param.decisionThreshold.get() /*&& Math.random() < bestop_truthexp */) {
                    Task t = new Task(new Sentence(bestop,Symbols.JUDGMENT_MARK,bestop_truth, projectedGoal.stamp), new BudgetValue(1.0f,1.0f,1.0f));
                    System.out.println("used " +t.getTerm().toString() + String.valueOf(memory.randomNumber.nextInt()));
                    if(!executeDecision(t)) { //this task is just used as dummy
                        memory.emit(UnexecutableGoal.class, task, this, nal);
                    } else {
                        SyllogisticRules.discountPredictiveHypothesis(nal, executable_precond.sentence, executable_precond.budget);
                    }
                }
                }catch(Exception ex){
                    System.out.println("Failure in operation choice rule, analyze!");
                }
                
                questionFromGoal(task, nal);
                
                addToTable(task, false, desires, Parameters.CONCEPT_GOALS_MAX, ConceptGoalAdd.class, ConceptGoalRemove.class);
                
                InternalExperience.InternalExperienceFromTask(memory,task,false);
                
                if(!executeDecision(task)) {
                    memory.emit(UnexecutableGoal.class, task, this, nal);
                    return true; //it was made true by itself
                }
                return false;
            }
            return fullfilled;
        }
        return false;
    }

    private void questionFromGoal(final Task task, final DerivationContext nal) {
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
                    Sentence s=new Sentence(q,Symbols.QUESTION_MARK,null,st);
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
    protected void processQuestion(final DerivationContext nal, final Task task) {

        Task quesTask = task;
        boolean newQuestion = true;
        for (final Task t : questions) {
            if (t.sentence.equalsContent(quesTask.sentence)) {
                quesTask = t;
                newQuestion = false;
                break;
            }
        }
        
        if (newQuestion) {
            if (questions.size() + 1 > Parameters.CONCEPT_QUESTIONS_MAX) {
                Task removed = questions.remove(0);    // FIFO
                memory.event.emit(ConceptQuestionRemove.class, this, removed);
            }

            questions.add(task);
            memory.event.emit(ConceptQuestionAdd.class, this, task);
        }

        Sentence ques = task.sentence;
        final Task newAnswerT = (ques.isQuestion())
                ? selectCandidate(task, beliefs, false)
                : selectCandidate(task, desires, false);

        if (newAnswerT != null) {
            trySolution(newAnswerT.sentence, task, nal, true);
        } 
        else
        if(task.isInput() && !task.getTerm().hasVarQuery() && quesTask.getBestSolution() != null) { //show previously found solution anyway in case of input
            memory.emit(Events.Answer.class, quesTask, quesTask.getBestSolution()); 
        }
    }

    /**
     * Link to a new task from all relevant concepts for continued processing in
     * the near future for unspecified time.
     * <p>
     * The only method that calls the TaskLink constructor.
     *
     * @param task The task to be linked
     * @param content The content of the task
     */
    public void linkToTask(final Task task, DerivationContext cont) {
        final BudgetValue taskBudget = task.budget;

        insertTaskLink(new TaskLink(task, null, taskBudget,
                Parameters.TERM_LINK_RECORD_LENGTH), cont);  // link type: SELF

        if (!(term instanceof CompoundTerm)) {
            return;
        }
        if (termLinkTemplates.isEmpty()) {
            return;
        }
                
        final BudgetValue subBudget = distributeAmongLinks(taskBudget, termLinkTemplates.size());
        if (subBudget.aboveThreshold()) {

            for (int t = 0; t < termLinkTemplates.size(); t++) {
                TermLink termLink = termLinkTemplates.get(t);

               if(termLink.type == TermLink.TEMPORAL)
                    continue;
//              if (!(task.isStructural() && (termLink.getType() == TermLink.TRANSFORM))) { // avoid circular transform
                Term componentTerm = termLink.target;

                Concept componentConcept = memory.conceptualize(subBudget, componentTerm);

                if (componentConcept != null) {
                    componentConcept.insertTaskLink(
                        new TaskLink(task, termLink, subBudget, Parameters.TERM_LINK_RECORD_LENGTH), cont
                    );
                }
//              }
            }

            buildTermLinks(taskBudget);  // recursively insert TermLink
        }
    }

    /**
     * Add a new belief (or goal) into the table Sort the beliefs/desires by
     * rank, and remove redundant or low rank one
     *
     * @param newSentence The judgment to be processed
     * @param table The table to be revised
     * @param capacity The capacity of the table
     * @return whether table was modified
     */
    public static Task addToTable(final Task newTask, final List<Task> table, final int capacity, boolean rankTruthExpectation) {
        Sentence newSentence = newTask.sentence;
        final float rank1 = rankBelief(newSentence, rankTruthExpectation);    // for the new isBelief
        float rank2;        
        int i;
        for (i = 0; i < table.size(); i++) {
            Sentence judgment2 = table.get(i).sentence;
            rank2 = rankBelief(judgment2, rankTruthExpectation);
            if (rank1 >= rank2) {
                if (newSentence.equivalentTo(judgment2)) {
                    //System.out.println(" ---------- Equivalent Belief: " + newSentence + " == " + judgment2);
                    return null;
                }
                table.add(i, newTask);
                break;
            }            
        }
        
        if (table.size() == capacity) {
            // nothing
        }
        else if (table.size() > capacity) {
            Task removed = table.remove(table.size() - 1);
            return removed;
        }
        else if (i == table.size()) { // branch implies implicit table.size() < capacity
            table.add(newTask);
        }
        
        return null;
    }

    /**
     * Select a belief value or desire value for a given query
     *
     * @param query The query to be processed
     * @param list The list of beliefs or desires to be used
     * @return The best candidate selected
     */
    public Task selectCandidate(final Task query, final List<Task> list, boolean forRevision) {
 //        if (list == null) {
        //            return null;
        //        }
        float currentBest = 0;
        float beliefQuality;
        Task candidate = null;
        boolean rateByConfidence = true; //table vote, yes/no question / local processing
        synchronized (list) {            
            for (int i = 0; i < list.size(); i++) {
                Task judgT = list.get(i);
                Sentence judg = judgT.sentence;
                beliefQuality = solutionQuality(rateByConfidence, query, judg, memory); //makes revision explicitly search for 
                if (beliefQuality > currentBest /*&& (!forRevision || judgT.sentence.equalsContent(query)) */ /*&& (!forRevision || !Stamp.baseOverlap(query.stamp.evidentialBase, judg.stamp.evidentialBase)) */) {
                    currentBest = beliefQuality;
                    candidate = judgT;
                }
            }
        }
        return candidate;
    }

    /* ---------- insert Links for indirect processing ---------- */
    /**
     * Insert a TaskLink into the TaskLink bag
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskLink The termLink to be inserted
     */
    protected boolean insertTaskLink(final TaskLink taskLink, DerivationContext nal) {        
        Task target = taskLink.getTarget();
        
        Task ques = taskLink.getTarget();
        if((ques.sentence.isQuestion() || ques.sentence.isQuest()) && ques.getTerm().hasVarQuery()) { //ok query var, search
            boolean newAnswer = false;
            
            for(TaskLink t : this.taskLinks) {
                
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
                memory.emit(Events.Answer.class, ques, ques.getBestSolution()); 
            }
        }
        
        //belief side:
        Task t = taskLink.getTarget();
        if(t.sentence.isJudgment()) { //ok query var, search
            for(TaskLink quess: this.taskLinks) {
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
                        memory.emit(Events.Answer.class, ques, ques.getBestSolution()); 
                    }
                }
            }
        }
        
        //HANDLE MAX PER CONTENT
        //if taskLinks already contain a certain amount of tasks with same content then one has to go
        boolean isEternal = target.sentence.isEternal();
        int nSameContent = 0;
        float lowest_priority = Float.MAX_VALUE;
        TaskLink lowest = null;
        for(TaskLink tl : taskLinks) {
            Sentence s = tl.getTarget().sentence;
            if(s.getTerm().equals(taskLink.getTerm()) && s.isEternal() == isEternal) {
                nSameContent++; //same content and occurrence-type, so count +1
                if(tl.getPriority() < lowest_priority) { //the current one has lower priority so save as lowest
                    lowest_priority = tl.getPriority();
                    lowest = tl;
                }
                if(nSameContent > Parameters.TASKLINK_PER_CONTENT) { //ok we reached the maximum so lets delete the lowest
                    taskLinks.take(lowest);
                    memory.emit(TaskLinkRemove.class, lowest, this);
                    break;
                }
            }
        }
        //END HANDLE MAX PER CONTENT
        
        
        TaskLink removed = taskLinks.putIn(taskLink);
        
        if (removed!=null) {
            if (removed == taskLink) {
                memory.emit(TaskLinkRemove.class, taskLink, this);
                return false;
            }
            else {
                memory.emit(TaskLinkRemove.class, removed, this);
            }
            
            removed.end();
        }
        memory.emit(TaskLinkAdd.class, taskLink, this);
        return true;
    }

    /**
     * Recursively build TermLinks between a compound and its components
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskBudget The BudgetValue of the task
     */
    public void buildTermLinks(final BudgetValue taskBudget) {
        if (termLinkTemplates.size() == 0) {
            return;
        }
        
        BudgetValue subBudget = distributeAmongLinks(taskBudget, termLinkTemplates.size());

        if (!subBudget.aboveThreshold()) {
            return;
        }

        for (final TermLink template : termLinkTemplates) {
            if (template.type != TermLink.TRANSFORM) {

                Term target = template.target;

                final Concept concept = memory.conceptualize(taskBudget, target);
                if (concept == null) {
                    continue;
                }

                // this termLink to that
                insertTermLink(new TermLink(target, template, subBudget));

                // that termLink to this
                concept.insertTermLink(new TermLink(term, template, subBudget));

                if (target instanceof CompoundTerm && template.type != TermLink.TEMPORAL) {
                    concept.buildTermLinks(subBudget);
                }
            }
        }
    }

    /**
     * Insert a TermLink into the TermLink bag
     * <p>
     * called from buildTermLinks only
     *
     * @param termLink The termLink to be inserted
     */
    public boolean insertTermLink(final TermLink termLink) {
        TermLink removed = termLinks.putIn(termLink);
        if (removed!=null) {
            if (removed == termLink) {
                memory.emit(TermLinkRemove.class, termLink, this);
                return false;
            }
            else {
                memory.emit(TermLinkRemove.class, removed, this);
            }
        }
        memory.emit(TermLinkAdd.class, termLink, this);
        return true;        
    }

    /**
     * Return a string representation of the concept, called in ConceptBag only
     *
     * @return The concept name, with taskBudget in the full version
     */
    @Override
    public String toString() {  // called from concept bag
        //return (super.toStringBrief() + " " + key);
        return super.toStringExternal();
    }

    /**
     * called from {@link NARConsole}
     */
    @Override
    public String toStringLong() {
        String res = 
                toStringExternal() + " " + term.name()
                + toStringIfNotNull(termLinks.size(), "termLinks")
                + toStringIfNotNull(taskLinks.size(), "taskLinks")
                + toStringIfNotNull(beliefs.size(), "beliefs")
                + toStringIfNotNull(desires.size(), "desires")
                + toStringIfNotNull(questions.size(), "questions")
                + toStringIfNotNull(quests.size(), "quests");
        
                //+ toStringIfNotNull(null, "questions");
        /*for (Task t : questions) {
            res += t.toString();
        }*/
        // TODO other details?
        return res;
    }

    private String toStringIfNotNull(final Object item, final String title) {
        if (item == null) {
            return "";
        }

        final String itemString = item.toString();

        return new StringBuilder(2 + title.length() + itemString.length() + 1).
                append(" ").append(title).append(':').append(itemString).toString();
    }

    /**
     * Recalculate the quality of the concept [to be refined to show
     * extension/intension balance]
     *
     * @return The quality value
     */
    @Override
    public float getQuality() {
        float linkPriority = termLinks.getAveragePriority();
        float termComplexityFactor = 1.0f / term.getComplexity()*Parameters.COMPLEXITY_UNIT;
        float result = or(linkPriority, termComplexityFactor);
        if (result < 0) {
            throw new RuntimeException("Concept.getQuality < 0:  result=" + result + ", linkPriority=" + linkPriority + " ,termComplexityFactor=" + termComplexityFactor + ", termLinks.size=" + termLinks.size());
        }
        return result;

    }

    /**
     * Return the templates for TermLinks, only called in
     * Memory.continuedProcess
     *
     * @return The template get
     */
    public List<TermLink> getTermLinkTemplates() {
        return termLinkTemplates;
    }

    /**
     * Select a isBelief to interact with the given task in inference
     * <p>
     * get the first qualified one
     * <p>
     * only called in RuleTables.reason
     *
     * @param task The selected task
     * @return The selected isBelief
     */
    public Sentence getBelief(final DerivationContext nal, final Task task) {
        final Stamp taskStamp = task.sentence.stamp;
        final long currentTime = memory.time();

        for (final Task beliefT : beliefs) {  
            Sentence belief = beliefT.sentence;
            nal.emit(BeliefSelect.class, belief);
            nal.setTheNewStamp(taskStamp, belief.stamp, currentTime);
            
            Sentence projectedBelief = belief.projection(taskStamp.getOccurrenceTime(), memory.time());
            /*if (projectedBelief.getOccurenceTime() != belief.getOccurenceTime()) {
               nal.singlePremiseTask(projectedBelief, task.budget);
            }*/
            
            return projectedBelief;     // return the first satisfying belief
        }
        return null;
    }
    
    public Sentence getBeliefForTemporalInference(final Task task) {
        
        if(task.sentence.isEternal()) { //this is for event-event inference only
            return null;
        }
        
        Sentence bestSoFar = null;
        long distance = Long.MAX_VALUE;

        for (final Task beliefT : beliefs) {  
            if(!beliefT.sentence.isEternal()) {
                long distance_new = Math.abs(task.sentence.getOccurenceTime() - beliefT.sentence.getOccurenceTime());
                if(distance_new < distance) {
                    distance = distance_new;
                    bestSoFar = beliefT.sentence;
                }
            }
        }
        return bestSoFar;
    }

    /**
     * Get the current overall desire value. TODO to be refined
     */
    public TruthValue getDesire() {
        if (desires.isEmpty()) {
            return null;
        }
        TruthValue topValue = desires.get(0).sentence.truth;
        return topValue;
    }



    @Override
    public void end() {
        for (Task t : questions) t.end();
        for (Task t : quests) t.end();
        
        questions.clear();
        quests.clear();                
        desires.clear();
        //evidentalDiscountBases.clear();
        termLinks.clear();
        taskLinks.clear();        
        beliefs.clear();
        termLinkTemplates.clear();
    }
    

    /**
     * Collect direct isBelief, questions, and desires for display
     *
     * @return String representation of direct content
     */
    public String displayContent() {
        final StringBuilder buffer = new StringBuilder(18);
        buffer.append("\n  Beliefs:\n");
        if (!beliefs.isEmpty()) {
            for (Task t : beliefs) {
                buffer.append(t.sentence).append('\n');
            }
        }
        if (!questions.isEmpty()) {
            buffer.append("\n  Question:\n");
            for (Task t : questions) {
                buffer.append(t).append('\n');
            }
        }
        return buffer.toString();
    }

    /**
     * Replace default to prevent repeated inference, by checking TaskLink
     *
     * @param taskLink The selected TaskLink
     * @param time The current time
     * @return The selected TermLink
     */
    public TermLink selectTermLink(final TaskLink taskLink, final long time) {
        
        
        int toMatch = Parameters.TERM_LINK_MAX_MATCHED; //Math.min(memory.param.termLinkMaxMatched.get(), termLinks.size());
        for (int i = 0; (i < toMatch) && (termLinks.size() > 0); i++) {
            
            final TermLink termLink = termLinks.takeNext();
            if (termLink==null)
                break;
            
            if (taskLink.novel(termLink, time)) {
                //return, will be re-inserted in caller method when finished processing it
                return termLink;
            }
            
            returnTermLink(termLink);
        }
        return null;

    }

    public void returnTermLink(TermLink termLink) {
        termLinks.putBack(termLink, memory.cycles(memory.param.termLinkForgetDurations), memory);
    }

    /**
     * Return the questions, called in ComposionalRules in
     * dedConjunctionByQuestion only
     */
    public List<Task> getQuestions() {
        return questions;
    }

    public void discountConfidence(final boolean onBeliefs) {
        if (onBeliefs) {
            for (final Task t : beliefs) {
                t.sentence.discountConfidence();
            }
        } else {
            for (final Task t : desires) {
                t.sentence.discountConfidence();
            }
        }
    }

    /** get a random belief, weighted by their sentences confidences */
    public Sentence getBeliefRandomByConfidence() {        
        if (beliefs.isEmpty()) return null;
        
        float totalConfidence = getBeliefConfidenceSum();
        float r = Memory.randomNumber.nextFloat() * totalConfidence;
                
        Sentence s = null;
        for (int i = 0; i < beliefs.size(); i++) {
            s = beliefs.get(i).sentence;            
            r -= s.truth.getConfidence();
            if (r < 0)
                return s;
        }
        
        return s;
    }
    
    public float getBeliefConfidenceSum() {
        float t = 0;
        for (final Task ts : beliefs)
            t += ts.sentence.truth.getConfidence();
        return t;
    }
    public float getBeliefFrequencyMean() {
        if (beliefs.isEmpty()) return 0.5f;
        
        float t = 0;
        for (final Task s : beliefs)
            t += s.sentence.truth.getFrequency();
        return t / beliefs.size();        
    }

    
    public CharSequence getBeliefsSummary() {
        if (beliefs.isEmpty())
            return "0 beliefs";        
        StringBuilder sb = new StringBuilder();
        for (Task ts : beliefs)
            sb.append(ts.toString()).append('\n');       
        return sb;
    }
    public CharSequence getDesiresSummary() {
        if (desires.isEmpty())
            return "0 desires";        
        StringBuilder sb = new StringBuilder();
        for (Task ts : desires)
            sb.append(ts.sentence.toString()).append('\n');       
        return sb;
    }

    public NativeOperator operator() {
        return term.operator();
    }

    public Term getTerm() {
        return term;
    }

    /** returns unmodifidable collection wrapping beliefs */
    public List<Task> getBeliefs() {
        return Collections.unmodifiableList(beliefs);
    }
    
    /** returns unmodifidable collection wrapping beliefs */
    public List<Task> getDesires() {
        return Collections.unmodifiableList(desires);
    }
}

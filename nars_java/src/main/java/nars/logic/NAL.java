/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic;

import nars.core.*;
import nars.logic.entity.*;
import nars.logic.nal1.Negation;
import nars.logic.nal8.Operation;
import nars.operator.mental.Anticipate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * NAL Reasoner Process.  Includes all reasoning process state.
 * <p>
 * https://code.google.com/p/open-nars/wiki/SingleStepTestingCases
 * according to derived Task: if it contains a mental operator it is NAL9, if it contains a operation it is NAL8, if it contains temporal information it is NAL7, if it contains in/dependent vars it is NAL6, if it contains higher order copulas like &&, ==> or negation it is NAL5
 * <p>
 * if it contains product or image it is NAL4, if it contains sets or set operations like &, -, | it is NAL3
 * <p>
 * if it contains similarity or instances or properties it is NAL2
 * and if it only contains inheritance
 */
public abstract class NAL implements Runnable {


    public interface DerivationFilter extends Plugin {
        /**
         * returns null if allowed to derive, or a String containing a short rejection reason for logging
         */
        public String reject(NAL nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief);

        @Override
        public default boolean setEnabled(NAR n, boolean enabled) {
            return true;
        }

    }

    public final Memory memory;
    protected Term currentTerm;
    protected Concept currentConcept;
    protected Task currentTask;
    protected TermLink currentBeliefLink;
    protected TaskLink currentTaskLink;
    protected Sentence currentBelief;
    protected Stamp newStamp;
    protected StampBuilder newStampBuilder;


    /**
     * stores the tasks that this process generates, and adds to memory
     */
    public final List<Task> tasksAdded = new ArrayList();

    //TODO tasksDicarded

    public NAL(Memory mem, int nalLevel) {
        super();
        memory = mem;
        currentTerm = null;
        currentConcept = null;
        currentTask = null;
        currentBeliefLink = null;
        currentTaskLink = null;
        currentBelief = null;
        newStamp = null;
        newStampBuilder = null;
    }

    public NAL(Memory mem) {
        this(mem, Parameters.DEFAULT_NAL);
    }

    public void emit(final Class c, final Object... o) {
        memory.emit(c, o);
    }

    public int nal() {
        return memory.nal();
    }

    /**
     * whether at least NAL level N is enabled
     */
    public boolean nal(int n) {
        return nal() >= n;
    }


    /**
     * iived task comes from the logic rules.
     *
     * @param task the derived task
     */
    public boolean derivedTask(final Task task, final boolean revised, final boolean single, Task parent, Sentence occurence2) {
        List<DerivationFilter> derivationFilters = memory.param.getDerivationFilters();

        if (derivationFilters != null) {
            for (int i = 0; i < derivationFilters.size(); i++) {
                DerivationFilter d = derivationFilters.get(i);
                String rejectionReason = d.reject(this, task, revised, single, parent, occurence2);
                if (rejectionReason != null) {
                    memory.removeTask(task, rejectionReason);
                    return false;
                }
            }
        }

        final Sentence occurence = parent != null ? parent.sentence : null;


        if (!task.budget.aboveThreshold()) {
            memory.removeTask(task, "Insufficient Budget");
            return false;
        }

        if (task.sentence != null && task.sentence.truth != null) {
            float conf = task.sentence.truth.getConfidence();
            if (conf == 0) {
                //no confidence - we can delete the wrongs out that way.
                memory.removeTask(task, "Ignored (zero confidence)");
                return false;
            }
        }


        if (task.sentence.term instanceof Operation) {
            Operation op = (Operation) task.sentence.term;
            if (op.getSubject() instanceof Variable || op.getPredicate() instanceof Variable) {
                memory.removeTask(task, "Operation with variable as subject or predicate");
                return false;
            }
        }


        final Stamp stamp = task.sentence.stamp;
        if (occurence != null && !occurence.isEternal()) {
            stamp.setOccurrenceTime(occurence.getOccurenceTime());
        }
        if (occurence2 != null && !occurence2.isEternal()) {
            stamp.setOccurrenceTime(occurence2.getOccurenceTime());
        }
        if (stamp.latency > 0) {
            memory.logic.DERIVATION_LATENCY.set((double) stamp.latency);
        }

        final Term currentTaskContent = getCurrentTask().getTerm();
        if (getCurrentBelief() != null && getCurrentBelief().isJudgment()) {
            final Term currentBeliefContent = getCurrentBelief().term;
            stamp.chainRemove(currentBeliefContent);
            stamp.chainAdd(currentBeliefContent);
        }
        //workaround for single premise task issue:
        if (currentBelief == null && single && currentTask != null && currentTask.sentence.isJudgment()) {
            stamp.chainRemove(currentTaskContent);
            stamp.chainAdd(currentTaskContent);
        }
        //end workaround
        if (currentTask != null && !single && currentTask.sentence.isJudgment()) {
            stamp.chainRemove(currentTaskContent);
            stamp.chainAdd(currentTaskContent);
        }
        //its a logic rule, so we have to do the derivation chain check to hamper cycles
        if (!revised) {
            Term tc = task.getTerm();

            if (task.sentence.isJudgment()) {

                Term ptc = task.getParentTask() != null ? task.getParentTask().getTerm() : null;

                if (
                        (task.getParentTask() == null) || (!Negation.areMutuallyInverse(tc, ptc))
                        ) {

                    final Collection<Term> chain = stamp.getChain();
                    if (chain.contains(tc)) {
                        memory.removeTask(task, "Cyclic Reasoning");
                        return false;
                    }
                }
            }

        } else {
            //its revision, of course its cyclic, apply evidental base policy
            final int stampLength = stamp.baseLength;
            for (int i = 0; i < stampLength; i++) {
                final long baseI = stamp.evidentialBase[i];
                for (int j = 0; j < stampLength; j++) {
                    if ((i != j) && (baseI == stamp.evidentialBase[j])) {
                        memory.removeTask(task, "Overlapping Revision Evidence");
                        //"(i=" + i + ",j=" + j +')' /* + " in " + stamp.toString()*/
                        return false;
                    }
                }
            }
        }

        if (task.sentence.getOccurenceTime() > memory.time()) {
            memory.event.emit(Events.TaskDeriveFuture.class, task, this);
        }

        task.setParticipateInTemporalInduction(false);
        memory.event.emit(Events.TaskDerive.class, task, revised, single, occurence, occurence2, getCurrentTask());
        memory.logic.TASK_DERIVED.hit();
        addTask(task, "Derived");
        return true;
    }

    /* --------------- new task building --------------- */

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth   The truth value of the sentence in task
     * @param newBudget  The budget value in task
     */
    public boolean doublePremiseTaskRevised(final CompoundTerm newContent, final TruthValue newTruth, final BudgetValue newBudget) {
        Sentence newSentence = new Sentence(newContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());
        Task newTask = new Task(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
        return derivedTask(newTask, true, false, null, null);
    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth   The truth value of the sentence in task
     * @param newBudget  The budget value in task
     */
    @Deprecated
    public Task doublePremiseTask(final Term newTaskContent, final TruthValue newTruth, final BudgetValue newBudget, boolean temporalAdd) {
        CompoundTerm newContent = Sentence.termOrNull(newTaskContent);
        if (newContent == null)
            return null;
        return doublePremiseTask(newContent, newTruth, newBudget, temporalAdd);
    }

    public Task doublePremiseTask(CompoundTerm newTaskContent, final TruthValue newTruth, final BudgetValue newBudget, boolean temporalAdd) {
        if (!newBudget.aboveThreshold()) {
            return null;
        }

        newTaskContent = Sentence.termOrNull(newTaskContent);
        if (newTaskContent == null)
            return null;

        Task derived = null;

        final Sentence newSentence = new Sentence(newTaskContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());

        final Task newTask = Task.make(newSentence, newBudget, getCurrentTask(), getCurrentBelief());

        if (newTask != null) {
            boolean added = derivedTask(newTask, false, false, null, null);
            if (added)
                derived = newTask;
        }

        temporalAdd = temporalAdd && nal(7);

        //"Since in principle it is always valid to eternalize a tensed belief"
        if (temporalAdd && Parameters.IMMEDIATE_ETERNALIZATION) { //temporal induction generated ones get eternalized directly

            TruthValue truthEt = TruthFunctions.eternalize(newTruth);
            Stamp st = getTheNewStamp().clone();
            st.setEternal();
            final Sentence newSentence2 = new Sentence(newTaskContent, getCurrentTask().sentence.punctuation, truthEt, st);
            final Task newTask2 = Task.make(newSentence2, newBudget, getCurrentTask(), getCurrentBelief());
            if (newTask2 != null) {
                derivedTask(newTask2, false, false, null, null);
            }
        }

        return derived;
    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     * @param revisible Whether the sentence is revisible
     */
    //    public void doublePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget, boolean revisible) {
    //        if (newContent != null) {
    //            Sentence taskSentence = currentTask.getSentence();
    //            Sentence newSentence = new Sentence(newContent, taskSentence.getPunctuation(), newTruth, newStamp, revisible);
    //            Task newTaskAt = new Task(newSentence, newBudget, currentTask, currentBelief);
    //            derivedTask(newTaskAt, false, false);
    //        }
    //    }

    /**
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth   The truth value of the sentence in task
     * @param newBudget  The budget value in task
     */
    public boolean singlePremiseTask(CompoundTerm newContent, TruthValue newTruth, BudgetValue newBudget) {
        return singlePremiseTask(newContent, getCurrentTask().sentence.punctuation, newTruth, newBudget);
    }

    /**
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent  The content of the sentence in task
     * @param punctuation The punctuation of the sentence in task
     * @param newTruth    The truth value of the sentence in task
     * @param newBudget   The budget value in task
     */
    public boolean singlePremiseTask(final CompoundTerm newContent, final char punctuation, final TruthValue newTruth, final BudgetValue newBudget) {

        if (!newBudget.aboveThreshold())
            return false;

        Task parentTask = getCurrentTask().getParentTask();
        if (parentTask != null) {
            if (parentTask.getTerm() == null) {
                return false;
            }
            if (newContent == null) {
                return false;
            }
            if (newContent.equals(parentTask.getTerm())) {
                return false;
            }
        }
        Sentence taskSentence = getCurrentTask().sentence;
        if (taskSentence.isJudgment() || getCurrentBelief() == null) {
            setTheNewStamp(new Stamp(taskSentence.stamp, getTime()));
        } else {
            // to answer a question with negation in NAL-5 --- move to activated task?
            setTheNewStamp(new Stamp(getCurrentBelief().stamp, getTime()));
        }

        if (newContent.subjectOrPredicateIsIndependentVar()) {
            return false;
        }

        Sentence newSentence = new Sentence(newContent, punctuation, newTruth, getTheNewStamp());
        Task newTask = Task.make(newSentence, newBudget, getCurrentTask());
        if (newTask != null) {
            return derivedTask(newTask, false, true, null, null);
        }
        return false;
    }

    public boolean singlePremiseTask(Sentence newSentence, BudgetValue newBudget) {
        if (!newBudget.aboveThreshold()) {
            return false;
        }
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        return derivedTask(newTask, false, true, null, null);
    }

    //    protected void reset(Memory currentMemory) {
    //        mem = currentMemory;
    //        setCurrentTerm(null);
    //        setCurrentBelief(null);
    //        setCurrentConcept(null);
    //        setCurrentTask(null);
    //        setCurrentBeliefLink(null);
    //        setCurrentTaskLink(null);
    //        setNewStamp(null);
    //    }
    public long getTime() {
        return memory.time();
    }

    public Stamp getNewStamp() {
        return newStamp;
    }

    public void setNewStamp(Stamp newStamp) {
        this.newStamp = newStamp;
    }

    /**
     * @return the currentTask
     */
    public Task getCurrentTask() {
        return currentTask;
    }

    /**
     * @param currentTask the currentTask to set
     */
    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public void setCurrentConcept(Concept currentConcept) {
        this.currentConcept = currentConcept;
    }

    /**
     * @return the newStamp
     */
    public Stamp getTheNewStamp() {
        if (newStamp == null) {
            //if newStamp==null then newStampBuilder must be available. cache it's return value as newStamp
            newStamp = newStampBuilder.build();
            newStampBuilder = null;
        }
        return newStamp;
    }

    /**
     * @param newStamp the newStamp to set
     */
    public Stamp setTheNewStamp(Stamp newStamp) {
        this.newStamp = newStamp;
        this.newStampBuilder = null;
        return newStamp;
    }

    interface StampBuilder {

        Stamp build();
    }

    /**
     * creates a lazy/deferred StampBuilder which only constructs the stamp if getTheNewStamp() is actually invoked
     */
    public void setTheNewStamp(final Stamp first, final Stamp second, final long time) {
        newStamp = null;
        newStampBuilder = new NewStampBuilder(first, second, time);
    }

    /**
     * @return the currentBelief
     */
    public Sentence getCurrentBelief() {
        return currentBelief;
    }

    /**
     * @param currentBelief the currentBelief to set
     */
    public Sentence setCurrentBelief(Sentence currentBelief) {
        this.currentBelief = currentBelief;
        return currentBelief;
    }

    /**
     * @return the currentBeliefLink
     */
    public TermLink getCurrentBeliefLink() {
        return currentBeliefLink;
    }

    /**
     * @param currentBeliefLink the currentBeliefLink to set
     */
    public void setCurrentBeliefLink(TermLink currentBeliefLink) {
        this.currentBeliefLink = currentBeliefLink;
    }

    /**
     * @return the currentTaskLink
     */
    public TaskLink getCurrentTaskLink() {
        return currentTaskLink;
    }

    /**
     * @param currentTaskLink the currentTaskLink to set
     */
    public void setCurrentTaskLink(TaskLink currentTaskLink) {
        this.currentTaskLink = currentTaskLink;
    }

    /**
     * @return the currentTerm
     */
    public Term getCurrentTerm() {
        return currentTerm;
    }

    /**
     * @param currentTerm the currentTerm to set
     */
    public void setCurrentTerm(Term currentTerm) {
        this.currentTerm = currentTerm;
    }

    /**
     * @return the currentConcept
     */
    public Concept getCurrentConcept() {
        return currentConcept;
    }

    public Memory mem() {
        return memory;
    }

    /**
     * tasks added with this method will be remembered by this NAL instance; useful for feedback
     */
    public void addTask(Task t, String reason) {

        memory.addNewTask(t, reason);

        tasksAdded.add(t);

    }

    /**
     * Activated task called in MatchingRules.trySolution and
     * Concept.processGoal
     *
     * @param budget          The budget value of the new Task
     * @param sentence        The content of the new Task
     * @param candidateBelief The belief to be used in future logic, for
     *                        forward/backward correspondence
     */
    public void addTask(final Task currentTask, final BudgetValue budget, final Sentence sentence, final Sentence candidateBelief) {
        addTask(new Task(sentence, budget, currentTask, sentence, candidateBelief),
                "Activated");
    }


    /**
     * for lazily constructing a stamp, in case it will not actually be necessary to completely construct a stamp
     */
    private static class NewStampBuilder implements StampBuilder {
        private final Stamp first;
        private final Stamp second;
        private final long time;

        public NewStampBuilder(Stamp first, Stamp second, long time) {
            this.first = first;
            this.second = second;
            this.time = time;
        }

        @Override
        public Stamp build() {
            return new Stamp(first, second, time);
        }
    }
}

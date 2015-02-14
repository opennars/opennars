/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.logic;

import nars.core.*;
import nars.logic.entity.*;
import reactor.event.Event;
import reactor.function.Supplier;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * NAL Reasoner Process.  Includes all reasoning process state and common utility methods that utilize it.
 * <p>
 * https://code.google.com/p/open-nars/wiki/SingleStepTestingCases
 * according to derived Task: if it contains a mental operator it is NAL9, if it contains a operation it is NAL8, if it contains temporal information it is NAL7, if it contains in/dependent vars it is NAL6, if it contains higher order copulas like &&, ==> or negation it is NAL5
 * <p>
 * if it contains product or image it is NAL4, if it contains sets or set operations like &, -, | it is NAL3
 * <p>
 * if it contains similarity or instances or properties it is NAL2
 * and if it only contains inheritance
 */
public abstract class NAL extends Event implements Runnable, Supplier<Task> {



    public interface DerivationFilter extends Plugin {


        /**
         * returns null if allowed to derive, or a String containing a short rejection reason for logging
         */
        public String reject(NAL nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief, Sentence derivedCurrentBelief, Task derivedCurrentTask);

        @Override
        public default boolean setEnabled(NAR n, boolean enabled) {
            return true;
        }
    }

    public final Memory memory;

    protected final Task currentTask;
    protected Sentence currentBelief;

    protected final NALRuleEngine reasoner;

    /**
     * stores the tasks that this process generates, and adds to memory
     */
    Deque<Task> newTasks = null; //lazily instantiated



    //TODO tasksDicarded

    public NAL(Memory mem, Task task) {
        this(mem, -1, task);
    }

    /** @param nalLevel the NAL level to limit processing of this reasoning context. set to -1 to use Memory's default value */
    public NAL(Memory mem, int nalLevel, Task task) {
        super(null);


        //setKey(getClass());
        setData(this);

        memory = mem;
        reasoner = memory.rules;

        if ((nalLevel!=-1) && (nalLevel!=mem.nal()))
            throw new RuntimeException("Different NAL level than Memory not supported yet");

        currentTask = task;
        currentBelief = null;

    }


    @Override
    public void run() {
        onStart();
        reason();
        onFinished();
    }

    protected void onStart() {
        /** implement if necessary in subclasses */
    }
    protected void onFinished() {
        /** implement if necessary in subclasses */
    }

    protected abstract void reason();

    protected int newTasksCount() {
        if (newTasks == null) return 0;
        return newTasks.size();
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




    public boolean deriveTask(final Task task, final boolean revised, final boolean single, Task parent, Sentence occurence2) {
        return deriveTask(task, revised, single, parent, occurence2, getCurrentBelief(), getCurrentTask());
    }

    /**
     * iived task comes from the logic rules.
     *
     * @param task the derived task
     */
    public boolean deriveTask(final Task task, final boolean revised, final boolean single, Task parent, Sentence occurence2,
                              Sentence subbedCurrentBelief, Task subbedCurrentTask) {

        List<DerivationFilter> derivationFilters = reasoner.getDerivationFilters();

        if (derivationFilters != null) {
            for (int i = 0; i < derivationFilters.size(); i++) {
                DerivationFilter d = derivationFilters.get(i);
                String rejectionReason = d.reject(this, task, revised, single, parent, occurence2, subbedCurrentBelief, subbedCurrentTask);
                if (rejectionReason != null) {
                    memory.removeTask(task, rejectionReason);
                    return false;
                }
            }
        }


        if (nal(7)) {
            final Sentence parentOccurrence = parent != null ? parent.sentence : null;
            long ocurrence = task.sentence.getOccurenceTime();
            if (parentOccurrence != null && !parentOccurrence.isEternal()) {
                ocurrence = parentOccurrence.getOccurenceTime();
            }
            if (occurence2 != null && !occurence2.isEternal()) {
                ocurrence = occurence2.getOccurenceTime();
            }
            task.sentence.setOccurrenceTime(ocurrence);

        }
        else {
            task.sentence.setOccurrenceTime(Stamp.ETERNAL);
        }



        if (task.sentence.stamp.latency > 0) {
            memory.logic.DERIVATION_LATENCY.set((double) task.sentence.stamp.latency);
        }

        task.setParticipateInTemporalInduction(false);

        memory.event.emit(Events.TaskDerive.class, task, revised, single, subbedCurrentTask);
        memory.logic.TASK_DERIVED.hit();

        addNewTask(task, "Derived");

        if (nal(7)) {
            if (task.sentence.getOccurenceTime() > memory.time()) {
                memory.event.emit(Events.TaskDeriveFuture.class, task, this);
            }
        }

        return true;
    }

    /* --------------- new task building --------------- */

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newTaskContent The content of the sentence in task
     * @param newTruth       The truth value of the sentence in task
     * @param newBudget      The budget value in task
     */
    public boolean doublePremiseTask(CompoundTerm newTaskContent, final TruthValue newTruth, final BudgetValue newBudget, StampBuilder newStamp, boolean temporalAdd) {
        return doublePremiseTask(newTaskContent, newTruth, newBudget, newStamp, temporalAdd, getCurrentBelief(), getCurrentTask());
    }

    public boolean doublePremiseTask(CompoundTerm newTaskContent, final TruthValue newTruth, final BudgetValue newBudget, StampBuilder stamp, final boolean temporalAdd, Sentence subbedBelief, Task subbedTask) {
        if (!newBudget.aboveThreshold()) {
            return false;
        }

        newTaskContent = Sentence.termOrNull(newTaskContent);
        if (newTaskContent == null)
            return false;

        final Stamp newStamp = stamp.build();

        boolean derived = deriveTask(new Task(
                new Sentence(newTaskContent, subbedTask.sentence.punctuation, newTruth, newStamp),
                newBudget, subbedTask, subbedBelief), false, false, subbedTask, subbedBelief);

        //"Since in principle it is always valid to eternalize a tensed belief"
        if (temporalAdd && nal(7) && Parameters.IMMEDIATE_ETERNALIZATION) {
        //temporal induction generated ones get eternalized directly
            derived |= deriveTask(
                    new Task(
                        new Sentence(newTaskContent,
                            subbedTask.sentence.punctuation,
                            TruthFunctions.eternalize(newTruth),
                            newStamp.cloneEternal()),
                        newBudget, subbedTask, subbedBelief),
                false, false, null, null);
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

        final Stamp stamp;
        if (taskSentence.isJudgment() || getCurrentBelief() == null) {
            stamp = new Stamp(taskSentence.stamp, time());
        } else {
            // to answer a question with negation in NAL-5 --- move to activated task?
            stamp = new Stamp(getCurrentBelief().stamp, time());
        }

        if (newContent.subjectOrPredicateIsIndependentVar()) {
            return false;
        }

        return deriveTask(new Task(new Sentence(newContent, punctuation, newTruth, stamp), newBudget, getCurrentTask(), null), false, true, null, null);
    }

    public boolean singlePremiseTask(Sentence newSentence, BudgetValue newBudget) {
        /*if (!newBudget.aboveThreshold()) {
            return false;
        }*/
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        return deriveTask(newTask, false, true, null, null);
    }


    public long time() {
        return memory.time();
    }



    /**
     * @return the currentTask
     */
    public Task getCurrentTask() {
        return currentTask;
    }




//
//    /**
//     * @return the newStamp
//     */
//    public Stamp getTheNewStamp() {
//        if (newStamp == null) {
//            //if newStamp==null then newStampBuilder must be available. cache it's return value as newStamp
//            newStamp = newStampBuilder.build();
//            newStampBuilder = null;
//        }
//        return newStamp;
//    }
//    public Stamp getTheNewStampForRevision() {
//        if (newStamp == null) {
//            if (newStampBuilder.overlapping()) {
//                newStamp = null;
//            }
//            else {
//                newStamp = newStampBuilder.build();
//            }
//            newStampBuilder = null;
//        }
//        return newStamp;
//    }
//
//    /**
//     * @param newStamp the newStamp to set
//     */
//    public Stamp setNextNewStamp(Stamp newStamp) {
//        this.newStamp = newStamp;
//        this.newStampBuilder = null;
//        return newStamp;
//    }
//
//    /**
//     * creates a lazy/deferred StampBuilder which only constructs the stamp if getTheNewStamp() is actually invoked
//     */
//    public void setNextNewStamp(final Stamp first, final Stamp second, final long time) {
//        newStamp = null;
//        newStampBuilder = new NewStampBuilder(first, second, time);
//    }

//    interface StampBuilder {
//
//        public Stamp build();
//
//        default public Stamp getFirst() { return null; }
//        default public Stamp getSecond(){ return null; }
//
//        default public boolean overlapping() {
//            /*final int stampLength = stamp.baseLength;
//            for (int i = 0; i < stampLength; i++) {
//                final long baseI = stamp.evidentialBase[i];
//                for (int j = 0; j < stampLength; j++) {
//                    if ((i != j) && (baseI == stamp.evidentialBase[j])) {
//                        throw new RuntimeException("Overlapping Revision Evidence: Should have been discovered earlier: " + Arrays.toString(stamp.evidentialBase));
//                    }
//                }
//            }*/
//
//            long[] a = getFirst().toSet();
//            long[] b = getSecond().toSet();
//            for (long ae : a) {
//                for (long be : b) {
//                    if (ae == be) return true;
//                }
//            }
//            return false;
//        }
//    }



    /**
     * @return the currentBelief
     */
    public Sentence getCurrentBelief() {
        return currentBelief;
    }




    /**
     * tasks added with this method will be buffered by this NAL instance;
     * at the end of the processing they can be reviewed and filtered
     * then they need to be added to memory with inputTask(t)
     */
    protected void addNewTask(Task t, String reason) {
        t.setReason(reason);

        if (newTasks==null)
            newTasks = new ArrayDeque(4);

        newTasks.add(t);
    }


    /** called from consumers of the tasks that this context generates */
    @Override public Task get() {
        if (newTasks == null || newTasks.isEmpty())
            return null;
        return newTasks.removeFirst();
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
    public void addSolution(final Task currentTask, final BudgetValue budget, final Sentence sentence, final Sentence candidateBelief) {
        addNewTask(new Task(sentence, budget, currentTask, sentence, candidateBelief),
                "Activated");
    }


    public interface StampBuilder {
        public Stamp build();
    }

    public static class LazyStampBuilder implements StampBuilder {

        public final Stamp a, b;
        public final long creationTime, occurrenceTime;
        protected Stamp stamp = null;

        public LazyStampBuilder(Stamp a, Stamp b, long creationTime, long occurrenceTime) {
            this.a = a;
            this.b = b;
            this.creationTime = creationTime;
            this.occurrenceTime = occurrenceTime;
        }

        @Override
        public Stamp build() {
            if (stamp == null)
                stamp = new Stamp(a, b, creationTime).setOccurrenceTime(occurrenceTime);
            return stamp;
        }
    }

    public StampBuilder newStamp(Sentence a, Sentence b, long at) {
        return new LazyStampBuilder(a.stamp, b.stamp, time(), at);
    }

    public StampBuilder newStamp(Sentence a, Sentence b) {
        /** eternal by default, it may be changed later */
        return newStamp(a, b, Stamp.ETERNAL);
    }

    /** returns a new stamp if A and B do not have overlapping evidence; null otherwise */
    public StampBuilder newStampIfNotOverlapping(Sentence A, Sentence B) {
        long[] a = A.stamp.toSet();
        long[] b = B.stamp.toSet();
        for (long ae : a) {
            for (long be : b) {
                if (ae == be) return null;
            }
        }
        return newStamp(A, B);
    }


}

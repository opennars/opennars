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
 * <p/>
 * https://code.google.com/p/open-nars/wiki/SingleStepTestingCases
 * according to derived Task: if it contains a mental operator it is NAL9, if it contains a operation it is NAL8, if it contains temporal information it is NAL7, if it contains in/dependent vars it is NAL6, if it contains higher order copulas like &&, ==> or negation it is NAL5
 * <p/>
 * if it contains product or image it is NAL4, if it contains sets or set operations like &, -, | it is NAL3
 * <p/>
 * if it contains similarity or instances or properties it is NAL2
 * and if it only contains inheritance
 */
public abstract class NAL extends Event implements Runnable, Supplier<Task> {


    public final Memory memory;
    protected final Task currentTask;
    protected final NALRuleEngine reasoner;
    protected Sentence currentBelief;
    /**
     * stores the tasks that this process generates, and adds to memory
     */
    Deque<Task> newTasks = null; //lazily instantiated

    public NAL(Memory mem, Task task) {
        this(mem, -1, task);
    }


    //TODO tasksDicarded

    /**
     * @param nalLevel the NAL level to limit processing of this reasoning context. set to -1 to use Memory's default value
     */
    public NAL(Memory mem, int nalLevel, Task task) {
        super(null);


        //setKey(getClass());
        setData(this);

        memory = mem;
        reasoner = memory.rules;

        if ((nalLevel != -1) && (nalLevel != mem.nal()))
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


    public boolean deriveTask(final Task task, final boolean revised, final boolean single) {
        return deriveTask(task, revised, single, null, null);
    }

    /**
     * iived task comes from the logic rules.
     *
     * @param task the derived task
     */
    public boolean deriveTask(final Task task, final boolean revised, final boolean single, Sentence currentBelief, Task currentTask) {


        if (!nal(7) && !task.sentence.isEternal()) {
            throw new RuntimeException("Temporal task derived with non-temporal reasoning");
        }

        //use this NAL's instance defaults for the values because specific values were not substituted:
        if (currentBelief == null)
            currentBelief = getCurrentBelief();
        if (currentTask == null)
            currentTask = getCurrentTask();

        List<DerivationFilter> derivationFilters = reasoner.getDerivationFilters();

        if (derivationFilters != null) {
            for (int i = 0; i < derivationFilters.size(); i++) {
                DerivationFilter d = derivationFilters.get(i);
                String rejectionReason = d.reject(this, task, revised, single, currentBelief, currentTask);
                if (rejectionReason != null) {
                    memory.removeTask(task, rejectionReason);
                    return false;
                }
            }
        }

        if (task.sentence.stamp.latency > 0) {
            memory.logic.DERIVATION_LATENCY.set((double) task.sentence.stamp.latency);
        }

        task.setParticipateInTemporalInduction(false);

        memory.event.emit(Events.TaskDerive.class, task, revised, single, currentTask);
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
                newBudget, subbedTask, subbedBelief), false, false, subbedBelief, subbedTask);

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
                    false, false, subbedBelief, subbedTask);
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

        Task parentTask = getCurrentTask().getParentTask();
        if (parentTask != null) {
            if (parentTask.getTerm() == null)
                return false;
            if (newContent.equals(parentTask.getTerm()))
                return false;
        }

        if (newContent.subjectOrPredicateIsIndependentVar()) {
            return false;
        }

        return singlePremiseTask(
                new Sentence(newContent, punctuation, newTruth,
                    newStamp(getCurrentTask().sentence,
                            getCurrentBelief())),
                newBudget);
    }

    public boolean singlePremiseTask(Sentence newSentence, BudgetValue newBudget) {
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        return deriveTask(newTask, false, true);
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

        if (newTasks == null)
            newTasks = new ArrayDeque(4);

        newTasks.add(t);
    }


    /**
     * called from consumers of the tasks that this context generates
     */
    @Override
    public Task get() {
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

    /**
     * create a new stamp builder for a specific occurenceTime
     */
    public StampBuilder newStamp(Sentence a, Sentence b, long occurrenceTime) {
        return new LazyStampBuilder(a.stamp, b.stamp, time(), occurrenceTime);
    }

    /**
     * create a new stamp builder with an occurenceTime determined by the parent sentence tenses.
     *
     * @param t generally the task's sentence
     * @param b generally the belief's sentence
     */
    public StampBuilder newStamp(Sentence t, Sentence b) {

        final long oc;
        if (nal(7)) {
            oc = inferOccurenceTime(t, b);
        } else {
            oc = Stamp.ETERNAL;
        }

        return new LazyStampBuilder(t!=null ? t.stamp : null,  b!=null ? b.stamp : null, time(), oc);
    }

    /**
     * returns a new stamp if A and B do not have overlapping evidence; null otherwise
     */
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

    public interface DerivationFilter extends Plugin {


        /**
         * returns null if allowed to derive, or a String containing a short rejection reason for logging
         */
        public String reject(NAL nal, Task task, boolean revised, boolean single, Sentence currentBelief, Task currentTask);

        @Override
        public default boolean setEnabled(NAR n, boolean enabled) {
            return true;
        }
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
            if (stamp == null) {
                if (a == null)
                    stamp = new Stamp(b, creationTime, occurrenceTime);
                else if (b == null)
                    stamp = new Stamp(a, creationTime, occurrenceTime);
                else
                    stamp = new Stamp(a, b, creationTime, occurrenceTime);
            }
            return stamp;
        }
    }


    public static long inferOccurenceTime(Sentence t, Sentence b) {
        final long oc;

        if ((t == null) && (b==null))
            throw new RuntimeException("Both sentence parameters null");
        if (t == null)
            return b.getOccurenceTime();
        else if (b == null)
            return t.getOccurenceTime();



        final long tOc = t.getOccurenceTime();
        final boolean tEternal = (tOc == Stamp.ETERNAL);
        final long bOc = b.getOccurenceTime();
        final boolean bEternal = (bOc == Stamp.ETERNAL);

        /* see: https://groups.google.com/forum/#!searchin/open-nars/eternal$20belief/open-nars/8KnAbKzjp4E/rBc-6V5pem8J) */

        if (tEternal && bEternal) {
            /* eternal belief, eternal task => eternal conclusion */
            oc = Stamp.ETERNAL;
        } else if (tEternal && !bEternal) {
            /*
            The task is eternal, while the belief is tensed.
            In this case, the conclusion will be eternal, by generalizing the belief
            on a moment to the general situation.
            According to the semantics of NARS, each truth-value provides a piece of
            evidence for the general statement, so this inference can be taken as a
            special case of abduction from the belief B<f,c> and G==>B<1,1> to G<f,c/(c+k)>
            where G is the eternal form of B."
            */
            oc = Stamp.ETERNAL;
        } else if (bEternal && !tEternal) {
            /*
            The belief is eternal, while the task is tensed.
            In this case, the conclusion will get the occurrenceTime of the task,
            because an eternal belief applies to every moment
            */
            oc = tOc;
        } else {
            /*
            Both premises are tensed.
            In this case, the truth-value of the belief B<f,c> will be "projected" from
            its previous OccurrenceTime t1 to the time of the task t2 to become B<f,d*c>,
            using the discount factor d = 1 - |t1-t2| / (|t0-t1| + |t0-t2|), where t0 is
            the current time.
            This formula is cited in https://code.google.com/p/open-nars/wiki/OpenNarsOneDotSix.
            Here the idea is that if a tensed belief is projected to a different time
            */
            oc = tOc;
        }


        /*
        //OLD occurence code:
        if (currentTaskSentence != null && !currentTaskSentence.isEternal()) {
            ocurrence = currentTaskSentence.getOccurenceTime();
        }
        if (currentBelief != null && !currentBelief.isEternal()) {
            ocurrence = currentBelief.getOccurenceTime();
        }
        task.sentence.setOccurrenceTime(ocurrence);
        */

        return oc;
    }

}

/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.nal;

import nars.Events;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.Stamper;
import nars.nal.task.TaskSeed;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * NAL Reasoner Process.  Includes all reasoning process state and common utility methods that utilize it.
 * <p/>
 * https://code.google.com/p/open-nars/wiki/SingleStepTestingCases
 * according to derived Task: if it contains a mental operate it is NAL9, if it contains a operation it is NAL8, if it contains temporal information it is NAL7, if it contains in/dependent vars it is NAL6, if it contains higher order copulas like &&, ==> or negation it is NAL5
 * <p/>
 * if it contains product or image it is NAL4, if it contains sets or set operations like &, -, | it is NAL3
 * <p/>
 * if it contains similarity or instances or properties it is NAL2
 * and if it only contains inheritance
 */
public abstract class NAL  implements Runnable {

    private Term currentTerm;

    public final Memory memory;
    protected final Task currentTask;
    protected final LogicPolicy reasoner;
    protected Sentence currentBelief;


    /**
     * stores the tasks that this process generates, and adds to memory
     */
    //protected SortedSet<Task> newTasks; //lazily instantiated



    public NAL(Memory mem, Task task) {
        this(mem, -1, task);
    }


    //TODO tasksDicarded

    /**
     * @param nalLevel the NAL level to limit processing of this reasoning context. set to -1 to use Memory's default value
     */
    public NAL(Memory mem, int nalLevel, Task task) {
        super();


        //setKey(getClass());

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
        process();
        onFinished();
    }

    protected void onStart() {
        /** implement if necessary in subclasses */
    }

    protected void onFinished() {
        /** implement if necessary in subclasses */
    }

    protected abstract void process();



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


    public Task deriveTask(final TaskSeed task, final boolean revised, final boolean single) {
        return deriveTask(task, revised, single, null, false);
    }

    /** TEMPORARY ADAPTER FOR OLD API */
    @Deprecated public Task deriveTask(final Task task, @Deprecated final boolean revised, final boolean single, Task currentTask, boolean allowOverlap) {
        return deriveTask(new TaskSeed(memory, task), revised, single, currentTask, allowOverlap);
    }

    /**
     * iived task comes from the logic rules.
     *
     * @param task the derived task
     * @param allowOverlap
     */
    public Task deriveTask(final TaskSeed task, @Deprecated final boolean revised, final boolean single, Task currentTask, boolean allowOverlap) {


        if (task.getParentTask() == null) {
            throw new RuntimeException("Derived task must have a parent: " + task + " via " + this);
        }


        //its revision, of course its cyclic, apply evidental base policy

        /*if(Parameters.DEBUG)*/ //TODO make this DEBUG only once it is certain
        {
            //if revised, the stamp should already have been prevented from even being created

            if (!Global.OVERLAP_ALLOW && (revised || !allowOverlap)) {
                if (task.isCyclic()) {
                    //RuntimeException re = new RuntimeException(task + " Overlapping Revision Evidence: Should have been discovered earlier: " + task.getStamp());
                    //re.printStackTrace();
                    memory.removed(task, "Cyclic");
                    return null;
                }
            }
        }


        if (nal(7)) {
            //adjust occurence time
            Task parent = task.getParentTask();
            final Sentence occurence = parent != null ? parent.sentence : null;
            if (occurence != null && !occurence.isEternal()) {
                //if (occurence.getOccurrenceTime()!=task.getStamp().getOccurrenceTime())
                task.occurr(occurence.getOccurrenceTime());
            }
        }


        Task taskCreated;
        if (null != (taskCreated = addNewTask(task, "Derived", false, revised, single, currentBelief, currentTask))) {

            memory.event.emit(Events.TaskDerive.class, taskCreated, revised, single, currentTask);
            memory.logic.TASK_DERIVED.hit();

            return taskCreated;
        }


        if (taskCreated!=null && nal(7)) {
            if (taskCreated.getOccurrenceTime() > memory.time()) {
                memory.event.emit(Events.TaskDeriveFuture.class, task, this);
            }
        }

        return null;
    }


    /**
     * The final destination of Tasks generated by this reasoning
     * process.  It receives all of the information about the state
     * of the new task, and can filter/reject it upon arrival.
     *
     * tasks added with this method will be buffered by this NAL instance;
     * at the end of the processing they can be reviewed and filtered
     * then they need to be added to memory with inputTask(t)
     *
     * if solution is false, it means it is a derivation
     */
    protected Task addNewTask(TaskSeed task, String reason, boolean solution, boolean revised, boolean single, @Deprecated Sentence currentBelief, @Deprecated Task currentTask) {

        if (!nal(7) && !task.isEternal()) {
            throw new RuntimeException("Temporal task derived with non-temporal reasoning");
        }

        //use this NAL's instance defaults for the values because specific values were not substituted:
        if (currentBelief == null)
            currentBelief = getCurrentBelief();
        if (currentTask == null)
            currentTask = getCurrentTask();


        String rejectionReason = reasoner.getDerivationRejection(this, task, solution, revised, single, currentBelief, currentTask);
        if (rejectionReason != null) {
            memory.removed(task, rejectionReason);
            return null;
        }


        Task taskCreated;
        if ((taskCreated = task.input()) != null) {

            taskCreated.setParticipateInTemporalInduction(false);

            if (Global.DEBUG && Global.DEBUG_DERIVATION_STACKTRACES) {
                taskCreated.addHistory(System.nanoTime() + " " + this.toString());
            }

            taskCreated.addHistory(reason);

            if (Global.DEBUG && Global.DEBUG_DERIVATION_STACKTRACES) {
                taskCreated.addHistory(getNALStack());
            }

            return taskCreated;
        }

        return null;


    }



    /**
     * @return the currentTerm
     */
    public Term getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(Term currentTerm) {
        this.currentTerm = currentTerm;
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
    public Task doublePremiseTask(Compound newTaskContent, final Truth newTruth, final Budget newBudget, Stamper newStamp, boolean temporalAdd, boolean allowOverlap) {
        return doublePremiseTask(newTaskContent, newTruth, newBudget, newStamp, temporalAdd, getCurrentTask(), allowOverlap);
    }
    public Task doublePremiseTask(Compound newTaskContent, final Truth newTruth, final Budget newBudget, Stamper stamp, final boolean temporalAdd, Task parentTask, boolean allowOverlap) {
        return doublePremiseTask(newTaskContent, parentTask.sentence.punctuation, newTruth, newBudget, stamp, temporalAdd, parentTask, allowOverlap);
    }

    public Task doublePremiseTask(Compound newTaskContent, char punctuation, final Truth newTruth, final Budget newBudget, Stamper stamp, final boolean temporalAdd, Task parentTask, boolean allowOverlap) {
        newTaskContent = Sentence.termOrNull(newTaskContent);
        if (newTaskContent == null)
            return null;

        TaskSeed task = newTask(newTaskContent)
                            .punctuation(punctuation)
                            .truth(newTruth)
                            .stamp(stamp)
                            .parent(parentTask, getCurrentBelief())
                            .budget(newBudget);

        return doublePremiseTask(task, temporalAdd, allowOverlap);
    }

    public Task doublePremiseTask(TaskSeed task, boolean temporalAdd, boolean allowOverlap) {

        final Task parentTask = task.getParentTask();

        Task derived;

        try {
            derived = deriveTask(task, false, false, parentTask, allowOverlap);
        } catch (RuntimeException e) {
            if (Global.DEBUG) throw e;
            return null;
        }

        //"Since in principle it is always valid to eternalize a tensed belief"
        if (derived!=null && temporalAdd && nal(7) && Global.IMMEDIATE_ETERNALIZATION) {
            //temporal induction generated ones get eternalized directly
            deriveTask(
                    newTask(task.getTerm())
                        .punctuation(task.getPunctuation())
                        .truth(TruthFunctions.eternalize(task.getTruth()))
                        .parent(parentTask, getCurrentBelief())
                        .budget(task)
                        .stamp(derived)
                        .eternal(),
                    false, false, parentTask, allowOverlap);
        }

        return derived;
    }

    public <T extends Compound> TaskSeed newTask(T term) {
        return memory.task(term);
    }

    @Deprecated public <T extends Compound> TaskSeed newTask(Sentence<T> s) {
        return new TaskSeed(memory, s);
    }

    public TaskSeed<Compound> newTask() {
        return new TaskSeed(memory);
    }

    /**
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth   The truth value of the sentence in task
     * @param newBudget  The budget value in task
     */
    public Task singlePremiseTask(Compound newContent, Truth newTruth, Budget newBudget) {
        return singlePremiseTask(newContent, getCurrentTask().sentence.punctuation, newTruth, newBudget);
    }

    public Task singlePremiseTask(final Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget) {
        return singlePremiseTask(newContent, punctuation, newTruth, newBudget, null);
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
    public Task singlePremiseTask(Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget, Stamper stamp) {
        return singlePremiseTask(newContent, punctuation, newTruth, newBudget, null, 1f, 1f);
    }

    public Task singlePremiseTask(Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget, Stamper stamp, float priMult, float durMult) {
        Task parentTask = getCurrentTask().getParentTask();
        if (parentTask != null) {
            if (parentTask.getTerm() == null) {
                return null;
            }
            if (newContent == null) {
                return null;
            }
            if (newContent.equals(parentTask.getTerm())) {
                return null;
            }
        }

        newContent = Sentence.termOrNull(newContent);
        if (newContent == null)
            return null;

        if (stamp == null) {
            final Sentence taskSentence = getCurrentTask().sentence;

            if (taskSentence.isJudgment() || getCurrentBelief() == null) {
                stamp = newStamp(taskSentence, null);
            } else {
                // to answer a question with negation in NAL-5 --- move to activated task?
                stamp = newStamp(null, getCurrentBelief());
            }
        }


        return singlePremiseTask(newTask(newContent)
                .punctuation(punctuation)
                .truth(newTruth)
                .budget(newBudget, priMult, durMult)
                .stamp(stamp)
                .parent(getCurrentTask()));

    }

    public Task singlePremiseTask(TaskSeed t) {
        return deriveTask(t, false, true);
    }

    @Deprecated public Task singlePremiseTask(Sentence newSentence, Task parentTask, Budget b) {
        //newTask.sentence.setRevisible(getCurrentTask().sentence.isRevisible());
        return deriveTask(newTask(newSentence).parent(parentTask).budget(b), false, true);
    }

    @Deprecated public Task singlePremiseTask(Sentence newSentence, Task parentTask) {
        return singlePremiseTask(newSentence, parentTask, parentTask);
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

    public void setCurrentBelief(Sentence belief) {
        this.currentBelief = belief;
    }





    /**
     * Activated task called in MatchingRules.trySolution and
     * Concept.processGoal
     *
     * @param budget          The budget value of the new Task
     * @param solutionBelief        The content of the new Task
     * @param candidateBelief The belief to be used in future logic, for
     *                        forward/backward correspondence
     */
    public Task addSolution(final Task currentTask, final Budget budget, final Sentence solutionBelief, final Task parentBeliefTask) {
        return addNewTask(
                newTask(solutionBelief)
                        .budget(budget)
                        .parent(currentTask, parentBeliefTask.getParentBelief())
                        .solution(solutionBelief),
                        "Activated",
                true, false, false, solutionBelief, currentTask);

        //.reason(currentTask.getHistory())
    }

    /**
     * create a new stamp builder for a specific occurenceTime
     */

    public Stamper newStamp(Stamp a, Stamp b, long occurrenceTime) {
        return new Stamper(a, b, time(), occurrenceTime);
    }
    public Stamper newStamp(Sentence a, long occurrenceTime) {
        return newStamp(a, null, occurrenceTime);
    }



    /**
     * create a new stamp builder with an occurenceTime determined by the parent sentence tenses.
     *
     * @param t generally the task's sentence
     * @param b generally the belief's sentence
     */
    public Stamper newStamp(Stamp t, Stamp b) {

        final long oc;
        if (nal(7)) {
            oc = inferOccurenceTime(t, b);
        } else {
            oc = Stamp.ETERNAL;
        }

        return new Stamper(t, b, time(), oc);
    }

    /**
     * returns a new stamp if A and B do not have overlapping evidence; null otherwise
     */
    public Stamper newStampIfNotOverlapping(Sentence A, Sentence B) {
        long[] a = A.getEvidentialSet();
        long[] b = B.getEvidentialSet();
        for (long ae : a) {
            for (long be : b) {
                if (ae == be) return null;
                if (be > ae) break; //if be exceeds ae, it will never be equal so go to the next ae
            }
        }
        return newStamp(A, B, A.getOccurrenceTime());
    }

    public float conceptPriority(Term target) {
        return memory.conceptPriority(target);
    }

//    public boolean deriveTask(Task t, boolean revised, boolean single, String reason) {
//        t.addHistory(reason);
//        return deriveTask(t, revised, single);
//    }

    public Term self() {
        return memory.self();
    }

    public Stamper newStamp(Stamp stamp, long when) {

        return new Stamper(stamp, null, time(), when);
    }

    public Stamper newStamp(Stamp stamp, long when, long[] evidentialBase) {
        return new Stamper(evidentialBase, time(), when, stamp.getDuration());
    }

    public Stamper newStamp(Task task, long time) {
        return newStamp(task.getStamp(), time);
    }

    /** new stamp from one parent stamp, with occurence time = now */
    public Stamper newStampNow(Task task) {
        return newStamp(task, time());
    }


    public static long inferOccurenceTime(Stamp t, Stamp b) {
        final long oc;


        if ((t == null) && (b==null))
            throw new RuntimeException("Both sentence parameters null");
        if (t == null)
            return b.getOccurrenceTime();
        else if (b == null)
            return t.getOccurrenceTime();



        final long tOc = t.getOccurrenceTime();
        final boolean tEternal = (tOc == Stamp.ETERNAL);
        final long bOc = b.getOccurrenceTime();
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

            ---

            If the task is not tensed but the belief is,
            then an eternalization rule is used to take the belief as
            providing evidence for the sentence in the task.
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
            /*
            If both premises are tensed, then the belief is "projected" to the occurrenceTime of the task. Ideally, temporal inference is valid only when
            the premises are about the same moment, i.e., have the same occurrenceTime or no occurrenceTime (i.e., eternal). However, since
            occurrenceTime is an approximation and the system is adaptive, a conclusion about one moment (that of the belief) can be projected to
            another (that of the task), at the cost of a confidence discount. Let t0 be the current time, and t1 and t2 are the occurrenceTime of the
            premises, then the discount factor is d = 1 - |t1-t2| / (|t0-t1| + |t0-t2|), which is in [0,1]. This factor d is multiplied to the confidence of a
            promise as a "temporal discount" to project it to the occurrence of the other promise, so as to derive a conclusion about that moment. In
            this way, if there are conflicting conclusions, the temporally closer one will be preferred by the choice rule.
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

    /** produces a cropped and filtered stack trace (list of methods called) */
    public static List<String> getNALStack() {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();

        String prefix = "";

        boolean tracing = false;
        String prevMethodID = null;

        List<String> path = new ArrayList();
        int i;
        for (i = 0; i < s.length; i++) {
            StackTraceElement e = s[i];

            String className = e.getClassName();
            String methodName = e.getMethodName();


            if (tracing) {

                //Filter conditions
                if (className.contains("reactor."))
                    continue;
                if (className.contains("EventEmitter"))
                    continue;
                if ((className.equals("NAL") || className.equals("Memory")) && methodName.equals("emit"))
                    continue;

                int cli = className.lastIndexOf(".") + 1;
                if (cli != -1)
                    className = className.substring(cli, className.length()); //class's simpleName

                String methodID = className + '_' + methodName;

                String sm = prefix + methodID + '_' + e.getLineNumber();


                path.add(sm);

                prevMethodID = methodID;


                //Termination conditions
                if (className.contains("ConceptFireTask") && methodName.equals("accept"))
                    break;
                if (className.contains("ImmediateProcess") && methodName.equals("rule"))
                    break;
                if (className.contains("ConceptFire") && methodName.equals("rule"))
                    break;
            } else if (className.endsWith(".NAL") && methodName.equals("deriveTask")) {
                tracing = true; //begins with next stack element
            }

        }


        return path;

    }

}

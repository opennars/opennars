/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Events;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.nal.LogicPolicy;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.CyclesInterval;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variables;
import nars.truth.Truth;
import nars.truth.TruthFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * NAL Reasoner Process.  Includes all reasoning process state and common utility methods that utilize it.
 * <p>
 * https://code.google.com/p/open-nars/wiki/SingleStepTestingCases
 * according to derived Task: if it contains a mental operate it is NAL9, if it contains a operation it is NAL8, if it contains temporal information it is NAL7, if it contains in/dependent vars it is NAL6, if it contains higher order copulas like &&, ==> or negation it is NAL5
 * <p>
 * if it contains product or image it is NAL4, if it contains sets or set operations like &, -, | it is NAL3
 * <p>
 * if it contains similarity or instances or properties it is NAL2
 * and if it only contains inheritance
 */
public abstract class NAL implements Runnable {

    public final Memory memory;
    protected final Task currentTask;
    protected final LogicPolicy reasoner;
    private Task currentBelief;


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


    public Task derive(final TaskSeed task) {
        return derive(task, false);
    }

    public Task derive(final TaskSeed task, final boolean revised) {
        return derive(task, revised, !task.isDouble());
    }

    @Deprecated
    public Task derive(final TaskSeed task, final boolean revised, final boolean single) {
        return derive(task, revised, single, null, false);
    }

    public Task deriveDouble(final TaskSeed task) {
        return derive(task, false, false);
    }

//    /**
//     * TEMPORARY ADAPTER FOR OLD API
//     */
//    @Deprecated
//    public Task derive(final Task task, @Deprecated final boolean revised, final boolean single, Task currentTask, boolean allowOverlap) {
//        return derive(new TaskSeed(memory, task), revised, single, currentTask, allowOverlap);
//    }

    /**
     * iived task comes from the logic rules.
     *
     * @param task         the derived task
     * @param allowOverlap
     */
    @Deprecated
    public Task derive(final TaskSeed task, @Deprecated final boolean revised, final boolean single, Task currentTask, boolean allowOverlap) {


        if (task.getTerm() == null) {
            throw new RuntimeException("task has null term");
        }

        if (task.getParentTask() == null && task.getParentBelief() == null) {
            throw new RuntimeException("Derived task must have a parent task or belief: " + task + " via " + this);
        }

        if (single != !task.isDouble()) {
            throw new RuntimeException((single ? "single" : "double") + " premise not consistent with Stamp on derived task: " + task);
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
            final Task parent = task.getParentTask();
            if (task.isTimeless()) {
                final long o;
                if (parent != null && !parent.isEternal())
                    o = parent.getOccurrenceTime(); //inherit parent's occurence time
                else
                    o = Stamp.ETERNAL; //default ETERNAL

                task.occurr(o);
            }
        } else {
            task.eternal();
        }


        //TODO balance budget on input; original task + immediate eternalization budget should be shared

        Task derived;
        if (null != (derived = addNewTask(task, "Derived", false, revised, single))) {

            memory.event.emit(Events.TaskDerive.class, derived);
            memory.logic.TASK_DERIVED.hit();

            if (nal(7) && !derived.isEternal()) {
                if (derived.getOccurrenceTime() > memory.time()) {
                    memory.event.emit(Events.TaskDeriveFuture.class, derived, this);
                }


                //TODO move this to ImmediateEternalization.java handler that reacts to TaskDeriveTemporal (to prune reacting to Eternal events)

                //TODO budget and/or confidence thresholds


                //"Since in principle it is always valid to eternalize a tensed belief"
                if (Global.IMMEDIATE_ETERNALIZATION /*&& task.temporalInductable()*/) {
                    //temporal induction generated ones get eternalized directly
                    /*Task eternalized = derived.cloneEternal();

                    eternalized.mulPriority(0.25f);
                    eternalized.log("ImmediateEternalize");
                    memory.taskAdd(eternalized);*/

                    derive(
                        newTask(derived.getTerm())
                                .punctuation(derived.getPunctuation())
                                .truth(TruthFunctions.eternalize(derived.getTruth()))
                                .parent(derived)
                                .budget(derived, 0.25f, 1f)
                                .eternal(),
                    false);
                }
            }

            return derived;
        }


        return null;
    }


    /**
     * The final destination of Tasks generated by this reasoning
     * process.  It receives all of the information about the state
     * of the new task, and can filter/reject it upon arrival.
     * <p>
     * tasks added with this method will be buffered by this NAL instance;
     * at the end of the processing they can be reviewed and filtered
     * then they need to be added to memory with inputTask(t)
     * <p>
     * if solution is false, it means it is a derivation
     */
    public Task addNewTask(TaskSeed task, String reason, boolean solution, boolean revised, boolean single) {

        if (!nal(7) && !task.isEternal()) {
            throw new RuntimeException("Temporal task derived with non-temporal reasoning");
        }

        //use this NAL's instance defaults for the values because specific values were not substituted:


        String rejectionReason = reasoner.getDerivationRejection(this, task, solution, revised, single, getBelief(), getTask());
        if (rejectionReason != null) {
            memory.removed(task, rejectionReason);
            return null;
        }

        if (task.isInput()) {
            throw new RuntimeException("derived task must have one parentTask: " + task);
        }

        Task taskCreated;
        if ((taskCreated = task.input()) != null) {

            //taskCreated.setTemporalInducting(false);

            if (Global.DEBUG && Global.DEBUG_DERIVATION_STACKTRACES) {
                taskCreated.log(System.nanoTime() + " " + this.toString());
            }

            taskCreated.log(reason);

            if (Global.DEBUG && Global.DEBUG_DERIVATION_STACKTRACES) {
                taskCreated.log(getNALStack());
            }

            return taskCreated;
        }

        return null;


    }


    /**
     * @return the currentTerm
     */
    abstract public Term getCurrentTerm();

    /* --------------- new task building --------------- */

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newTaskContent The content of the sentence in task
     * @param newTruth       The truth value of the sentence in task
     * @param newBudget      The budget value in task
     */
    @Deprecated
    public Task deriveDouble(Compound newTaskContent, final Truth newTruth, final Budget newBudget, boolean temporalAdd, boolean allowOverlap) {
        return deriveDouble(newTaskContent, newTruth, newBudget, temporalAdd, getTask(), allowOverlap);
    }

    @Deprecated
    public Task deriveDouble(Compound newTaskContent, final Truth newTruth, final Budget newBudget, final boolean temporalAdd, Task parentTask, boolean allowOverlap) {
        return deriveDouble(newTaskContent, parentTask.getPunctuation(), newTruth, newBudget, parentTask, getBelief(), temporalAdd, allowOverlap);
    }

    @Deprecated
    public Task deriveDouble(Compound newTaskContent, char punctuation, final Truth newTruth, final Budget newBudget, Task parentTask, Sentence parentBelief, final boolean temporalAdd, boolean allowOverlap) {


        //experimental: quick filter for below confidence threshold truths.
        // this is also applied in derivation filters but this avoids some overhead
        /*if (newTruth!=null && newTruth.getConfidence() < memory.param.confidenceThreshold.floatValue())
            return null;*/

        newTaskContent = Sentence.termOrNull(newTaskContent);
        if (newTaskContent == null)
            return null;

        if ((currentTask == null) || (currentBelief == null))
            throw new RuntimeException("should not derive doublePremiseTask with non-double Stamp");

        TaskSeed task = newTask(newTaskContent)
                .punctuation(punctuation)
                .truth(newTruth)
                .parent(parentTask, getBelief())
                .temporalInductable(!temporalAdd)
                .budget(newBudget);

        return deriveDouble(task, allowOverlap);
    }

    public Task deriveDouble(TaskSeed task, boolean allowOverlap) {

        final Task parentTask = task.getParentTask();

        Task derived = derive(task, false, false, parentTask, allowOverlap);


        return derived;
    }

    public <T extends Compound> TaskSeed newTask(final T term) {
        return memory.newTask(term);
    }

    public <T extends Compound> TaskSeed newTask(final T term, final char punc) {
        return newTask(term).punctuation(punc);
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
    public Task deriveSingle(Compound newContent, Truth newTruth, Budget newBudget) {
        return deriveSingle(newContent, getTask().getPunctuation(), newTruth, newBudget);
    }

    public Task deriveSingle(final Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget) {
        return deriveSingle(newContent, punctuation, newTruth, newBudget, 1f, 1f);
    }

    public Task deriveSingle(Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget, float priMult, float durMult) {
        final Task currentTask = getTask();
        final Task parentTask = currentTask.getParentTask();
        if (parentTask != null) {
            final Compound parentTaskTerm = parentTask.getTerm();
            if (parentTaskTerm == null) {
                return null;
            }
            if (parentTaskTerm.equals(newContent)) {
                return null;
            }
        }

        newContent = Sentence.termOrNull(newContent);
        if (newContent == null)
            return null;


        final Task ptask;
        final Task currentBelief = getBelief();
        if (currentTask.isJudgment() || currentBelief == null) {
            ptask = currentTask;
        } else {
            // to answer a question with negation in NAL-5 --- move to activated task?
            ptask = currentBelief;
        }


        return deriveSingle(newTask(newContent, punctuation)
                .truth(newTruth)
                .budget(newBudget, priMult, durMult)
                .parent(ptask,null));

    }

    public Task deriveSingle(TaskSeed t) {
        return derive(t, false, true);
    }


    public long time() {
        return memory.time();
    }


    /**
     * @return the currentTask
     */
    public Task getTask() {
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


    public Task getBelief() {
        return currentBelief;
    }

    public void setCurrentBelief(Task nextBelief) {
        this.currentBelief = nextBelief;
    }


    //    /**
//     * create a new stamp builder for a specific occurenceTime
//     */
//
//    public Stamper newStamp(Stamp a, Stamp b, long occurrenceTime) {
//        return new Stamper(a, b, time(), occurrenceTime);
//    }
//
//    public Stamper newStamp(Sentence a, long occurrenceTime) {
//        return newStamp(a, null, occurrenceTime);
//    }


//    /**
//     * create a new stamp builder with an occurenceTime determined by the parent sentence tenses.
//     *
//     * @param t generally the task's sentence
//     * @param b generally the belief's sentence
//     */
//    public Stamper newStamp(Stamp t, Stamp b) {
//
//        final long oc;
//        if (nal(7)) {
//            oc = inferOccurenceTime(t, b);
//        } else {
//            oc = Stamp.ETERNAL;
//        }
//
//        return new Stamper(t, b, time(), oc);
//    }
//
//    /**
//     * returns a new stamp if A and B do not have overlapping evidence; null otherwise
//     */
//    public Stamper newStampIfNotOverlapping(Sentence A, Sentence B) {
//        long[] a = A.getEvidentialSet();
//        long[] b = B.getEvidentialSet();
//        for (long ae : a) {
//            for (long be : b) {
//                if (ae == be) return null;
//                if (be > ae) break; //if be exceeds ae, it will never be equal so go to the next ae
//            }
//        }
//        return newStamp(A, B, A.getOccurrenceTime());
//    }
//public Stamper newStamp(Stamp stamp, long when) {
//
//    return new Stamper(stamp, null, time(), when);
//}
//
//    public Stamper newStamp(Stamp stamp, long when, long[] evidentialBase) {
//        return new Stamper(evidentialBase, time(), when, stamp.getDuration());
//    }
//
//    public Stamper newStamp(Task task, long time) {
//        return newStamp(task.getSentence(), time);
//    }
//
//    /**
//     * new stamp from one parent stamp, with occurence time = now
//     */
//    public Stamper newStampNow(Task task) {
//        return newStamp(task, time());
//    }


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


    @Deprecated
    public static long inferOccurenceTime(Stamp t, Stamp b) {
        final long oc;


        if ((t == null) && (b == null))
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
        } else if (tEternal /*&& !bEternal*/) {
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
        } else if (bEternal /*&& !tEternal*/) {
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

    /**
     * produces a cropped and filtered stack trace (list of methods called)
     */
    public static List<String> getNALStack() {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();

        String prefix = "";

        boolean tracing = false;
        //String prevMethodID;

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

                //prevMethodID = methodID;


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

    public AbstractInterval newInterval(final long cycles) {
        //return Interval.intervalSequence(Math.abs(timeDiff), Global.TEMPORAL_INTERVAL_PRECISION, nal.memory);
        return CyclesInterval.make(cycles, memory.duration());

    }

    /**
     * for producing a non-cyclic derivation; returns null if the premise is cyclic
     */
    public TaskSeed newDoublePremise(Task asym, Sentence sym) {
        return newDoublePremise(asym, sym, false);
    }

    public TaskSeed newDoublePremise(Task a, Sentence b, boolean allowOverlap) {
        TaskSeed x = newTask().parent(a, b);
        if (!allowOverlap)
            if (x.isCyclic())
                return null;
        return x;
    }

    public <C extends Compound> TaskSeed<C> newTask(C content, Task task, Sentence belief, boolean allowOverlap) {
        TaskSeed s = newDoublePremise(task, belief, allowOverlap);
        if (s == null) return null;
        return s.term(content);
    }

    public boolean unify(char varType, Term a, Term b, Term[] u) {
        return Variables.unify(varType, a, b, u, memory.random);
    }

    public boolean unify(char varType, Term a, Term b) {
        return unify(varType, a, b, new Term[] { a, b } );
    }

}

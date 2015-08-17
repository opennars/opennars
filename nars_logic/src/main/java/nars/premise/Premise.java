package nars.premise;

import nars.Events;
import nars.Global;
import nars.Memory;
import nars.Param;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.CyclesInterval;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;
import nars.term.Variables;
import nars.truth.Truth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Defines the conditions used in an instance of a derivation
 */
public interface Premise {

    Concept getConcept();

    TermLink getTermLink();

    TaskLink getTaskLink();

    Task getBelief();

    default public Term getTerm() {
        return getConcept().getTerm();
    }


    public Task getTask();


    public Memory getMemory();


    default public long time() {
        return getMemory().time();
    }


    default public void emit(final Class c, final Object... o) {
        getMemory().emit(c, o);
    }


    /** curent maximum allowed NAL level the reasoner is configured to support */
    default public int nal() {
        return getMemory().nal();
    }

    /**
     * whether at least NAL level N is enabled
     */
    default public boolean nal(int n) {
        return nal() >= n;
    }




    default public float conceptPriority(Term target) {
        return getMemory().conceptPriority(target);
    }

//    public boolean deriveTask(Task t, boolean revised, boolean single, String reason) {
//        t.addHistory(reason);
//        return deriveTask(t, revised, single);
//    }

    default public Term self() {
        return getMemory().self();
    }


    default public Random getRandom() {
        return getMemory().random;
    }



    /**
     * produces a cropped and filtered stack trace (list of methods called)
     */
    public static List<String> getStack() {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();

        String prefix = "";

        boolean tracing = false;
        //String prevMethodID;

        List<String> path = new ArrayList();
        for (int i = 0; i < s.length; i++) {
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

    default public int duration() { return getMemory().duration(); }

    default public AbstractInterval newInterval(final long cycles) {
        //return Interval.intervalSequence(Math.abs(timeDiff), Global.TEMPORAL_INTERVAL_PRECISION, nal.memory);
        return CyclesInterval.make(cycles, duration());

    }



    /* --------------- new task building --------------- */

    default public <T extends Compound> TaskSeed newTask() {
        return getMemory().newTask();
    }

    default public <T extends Compound> TaskSeed newTask(final T term) {
        return getMemory().newTask(term);
    }

    default public <T extends Compound> TaskSeed newTask(final T term, final char punc) {
        return newTask(term).punctuation(punc);
    }


    /** queues a derivation during a reasoning process.
     * this is in order to combine duplicates at the end before inputting to memory */
    default void queue(Task derivedTask) {
        throw new RuntimeException("unsupported");
    }



    /**
     * for producing a non-cyclic derivation; returns null if the premise is cyclic
     */
    default public <C extends Compound> TaskSeed<?> newDoublePremise(Task asym, Task sym) {
        return newDoublePremise(asym, sym, false);
    }

    default public <C extends Compound> TaskSeed<C> newSinglePremise(Task parentTask, boolean allowOverlap) {
        return newDoublePremise(parentTask, null, allowOverlap);
    }

    default public <C extends Compound> TaskSeed<C> newDoublePremise(Task parentTask, Task parentBelief, boolean allowOverlap) {
        TaskSeed x = newTask();
        x.parent(parentTask, parentBelief);
        x.updateCyclic();
        if (!allowOverlap && x.isCyclic())
            return null;
        return x;
    }

    default public <C extends Compound> TaskSeed<C> newTask(C content, Task task, Task belief, boolean allowOverlap) {
        TaskSeed s = newDoublePremise(task, belief, allowOverlap);
        if (s == null) return null;
        return s.termIfValid(content);
    }

    default public boolean unify(char varType, Term a, Term b, Term[] u) {
        return Variables.unify(varType, a, b, u, getRandom());
    }

    default public boolean unify(char varType, Term a, Term b) {
        return unify(varType, a, b, new Term[] { a, b } );
    }



    default public Task derive(final TaskSeed task) {
        return derive(task, false);
    }

    default public Task derive(final TaskSeed task, final boolean revised) {
        return derive(task, revised, !task.isDouble());
    }

    @Deprecated
    default public Task derive(final TaskSeed task, final boolean revised, final boolean single) {
        return derive(task, revised, single, false);
    }

    default public Task deriveDouble(final TaskSeed task) {
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
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth   The truth value of the sentence in task
     * @param newBudget  The budget value in task
     */
    @Deprecated default public Task deriveSingle(Compound newContent, Truth newTruth, Budget newBudget) {
        return deriveSingle(newContent, getTask().getPunctuation(), newTruth, newBudget);
    }

    default public Task deriveSingle(final Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget) {
        return deriveSingle(newContent, punctuation, newTruth, newBudget, 1f, 1f);
    }

    default public Task deriveSingle(Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget, float priMult, float durMult) {
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

    default public Task deriveSingle(TaskSeed t) {
        return derive(t, false, true);
    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newTaskContent The content of the sentence in task
     * @param newTruth       The truth value of the sentence in task
     * @param newBudget      The budget value in task
     */
    @Deprecated
    default public Task deriveDouble(Compound newTaskContent, final Truth newTruth, final Budget newBudget, boolean temporalAdd, boolean allowOverlap) {
        return deriveDouble(newTaskContent, getTask().getPunctuation(), newTruth, newBudget, getTask(), getBelief(), false, allowOverlap);
    }

    default public Task deriveDoubleTemporal(Compound newTaskContent, final Truth newTruth, final Budget newBudget, Task parentTask, Task previousBelief) {
        return deriveDouble(newTaskContent, parentTask.getPunctuation(), newTruth, newBudget, parentTask, previousBelief, true, false);
    }


    default public Task deriveDouble(Compound newTaskContent, char punctuation, final Truth newTruth, final Budget newBudget, Task parentTask, Task parentBelief, final boolean temporalAdd, boolean allowOverlap) {


        newTaskContent = Sentence.termOrNull(newTaskContent);
        if (newTaskContent == null)
            return null;

        if ((parentTask == null) || (parentBelief == null))
            throw new RuntimeException("should not derive doublePremiseTask with non-double Stamp");

        TaskSeed task = newTask(newTaskContent)
                .punctuation(punctuation)
                .truth(newTruth)
                .parent(parentTask, parentBelief)
                .temporalInductable(!temporalAdd)
                .budget(newBudget);

        return deriveDouble(task, allowOverlap);
    }

    default public Task deriveDouble(TaskSeed task, boolean allowOverlap) {
        return derive(task, false, false, allowOverlap);
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
    default public Task addNewTask(TaskSeed task, String reason, @Deprecated boolean solution, boolean revised, boolean single) {

        final Memory memory = getMemory();

        if (!nal(7) && !task.isEternal()) {
            throw new RuntimeException("Temporal task derived with non-temporal reasoning");
        }

        if (!Terms.levelValid(task.getTerm(), nal())) {
            memory.removed(task, "Insufficient NAL level");
            return null;
        }

        //use this NAL's instance defaults for the values because specific values were not substituted:

        String rejectionReason = memory.rules.getDerivationRejection(this, task, solution, revised, single, getBelief(), getTask());
        if (rejectionReason != null) {
            memory.removed(task, rejectionReason);
            return null;
        }

        if (task.isInput()) {
            throw new RuntimeException("derived task must have one parentTask: " + task);
        }

        Task taskCreated;
        if ((taskCreated = task.get()) != null) {

            //taskCreated.setTemporalInducting(false);

            if (Global.DEBUG && Global.DEBUG_DERIVATION_STACKTRACES) {
                taskCreated.log(System.nanoTime() + " " + this.toString());
            }

            taskCreated.log(reason);

            if (Global.DEBUG && Global.DEBUG_DERIVATION_STACKTRACES) {
                taskCreated.log(Premise.getStack());
            }

            queue(taskCreated);

            return taskCreated;
        }



        return null;


    }


    @Deprecated
    public static long inferOccurrenceTime(Stamp t, Stamp b) {


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

        final long oc;
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
     * iived task comes from the logic rules.
     *  @param task         the derived task
     * @param allowOverlap
     */
    @Deprecated
    default public Task derive(final TaskSeed task, @Deprecated final boolean revised, final boolean single, boolean allowOverlap) {


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
                    removed(task, "Cyclic");
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
            if (task.isTimeless()) {
                task.eternal();
            }
            else if (!task.isEternal()) {
                throw new RuntimeException("non-eternal Task derived in non-temporal mode");
            }
            //task.eternal();
        }


        final Memory memory = getMemory();

        //TODO balance budget on input; original task + immediate eternalization budget should be shared

        Task derived;
        if (null != (derived = addNewTask(task, "Derived", false, revised, single))) {

            emit(Events.TaskDerive.class, derived, this);
            memory.logic.TASK_DERIVED.hit();

            if (nal(7) && !derived.isEternal()) {
                if (derived.getOccurrenceTime() > time()) {
                    emit(Events.TaskDeriveFuture.class, derived, this);
                }


//                //TODO move this to ImmediateEternalization.java handler that reacts to TaskDeriveTemporal (to prune reacting to Eternal events)
//
//                //TODO budget and/or confidence thresholds
//
//
//                //"Since in principle it is always valid to eternalize a tensed belief"
//                if (Global.IMMEDIATE_ETERNALIZATION /*&& task.temporalInductable()*/) {
//                    //temporal induction generated ones get eternalized directly
//                    /*Task eternalized = derived.cloneEternal();
//
//                    eternalized.mulPriority(0.25f);
//                    eternalized.log("ImmediateEternalize");
//                    memory.taskAdd(eternalized);*/
//
//                    derive(
//                            newTask(derived.getTerm())
//                                    .punctuation(derived.getPunctuation())
//                                    .truth(TruthFunctions.eternalize(derived.getTruth()))
//                                    .parent(derived)
//
//                                    .budget(derived)
//                                            //.budget(derived, 0.25f, 1f) //TODO scale eternalized
//
//                                    .eternal(),
//                            false);
//                }
            }

            return derived;
        }


        return null;
    }

    default public void removed(TaskSeed task, String reason) {
        getMemory().removed(task, reason);
    }
    default public void removed(Task task, String reason) {
        getMemory().removed(task, reason);
    }


    default Concept concept(Term x) {
        return getMemory().concept(x);
    }

    default Param param() {
        return getMemory().param;
    }

    default boolean isEvent() {
        return !(getTask().isEternal() || (getBelief()!=null && getBelief().isEternal()));
    }

}

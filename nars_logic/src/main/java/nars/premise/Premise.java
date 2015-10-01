package nars.premise;

import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.CyclesInterval;
import nars.process.Level;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.filter.FilterDuplicateExistingBelief;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.DefaultTruth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Defines the conditions used in an instance of a derivation
 */
public interface Premise extends Level {

    Concept getConcept();

    TermLink getTermLink();

    //TaskLink getTaskLink();

    Task getBelief();

    default public Term getTerm() {
        return getConcept().getTerm();
    }


    public Task getTask();


    public NAR nar();


    default public long time() {
        return nar().time();
    }


//    default public void emit(final Class c, final Object... o) {
//        nar().emit(c, o);
//    }


    /**
     * curent maximum allowed NAL level the reasoner is configured to support
     */
    default public int nal() {
        return nar().nal();
    }

    /**
     * whether at least NAL level N is enabled
     */
    default public boolean nal(int n) {
        return nal() >= n;
    }


    default public float conceptPriority(Term target, float valueForMissing) {
        return memory().conceptPriority(target, valueForMissing);
    }

    default Memory memory() {
        return nar().memory();
    }


    default public Term self() {
        return memory().self();
    }


    default public Random getRandom() {
        return memory().random;
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

    default public int duration() {
        return memory().duration();
    }

    default public AbstractInterval newInterval(final long cycles) {
        //return Interval.intervalSequence(Math.abs(timeDiff), Global.TEMPORAL_INTERVAL_PRECISION, nal.memory);
        return CyclesInterval.make(cycles, duration());

    }



    /* --------------- new task building --------------- */


    default public <T extends Compound> TaskSeed newTask(final T term) {
        return TaskSeed.make(nar().memory(), term);
    }

    default public <T extends Compound> TaskSeed newTask(final T term, final char punc) {
        return newTask(term).punctuation(punc);
    }








//    default public <C extends Compound> TaskSeed newTask(C content, Task task, Task belief, boolean allowOverlap) {
//        content = Sentence.termOrNull(content);
//        if (content == null)
//            return null;
//        TaskSeed s = newDoublePremise(task, belief, allowOverlap);
//        if (s == null) return null;
//        return s.term(content);
//    }

//    default public boolean unify(Op varType, Term a, Term b, Term[] u) {
//        return Variables.unify(varType, a, b, u, getRandom());
//    }

//    default public boolean unify(Op varType, Term a, Term b) {
//        return unify(varType, a, b, new Term[] { a, b } );
//    }





//    /**
//     * TEMPORARY ADAPTER FOR OLD API
//     */
//    @Deprecated
//    public Task derive(final Task task, @Deprecated final boolean revised, final boolean single, Task currentTask, boolean allowOverlap) {
//        return derive(new TaskSeed(memory, task), revised, single, currentTask, allowOverlap);
//    }

//
//    default public Task deriveSingle(final Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget) {
//        return deriveSingle(newContent, punctuation, newTruth, newBudget, 1f, 1f);
//    }
//
//    default public Task deriveSingle(Compound newContent, final char punctuation, final Truth newTruth, final Budget newBudget, float priMult, float durMult) {
//        final Task parentTask = getTask();
//        //final Task grandParentTask = parentTask.getParentTask();
//        /*if (parentTask != null) {
//            final Compound parentTaskTerm = parentTask.getTerm();
//            if (parentTaskTerm == null) {
//                return null;
//            }
//            if (parentTaskTerm.equals(newContent)) {
//                return null;
//            }
//        }*/
//
//        newContent = Sentence.termOrNull(newContent);
//        if (newContent == null)
//            return null;
//
//
////        final Task ptask;
////        final Task currentBelief = getBelief();
////        if (parentTask.isJudgment() || currentBelief == null) {
////            ptask = parentTask;
////        } else { //Unspecified cheat we need to get rid of.
////            // to answer a question with negation in NAL-5 --- move to activated task?
////            ptask = currentBelief;
////        }
//
//
//        return deriveSingle(newTask(newContent, punctuation)
//                .truth(newTruth)
//                .budget(newBudget, priMult, durMult)
//                .parent(parentTask, null));
//
//    }
//
//    default public Task deriveSingle(Task t) {
//        return derive(t, false, true);
//    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newTaskContent The content of the sentence in task
     * @param newTruth       The truth value of the sentence in task
     * @param newBudget      The budget value in task
     */


//    default public Task deriveDouble(Compound newTaskContent, char punctuation, final Truth newTruth, final Budget newBudget, Task parentTask, Task parentBelief, final boolean temporalAdd, boolean allowOverlap) {
//
//
//        newTaskContent = Sentence.termOrNull(newTaskContent);
//        if (newTaskContent == null)
//            return null;
//
//        if ((parentTask == null) || (parentBelief == null))
//            throw new RuntimeException("should not derive doublePremiseTask with non-double Stamp");
//
//        TaskSeed task = newTask(newTaskContent)
//                .punctuation(punctuation)
//                .truth(newTruth)
//                .parent(parentTask, parentBelief)
//                .temporalInductable(!temporalAdd)
//                .budget(newBudget);
//
//        return deriveDouble(task, allowOverlap);
//    }

//    default public Task deriveDouble(TaskSeed task, boolean allowOverlap) {
//        return derive(task, false, false);
//    }


    /**
     * The final destination of Tasks generated by this reasoning
     * process.  It receives all of the information about the state
     * of the new task, and can filter/reject it upon arrival.
     * <p>
     * tasks added with this method will be buffered by this NAL instance;
     * at the end of the processing they can be reviewed and filtered
     * then they need to be added to memory with inputTask(t)
     * <p>
     */
    default Task validate(Task task) {

        final Memory memory = nar().memory();

        if (task.getTerm() == null) {
            throw new RuntimeException("task has null term");
        }

        if (task.isInput()) {
            throw new RuntimeException("Derived task must have a parent task or belief: " + task + " via " + this);
        }


        if (task.isJudgmentOrGoal() && task.getConfidence() < DefaultTruth.DEFAULT_TRUTH_EPSILON) {
            memory.remove(task, "Insufficient confidence");
            return null;
        }





//        if (nal(7)) {
//            //adjust occurence time
//            final Task parent = task.getParentTask();
//            if (task.isTimeless()) {
//                final long o;
//                if (parent != null && !parent.isEternal())
//                    o = parent.getOccurrenceTime(); //inherit parent's occurence time
//                else
//                    o = Stamp.ETERNAL; //default ETERNAL
//
//                task.setOccurrenceTime(o);
//            }
//        } else {
//            if (task.isTimeless()) {
//                task.setEternal();
//            } else if (!task.isEternal()) {
//                throw new RuntimeException("non-eternal Task derived in non-temporal mode");
//            }
//        }

        //taskCreated.setTemporalInducting(false);


        Task t = task.normalized();

        if (!FilterDuplicateExistingBelief.isUniqueBelief(this, t)) {
            memory().remove(t);
            return null;
        }

        if (t!=null) {
            memory.eventDerived.emit(task);
            memory.logic.TASK_DERIVED.hit();
        }
        return t;
    }


//    @Deprecated
//    public static long inferOccurrenceTime(Stamp t, Stamp b) {
//
//
//        if ((t == null) && (b == null))
//            throw new RuntimeException("Both sentence parameters null");
//        if (t == null)
//            return b.getOccurrenceTime();
//        else if (b == null)
//            return t.getOccurrenceTime();
//
//
//        final long tOc = t.getOccurrenceTime();
//        final boolean tEternal = (tOc == Stamp.ETERNAL);
//        final long bOc = b.getOccurrenceTime();
//        final boolean bEternal = (bOc == Stamp.ETERNAL);
//
//        /* see: https://groups.google.com/forum/#!searchin/open-nars/eternal$20belief/open-nars/8KnAbKzjp4E/rBc-6V5pem8J) */
//
//        final long oc;
//        if (tEternal && bEternal) {
//            /* eternal belief, eternal task => eternal conclusion */
//            oc = Stamp.ETERNAL;
//        } else if (tEternal /*&& !bEternal*/) {
//            /*
//            The task is eternal, while the belief is tensed.
//            In this case, the conclusion will be eternal, by generalizing the belief
//            on a moment to the general situation.
//            According to the semantics of NARS, each truth-value provides a piece of
//            evidence for the general statement, so this inference can be taken as a
//            special case of abduction from the belief B<f,c> and G==>B<1,1> to G<f,c/(c+k)>
//            where G is the eternal form of B."
//            */
//            oc = Stamp.ETERNAL;
//        } else if (bEternal /*&& !tEternal*/) {
//            /*
//            The belief is eternal, while the task is tensed.
//            In this case, the conclusion will get the occurrenceTime of the task,
//            because an eternal belief applies to every moment
//
//            ---
//
//            If the task is not tensed but the belief is,
//            then an eternalization rule is used to take the belief as
//            providing evidence for the sentence in the task.
//            */
//            oc = tOc;
//        } else {
//            /*
//            Both premises are tensed.
//            In this case, the truth-value of the belief B<f,c> will be "projected" from
//            its previous OccurrenceTime t1 to the time of the task t2 to become B<f,d*c>,
//            using the discount factor d = 1 - |t1-t2| / (|t0-t1| + |t0-t2|), where t0 is
//            the current time.
//            This formula is cited in https://code.google.com/p/open-nars/wiki/OpenNarsOneDotSix.
//            Here the idea is that if a tensed belief is projected to a different time
//            */
//            /*
//            If both premises are tensed, then the belief is "projected" to the occurrenceTime of the task. Ideally, temporal inference is valid only when
//            the premises are about the same moment, i.e., have the same occurrenceTime or no occurrenceTime (i.e., eternal). However, since
//            occurrenceTime is an approximation and the system is adaptive, a conclusion about one moment (that of the belief) can be projected to
//            another (that of the task), at the cost of a confidence discount. Let t0 be the current time, and t1 and t2 are the occurrenceTime of the
//            premises, then the discount factor is d = 1 - |t1-t2| / (|t0-t1| + |t0-t2|), which is in [0,1]. This factor d is multiplied to the confidence of a
//            promise as a "temporal discount" to project it to the occurrence of the other promise, so as to derive a conclusion about that moment. In
//            this way, if there are conflicting conclusions, the temporally closer one will be preferred by the choice rule.
//             */
//            oc = tOc;
//        }
//
//
//        /*
//        //OLD occurence code:
//        if (currentTaskSentence != null && !currentTaskSentence.isEternal()) {
//            ocurrence = currentTaskSentence.getOccurenceTime();
//        }
//        if (currentBelief != null && !currentBelief.isEternal()) {
//            ocurrence = currentBelief.getOccurenceTime();
//        }
//        task.sentence.setOccurrenceTime(ocurrence);
//        */
//
//        return oc;
//    }





    default Concept concept(Term x) {
        return nar().concept(x);
    }

    default boolean isTaskAndBeliefEvent() {
        if (getBelief() == null) return false;

        return (!getTask().isEternal() && (!getBelief().isEternal()));
    }

    default boolean isTaskEvent() {
        return !getTask().isEternal();
    }

    //TODO cache this value
    default boolean isCyclic() {
        Task t = getTask();
        Task b = getBelief();
        if (b != null) {
            return Stamp.overlapping(t, b);
        }
        return false;
    }

    default Task input(Task t) {
        if (((t = validate(t))!=null)) {
            nar().input(t);
            return t;
        }
        return null;
    }

    default void input(Stream<Task> t) {
        t.forEach(this::input);
    }
}

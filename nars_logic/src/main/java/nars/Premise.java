package nars;

import nars.budget.Budget;
import nars.concept.Concept;
import nars.link.TermLink;
import nars.nal.Level;
import nars.nal.nal1.LocalRules;
import nars.nal.nal7.Tense;
import nars.task.DefaultTask;
import nars.task.FluentTask;
import nars.task.Task;
import nars.task.Tasked;
import nars.task.filter.FilterDuplicateExistingBelief;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.truth.DefaultTruth;
import nars.truth.Stamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Defines the conditions used in an instance of a derivation
 */
public interface Premise extends Level, Tasked {

    /**
     * The task and belief have the same content
     * <p>
     * called in RuleTables.reason
     *
     * @param question The task
     * @param solution The belief
     * @return null if no match
     */
    static Task match(final Task question, final Task solution, final Premise nal) {

        if (question.isQuestion() || question.isGoal()) {
            if (Tense.matchingOrder(question, solution)) {
                Term[] u = new Term[]{question.getTerm(), solution.getTerm()};
                if (unify(Op.VAR_QUERY, u, nal.getRandom())) {
                    return LocalRules.trySolution(question, solution, nal);
                }
            }
        }

        return solution;
    }

    /**
     * To unify two terms
     *
     * @param varType The varType of variable that can be substituted
     * @param t       The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     * <p>
     * only sets the values if it will return true, otherwise if it returns false the callee can expect its original values untouched
     */
    static boolean unify(final Op varType, final Term[] t, final Random random) {


        final FindSubst f = new FindSubst(varType, random);
        final boolean hasSubs = f.next(t[0], t[1], Global.UNIFICATION_POWER);
        if (!hasSubs) return false;

        //TODO combine these two blocks to use the same sub-method

        final Term a = t[0];
        Term aa = a;

        if (a instanceof Compound) {
            aa = applySubstituteAndRenameVariables(((Compound) a), f.xy());

            if (aa == null) return false;

            final Op aaop = aa.op();
            if (a.op() == Op.VAR_QUERY && (aaop == Op.VAR_INDEPENDENT || aaop == Op.VAR_DEPENDENT))
                return false;

        }

        final Term b = t[1];
        Term bb = b;

        if (b instanceof Compound) {
            bb = applySubstituteAndRenameVariables(((Compound) b), f.yx());

            if (bb == null) return false;

            final Op bbop = bb.op();
            if (b.op() == Op.VAR_QUERY && (bbop == Op.VAR_INDEPENDENT || bbop == Op.VAR_DEPENDENT))
                return false;
        }

        t[0] = aa;
        t[1] = bb;

        return true;
    }

    /**
     * appliesSubstitute and renameVariables, resulting in a cloned object,
     * will not change this instance
     */
    static Term applySubstituteAndRenameVariables(final Compound t, final Map<Term, Term> subs) {
        if ((subs == null) || (subs.isEmpty())) {
            //no change needed
            return t;
        }

        return t.substituted(subs);
    }

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
        return nar().memory;
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


    /* --------------- new task building --------------- */


    default public <T extends Compound> FluentTask newTask(final T term) {
        return DefaultTask.make(term);
    }

    default public <T extends Compound> Task<Compound<?>> newTask(final T term, final char punc) {
        return newTask(term).punctuation(punc);
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
     */
    default Task validate(Task task) {

        final Memory memory = nar().memory;

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

        if (!task.normalize()) {
            return null;
        }

        if (!FilterDuplicateExistingBelief.isUniqueBelief(this, task)) {
            memory.remove(task, "Duplicate");
            return null;
        }

        memory.eventDerived.emit(task);
        return task;
    }




    default Concept concept(Term x) {
        return nar().concept(x);
    }


    /** true if both task and (non-null) belief are temporal events */
    default boolean isEvent() {
        /* TODO This part is used commonly, extract into its own precondition */
        Task b = getBelief();
        if (b == null) return false;
        Task t = getTask();
        return (!Tense.isEternal(t.getOccurrenceTime()) &&
                (!Tense.isEternal(b.getOccurrenceTime())));
    }

    /** true if both task and belief (if not null) are eternal */
    default boolean isEternal() {
        Task b = getBelief();
        if ((b != null) && (!b.isEternal()))
            return false;
        Task t = getTask();
        return t.isEternal();
    }

    /** true if either task or belief is non-eternal */
    default boolean isTemporal() {
        Task t = getTask();
        if (!t.isEternal()) return true;

        Task b = getBelief();
        if ((b != null) && (!b.isEternal()))
            return true;

        return false;
    }


//    default boolean isTaskEvent() {
//        return !Temporal.isEternal(getTask().getOccurrenceTime());
//    }

    //TODO cache this value
    default boolean isCyclic() {
        Task t = getTask();
        Task b = getBelief();
        if (b != null) {
            return Stamp.overlapping(t, b);
        }
        return false;
    }

    /** gets the average summary of one or both task/belief task's */
    default public float getMeanPriority() {
        float total = 0;
        int n = 0;
        final Task pt = getTask();
        if (pt!=null) {
            if (!pt.isDeleted())
                total += pt.getPriority();
            n++;
        }
        final Task pb = getBelief();
        if (pb!=null) {
            if (!pb.isDeleted())
                total += pb.getPriority();
            n++;
        }

        return total/n;
    }


    /** may be called during inference to update the premise
     * with a better belief than what it had previously. */
    void updateBelief(Task revised);

    default public boolean validateDerivedBudget(Budget budget) {
        if (budget.isDeleted()) {
            throw new RuntimeException("why is " + budget + " deleted");

        }
        return !budget.summaryLessThan(memory().derivationThreshold.floatValue());
    }


}

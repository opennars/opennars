package nars.nal.task;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.*;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.stamp.AbstractStamper;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.Stamper;
import nars.nal.term.Compound;

import java.util.Arrays;

/**
 * utility method for creating new tasks following a fluent builder pattern
 * warning: does not correctly support parent stamps, use .stamp() to specify one
 * <p>
 * TODO abstract this and move this into a specialization of it called FluentTaskSeed
 */
public class TaskSeed<T extends Compound> extends Stamper<T> implements AbstractStamper {

    transient public final Memory memory;


    private T term;
    private char punc;
    private Truth truth;

    private Task parent;
    private Sentence parentBelief;
    private Sentence solutionBelief;

    private Operation cause;
    private String reason;

    private boolean temporalInduct = true;

    //@Deprecated private long occDelta = 0;


    /**
     * creates a TaskSeed from an existing Task
     */
    public TaskSeed(Memory memory, Task task) {
        this(memory, task.sentence);

        parent(task.getParentTask(), task.getParentBelief());
        solution(task.getBestSolution());
        budget(task.getBudget());

        /* NOTE: this ignores:
                task.history         */
    }


    /**
     * if possible, use the direct value truth(f,c) method instead of allocating a Truth instance as an argument here
     */
    @Deprecated
    public TaskSeed<T> truth(Truth tv) {
        this.truth = tv;
        return this;
    }

    public TaskSeed<T> budget(float p, float d, float q) {
        budgetDirect(p, d, q);
        return this;
    }

    /**
     * if possible, use the direct value budget(p,d,q) method instead of allocating a Budget instance as an argument here
     */
    @Deprecated
    public TaskSeed<T> budget(Budget bv) {
        return budget(bv.getPriority(), bv.getDurability(), bv.getQuality());
    }

    /**
     * if possible, use the direct value budget(p,d,q) method instead of allocating a Budget instance as an argument here
     */
    @Deprecated
    public TaskSeed<T> budget(Budget bv, float priMult, float durMult) {
        return budget(bv.getPriority() * priMult, bv.getDurability() * durMult, bv.getQuality());
    }

    protected boolean ensureBudget() {
        if (isBudgetValid()) return true;
        if (truth == null) return false;

        this.priority = Budget.newDefaultPriority(punc);
        this.durability = Budget.newDefaultDurability(punc);

        return true;
    }

    /**
     * uses default budget generation and multiplies it by gain factors
     */
    public TaskSeed<T> budgetScaled(float priorityFactor, float durFactor) {

        //TODO maybe lift this to Budget class
        if (!ensureBudget()) {
            throw new RuntimeException("budgetScaled unable to determine original budget values");
        }


        this.priority *= priorityFactor;
        this.durability *= durFactor;
        return this;
    }


    public TaskSeed<T> term(T t) {
        this.term = t;
        return this;
    }

    public TaskSeed<T> truth(boolean freqAsBoolean, float conf) {
        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
    }

    public TaskSeed<T> truth(float freq, float conf) {
        if (this.truth == null)
            this.truth = new DefaultTruth(freq, conf, Global.TRUTH_EPSILON);
        else {
            this.truth.set(freq, conf);
        }
        return this;
        //return truth(freq, conf, Global.TRUTH_EPSILON);
    }

//    public TaskSeed<T> truth(float freq, float conf, float epsilon) {
//        if (this.truth == null)
//            this.truth = new DefaultTruth(freq, conf, epsilon);
//        else {
//            this.truth.set(freq, conf);
//            this.truth.setEpsilon(epsilon);
//        }
//
//        return this;
//    }

    /**
     * alias for judgment
     */
    public TaskSeed<T> belief() {
        return judgment();
    }

    public TaskSeed<T> judgment() {
        this.punc = Symbols.JUDGMENT;
        return this;
    }

    public TaskSeed<T> question() {
        this.punc = Symbols.QUESTION;
        return this;
    }

    public TaskSeed<T> quest() {
        this.punc = Symbols.QUEST;
        return this;
    }

    public TaskSeed<T> goal() {
        this.punc = Symbols.GOAL;
        return this;
    }


    public TaskSeed<T> tense(Tense t) {
        this.occurr(Stamp.getOccurrenceTime(memory.time(), t, memory));
        return this;
    }

    //TODO make these return the task, as the final call in the chain
    public TaskSeed<T> eternal() {
        return tense(Tense.Eternal);
    }

    public TaskSeed<T> present() {
        return tense(Tense.Present);
    }

    public TaskSeed<T> past() {
        return tense(Tense.Past);
    }

    public TaskSeed<T> future() {
        return tense(Tense.Future);
    }

    public TaskSeed<T> parent(Task task) {
        this.parent = task;
        return this;
    }

    public TaskSeed<T> parent(Sentence<?> parentBelief) {
        this.parentBelief = parentBelief;
        return this;
    }

    public TaskSeed<T> stamp(Task t) {
        //should this set parent too?
        t.sentence.applyToStamp(this);
        return this;
    }

    public TaskSeed<T> stamp(Stamper s) {
        s.applyToStamp(this);
        return this;
    }

    public TaskSeed<T> budget(float p, float d) {
        final float q;
        Truth t = getTruth();
        if ((getPunctuation()!=Symbols.QUESTION) && (getPunctuation()!=Symbols.QUEST)) {
            if (t == null)
                throw new RuntimeException("Truth needs to be defined prior to budget to calculate truthToQuality");
            q = BudgetFunctions.truthToQuality(t);
        }
        else
            q = Float.NaN;

        return budget(p, d, q);
    }

    public TaskSeed(Memory memory) {
        super();
        setOccurrenceTime(Stamp.ETERNAL);
        setCreationTime(memory.time());
        this.memory = memory;
    }

    public TaskSeed(Memory memory, T t) {
        /** budget triple - to be valid, at least the first 2 of these must be non-NaN (unless it is a question)  */
        this(memory);

        this.term = t;
    }

    @Deprecated
    public TaskSeed(Memory memory, Sentence<T> t) {
        this(memory, t.getTerm());
        this.punc = t.punctuation;
        this.truth = t.truth;
        t.applyToStamp(this);
    }


    public TaskSeed<T> duration(int duration) {
        this.setDuration(duration);
        return this;
    }


    /**
     * attempt to build the task, and insert into memory. returns non-null if successful
     */
    public Task input() {

        Task t = get();
        if (t == null) return null;

        if (t.isInput()) {
            if (memory.input(t) > 0) {
                return t;
            }
        } else {
            if (memory.taskAdd(t)) {
                return t;
            }
        }

        return null;

    }

    /**
     * attempt to build the task. returns non-null if successful
     */
    public Task get() {
        if (punc == 0)
            throw new RuntimeException("Punctuation must be specified before generating a default budget");

        if ((truth == null) && !((punc == Symbols.QUEST) || (punc == Symbols.QUESTION))) {
            truth = new DefaultTruth(punc);
        }

//        if (this.budget == null) {
//            //if budget not specified, use the default given the punctuation and truth
//            //TODO avoid creating a Budget instance here, it is just temporary because Task is its own Budget instance
//            this.budget = new Budget(punc, truth);
//        }

        /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
        if (Float.isNaN(quality) && truth != null) {
            quality = BudgetFunctions.truthToQuality(truth);
        }


        Compound sentenceTerm = term.sentencize(this);
        if (sentenceTerm == null)
            return null;

        if (Global.DEBUG) {
            sentenceTerm.ensureNormalized("Sentence term");

            if (Sentence.invalidSentenceTerm(sentenceTerm))
                throw new RuntimeException("Invalid sentence content term: " + sentenceTerm + ", seedTerm=" + term);
        }

        if (getEvidentialBase() == null) {
            if (getParentTask() != null)
                throw new RuntimeException(this + " has parent " + getParentTask() + " and " + getParentBelief() + " yet no evidentialBase was supplied");

            setEvidentialBase(new long[]{memory.newStampSerial()});
        } else {
            if (getParentTask() == null)
                throw new RuntimeException(this + " has no parent so where did the evidentialBase originate?: " + Arrays.toString(getEvidentialBase()));
        }

        Task t = new Task(sentenceTerm, punc, truth, this,
                getBudget(),
                getParentTask(),
                getParentBelief(),
                solutionBelief);

        t.setTemporalInducting(temporalInduct);

        if (this.cause != null) t.setCause(cause);
        if (this.reason != null) t.addHistory(reason);

        return t;
    }

    private Budget getBudget() {
        ensureBudget();
        return this;
    }

    public Task getParentTask() {
        return parent;
    }

    public Sentence getParentBelief() {
        return parentBelief;
    }


    public TaskSeed<T> punctuation(final char punctuation) {
        this.punc = punctuation;
        return this;
    }

    public TaskSeed<T> time(long creationTime, long occurrenceTime) {
        setCreationTime(creationTime);
        occurr(occurrenceTime);
        return this;
    }

    public TaskSeed<T> cause(Operation operation) {
        this.cause = operation;
        return this;
    }

    public TaskSeed<T> reason(String reason) {
        this.reason = reason;
        return this;
    }

    public boolean isEternal() {
        long o = getOccurrenceTime();
        return (o == Stamp.UNPERCEIVED) || (o == Stamp.ETERNAL);
    }

    /**
     * if a stamp exists, determine if it will be cyclic;
     * otherwise assume that it is not.
     */
    public boolean isCyclic() {
        if (getEvidentialBase() != null)
            return Stamp.isCyclic(this);

//        if ((stamp != null) && (stamp instanceof StampEvidence))
//            return ((StampEvidence) stamp).isCyclic();

        throw new RuntimeException(this + " has no evidence to determine cyclicity");
    }


    public TaskSeed<T> parent(Task parentTask, Task parentBeliefTask) {
        return parent(parentTask, parentBeliefTask.sentence);
    }

    public TaskSeed<T> parent(Task parentTask, Sentence<?> parentBelief) {
        return parent(parentTask).parent(parentBelief);
    }

    public TaskSeed<T> solution(Sentence<?> solutionBelief) {
        this.solutionBelief = solutionBelief;
        return this;
    }

    public TaskSeed<T> occurr(long occurenceTime) {
        this.setOccurrenceTime(occurenceTime);
        return this;
    }



    public boolean isGoal() {
        return punc == Symbols.GOAL;
    }

    public boolean isJudgment() {
        return punc == Symbols.JUDGMENT;
    }

    public boolean isQuestion() {
        return punc == Symbols.QUESTION;
    }

    public boolean isQuest() {
        return punc == Symbols.QUEST;
    }

    public T getTerm() {
        return term;
    }

    public Truth getTruth() {
        return truth;
    }

    public int getDuration() {
        int d = super.getDuration();
        if (d < 0) d = memory.duration();
        return d;
    }


    public char getPunctuation() {
        return punc;
    }

//    @Override
//    public void applyToStamp(Stamp target) {
//
//        super.applyToStamp(target);
//
//        long o = getOccurrenceTime( /*target.getCreationTime()*/);
//
//        if (((occDelta != 0) && (o != Stamp.ETERNAL) && (o != Stamp.UNPERCEIVED))) {
//            o += +occDelta;
//        }
//        if (o != Stamp.UNPERCEIVED)
//            target.setOccurrenceTime(o);
//    }
//

    public TaskSeed<T> budgetCompoundForward(Compound result, NAL nal) {
        BudgetFunctions.compoundForward(this, getTruth(), result, nal);
        return this;
    }


    public TaskSeed<T> temporalInduct(boolean b) {
        this.temporalInduct = b;
        return this;
    }

    public TaskSeed<T> stamp(Stamp a, Stamp b) {
        setA(a);
        setB(b);
        return this;
    }

    public TaskSeed<T> parentStamp(Task parentTask, Task parentBeliefTask) {
        return parentStamp(parentTask, parentBeliefTask.sentence);
    }

    public TaskSeed<T> parentStamp(Task parentTask, Sentence parentBelief) {
        parent(parentTask, parentBelief);
        stamp(parentTask.sentence, parentBelief);
        return this;
    }
    public TaskSeed<T> parentStamp(Task parentTask, Sentence parentBelief, long occurence) {
        parentStamp(parentTask, parentBelief);
        setOccurrenceTime(occurence);
        return this;
    }

    public TaskSeed<T> parentStamp(Task task, long occurrenceTime) {
        parentStamp(task);
        setOccurrenceTime(occurrenceTime);
        return this;
    }

    public TaskSeed<T> parentStamp(Task task) {
        parent(task);
        stamp(task);
        return this;
    }

    /**
     //     * sets an amount of cycles to shift the final applied occurence time
     //     */
//    public TaskSeed<T> occurrDelta(long occurenceTime) {
//        this.occDelta = occurenceTime;
//        return this;
//    }
}

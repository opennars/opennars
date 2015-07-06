package nars.task;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.DirectBudget;
import nars.io.JSONOutput;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.process.NAL;
import nars.task.stamp.AbstractStamper;
import nars.task.stamp.Stamp;
import nars.task.stamp.StampEvidence;
import nars.term.Compound;
import nars.truth.BasicTruth;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.Arrays;

/**
 * utility method for creating new tasks following a fluent builder pattern
 * warning: does not correctly support parent stamps, use .stamp() to specify one
 * <p>
 * TODO abstract this and move this into a specialization of it called FluentTaskSeed
 */
public class TaskSeed<T extends Compound> extends DirectBudget implements AbstractStamper, StampEvidence, Stamp {

    transient public final Memory memory;

    protected int duration;

    protected long creationTime = Stamp.TIMELESS;
    protected long occurrenceTime = Stamp.TIMELESS;

    private long[] evidentialSet;


    private T term;
    private char punc;
    private Truth truth;

    private Task parent;
    private Sentence parentBelief;
    private Sentence solutionBelief;

    private Operation cause;
    private String reason;

    private boolean temporalInducatable = true;
    private boolean cyclic = false;



    //@Deprecated private long occDelta = 0;




    public TaskSeed(Memory memory) {
        super();
        //setOccurrenceTime(Stamp.ETERNAL);
        //setCreationTime(memory.time());
        this.memory = memory;
    }

    public TaskSeed(Memory memory, T t) {
        /** budget triple - to be valid, at least the first 2 of these must be non-NaN (unless it is a question)  */
        this(memory);

        this.duration = memory.duration();
        this.term = t;
    }

    @Deprecated
    public TaskSeed(Memory memory, Sentence<T> t) {
        this(memory, t.getTerm());
        this.punc = t.punctuation;
        this.truth = t.truth;
        t.applyToStamp(this);
    }


    public TaskSeed setOccurrenceTime(long occurrenceTime) {
        this.occurrenceTime = occurrenceTime;
        return this;
    }
    public boolean isDouble() {
        return this.getParentTask()!=null && this.getParentBelief()!=null;
    }


    public TaskSeed setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }


    /**
     * duration (in cycles) which any contained intervals are measured by
     */
    public int getDuration() {
        if (this.duration == 0) {
            int d;
            if (getParentBelief() !=null) {
                d = (this.duration = getParentBelief().getDuration());
                if (d!=0) return d;
            }
            if (getParentTask() !=null) {
                d = (this.duration = getParentTask().sentence.getDuration());
                if (d!=0) return d;
            }
            else {
                return memory.duration();
            }
        }
        return duration;
    }


    @Override public void applyToStamp(final Stamp target) {


        target.setDuration(getDuration())
                .setTime(getCreationTime(), getOccurrenceTime())
                .setEvidence(getEvidentialSet())
                .setCyclic(isCyclic());

    }

    @Override
    public long[] getEvidentialSet() {
        updateEvidence();
        return evidentialSet;
    }



    protected void updateEvidence() {
        if (evidentialSet== null) {

            if ((getParentTask() == null) && (getParentBelief() == null)) {
                //supplying no evidence will be assigned a new serial
                //but this should only happen for input tasks (with no parent)
            } else if (isDouble()) {
                long[] as = getParentTask().getEvidentialSet();
                long[] bs = getParentBelief().getEvidentialSet();

                //temporary
                if (as == null)
                    throw new RuntimeException("parentTask " + getParentTask() + " has no evidentialSet");
                if (bs == null)
                    throw new RuntimeException("parentBelief " + getParentBelief() + " has no evidentialSet");

                setEvidentialSet(Stamp.toSetArray(Stamp.zip(as, bs)));

                if (getParentTask().isInput() || getParentBelief().isInput()) {
                    setCyclic(false);
                }
                else {
                    /*
                    <patham9> since evidental overlap is not checked on deduction, a derivation can be cyclic
                    <patham9> its on revision when it finally matters, but not whether the two parents are cyclic, but whether the combination of both evidental bases of both parents would be cyclic/have an overlap
                    <patham9> else deductive conclusions could not lead to revisions altough the overlap is only local to the parent (the deductive conclusion)
                    <patham9> revision is allowed here because the two premises to revise dont have an overlapping evidental base element
                    */
                    boolean bothParentsCyclic = getParentTask().isCyclic() && getParentBelief().isCyclic();

                    boolean overlapBetweenParents = ((as.length + bs.length) > evidentialSet.length);

                    //if the sum of the two parents length is greater than the result then there was some overlap
                    setCyclic(bothParentsCyclic || overlapBetweenParents);
                }

            }
            else {
                //Single premise

                Stamp p = null; //parent to inherit some properties from
                if (getParentTask() == null) p = getParentBelief();
                else if (getParentBelief() == null) p = getParentTask().sentence;

                if (p!=null) {
                    setEvidentialSet(p.getEvidentialSet());
                    setCyclic(p.isCyclic());
                }
            }

        }
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

        /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
        if (Float.isNaN(quality)) {
            quality = BudgetFunctions.truthToQuality(truth);
        }

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

    public TaskSeed<T> term(T t) {
        this.term = t;
        return this;
    }

    public TaskSeed<T> truth(boolean freqAsBoolean, float conf) {
        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
    }

    public TaskSeed<T> truth(float freq, float conf, float epsilon) {
        if (this.truth != null) {
            System.err.println("warning: " + this + " modifying existing truth: " + this.truth);
        }
        this.truth = BasicTruth.get(freq, conf, epsilon);
        return this;
    }

    public TaskSeed<T> truth(float freq, float conf) {
        if (!(this.truth instanceof DefaultTruth)) //includes the null case as false
            this.truth = new DefaultTruth(freq, conf);
        else {
            this.truth.set(freq, conf);
        }
        return this;
        //return truth(freq, conf, Global.TRUTH_EPSILON);
    }

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


    public TaskSeed<T> stamp(Task t) {
        //should this set parent too?
        t.sentence.applyToStamp(this);
        return this;
    }

//    public TaskSeed<T> stamp(Stamper s) {
//        s.applyToStamp(this);
//        return this;
//    }

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

    public boolean isInput() {
        return getParentTask() == null;
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

//



        Compound sentenceTerm = getTerm();
        if (sentenceTerm == null)
            return null;

        if (Global.DEBUG) {
            if (Sentence.invalidSentenceTerm(sentenceTerm)) {
                System.err.println("Invalid sentence content term: " + sentenceTerm + ", seedTerm=" + term);
                return null;
            }
        }

        if (getEvidentialSet() == null) {
            if (getParentTask() != null)
                throw new RuntimeException(this + " has parent " + getParentTask() + " and " + getParentBelief() + " yet no evidentialBase was supplied");

            setEvidentialSet(new long[]{memory.newStampSerial()});

        } else {
            if (getParentTask() == null && getParentBelief()==null)
                throw new RuntimeException(this + " has no parent task or belief so where did the evidentialBase originate?: " + Arrays.toString(getEvidentialSet()));
        }


        Task t = new Task(sentenceTerm, punc,
                (truth != null) ? BasicTruth.clone(truth) : null, //clone the truth so that this class can be re-used multiple times with different values to create different tasks
                getBudget(),
                getParentTask(),
                getParentBelief(),
                solutionBelief);

        applyToStamp(t);

        t.setTemporalInducting(temporalInducatable);

        if (this.cause != null) t.setCause(cause);
        if (this.reason != null) t.log(reason);

        return t;
    }


    public TaskSeed setEvidentialSet(long[] evidentialSet) {
        this.evidentialSet = evidentialSet;
        return this;
    }

    public TaskSeed setDuration(int d) {
        this.duration = d;
        return this;
    }


    /**
     * creation time of the stamp
     */
    public long getCreationTime() {
        if (creationTime == Stamp.TIMELESS) {
            //Default: created now
            return memory.time();
        }
        return  creationTime;
    }

    /**
     * estimated occurrence time of the event*
     */
    public long getOccurrenceTime() {
        return occurrenceTime;
    }


    @Override
    public String toString() {
        try {
            return JSONOutput.stringFromFields(this);
        }
        catch (StackOverflowError e) {
            e.printStackTrace();
            //TODO prevent this
            return getClass().getSimpleName() + "[JSON_Error]";
        }
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

    public boolean isTimeless() {
        return getOccurrenceTime() == Stamp.TIMELESS;
    }
    public boolean isEternal() {
        return getOccurrenceTime() == Stamp.ETERNAL;
    }


    /**
     * if a stamp exists, determine if it will be cyclic;
     * otherwise assume that it is not.
     */
    public boolean isCyclic() {
        if (getEvidentialSet() != null) {
            //HACK when Stamp and parents are unified the extra conditoins here will not be necessary:
            if (getParentTask()!=null && getParentTask().isInput()) {
                cyclic = false;
            }
            else if (getParentBelief()!=null && getParentBelief().isInput()) {
                cyclic = false;
            }

            return cyclic;
        }

//        if ((stamp != null) && (stamp instanceof StampEvidence))
//            return ((StampEvidence) stamp).isCyclic();

        throw new RuntimeException(this + " has no evidence to determine cyclicity");
    }

    @Override
    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    public TaskSeed<T> parent(final Task parentTask, final Sentence<?> parentBelief) {
        this.parent = parentTask;
        this.parentBelief = parentBelief;
        return this;
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
        final T t = this.term;
        if (t == null) return null;

        //return normalized version no matter what, and save it to prevent redundant work
        return this.term = t.normalized();
    }

    public Truth getTruth() {
        return truth;
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


    public TaskSeed<T> temporalInductable(boolean b) {
        this.temporalInducatable = b;
        return this;
    }


    public TaskSeed<T> parent(Task parentTask, Sentence parentBelief, long occurence) {
        parent(parentTask, parentBelief);
        setOccurrenceTime(occurence);
        return this;
    }

    public TaskSeed<T> parent(Task task, long occurrenceTime) {
        parent(task, null);
        setOccurrenceTime(occurrenceTime);
        return this;
    }

    public TaskSeed<T> parent(Task task) {
        parent(task, null);
        return this;
    }

    public TaskSeed<T> occurrNow() {
        return setOccurrenceTime(memory.time());
    }

    public boolean temporalInductable() {
        return temporalInducatable;
    }

    /**
     //     * sets an amount of cycles to shift the final applied occurence time
     //     */
//    public TaskSeed<T> occurrDelta(long occurenceTime) {
//        this.occDelta = occurenceTime;
//        return this;
//    }
}

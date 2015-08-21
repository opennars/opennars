package nars.task;

import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.premise.Premise;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.Arrays;

/**
 * utility method for creating new tasks following a fluent builder pattern
 * warning: does not correctly support parent stamps, use .stamp() to specify one
 * <p>
 * TODO abstract this and move this into a specialization of it called FluentTaskSeed
 */
public class TaskSeed<T extends Compound> extends DefaultTask<T> implements Stamp {


    transient private final Memory memory;

    public static <C extends Compound> TaskSeed<C> make(Memory memory, C t) {
        TaskSeed<C> x = make(memory);
        if (t!=null) {
            t = t.normalized();
            if (t == null)
                return null;
            x.setTerm(t);
        }
        return x;
    }


    public static <C extends Compound> TaskSeed<C> make(Memory memory) {
        return new TaskSeed(memory);
    }

    TaskSeed(Memory memory) {
        /** budget triple - to be valid, at least the first 2 of these must be non-NaN (unless it is a question)  */
        super(null, (char)0, null, 0, 0, 0);

        budgetDirect(Float.NaN, Float.NaN, Float.NaN);

        this.memory = memory;
        setDuration(memory.duration());
        setOccurrenceTime(TIMELESS);
    }

//    @Deprecated
//    public TaskSeed(Memory memory, Sentence<T> t) {
//        this(memory, t.getTerm());
//        this.punc = t.punctuation;
//        this.truth = t.truth;
//        t.applyToStamp(this);
//    }


    @Override
    public TaskSeed<T> setOccurrenceTime(long occurrenceTime) {
        super.setOccurrenceTime(occurrenceTime);
        return this;
    }

    /** is double-premise */
    public boolean isDouble() {
        return this.getParentTask()!=null && this.getParentBelief()!=null;
    }


    @Override
    public TaskSeed<T> setCreationTime(long creationTime) {
        super.setCreationTime(creationTime);
        return this;
    }


    /**
     * duration (in cycles) which any contained intervals are measured by
     */
    @Override
    public int getDuration() {
        int duration = super.getDuration();
        if (duration == 0) {
            int d = 0;
            if (getParentBelief() !=null) {
                d = getParentBelief().getDuration();
                if (d!=0) return d;
            }
            else if (d == 0 && getParentTask() !=null) {
                d = getParentTask().getDuration();
            }

            if (d!=0) setDuration(d);
        }
        return duration;
    }


    @Deprecated @Override
    public void applyToStamp(final Stamp target) {
        throw new RuntimeException("untested / depr");
//        target.setDuration(getDuration())
//                .setTime(getCreationTime(), getOccurrenceTime())
//                .setEvidence(getEvidence())
//                .setCyclic(isCyclic());

    }

    @Override
    public long[] getEvidence() {
        updateEvidence();
        return super.getEvidence();
    }



    protected void updateEvidence() {
        if (super.getEvidence() == null) {

            if ((getParentTask() == null) && (getParentBelief() == null)) {
                //supplying no evidence will be assigned a new serial
                //but this should only happen for input tasks (with no parent)
            } else if (isDouble()) {
                long[] as = getParentTask().getEvidence();
                long[] bs = getParentBelief().getEvidence();

                //temporary
                if (as == null)
                    throw new RuntimeException("parentTask " + getParentTask() + " has no evidentialSet");
                if (bs == null)
                    throw new RuntimeException("parentBelief " + getParentBelief() + " has no evidentialSet");

                setEvidence(Stamp.toSetArray(Stamp.zip(as, bs)));

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

                    boolean overlapBetweenParents = ((as.length + bs.length) > getEvidence().length);

                    //if the sum of the two parents length is greater than the result then there was some overlap
                    setCyclic(bothParentsCyclic || overlapBetweenParents);
                }

            }
            else {
                //Single premise

                Stamp p = null; //parent to inherit some properties from
                if (getParentTask() == null) p = getParentBelief();
                else if (getParentBelief() == null) p = getParentTask();

                if (p!=null) {
                    setEvidence(p.getEvidence());
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
    public TaskSeed<T> budget(Budget bv, float priMult, float durMult) {
        return budget(bv.getPriority() * priMult, bv.getDurability() * durMult, bv.getQuality());
    }

    protected boolean applyDefaultBudget() {
        //if (getBudget().isBudgetValid()) return true;
        if (getTruth() == null) return false;

        final char punc = getPunctuation();
        setPriority( Budget.newDefaultPriority(punc) );
        setDurability( Budget.newDefaultDurability(punc) );

        /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
        if (Float.isNaN(quality)) {
            setQuality( BudgetFunctions.truthToQuality(truth) );
        }

        return true;
    }

    /**
     * uses default budget generation and multiplies it by gain factors
     */
    public TaskSeed<T> budgetScaled(float priorityFactor, float durFactor) {

        //TODO maybe lift this to Budget class
        if (!applyDefaultBudget()) {
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

    public TaskSeed<T> termIfValid(T t) {
        t = Sentence.termOrNull(t);
        if (t == null) return null;
        term(t);
        return this;
    }

    public TaskSeed<T> truth(boolean freqAsBoolean, float conf) {
        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
    }

    public TaskSeed<T> truth(float freq, float conf) {
        if (this.truth != null) {
            System.err.println("warning: " + this + " modifying existing truth: " + this.truth);
        }
        this.truth = new DefaultTruth(freq, conf);
        return this;
    }



    /**
     * alias for judgment
     */
    public TaskSeed<T> belief() {
        return judgment();
    }

    public TaskSeed<T> judgment() {
        setPunctuation( Symbols.JUDGMENT );
        return this;
    }

    public TaskSeed<T> question() {
        setPunctuation( Symbols.QUESTION );
        return this;
    }

    public TaskSeed<T> quest() {
        setPunctuation( Symbols.QUEST );
        return this;
    }

    public TaskSeed<T> goal() {
        setPunctuation( Symbols.GOAL );
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
        t.applyToStamp(this);
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


//    /**
//     * attempt to build the task, and insert into memory. returns non-null if successful
//     */
//    public Task input() {
//
//        Task t = get();
//        if (t == null) return null;
//
//        if (t.isInput()) {
//            if (memory.input(t) > 0) {
//                return t;
//            }
//        } else {
//            if (memory.addDerived(t)) {
//                return t;
//            }
//        }
//
//        return null;
//
//    }

    @Override
    public boolean isInput() {
        return getParentTask() == null;
    }

    /**
     * attempt to build the task. returns non-null if successful
     */
    public Task get() {
        final char punc = getPunctuation();
        if (punc == 0)
            throw new RuntimeException("Punctuation must be specified before generating a default budget");

        if ((truth == null) && !((punc == Symbols.QUEST) || (punc == Symbols.QUESTION))) {
            truth = new DefaultTruth(punc);
        }


        Compound sentenceTerm = getTerm();
        if (sentenceTerm == null)
            return null;


        if (getEvidence() == null) {
            if (getParentTask() != null)
                throw new RuntimeException(this + " has parent " + getParentTask() + " and " + getParentBelief() + " yet no evidentialBase was supplied");

            setEvidence(new long[]{memory.newStampSerial()});

        } else {
            if (getParentTask() == null && getParentBelief()==null)
                throw new RuntimeException(this + " has no parent task or belief so where did the evidentialBase originate?: " + Arrays.toString(getEvidence()));
        }


        /*Task t = new DefaultTask(sentenceTerm, punc,
                (truth != null) ? new DefaultTruth(truth) : null, //clone the truth so that this class can be re-used multiple times with different values to create different tasks
                getBudget(),
                getParentTask(),
                getParentBelief(),
                solutionBelief);*/

        setDuration(getDuration())
                .setTime(getCreationTime(), getOccurrenceTime())
                .setEvidence(getEvidence());


        if (!Float.isFinite(getQuality())) {
            applyDefaultBudget();
        }


        //applyToStamp(t);

        //setTemporalInducting(temporallyInductable);

        //if (this.cause != null) t.setCause(cause);
        //if (this.reason != null) t.log(reason);

        return this;
    }


    @Override
    public TaskSeed setEvidence(long[] evidentialSet) {
        super.setEvidence(evidentialSet);
        return this;
    }

    @Override
    public TaskSeed setDuration(int d) {
        super.setDuration(d);
        return this;
    }


    /**
     * creation time of the stamp
     */
    @Override
    public long getCreationTime() {
        long creationTime = super.getCreationTime();
//        if (creationTime == Stamp.ETERNAL) {
//            throw new RuntimeException("creation time should be specified or timeless, not eternal");
//        }
        if (creationTime == Stamp.TIMELESS) {
            //Default: created now
            return memory.time();
        }
        return creationTime;
    }




//    @Override
//    public String toString() {
//        try {
//            return JSONOutput.stringFromFields(this);
//        }
//        catch (StackOverflowError e) {
//            e.printStackTrace();
//            //TODO prevent this
//            return getClass().getSimpleName() + "[JSON_Error]";
//        }
//    }


    public TaskSeed<T> punctuation(final char punctuation) {
        setPunctuation(punctuation);
        return this;
    }

    public TaskSeed<T> time(long creationTime, long occurrenceTime) {
        setCreationTime(creationTime);
        occurr(occurrenceTime);
        return this;
    }

    public TaskSeed<T> cause(Operation operation) {
        setCause(operation);
        return this;
    }

    public TaskSeed<T> reason(String reason) {
        log(reason);
        return this;
    }


    public boolean updateCyclic() {

        if (getEvidence() != null) {

            boolean cyclic = true;

            //HACK when Stamp and parents are unified the extra conditoins here will not be necessary:
            if (getParentTask()!=null && getParentTask().isInput()) {
                cyclic = false;
            }
            else if (getParentBelief()!=null && getParentBelief().isInput()) {
                cyclic = false;
            }

            return cyclic;
        }

        return false;
    }


    public TaskSeed<T> parent(final Task parentTask, final Task parentBelief) {
        setParentTask(parentTask);
        setParentBelief(parentBelief);
        return this;
    }

    public TaskSeed<T> solution(Task solutionBelief) {
        setBestSolution(memory, solutionBelief);
        return this;
    }

    public TaskSeed<T> occurr(long occurrenceTime) {
        this.setOccurrenceTime(occurrenceTime);
        return this;
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

    public TaskSeed<T> budgetCompoundForward(Compound result, Premise p) {
        BudgetFunctions.compoundForward(this, getTruth(), result, p);
        return this;
    }


    public TaskSeed<T> temporalInductable(boolean b) {
        setTemporalInducting(b);
        return this;
    }


    public TaskSeed<T> parent(Task parentTask, Task parentBelief, long occurrence) {
        parent(parentTask, parentBelief);
        setOccurrenceTime(occurrence);
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



    /**
     //     * sets an amount of cycles to shift the final applied occurence time
     //     */
//    public TaskSeed<T> occurrDelta(long occurenceTime) {
//        this.occDelta = occurenceTime;
//        return this;
//    }
}

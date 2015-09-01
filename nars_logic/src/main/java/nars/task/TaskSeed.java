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
        if (t != null) {
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
        super(null, (char) 0, null, 0, 0, 0);

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
        setPunctuation(Symbols.JUDGMENT);
        return this;
    }

    public TaskSeed<T> question() {
        setPunctuation(Symbols.QUESTION);
        return this;
    }

    public TaskSeed<T> quest() {
        setPunctuation(Symbols.QUEST);
        return this;
    }

    public TaskSeed<T> goal() {
        setPunctuation(Symbols.GOAL);
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


//    public TaskSeed<T> stamp(Stamper s) {
//        s.applyToStamp(this);
//        return this;
//    }

    public TaskSeed<T> budget(float p, float d) {
        final float q;
        Truth t = getTruth();
        if ((getPunctuation() != Symbols.QUESTION) && (getPunctuation() != Symbols.QUEST)) {
            if (t == null)
                throw new RuntimeException("Truth needs to be defined prior to budget to calculate truthToQuality");
            q = BudgetFunctions.truthToQuality(t);
        } else
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
            if (getParentTask() != null && getParentTask().isInput()) {
                cyclic = false;
            } else if (getParentBelief() != null && getParentBelief().isInput()) {
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
        setOccurrenceTime(memory.time());
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

package nars.task;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.Memory;
import nars.NAR;
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

/**
 * task with additional fluent api utility methods for creating new tasks following a fluent builder pattern
 * warning: does not correctly support parent stamps, use .stamp() to specify one
 * <p>
 * TODO abstract this and move this into a specialization of it called FluentTaskSeed
 */
@JsonSerialize(using = ToStringSerializer.class)
@Deprecated public class TaskSeed extends DefaultTask<Compound>  {


    public static <C extends Compound> TaskSeed make(NAR nar, C t) {
        return make(nar.memory(), t);
    }

    public static <C extends Compound> TaskSeed make(Memory memory, C t) {
        t.normalizeDestructively();
        Compound u = Sentence.termOrNull(t);
        if (u == null)
            return null;

        TaskSeed x = make(memory);

        x.setTerm(u);

        return x;
    }


    public static <C extends Compound> TaskSeed make(Memory memory) {
        return new TaskSeed(memory);
    }

    public TaskSeed(Memory memory) {
        /** budget triple - to be valid, at least the first 2 of these must be non-NaN (unless it is a question)  */
        super(null, (char) 0, null, 0, 0, 0);

        budgetDirect(Float.NaN, Float.NaN, Float.NaN);

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
    public TaskSeed truth(final Truth tv) {

        if (tv == null) {
            if (isJudgmentOrGoal()) throw new RuntimeException("null truth value for judgment/goal");
        }
        else {
            if (!isJudgmentOrGoal()) throw new RuntimeException("non-null truth value for non-judgment/non-goal");
        }

        this.truth = tv;
        return this;
    }

    public final TaskSeed budget(final float p, final float d, final float q) {
        budgetDirect(p, d, q);
        return this;
    }

    /**
     * if possible, use the direct value budget(p,d,q) method instead of allocating a Budget instance as an argument here
     */
    @Deprecated
    public TaskSeed budget(final Budget bv) {
        return budget(bv.getPriority(), bv.getDurability(), bv.getQuality());
    }

    /**
     * if possible, use the direct value budget(p,d,q) method instead of allocating a Budget instance as an argument here
     */
    public TaskSeed budget(Budget bv, float priMult, float durMult) {
        return budget(bv.getPriority() * priMult, bv.getDurability() * durMult, bv.getQuality());
    }



    /**
     * uses default budget generation and multiplies it by gain factors
     */
    public TaskSeed budgetScaled(float priorityFactor, float durFactor) {

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

    public TaskSeed term(Compound t) {
        this.term = t;
        return this;
    }

    public TaskSeed termIfValid(Compound t) {
        t = Sentence.termOrNull(t);
        if (t == null) return null;
        term(t);
        return this;
    }

    public TaskSeed truth(boolean freqAsBoolean, float conf) {
        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
    }

    public TaskSeed truth(float freq, float conf) {
        if (this.truth != null) {
            System.err.println("warning: " + this + " modifying existing truth: " + this.truth);
        }
        this.truth = new DefaultTruth(freq, conf);
        return this;
    }


    /**
     * alias for judgment
     */
    public TaskSeed belief() {
        return judgment();
    }

    public TaskSeed judgment() {
        setPunctuation(Symbols.JUDGMENT);
        return this;
    }

    public TaskSeed question() {
        setPunctuation(Symbols.QUESTION);
        return this;
    }

    public TaskSeed quest() {
        setPunctuation(Symbols.QUEST);
        return this;
    }

    public TaskSeed goal() {
        setPunctuation(Symbols.GOAL);
        return this;
    }

    public TaskSeed tense(Tense t, Memory memory) {
        this.occurr(Stamp.getOccurrenceTime(memory.time(), t, memory));
        return this;
    }

    //TODO make these return the task, as the final call in the chain
    /*public TaskSeed eternal() {
        return tense(Tense.Eternal);
    }*/

    public TaskSeed present(Memory memory) {
        return tense(Tense.Present, memory);
    }

    public TaskSeed past(Memory memory) {
        return tense(Tense.Past, memory);
    }

    public TaskSeed future(Memory memory) {
        return tense(Tense.Future, memory);
    }


//    public TaskSeed<T> stamp(Stamper s) {
//        s.applyToStamp(this);
//        return this;
//    }

    public TaskSeed budget(float p, float d) {
        final float q;
        Truth t = getTruth();
        if (!isQuestOrQuestion()) {
            if (t == null)
                throw new RuntimeException("Truth needs to be defined prior to budget to calculate truthToQuality");
            q = BudgetFunctions.truthToQuality(t);
        } else
            throw new RuntimeException("incorrect punctuation");

        return budget(p, d, q);
    }

    public TaskSeed duration(int duration) {
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


    public TaskSeed punctuation(final char punctuation) {
        setPunctuation(punctuation);
        return this;
    }

    public TaskSeed time(long creationTime, long occurrenceTime) {
        setCreationTime(creationTime);
        occurr(occurrenceTime);
        return this;
    }

    public TaskSeed cause(Operation operation) {
        setCause(operation);
        return this;
    }

    public TaskSeed reason(String reason) {
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


    public TaskSeed parent(final Task parentTask, final Task parentBelief) {
        if (parentTask == null)
            throw new RuntimeException("parent task being set to null");

        setParentTask(parentTask);
        setParentBelief(parentBelief);
        return this;
    }

    public TaskSeed solution(Task solutionBelief, Memory memory) {
        setBestSolution(solutionBelief, memory);
        return this;
    }

    public TaskSeed occurr(long occurrenceTime) {
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

    public TaskSeed budgetCompoundForward(Compound result, Premise p) {
        BudgetFunctions.compoundForward(this, getTruth(), result, p);
        return this;
    }


    public TaskSeed temporalInductable(boolean b) {
        setTemporalInducting(b);
        return this;
    }


    public TaskSeed parent(Task parentTask, Task parentBelief, long occurrence) {
        parent(parentTask, parentBelief);
        setOccurrenceTime(occurrence);
        return this;
    }

    public TaskSeed parent(Task task, long occurrenceTime) {
        parent(task, null);
        setOccurrenceTime(occurrenceTime);
        return this;
    }

    public TaskSeed parent(Task task) {
        parent(task, null);
        return this;
    }

    public final TaskSeed occurrNow(final NAR nar) {
        return occurrNow(nar.memory);
    }

    public TaskSeed occurrNow(final Memory memory) {
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


    @Override
    @Deprecated public TaskSeed setEternal() {
        super.setEternal();
        return this;
    }
}

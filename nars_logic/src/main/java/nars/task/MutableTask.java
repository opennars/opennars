package nars.task;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.Memory;
import nars.Premise;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.nal7.Tense;
import nars.term.compound.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import javax.annotation.Nullable;

/**
 * mutable task with additional fluent api utility methods for creating new tasks following a fluent builder pattern
 * warning: does not correctly support parent stamps, use .stamp() to specify one
 * <p>
 * TODO abstract this and move this into a specialization of it called FluentTaskSeed
 */
@JsonSerialize(using = ToStringSerializer.class)
public class MutableTask<C extends Compound> extends DefaultTask<C>  {


//    public static <C extends Compound> TaskSeed make(NAR nar, C t) {
//        return make(nar.memory(), t);
//    }


    public MutableTask() {
        /** budget triple - to be valid, at least the first 2 of these must be non-NaN (unless it is a question)  */
        super(null, (char) 0, null, 0, 0, 0);

        budget(0, Float.NaN, Float.NaN);

        setEternal();

        setOccurrenceTime(TIMELESS);
    }

    public MutableTask(C term) {
        this();
        term(term);
    }

//    public FluentTask(String termOrTaskString) {
//        super();
//
//        termOrTaskString = termOrTaskString.trim();
//
//        int len = termOrTaskString.length();
//        char lastChar = termOrTaskString.charAt(len -1);
//        if ((Symbols.isPunctuation(lastChar) || lastChar == Symbols.TRUTH_VALUE_MARK || lastChar == Symbols.TENSE_MARK)) {
//            //set both term and punc
//            setTerm($._()term(termOrTaskString.substring(0, len-1)));
//            setPunctuation(lastChar);
//        }
//        else {
//            //set only term
//            setTerm(NarseseParser.the().term(termString));
//        }
//    }

//    @Deprecated
//    public TaskSeed(Memory memory, Sentence<T> t) {
//        this(memory, t.getTerm());
//        this.punc = t.punctuation;
//        this.truth = t.truth;
//        t.applyToStamp(this);
//    }





    /**
     * if possible, use the direct value truth(f,c) method instead of allocating a Truth instance as an argument here
     * this will set the truth instance directly. so avoid using shared terms unless it's really meant
     */
    @Deprecated
    public MutableTask truth(final Truth tv) {

        if (tv == null) {
            if (isJudgmentOrGoal()) throw new RuntimeException("null truth value for judgment/goal");
        }
        else {
            if (!isJudgmentOrGoal()) throw new RuntimeException("non-null truth value for non-judgment/non-goal");
        }

        this.truth = tv;
        return this;
    }




//    /**
//     * if possible, use the direct value budget(p,d,q) method instead of allocating a Budget instance as an argument here
//     */
//    public TaskSeed budget(Budget bv, float priMult, float durMult) {
//        budget(bv.getPriority() * priMult, bv.getDurability() * durMult, bv.getQuality());
//        return this;
//    }

    @Override
    public final MutableTask budget(float p, float d, float q) {
        super.budget(p, d, q);
        return this;
    }

    @Override
    public final MutableTask budget(@Nullable Budget source) {
        super.budget(source);
        return this;
    }

    /**
     * uses default budget generation and multiplies it by gain factors
     */
    public MutableTask budgetScaled(float priorityFactor, float durFactor) {

        //TODO maybe lift this to Budget class
        if (!applyDefaultBudget()) {
            throw new RuntimeException("budgetScaled unable to determine original budget values");
        }

        mulPriority(priorityFactor);
        mulDurability(durFactor);
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

    public final MutableTask term(C t) {
        this.term = t;
        return this;
    }

//    public TaskSeed termIfValid(Compound t) {
//        t = Task.termOrNull(t);
//        if (t == null) return null;
//        term(t);
//        return this;
//    }
//
//    public TaskSeed truth(boolean freqAsBoolean, float conf) {
//        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
//    }

    public final MutableTask truth(float freq, float conf) {
        if (truth == null)
            this.truth = new DefaultTruth(freq, conf);
        else
            this.truth.set(freq, conf);
        return this;
    }


    /**
     * alias for judgment
     */
    public MutableTask belief() {
        return judgment();
    }

    public MutableTask judgment() {
        setPunctuation(Symbols.JUDGMENT);
        return this;
    }

    public MutableTask question() {
        setPunctuation(Symbols.QUESTION);
        return this;
    }

    public MutableTask quest() {
        setPunctuation(Symbols.QUEST);
        return this;
    }

    public MutableTask goal() {
        setPunctuation(Symbols.GOAL);
        return this;
    }

    public MutableTask tense(Tense t, Memory memory) {
        this.occurr(Tense.getOccurrenceTime(memory.time(), t, memory));
        return this;
    }

    //TODO make these return the task, as the final call in the chain
    /*public TaskSeed eternal() {
        return tense(Tense.Eternal);
    }*/

    public final MutableTask present(Memory memory) {
        //return tense(Tense.Present, memory);
        long now = memory.time();
        return time(now, now);
    }

    //WARNING not tested:
//    public FluentTask past(Memory memory) {
//        return tense(Tense.Past, memory);
//    }
//
//    public FluentTask future(Memory memory) {
//        return tense(Tense.Future, memory);
//    }


//    public TaskSeed<T> stamp(Stamper s) {
//        s.applyToStamp(this);
//        return this;
//    }

    public MutableTask budget(float p, float d) {
        final float q;
        Truth t = getTruth();
        if (!isQuestOrQuestion()) {
            if (t == null)
                throw new RuntimeException("Truth needs to be defined prior to budget to calculate truthToQuality");
            q = BudgetFunctions.truthToQuality(t);
        } else
            throw new RuntimeException("incorrect punctuation");

        budget(p, d, q);
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


    public MutableTask<C> punctuation(final char punctuation) {
        setPunctuation(punctuation);
        return this;
    }

    public MutableTask time(long creationTime, long occurrenceTime) {
        setCreationTime(creationTime);
        occurr(occurrenceTime);
        return this;
    }


    public MutableTask because(Object reason) {
        log(reason);
        return this;
    }


//    public boolean updateCyclic() {
//
//        if (getEvidence() != null) {
//
//            boolean cyclic = true;
//
//            //HACK when Stamp and parents are unified the extra conditoins here will not be necessary:
//            if (getParentTask() != null && getParentTask().isInput()) {
//                cyclic = false;
//            } else if (getParentBelief() != null && getParentBelief().isInput()) {
//                cyclic = false;
//            }
//
//            return cyclic;
//        }
//
//        return false;
//    }


    public MutableTask parent(final Task parentTask, final Task parentBelief) {
        if (parentTask == null)
            throw new RuntimeException("parent task being set to null");

        setParentTask(parentTask);
        setParentBelief(parentBelief);
        return this;
    }

//    public TaskSeed solution(Task solutionBelief, Memory memory) {
//        setBestSolution(solutionBelief, memory);
//        return this;
//    }

    public MutableTask occurr(long occurrenceTime) {
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

    public MutableTask budgetCompoundForward(Compound result, Premise p) {
        BudgetFunctions.compoundForward(this, getTruth(), result, p);
        return this;
    }


//    public TaskSeed temporalInductable(boolean b) {
//        setTemporalInducting(b);
//        return this;
//    }


//    public TaskSeed parent(Task parentTask, Task parentBelief, long occurrence) {
//        parent(parentTask, parentBelief);
//        setOccurrenceTime(occurrence);
//        return this;
//    }
//
//    public TaskSeed parent(Task task, long occurrenceTime) {
//        parent(task, null);
//        setOccurrenceTime(occurrenceTime);
//        return this;
//    }

    public MutableTask parent(Task task) {
        parent(task, null);
        return this;
    }

//    public final TaskSeed occurrNow(final NAR nar) {
//        return occurrNow(nar.memory);
//    }
//
//    public TaskSeed occurrNow(final Memory memory) {
//        setOccurrenceTime(memory.time());
//        return this;
//    }


    /**
     //     * sets an amount of cycles to shift the final applied occurence time
     //     */
//    public TaskSeed<T> occurrDelta(long occurenceTime) {
//        this.occDelta = occurenceTime;
//        return this;
//    }

    public MutableTask<C> eternal() {
        setEternal();
        return this;
    }


    /** flag used for anticipatable derivation */
    public MutableTask anticipate(boolean a) {
        this.anticipate = a;
        return this;
    }

    public MutableTask budgetCompoundForward(Premise premise) {
        BudgetFunctions.compoundForward(
                getBudget(), getTruth(),
                getTerm(), premise);
        return this;
    }
}

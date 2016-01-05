package nars.task;

import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.nal7.Tense;
import nars.process.ConceptProcess;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import javax.annotation.Nullable;

/**
 * Mutable task with additional fluent api utility methods
 */
public class MutableTask extends AbstractTask {

    public MutableTask() {
        /** budget triple - to be valid, at least the first 2 of these must be non-NaN (unless it is a question)  */
        super(null, (char) 0, null,
            /* budget: */ 0, Float.NaN, Float.NaN);

        setEternal();
        setOccurrenceTime(Tense.TIMELESS);
    }

    public static MutableTask clone(Task t) {
        return new MutableTask(t, true);
    }

    MutableTask(Task taskToClone, boolean dummy) {
        super(taskToClone);
    }

    public MutableTask(Termed<Compound> term) {
        this();
        term(term);
    }

    public MutableTask(Termed<Compound> content, char punc) {
        this(content);
        punctuation(punc);
    }


    public MutableTask truth(Truth tv) {
        if (tv == null)
            setTruth(null);
        else
            setTruth(new DefaultTruth(tv));
        return this;
    }


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
        mulPriority(priorityFactor);
        mulDurability(durFactor);
        return this;
    }

    public final MutableTask term(Termed<Compound> t) {
        setTerm(t);
        return this;
    }

    public final MutableTask truth(float freq, float conf) {
        //if (truth == null)
            setTruth(new DefaultTruth(freq, conf));
        //else
            //this.truth.set(freq, conf);
        return this;
    }


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
        occurr(Tense.getOccurrenceTime(memory.time(), t, memory));
        return this;
    }

    public final MutableTask present(Memory memory) {
        //return tense(Tense.Present, memory);
        long now = memory.time();
        return time(now, now);
    }

    public MutableTask budget(float p, float d) {
        float q;
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

    public MutableTask punctuation(char punctuation) {
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


    public MutableTask parent(Task parentTask, Task parentBelief) {
        if (parentTask == null)
            throw new RuntimeException("parent task being set to null");

        setParentTask(parentTask);
        setParentBelief(parentBelief);
        return this;
    }


    public MutableTask occurr(long occurrenceTime) {
        setOccurrenceTime(occurrenceTime);
        return this;
    }

    public MutableTask parent(Task task) {
        parent(task, null);
        return this;
    }

    /**
     //     * sets an amount of cycles to shift the final applied occurence time
     //     */
//    public TaskSeed<T> occurrDelta(long occurenceTime) {
//        this.occDelta = occurenceTime;
//        return this;
//    }

    public MutableTask eternal() {
        setEternal();
        return this;
    }


    /** flag used for anticipatable derivation */
    public MutableTask anticipate(boolean a) {
        anticipate = a;
        return this;
    }

    public MutableTask budgetCompoundForward(ConceptProcess premise) {
        BudgetFunctions.compoundForward(
                getBudget(), getTruth(),
                term(), premise);
        return this;
    }
}

package nars.nal;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.nal7.Tense;
import nars.nal.stamp.Stamp;
import nars.nal.term.Compound;

/** utility method for creating new tasks following a fluent builder pattern
 *  warning: does not correctly support parent stamps, use .stamp() to specify one
 * */
public class ProtoTask<T extends Compound> {

    private final T term;
    private final Memory memory;

    private char punc;
    private Tense tense;
    private float pri;
    private float dur;
    private Stamp stamp = null;
    private Truth truth;
    private Task parent;
    private Budget budget;


    public ProtoTask<T> truth(Truth tv) {
        this.truth = tv;
        return this;
    }
    public ProtoTask<T> budget(Budget bv) {
        this.budget = bv;
        return this;
    }
    public ProtoTask<T> priority(float p) {
        if (budget == null)
            pri = p;
        else
            budget.setPriority(p);
        return this;
    }

    public ProtoTask<T> truth(boolean freqAsBoolean, float conf) {
        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
    }

    public ProtoTask<T> truth(float freq, float conf) {
        return truth(freq, conf, Global.TRUTH_EPSILON);
    }

    public ProtoTask<T> truth(float freq, float conf, float epsilon) {
        this.truth = new DefaultTruth(freq, conf, epsilon);
        if (budget == null) {
            //set a default budget if none exists
            budget = new Budget(punc, truth);
        }
        return this;
    }

    /** alias for judgment */
    public ProtoTask<T> belief() { return judgment(); }

    public ProtoTask<T> judgment() { this.punc = Symbols.JUDGMENT; return this;}
    public ProtoTask<T> question() { this.punc = Symbols.QUESTION; return this;}
    public ProtoTask<T> quest() { this.punc = Symbols.QUEST; return this;}
    public ProtoTask<T> goal() { this.punc = Symbols.GOAL; return this;}


    //TODO make these return the task, as the final call in the chain
    public ProtoTask<T> eternal() { this.tense = Tense.Eternal; return this;}
    public ProtoTask<T> present() { this.tense = Tense.Present; return this;}
    public ProtoTask<T> past() { this.tense = Tense.Past; return this;}
    public ProtoTask<T> future() { this.tense = Tense.Future; return this;}

    public ProtoTask<T> parent(Task task) {
        this.parent = task;
        return this;
    }

    public ProtoTask<T> stamp(Stamp s) { this.stamp = s; return this;}

    public ProtoTask<T> budget(float p, float d) {
        budget = null;
        this.pri = p;
        this.dur = d;
        return this;
    }

    public ProtoTask(Memory memory, T t) {
        this.memory = memory;
        this.term = t;
    }
    public ProtoTask(Memory memory, Sentence<T> t) {
        this(memory, t.getTerm());
        this.punc = t.punctuation;
        this.truth = t.truth;
        this.stamp = t.stamp;

    }


    /** build the new instance */
    public Task get() {
        if (this.budget == null)
            this.budget = new Budget(punc, truth);
        return new Task(this);
    }


    public Sentence<T> getSentence() {
        return new Sentence(term, punc, truth,
                stamp == null ? new Stamp(memory, tense) : stamp);
    }

    public Budget getBudget() {
//        if (budget == null)
//            return new Budget(pri, dur, truth);
//        else
            return budget;
    }

    public Task getParentTask() {
        return parent;
    }

    public Sentence getParentBelief() {
        return null;
    }


    public ProtoTask<T> punctuation(final char punctuation) {
        this.punc = punctuation;
        return this;
    }

    public ProtoTask<T> time(long creationTime, long occurrenceTime) {
        return stamp(new Stamp(memory,creationTime, occurrenceTime));
    }
}

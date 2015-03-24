package nars.nal.entity;

import nars.Memory;
import nars.energy.Budget;
import nars.io.Symbols;
import nars.nal.entity.stamp.Stamp;
import nars.nal.nal7.Tense;

/** utility method for creating new tasks following a fluent builder pattern
 *  warning: does not correctly support parent stamps, use .stamp() to specify one
 * */
public class NewTask<T extends Compound> {

    private final T term;
    private final Memory memory;

    private char punc;
    private Tense tense;
    private float pri;
    private float dur;
    private Stamp stamp = null;
    private TruthValue truth;
    private Task parent;
    private Budget budget;


    public NewTask<T> truth(TruthValue tv) {
        this.truth = tv;
        return this;
    }
    public NewTask<T> budget(Budget bv) {
        this.budget = bv;
        return this;
    }

    public NewTask<T> truth(boolean freqAsBoolean, float conf) {
        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
    }

    public NewTask<T> truth(float freq, float conf) {
        this.truth = new TruthValue(freq, conf);
        return this;
    }

    public NewTask<T> judgment() { this.punc = Symbols.JUDGMENT; return this;}
    public NewTask<T> question() { this.punc = Symbols.QUESTION; return this;}
    public NewTask<T> quest() { this.punc = Symbols.QUEST; return this;}
    public NewTask<T> goal() { this.punc = Symbols.GOAL; return this;}

    public NewTask<T> eternal() { this.tense = Tense.Eternal; return this;}
    public NewTask<T> present() { this.tense = Tense.Present; return this;}
    public NewTask<T> past() { this.tense = Tense.Past; return this;}
    public NewTask<T> future() { this.tense = Tense.Future; return this;}

    public NewTask<T> parent(Task task) {
        this.parent = task;
        return this;
    }

    public NewTask<T> stamp(Stamp s) { this.stamp = s; return this;}

    public NewTask<T> budget(float p, float d) {
        budget = null;
        this.pri = p;
        this.dur = d;
        return this;
    }

    public NewTask(Memory memory, T t) {
        this.memory = memory;
        this.term = t;
    }
    public NewTask(Memory memory, Sentence<T> t) {
        this(memory, t.getTerm());
        this.punc = t.punctuation;
        this.truth = t.truth;
        this.stamp = t.stamp;
        this.budget = new Budget(t.punctuation, t.truth);
    }


    /** build the new instance */
    public Task get() {
        return new Task(this);
    }


    public Sentence<T> getSentence() {
        return new Sentence(term, punc, truth,
                stamp == null ? new Stamp(memory, tense) : stamp);
    }

    public Budget getBudget() {
        if (budget == null)
            return new Budget(pri, dur, truth);
        else
            return budget;
    }

    public Task getParentTask() {
        return parent;
    }

    public Sentence getParentBelief() {
        return null;
    }


}

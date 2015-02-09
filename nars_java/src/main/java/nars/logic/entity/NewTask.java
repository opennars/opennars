package nars.logic.entity;

import nars.core.Memory;
import nars.io.Symbols;
import nars.logic.nal7.Tense;

/** utility method for creating new tasks following a fluent builder pattern
 *  warning: does not correctly support parent stamps, use .stamp() to specify one
 * */
public class NewTask<T extends CompoundTerm> {

    private final T term;
    private final Memory memory;

    private char punc;
    private Tense tense;
    private float pri;
    private float dur;
    private Stamp stamp = null;
    private TruthValue truth;
    private Task parent;


    public NewTask<T> truth(float freq, float conf) {
        this.truth = new TruthValue(freq, conf);
        return this;
    }

    public NewTask<T> judgment() { this.punc = Symbols.JUDGMENT; return this;}
    public NewTask<T> question() { this.punc = Symbols.QUESTION; return this;}
    public NewTask<T> quest() { this.punc = Symbols.QUEST; return this;}
    public NewTask<T> goal() { this.punc = Symbols.GOAL; return this;}

    public NewTask<T> eternal() { this.tense = Tense.Eternal; return this;}
    public NewTask<T> now() { this.tense = Tense.Present; return this;}
    public NewTask<T> past() { this.tense = Tense.Past; return this;}
    public NewTask<T> future() { this.tense = Tense.Future; return this;}

    public NewTask<T> parent(Task task) {
        this.parent = task;
        return this;
    }

    public NewTask<T> Stamp(Stamp s) { this.stamp = s; return this;}

    public NewTask<T> budget(float p, float d) {
        this.pri = p;
        this.dur = d;
        return this;
    }

    public NewTask(Memory memory, T t) {
        this.memory = memory;
        this.term = t;
    }


    /** build the new instance */
    public Task get() {
        return new Task(this);
    }


    public Sentence<T> getSentence() {
        return new Sentence(term, punc, truth,
                stamp == null ? new Stamp(memory, tense) : stamp);
    }

    public BudgetValue getBudget() {
        return new BudgetValue(pri, dur, truth);
    }

    public Task getParentTask() {
        return parent;
    }

    public Sentence getParentBelief() {
        return null;
    }

    public Sentence getSolution() {
        return null;
    }


}

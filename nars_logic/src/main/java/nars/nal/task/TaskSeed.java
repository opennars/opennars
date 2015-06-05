package nars.nal.task;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.DefaultTruth;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.Truth;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.stamp.IStamp;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.Stamper;
import nars.nal.term.Compound;

/** utility method for creating new tasks following a fluent builder pattern
 *  warning: does not correctly support parent stamps, use .stamp() to specify one
 *
 *  TODO abstract this and move this into a specialization of it called FluentTaskSeed
 * */
public class TaskSeed<T extends Compound> {

    private final T term;
    private final Memory memory;

    private char punc;
    private Tense tense;
    private IStamp<T> stamp = null;
    private Truth truth;
    private Task parent;
    private Operation cause;
    private String reason;

    /** budget triple - to be valid, at least the first 2 of these must be non-NaN (unless it is a question)  */
    float p = Float.NaN, d = Float.NaN, q = Float.NaN;


    public TaskSeed<T> truth(Truth tv) {
        this.truth = tv;
        return this;
    }

    public TaskSeed<T> budget(float p, float d, float q) {
        this.p = p;
        this.d = d;
        this.q = q;
        return this;
    }

    public TaskSeed<T> budget(Budget bv) {
        return budget(bv.getPriority(), bv.getDurability(), bv.getQuality());
    }

    public TaskSeed<T> budgetScaled(Budget bv, float priMult, float durMult) {
        return budget(bv.getPriority() * priMult, bv.getDurability() * durMult, bv.getQuality());
    }

    protected boolean ensureBudget() {
        if (Float.isFinite(this.p)) return true;
        if (truth == null) return false;

        this.p = Budget.newDefaultPriority(punc);
        this.d = Budget.newDefaultDurability(punc);

        return true;
    }

    /** uses default budget generation and multiplies it by gain factors */
    public TaskSeed<T> budgetScaled(float priorityFactor, float durFactor) {

        if (!ensureBudget()) {
            throw new RuntimeException("budgetScaled unable to determine original budget values");
        }

        this.p *= priorityFactor;
        this.d *= durFactor;
        return this;
    }


    public TaskSeed<T> truth(boolean freqAsBoolean, float conf) {
        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
    }

    public TaskSeed<T> truth(float freq, float conf) {
        return truth(freq, conf, Global.TRUTH_EPSILON);
    }

    public TaskSeed<T> truth(float freq, float conf, float epsilon) {
        this.truth = new DefaultTruth(freq, conf, epsilon);
        return this;
    }

    /** alias for judgment */
    public TaskSeed<T> belief() { return judgment(); }

    public TaskSeed<T> judgment() { this.punc = Symbols.JUDGMENT; return this;}
    public TaskSeed<T> question() { this.punc = Symbols.QUESTION; return this;}
    public TaskSeed<T> quest() { this.punc = Symbols.QUEST; return this;}
    public TaskSeed<T> goal() { this.punc = Symbols.GOAL; return this;}


    //TODO make these return the task, as the final call in the chain
    public TaskSeed<T> eternal() { this.tense = Tense.Eternal; return this;}
    public TaskSeed<T> present() { this.tense = Tense.Present; return this;}
    public TaskSeed<T> past() { this.tense = Tense.Past; return this;}
    public TaskSeed<T> future() { this.tense = Tense.Future; return this;}

    public TaskSeed<T> parent(Task task) {
        this.parent = task;
        return this;
    }

    public TaskSeed<T> stamp(IStamp<T> s) { this.stamp = s; return this;}

    public TaskSeed<T> budget(float p, float d) {
        return budget(p, d, Float.NaN);
    }

    public TaskSeed(Memory memory, T t) {
        this.memory = memory;
        this.term = t;
    }
    public TaskSeed(Memory memory, Sentence<T> t) {
        this(memory, t.getTerm());
        this.punc = t.punctuation;
        this.truth = t.truth;
        this.stamp = t;

    }



    /** attempt to build the task, and insert into memory. returns non-null if successful */
    public Task input() {

        if (punc == 0)
            throw new RuntimeException("Punctuation must be specified before generating a default budget");

        if (stamp != null && tense!=Tense.Eternal) {
            throw new RuntimeException("both tense " + tense + " and stamp " + stamp + "; only use one to avoid any inconsistency");
        }

        if ((truth == null) && ((punc!=Symbols.QUEST) || (punc!=Symbols.QUESTION))) {
            truth = new DefaultTruth(punc);
        }

//        if (this.budget == null) {
//            //if budget not specified, use the default given the punctuation and truth
//            //TODO avoid creating a Budget instance here, it is just temporary because Task is its own Budget instance
//            this.budget = new Budget(punc, truth);
//        }

        Sentence s = new Sentence(term, punc, truth,
                stamp == null ? memory : stamp);

        if (s == null)
            return null;

        if (stamp == null && tense!=Tense.Eternal) {
            /* apply the Tense on its own, with respect to the creation time and memory duration */
            s.setOccurrenceTime(Stamp.occurrence(s.getCreationTime(), tense, memory.duration()) );
        }


        /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
        if (!Float.isFinite(q) && truth != null) {
            q = BudgetFunctions.truthToQuality(truth);
        }

        Task t = new Task(s,
                p, d, q, //budget
                Global.reference(getParentTask()), getParentBelief(), null);

        if (this.cause!=null) t.setCause(cause);
        if (this.reason!=null) t.addHistory(reason);

        if (memory.input(t) == 0) {
            return null;
        }

        return t;

    }



    public Task getParentTask() {
        return parent;
    }

    public Sentence getParentBelief() {
        return null;
    }


    public TaskSeed<T> punctuation(final char punctuation) {
        this.punc = punctuation;
        return this;
    }

    public TaskSeed<T> time(long creationTime, long occurrenceTime) {
        return stamp(new Stamper(memory,creationTime, occurrenceTime));
    }

    public TaskSeed<T> cause(Operation operation) {
        this.cause = operation;
        return this;
    }

    public TaskSeed<T> reason(String reason) {
        this.reason = reason;
        return this;
    }

}

package nars.nal.task;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.DirectBudget;
import nars.nal.*;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.stamp.AbstractStamper;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.StampEvidence;
import nars.nal.stamp.Stamper;
import nars.nal.term.Compound;

/** utility method for creating new tasks following a fluent builder pattern
 *  warning: does not correctly support parent stamps, use .stamp() to specify one
 *
 *  TODO abstract this and move this into a specialization of it called FluentTaskSeed
 * */
public class TaskSeed<T extends Compound> extends DirectBudget implements AbstractStamper {

    private final Memory memory;


    private T term;
    private char punc;
    private Tense tense;
    private AbstractStamper stamp;
    private Truth truth;
    private Task parent;
    private Operation cause;
    private String reason;



    /** if non-UNPERCEIVED, it is allowed to override the value the Stamp applied */
    private long occurrenceTime = Stamp.UNPERCEIVED;
    private Sentence parentBelief;
    private Sentence solutionBelief;

    /** creates a TaskSeed from an existing Task  */
    public TaskSeed(Memory memory, Task task) {
        this(memory, task.sentence);

        parent(task.getParentTask(), task.getParentBelief());
        solution(task.getBestSolution());
        budget(task.getBudget());

        /* NOTE: this ignores:
                task.history         */
    }


    /** if possible, use the direct value truth(f,c) method instead of allocating a Truth instance as an argument here */
    @Deprecated public TaskSeed<T> truth(Truth tv) {
        this.truth = tv;
        return this;
    }

    public TaskSeed<T> budget(float p, float d, float q) {
        budgetDirect(p, d, q);
        return this;
    }

    /** if possible, use the direct value budget(p,d,q) method instead of allocating a Budget instance as an argument here */
    @Deprecated public TaskSeed<T> budget(Budget bv) {
        return budget(bv.getPriority(), bv.getDurability(), bv.getQuality());
    }

    /** if possible, use the direct value budget(p,d,q) method instead of allocating a Budget instance as an argument here */
    @Deprecated public TaskSeed<T> budget(Budget bv, float priMult, float durMult) {
        return budget(bv.getPriority() * priMult, bv.getDurability() * durMult, bv.getQuality());
    }

    protected boolean ensureBudget() {
        if (isBudgetValid()) return true;
        if (truth == null) return false;

        this.priority = Budget.newDefaultPriority(punc);
        this.durability = Budget.newDefaultDurability(punc);

        return true;
    }

    /** uses default budget generation and multiplies it by gain factors */
    public TaskSeed<T> budgetScaled(float priorityFactor, float durFactor) {

        //TODO maybe lift this to Budget class
        if (!ensureBudget()) {
            throw new RuntimeException("budgetScaled unable to determine original budget values");
        }


        this.priority *= priorityFactor;
        this.durability *= durFactor;
        return this;
    }


    public TaskSeed<T> term(T t) {
        this.term = t;
        return this;
    }

    public TaskSeed<T> truth(boolean freqAsBoolean, float conf) {
        return truth(freqAsBoolean ? 1.0f : 0.0f, conf);
    }

    public TaskSeed<T> truth(float freq, float conf) {
        if (this.truth == null)
            this.truth = new DefaultTruth(freq, conf, Global.TRUTH_EPSILON);
        else {
            this.truth.set(freq, conf);
        }
        return this;
        //return truth(freq, conf, Global.TRUTH_EPSILON);
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

    public TaskSeed<T> parent(Sentence<?> parentBelief) {
        this.parentBelief = parentBelief;
        return this;
    }


    public TaskSeed<T> stamp(AbstractStamper s) { this.stamp = s; return this;}

    public TaskSeed<T> budget(float p, float d) {
        return budget(p, d, Float.NaN);
    }

    public TaskSeed(Memory memory) {
        super();
        this.memory = memory;
    }

    public TaskSeed(Memory memory, T t) {
        /** budget triple - to be valid, at least the first 2 of these must be non-NaN (unless it is a question)  */
        this(memory);

        this.term = t;
    }

    @Deprecated public TaskSeed(Memory memory, Sentence<T> t) {
        this(memory, t.getTerm());
        this.punc = t.punctuation;
        this.truth = t.truth;
        this.stamp = t;
    }



    /** attempt to build the task, and insert into memory. returns non-null if successful */
    public Task input() {

        Task t = get();
        if (t == null) return null;

        if (memory.input(t) == 0) {
            return null;
        }

        return t;

    }

    /** attempt to build the task. returns non-null if successful */
    public Task get() {
        if (punc == 0)
            throw new RuntimeException("Punctuation must be specified before generating a default budget");

        if ((truth == null) && !((punc==Symbols.QUEST) || (punc==Symbols.QUESTION))) {
            truth = new DefaultTruth(punc);
        }

//        if (this.budget == null) {
//            //if budget not specified, use the default given the punctuation and truth
//            //TODO avoid creating a Budget instance here, it is just temporary because Task is its own Budget instance
//            this.budget = new Budget(punc, truth);
//        }

        AbstractStamper st = (this.stamp == null ? memory : this);



        /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
        if (Float.isNaN(quality) && truth != null) {
            quality = BudgetFunctions.truthToQuality(truth);
        }

        Task t = new Task(term, punc, truth, st,
                getBudget(),
                getParentTask(),
                getParentBelief(),
                solutionBelief);

        if (this.cause!=null) t.setCause(cause);
        if (this.reason!=null) t.addHistory(reason);

        return t;
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

    public boolean isEternal() {
        if (stamp instanceof Stamper) {
            return ((Stamper)stamp).isEternal();
        }
        if (tense!=null)
            return tense!=Tense.Eternal;
        return true;
    }

    /** if a stamp exists, determine if it will be cyclic;
     *  otherwise assume that it is not. */
    public boolean isCyclic() {
        if ((stamp!=null) && (stamp instanceof StampEvidence))
            return ((StampEvidence)stamp).isCyclic();

        throw new RuntimeException(this + " has no evidence to determine cyclicity");
    }


    public TaskSeed<T> parent(Task parentTask, Sentence<?> parentBelief) {
        return parent(parentTask).parent(parentBelief);
    }

    public TaskSeed<T> solution(Sentence<?> solutionBelief) {
        this.solutionBelief = solutionBelief;
        return this;
    }

    public TaskSeed<T> occurr(long occurenceTime) {
        this.occurrenceTime = occurenceTime;
        return this;
    }

    public boolean isGoal() { return punc == Symbols.GOAL;     }
    public boolean isJudgment() { return punc == Symbols.JUDGMENT;     }
    public boolean isQuestion() { return punc == Symbols.QUESTION;     }
    public boolean isQuest() { return punc == Symbols.QUEST;     }

    public T getTerm() {
        return term;
    }

    public Truth getTruth() {
        return truth;
    }


    public char getPunctuation() {
        return punc;
    }

    @Override
    public void applyToStamp(Stamp target) {
        if (stamp!=null)
            stamp.applyToStamp(target);

        //override occurence time provided by the stamp with either tense or a specific occurrence time:
        {
            boolean hasTense = (tense != null);
            boolean hasSpecificOcccurence = (this.occurrenceTime != Stamp.UNPERCEIVED);

            if (hasTense && hasSpecificOcccurence) {
                throw new RuntimeException("ambiguous choice between tense " + tense + " and specific occurence time " + occurrenceTime + "; only use one to avoid any inconsistency");
            }

            if (hasTense) {
                /* apply the Tense on its own, with respect to the creation time and memory duration */
                target.setOccurrenceTime(Stamp.getOccurrenceTime(target.getCreationTime(), tense, memory.duration()));
            }
            else if (hasSpecificOcccurence) {
                target.setOccurrenceTime(this.occurrenceTime);
            }
        }
    }


    public TaskSeed<T> budgetCompoundForward(Compound result, NAL nal) {
        BudgetFunctions.compoundForward(this, getTruth(), result, nal);
        return this;
    }
}

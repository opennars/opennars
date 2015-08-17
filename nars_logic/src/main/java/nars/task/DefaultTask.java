package nars.task;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.AbstractMemory;
import nars.Global;
import nars.budget.Budget;
import nars.budget.Item;
import nars.nal.nal7.Sequence;
import nars.nal.nal8.Operation;
import nars.op.mental.InternalExperience;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.TermMetadata;
import nars.truth.Truth;
import nars.util.data.Util;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static nars.Global.dereference;
import static nars.Global.reference;

/**
 * Created by me on 8/17/15.
 */
@JsonSerialize(using = ToStringSerializer.class)
public class DefaultTask<T extends Compound> extends Item<Sentence<T>> implements Task<T>, Serializable, JsonSerializable {
    /**
     * The punctuation also indicates the type of the Sentence:
     * Judgment, Question, Goal, or Quest.
     * Represented by characters: '.', '?', '!', or '@'
     */
    public final char punctuation;
    /**
     * The truth value of Judgment, or desire value of Goal
     * TODO can we make this final eventually like it was before.. Concept.discountBeliefConfidence needed to mutate the truth on discount
     */
    public Truth truth;
    protected T term;
    transient private int hash;

    private long[] evidentialSet = null;


    private long creationTime = Stamp.TIMELESS;

    private long occurrenceTime = Stamp.ETERNAL;

    private int duration = 0;
    private boolean cyclic;


    /**
     * Task from which the Task is derived, or null if input
     */
    transient public final Reference<Task> parentTask; //should this be transient? we may want a Special kind of Reference that includes at least the parent's Term

    /**
     * Belief from which the Task is derived, or null if derived from a theorem
     */
    transient public final Reference<Task> parentBelief;


    /**
     * TODO move to SolutionTask subclass
     * For Question and Goal: best solution found so far
     */
    private Reference<Task> bestSolution;

    /**
     * TODO move to DesiredTask subclass
     * causal factor if executed; an instance of Operation
     */
    private Operation cause;

    private List<String> history = null;

    /**
     * indicates this Task can be used in Temporal induction
     */
    private boolean temporallyInductable = true;

    public DefaultTask(T term, final char punctuation, final Truth truth, final Budget bv, final Task parentTask, final Task parentBelief, final Task solution) {
        this(term, punctuation, truth,
                bv != null ? bv.getPriority() : 0,
                bv != null ? bv.getDurability() : 0,
                bv != null ? bv.getQuality() : 0,
                parentTask, parentBelief, solution);
    }

    public DefaultTask(T term, final char punc, final Truth truth, final float p, final float d, final float q) {
        this(term, punc, truth, p, d, q, (Task) null, null, null);
    }

    public DefaultTask(T term, final char punc, final Truth truth, final float p, final float d, final float q, final Task parentTask, final Task parentBelief, final Task solution) {
        this(term, punc, truth,
                p, d, q,
                Global.reference(parentTask),
                reference(parentBelief),
                reference(solution)
        );
    }

    public DefaultTask(T term, final char punctuation, final Truth truth, final float p, final float d, final float q, final Reference<Task> parentTask, final Reference<Task> parentBelief, final Reference<Task> solution) {
        super(p, d, q);
        //super(term, punctuation, truth, p, d, q);

        this.punctuation = punctuation;

        boolean isQuestionOrQuest = isQuestion() || isQuest();
        if (isQuestionOrQuest) {
            this.truth = null;
        } else if (truth == null) {
            throw new RuntimeException("Judgment and Goal sentences require non-null truth value");
        } else {
            this.truth = truth;
        }

        if (term instanceof Sequence) {
            this.term = (T) ((Sequence) term).cloneRemovingSuffixInterval();
        } else {
            this.term = term;
        }

        this.parentTask = parentTask;

        if (parentTask == null)
            log("Input");

        this.parentBelief = parentBelief;
        this.bestSolution = solution;

        if (Global.DEBUG) {
            if ((parentTask != null && parentTask.get() == null))
                throw new RuntimeException("parentTask must be null itself, or reference a non-null Task");

            ///*if (this.equals(getParentTask())) {
            if (this == getParentTask()) {
                throw new RuntimeException(this + " has parentTask equal to itself");
            }
        /*
        //IS THERE SOME WAY TO MERGE EQUIVALENT BELIEFS HERE?
        if (this.sentence.equals(parentBelief)) {
            throw new RuntimeException(this + " has parentBelief equal to its sentence");
        }
        */
        }


        this.hash = getHash();


    }

    @Override
    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    private int getHash() {
        //stamp (evidentialset, occurrencetime), truth, term, punctuation

        int hashStamp = Util.hash(Arrays.hashCode(getEvidentialSet()), (int) this.getOccurrenceTime());

        final int truthHash = (getTruth() != null) ? getTruth().hashCode() : 0;

        return (Util.hash(hashStamp, getTerm().hashCode(), truthHash) * 31) + getPunctuation();
    }

    public void setTermShared(final T equivalentInstance) {

        //intermval generally contains unique information that should not be replaced
        if (this.term instanceof TermMetadata)
            return;

        //if debug, check that they are equal..

        this.term = equivalentInstance;
    }

    @Override
    public T getTerm() {
        return term;
    }

    @Override
    public Truth getTruth() {
        return truth;
    }

    @Override
    public boolean isCyclic() {
        return cyclic;
    }

    @Override
    public void applyToStamp(Stamp target) {

    }

    @Override
    public Task<T> setEvidentialSet(long[] evidentialSet) {
        this.evidentialSet = evidentialSet;
        return this;
    }

    public Task log(List<String> historyToCopy) {
        if (!Global.DEBUG_TASK_HISTORY)
            return this;

        if (historyToCopy != null) {
            if (this.history == null) this.history = new ArrayList(historyToCopy.size());
            history.addAll(historyToCopy);
        }
        return this;
    }


    @Override
    public char getPunctuation() {
        return punctuation;
    }

    @Override
    public long[] getEvidentialSet() {
        return evidentialSet;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getOccurrenceTime() {
        return occurrenceTime;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    public Sentence<T> setCreationTime(long creationTime) {
        if ((this.creationTime <= Stamp.TIMELESS) && (this.occurrenceTime > Stamp.TIMELESS)) {
            //use the occurrence time as the delta, now that this has a "finite" creationTime
            this.occurrenceTime = this.occurrenceTime + creationTime;
        }
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public Sentence setOccurrenceTime(long o) {
        setOccurrenceTime(o);
        return this;
    }

    @Override
    public Sentence setDuration(int d) {
        setDuration(d);
        return this;
    }


    public DefaultTask(Sentence<T> s, Budget budget, Task parentTask, Task parentBelief) {
        this(s.getTerm(), s.getPunctuation(), s.getTruth(), budget, parentTask, parentBelief, null);
    }

    /**
     * To check whether two sentences are equal
     *
     * @param that The other sentence
     * @return Whether the two sentences have the same content
     */

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that instanceof Sentence) {
            //if (that.hashCode()!=hashCode()) return false;
            return equivalentTo((Sentence) that, true, true, true, true, false);
        }
        return false;
    }

    public Reference<Task> getParentTaskRef() {
        return parentTask;
    }

    public Reference<Task> getParentBeliefRef() {
        return parentBelief;
    }

    @Override
    public Reference<Task> getBestSolutionRef() {
        return bestSolution;
    }

    /**
     * Get the best-so-far solution for a Question or Goal
     *
     * @return The stored Sentence or null
     */
    @Override
    public Sentence getBestSolution() {
        return dereference(bestSolution);
    }

    /**
     * Set the best-so-far solution for a Question or Goal, and report answer
     * for input question
     *
     * @param judg The solution to be remembered
     */
    @Override public void setBestSolution(final AbstractMemory memory, final Task judg) {
        InternalExperience.experienceFromBelief(memory, this, judg);
        bestSolution = reference(judg);
    }

    /**
     * flag to indicate whether this Event Task participates in tempporal induction
     */
    public void setTemporalInducting(boolean b) {
        this.temporallyInductable = b;
    }

    public boolean isTemporalInductable() {
        return temporallyInductable;
    }


    /**
     * add to this task's log history
     * useful for debugging but can also be applied to meta-analysis
     */
    public void log(String reason) {
        if (!Global.DEBUG_TASK_HISTORY)
            return;

        //TODO parameter for max history length, although task history should not grow after they are crystallized with a concept
        if (this.history == null)
            this.history = Global.newArrayList(2);

        this.history.add(reason);
    }

    public List<String> getHistory() {
        return history;
    }


    /**
     * Get the parent belief of a task
     *
     * @return The belief from which the task is derived
     */
    public Task getParentBelief() {
        return dereference(parentBelief);
    }

    /**
     * Get the parent task of a task
     *
     * @return The task from which the task is derived
     */
    public Task getParentTask() {
        return dereference(parentTask);
    }

    @Override
    public Sentence<T> name() {
        return this;
    }

    @Override
    @Deprecated
    public String toString() {
        return appendTo(null, null).toString();
    }


    /**
     * the causing Operation, or null if not applicable.
     */
    public Operation getCause() {
        return cause;
    }


    @Override
    public Task setCause(final Operation op) {
        if (op != null) {
            if (this.equals(op.getTask()))
                return this; //dont set the cause to itself
        }

        this.cause = op;

        return this;
    }

    public void setEvidentialSet(long serial) {
        setEvidentialSet(new long[]{serial});
    }


    public void setTruth(Truth t) {
        this.truth = t;
    }

    public void discountConfidence() {
        setTruth(getTruth().discountConfidence());
    }


    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(toString());
    }

    @Override
    public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        serialize(jgen, provider);
    }

}

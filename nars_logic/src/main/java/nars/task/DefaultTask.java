package nars.task;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.AbstractMemory;
import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.Item;
import nars.nal.nal7.Sequence;
import nars.nal.nal8.Operation;
import nars.op.mental.InternalExperience;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.TermMetadata;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.util.data.Util;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private char punctuation;
    /**
     * Task from which the Task is derived, or null if input
     */
    transient protected Reference<Task> parentTask; //should this be transient? we may want a Special kind of Reference that includes at least the parent's Term
    /**
     * Belief from which the Task is derived, or null if derived from a theorem
     */
    transient protected Reference<Task> parentBelief;

    public Truth truth;
    protected T term;
    transient private int hash;
    private long[] evidentialSet = null;
    private long creationTime = Stamp.TIMELESS;
    private long occurrenceTime = Stamp.ETERNAL;
    private int duration = 0;
    private boolean cyclic;
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

    private List<String> log = null;

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

    protected void setTerm(T t) {
        //if (Global.DEBUG) {
        if (Sentence.invalidSentenceTerm(t)) {
            throw new RuntimeException("Invalid sentence content term: " + t);
        }
        //}

        term = t;
    }


    public DefaultTask(T term, final char punctuation, final Truth truth, final float p, final float d, final float q, final Reference<Task> parentTask, final Reference<Task> parentBelief, final Reference<Task> solution) {
        super(p, d, q);
        //super(term, punctuation, truth, p, d, q);

        this.truth = truth;
        this.punctuation = punctuation;

        if (term instanceof Sequence) {
            this.term = (T) ((Sequence) term).cloneRemovingSuffixInterval();
        } else {
            this.term = term;
        }

        this.parentTask = parentTask;
        this.parentBelief = parentBelief;
        this.bestSolution = solution;
    }

    public DefaultTask(Sentence<T> s, Budget budget, Task parentTask, Task parentBelief) {
        this(s.getTerm(), s.getPunctuation(), s.getTruth(), budget, parentTask, parentBelief, null);
    }

    protected void setPunctuation(char punctuation) {
        this.punctuation = punctuation;
    }

    private final int getHash() {
        //stamp (evidentialset, occurrencetime), truth, term, punctuation

        int hashStamp = Util.hash(Arrays.hashCode(getEvidence()), (int) this.getOccurrenceTime());

        final int truthHash = (getTruth() != null) ? getTruth().hashCode() : 0;

        int h = (Util.hash(hashStamp, getTerm().hashCode(), truthHash) * 31) + getPunctuation();

        if (h == 0) h = 1; //reserve 0 for non-hashed

        return h;
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

    public void setTruth(Truth t) {
        if (!Objects.equals(this.truth, t)) {
            this.truth = t;
            invalidate();
        }
    }

    @Override
    final public boolean isCyclic() {
        return cyclic;
    }

    @Override
    final public void setCyclic(boolean cyclic) {
        if (Global.OVERLAP_ALLOW) cyclic = false;
        this.cyclic = cyclic;
        //TODO decide if to include cyclic ni equality and hash, then invalidate() here
    }


    @Override
    public Task<T> setEvidence(final long... evidentialSet) {
        this.evidentialSet = evidentialSet;
        invalidate();
        return this;
    }

    final public boolean isDouble() {
        return getParentBelief() != null && getParentTask() != null;
    }
    final public boolean isSingle() {
        return getParentTask() != null && getParentBelief()==null;
    }

    public Task log(List<String> historyToCopy) {
        if (!Global.DEBUG_TASK_LOG)
            return this;

        if (historyToCopy != null) {
            if (this.log == null) this.log = Global.newArrayList(historyToCopy.size());
            log.addAll(historyToCopy);
        }
        return this;
    }

    @Override
    public final char getPunctuation() {
        return punctuation;
    }

    @Override
    public final long[] getEvidence() {
        return evidentialSet;
    }

    @Override
    public final long getCreationTime() {
        return creationTime;
    }

    @Override
    public final long getOccurrenceTime() {
        return occurrenceTime;
    }

    @Override
    public final int getDuration() {
        return duration;
    }

    public Sentence<T> setCreationTime(long creationTime) {
        if ((this.creationTime <= Stamp.TIMELESS) && (this.occurrenceTime > Stamp.TIMELESS)) {
            //use the occurrence time as the delta, now that this has a "finite" creationTime
            setOccurrenceTime(this.occurrenceTime + creationTime);
        }
        if (this.creationTime != creationTime) {
            this.creationTime = creationTime;
            //does not need invalidated since creation time is not part of hash
        }
        return this;
    }

    public boolean isDeleted() {
        return !Float.isFinite(getPriority());
    }

    /**
     * call if the task was changed; re-hashes it at the end.
     * if the task was removed then this returns null
     */
    public Task normalized() {

        if (isDeleted()) {
            return null;
        }

        //dont recompute if hash isnt invalid (==0)
        if (this.hash != 0)
            return this;

        final char punc = getPunctuation();
        if (punc == 0)
            throw new RuntimeException("Punctuation must be specified before generating a default budget");

        if ((truth == null) && !((punc == Symbols.QUEST) || (punc == Symbols.QUESTION))) {
            truth = new DefaultTruth(punc);
        }


        Compound sentenceTerm = getTerm();
        if (sentenceTerm == null)
            return null;


        updateEvidence();




        /*Task t = new DefaultTask(sentenceTerm, punc,
                (truth != null) ? new DefaultTruth(truth) : null, //clone the truth so that this class can be re-used multiple times with different values to create different tasks
                getBudget(),
                getParentTask(),
                getParentBelief(),
                solutionBelief);*/


        if (!Float.isFinite(getQuality())) {
            applyDefaultBudget();
        }

        //if (this.cause != null) t.setCause(cause);
        //if (this.reason != null) t.log(reason);

        this.hash = getHash();

        return this;
    }

    protected boolean applyDefaultBudget() {
        //if (getBudget().isBudgetValid()) return true;
        if (getTruth() == null) return false;

        final char punc = getPunctuation();
        setPriority(Budget.newDefaultPriority(punc));
        setDurability(Budget.newDefaultDurability(punc));

        /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
        if (Float.isNaN(quality)) {
            setQuality(BudgetFunctions.truthToQuality(truth));
        }

        return true;
    }

    final void updateEvidence() {
        //supplying no evidence will be assigned a new serial
        //but this should only happen for input tasks (with no parent)

        if (isDouble()) {
            long[] as = getParentTask().getEvidence();
            long[] bs = getParentBelief().getEvidence();

            //temporary
            if (as == null)
                throw new RuntimeException("parentTask " + getParentTask() + " has no evidentialSet");
            if (bs == null)
                throw new RuntimeException("parentBelief " + getParentBelief() + " has no evidentialSet");

            final long[] zipped = Stamp.zip(as, bs);
            final long[] uniques = Stamp.toSetArray(zipped);
            setEvidence(uniques);

                /*if (getParentTask().isInput() || getParentBelief().isInput()) {
                    setCyclic(false);
                } else {*/
                    /*
                    <patham9> since evidental overlap is not checked on deduction, a derivation can be cyclic
                    <patham9> its on revision when it finally matters, but not whether the two parents are cyclic, but whether the combination of both evidental bases of both parents would be cyclic/have an overlap
                    <patham9> else deductive conclusions could not lead to revisions altough the overlap is only local to the parent (the deductive conclusion)
                    <patham9> revision is allowed here because the two premises to revise dont have an overlapping evidental base element
                    */

            setCyclic(
                    //boolean bothParentsCyclic =
                    ((getParentTask().isCyclic() && getParentBelief().isCyclic())
                            ||
                            //boolean overlapBetweenParents = if the sum of the two parents length is greater than the result then there was some overlap
                            (zipped.length > uniques.length))
            );

            //}

        } if (isSingle()) {
            //Single premise
            setEvidence(getParentTask().getEvidence());

            setCyclic(true); //p.isCyclic());
        }
        else {
            setCyclic(false);
        }

    }


    protected final void invalidate() {
        hash = 0;
    }

    @Override
    public Sentence setOccurrenceTime(long o) {
        if (o != occurrenceTime) {
            this.occurrenceTime = o;
            invalidate();
        }
        return this;
    }

    @Override
    public final Sentence setDuration(int d) {
        this.duration = d;
        return this;
    }

    @Override
    public final int hashCode() {
        if (hash == 0) {
            throw new RuntimeException(this + " not normalized");
        }
        return hash;
    }

    /**
     * To check whether two sentences are equal
     * Must be consistent with the values calculated in getHash()
     *
     * @param that The other sentence
     * @return Whether the two sentences have the same content
     */
    @Override
    public final boolean equals(final Object that) {
        if (this == that) return true;
        if (that instanceof Sentence) {

            if (hashCode() != that.hashCode()) return false;

            return equivalentTo((Sentence) that, true, true, true, true, false);
        }
        return false;
    }

    public final boolean equivalentTo(final Sentence that, final boolean punctuation, final boolean term, final boolean truth, final boolean stamp, final boolean creationTime) {

        if (this == that) return true;

        final char thisPunc = this.getPunctuation();

        if (term) {
            if (!equalTerms(that)) return false;
        }

        if (punctuation) {
            if (thisPunc != that.getPunctuation()) return false;
        }

        if (truth) {
            Truth thisTruth = this.getTruth();
            if (thisTruth == null) {
                //equal punctuation will ensure thatTruth is also null
            } else {
                if (!thisTruth.equals(that.getTruth())) return false;
            }
        }


        if (stamp) {
            //uniqueness includes every aspect of stamp except creation time
            //<patham9> if they are only different in creation time, then they are the same
            if (!this.equalStamp(that, true, creationTime, true))
                return false;
        }

        return true;
    }

    /**
     * Check if two stamps contains the same types of content
     * <p>
     * NOTE: hashcode will include within it the creationTime & occurrenceTime, so if those are not to be compared then avoid comparing hash
     *
     * @param s The Stamp to be compared
     * @return Whether the two have contain the same evidential base
     */
    public final boolean equalStamp(final Stamp s, final boolean evidentialSet, final boolean creationTime, final boolean occurrenceTime) {
        if (this == s) return true;

        /*if (hash && (!occurrenceTime || !evidentialSet))
            throw new RuntimeException("Hash equality test must be followed by occurenceTime and evidentialSet equality since hash incorporates them");

        if (hash)
            if (hashCode() != s.hashCode()) return false;*/
        if (creationTime)
            if (getCreationTime() != s.getCreationTime()) return false;
        if (occurrenceTime)
            if (getOccurrenceTime() != s.getOccurrenceTime()) return false;
        if (evidentialSet) {
            return Arrays.equals(getEvidence(), s.getEvidence());
        }


        return true;
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
    @Override
    public void setBestSolution(final AbstractMemory memory, final Task judg) {
        InternalExperience.experienceFromBelief(memory, this, judg);
        bestSolution = reference(judg);
    }

    /**
     * flag to indicate whether this Event Task participates in tempporal induction
     */
    public Task setTemporalInducting(boolean b) {
        this.temporallyInductable = b;
        return this;
    }

    public boolean isTemporalInductable() {
        return temporallyInductable;
    }

    /**
     * add to this task's log history
     * useful for debugging but can also be applied to meta-analysis
     */
    public void log(String reason) {
        if (!Global.DEBUG_TASK_LOG)
            return;

        //TODO parameter for max history length, although task history should not grow after they are crystallized with a concept
        if (this.log == null)
            this.log = Global.newArrayList(1);

        this.log.add(reason);
    }

    public final List<String> getLog() {
        return log;
    }


    /*
    @Override
    public void delete() {
        super.delete();
//        this.parentBelief = this.parentTask = this.bestSolution = null;
//        this.cause = null;
//        log.clear();
//        this.term = null;
//        this.truth = null;
//        this.hash = 0;
    }*/

    public void setParentTask(Task parentTask) {
        this.parentTask = reference(parentTask!=null ? parentTask.normalized() : null);
        invalidate();
    }

    public void setParentBelief(Task parentBelief) {
        this.parentBelief = reference(parentBelief!=null ? parentBelief.normalized() : null);
        invalidate();
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

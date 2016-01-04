package nars.task;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.Item;
import nars.concept.Concept;
import nars.nal.nal7.Interval;
import nars.nal.nal7.Sequence;
import nars.op.mental.Anticipate;
import nars.term.Compound;
import nars.term.Term;
import nars.term.TermMetadata;
import nars.truth.DefaultTruth;
import nars.truth.Stamp;
import nars.truth.Truth;
import nars.util.data.Util;
import nars.util.data.array.LongArrays;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static nars.Global.dereference;
import static nars.Global.reference;

/**
 * Default Task implementation
 */
@JsonSerialize(using = ToStringSerializer.class)
public class DefaultTask<T extends Compound> extends Item<Sentence<T>> implements Task<T>, Serializable, JsonSerializable {

    /** content term of this task */
    protected T term;


    private char punctuation;

    private boolean introspective_event = false;

    public Truth truth;

    public boolean executed = false;

    private long[] evidentialSet = LongArrays.EMPTY_ARRAY;

    long creationTime = Stamp.TIMELESS;
    long occurrenceTime = Stamp.ETERNAL;
    private int duration = Stamp.TIMELESS;

    /**
     * Task from which the Task is derived, or null if input
     */
    transient private Reference<Task> parentTask; //should this be transient? we may want a Special kind of Reference that includes at least the parent's Term
    /**
     * Belief from which the Task is derived, or null if derived from a theorem
     */
    transient private Reference<Task> parentBelief;


    transient private int hash;

    /**
     * TODO move to SolutionTask subclass
     * For Question and Goal: best solution found so far
     */
    transient private Reference<Task> bestSolution;


    private List log = null;



    public DefaultTask(T term, final char punctuation, final Truth truth, final Budget bv, final Task parentTask, final Task parentBelief, final Task solution) {
        this(term, punctuation, truth,
                bv.getPriority(),
                bv.getDurability(),
                bv.getQuality(),
                parentTask, parentBelief,
                solution);
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

    /** copy/clone constructor */
    public DefaultTask(Task<T> task) {
        this(task.getTerm(), task.getPunctuation(), task.getTruth(),
                task.getPriority(), task.getDurability(), task.getQuality(),
                task.getParentTaskRef(), task.getParentBeliefRef(), task.getBestSolutionRef());
    }


    protected final void setTerm(T t) {
//        //if (Global.DEBUG) {
//        if (Sentence.invalidSentenceTerm(t)) {
//            throw new RuntimeException("Invalid sentence content term: " + t);
//        }
//        //}

        term = t;
    }


    public DefaultTask(T term, final char punctuation, final Truth truth, final float p, final float d, final float q, final Reference<Task> parentTask, final Reference<Task> parentBelief, final Reference<Task> solution) {
        super(p, d, q);
        this.truth = truth;
        this.punctuation = punctuation;
        this.term = term;
        this.parentTask = parentTask;
        this.parentBelief = parentBelief;
        this.bestSolution = solution;
    }

    public boolean init(final Memory memory) {

        if (!isCommand()) {

            Task.ensureValidPunctuationAndTruth(getPunctuation(), getTruth()!=null);

            ensureValidParentTaskRef();

        }

        if (normalize()) {

            // if a task has an unperceived creationTime,
            // set it to the memory's current time here,
            // and adjust occurenceTime if it's not eternal

            if (getCreationTime() <= Stamp.TIMELESS) {
                final long now = memory.time();
                long oc = getOccurrenceTime();
                if (oc != Stamp.ETERNAL)
                    oc += now;

                setTime(now, oc);
            }

            setDuration(
                    memory.duration() //assume the default perceptual duration?
            );

            //finally, assign a unique stamp if none specified (input)
            if (getEvidence().length == 0) {
                setEvidence(memory.newStampSerial());

                //this actually means it arrived from unknown origin.
                //we'll clarify what null evidence means later.
                //if data arrives via a hardware device, can a virtual
                //task be used as the parent when it generates it?
                //doesnt everything originate from something else?
                if (log == null)
                    log("Input");
            }

            setTerm((T) memory.terms.get( term ).getTerm());

            return true;
        }

        return false;
    }

    protected final void setPunctuation(char punctuation) {
        this.punctuation = punctuation;
    }

    protected final void setIsIntrospectiveEvent(boolean isIntrospectiveEvent) {
        this.introspective_event = isIntrospectiveEvent;
    }

    public final boolean getIsIntrospectiveEvent() {
        return this.introspective_event;
    }

    /** includes: evidentialset, occurrencetime, truth, term, punctuation */
    private final int rehash() {

        final int h = Objects.hash(
                Arrays.hashCode(getEvidence()),
                getTerm(),
                getPunctuation(),
                getTruth(),
                getOccurrenceTime()
        );

        if (h == 0) return 1; //reserve 0 for non-hashed

        return h;
    }

    @Override
    public final void onConcept(final Concept c) {

        //intermval generally contains unique information that should not be replaced
        /*if (this.term instanceof TermMetadata)
            return;

        //if debug, check that they are equal..

        this.term = (T) c.getTerm(); //HACK the cast */
    }

    @Override
    public final T getTerm() {
        return term;
    }

    @Override
    public Truth getTruth() {
        return truth;
    }

    @Override
    public void setTruth(Truth t) {
        if (!Objects.equals(this.truth, t)) {
            this.truth = t;
            invalidate();
        }
    }



    @Override
    public Task<T> setEvidence(final long... evidentialSet) {
        this.evidentialSet = evidentialSet;
        invalidate();
        return this;
    }

    @Override
    final public boolean isDouble() {
        return getParentBelief() != null && getParentTask() != null;
    }
    @Override
    final public boolean isSingle() {
        return getParentBelief()==null && getParentTask()!=null ;
    }

    @Override
    public final void setDuration(int duration) {
        /*if (this.duration!=Stamp.TIMELESS)
            throw new RuntimeException(this + " has corrupted duration");*/
        if (duration < 0)
            throw new RuntimeException(this + " negative duration");

        final Term term = this.term;

        term.setDuration(duration); //HACK int<->long stuff

        final int d;
        if (term instanceof Interval) {
            d = ((Interval) term).duration(); //set the task's duration to the term's actual (expanded) duration
        }
        else {
            d = duration;
        }
        this.duration = d;
    }

    @Override
    public void log(List historyToCopy) {
        if (!Global.DEBUG_TASK_LOG)
            return;

        if (historyToCopy != null) {
            if (this.log == null) this.log = Global.newArrayList(historyToCopy.size());
            log.addAll(historyToCopy);
        }
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
    public final int duration() {
        final Term t = this.term;
        if (t instanceof Interval)
            return ((Interval)t).duration();
        return duration;
    }

    @Override
    public int compareTo(Object obj) {
        if (this == obj) return 0;

        Task o = (Task)obj;
        int tc = term.compareTo(o.getTerm());
        if (tc != 0) return tc;
        tc = Character.compare(punctuation, o.getPunctuation());
        if (tc != 0) return tc;

        if (truth!=null) {

            Truth otruth = o.getTruth();
            tc = Truth.compare(truth, otruth);
            if (tc!=0) return tc;

        }

        tc = Long.compare( getOccurrenceTime(), o.getOccurrenceTime() );
        if (tc!=0) return tc;



        long[] e1 = getEvidence();
        long[] e2 = o.getEvidence();
        return Util.compare(e1, e2);
    }

    @Override
    public final Sentence<T> setCreationTime(final long creationTime) {
        if ((this.creationTime <= Stamp.TIMELESS) && (this.occurrenceTime > Stamp.TIMELESS)) {
            //use the occurrence time as the delta, now that this has a "finite" creationTime
            setOccurrenceTime(this.occurrenceTime + creationTime);
        }
        //if (this.creationTime != creationTime) {
        this.creationTime = creationTime;
            //does not need invalidated since creation time is not part of hash
        //}
        return this;
    }


    @Override
    public final boolean isNormalized() {
        return this.hash != 0;
    }

    /**
     * call if the task was changed; re-hashes it at the end.
     * if the task was removed then this returns null
     */
    @Override
    public final boolean normalize() {

        //dont recompute if hash isnt invalid (==0)
        if (isNormalized())
            return true;

        if (isDeleted())
            return false;

        return normalizeThis();
    }

    /** actual normalization process */
    protected boolean normalizeThis() {

        final char punc = getPunctuation();
        if (punc == 0)
            throw new RuntimeException("Punctuation must be specified before generating a default budget");

        if ((truth == null) && (isJudgmentOrGoal())) {
            truth = new DefaultTruth(punc);
        }


        Compound t = getTerm();
        if (t == null)
            return false;


        if (t instanceof Sequence)  {
            long[] offset = new long[1];
            Term st = ((Sequence)t).cloneRemovingSuffixInterval(offset);
            if ((st == null) || (Sentence.invalidSentenceTerm(st)))
                return false; //it reduced to an invalid sentence term so return null
            this.term = (T)st;
            if (!isEternal())
                occurrenceTime -= offset[0];
        }

        updateEvidence();



        /** NaN quality is a signal that a budget's values need initialized */
        if (Float.isNaN(getQuality())) {
            applyDefaultBudget();
        }

        //if (this.cause != null) t.setCause(cause);
        //if (this.reason != null) t.log(reason);

        this.hash = rehash();

        return true;
    }

    protected boolean applyDefaultBudget() {
        //if (getBudget().isBudgetValid()) return true;
        if (getTruth() == null) return false;

        final char punc = getPunctuation();
        setPriority(Budget.newDefaultPriority(punc));
        setDurability(Budget.newDefaultDurability(punc));

        /** if q was not specified, and truth is, then we can calculate q from truthToQuality */
        if (Float.isNaN(getQuality())) {
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

//            setCyclic(
//                    //boolean bothParentsCyclic =
//                    ((getParentTask().isCyclic() && getParentBelief().isCyclic())
//                            ||
//                            //boolean overlapBetweenParents = if the sum of the two parents length is greater than the result then there was some overlap
//                            (zipped.length > uniques.length))
//            );

            //}

        } else if (isSingle()) {
            setEvidence(getParentTask().getEvidence());
        }


    }


    public final void invalidate() {
        hash = 0;
    }

    @Override
    public Task setOccurrenceTime(final long o) {
        if (o != occurrenceTime) {
            this.occurrenceTime = o;
            invalidate();
        }
        return this;
    }

    public static <C extends Compound> FluentTask make(C t) {
        t.normalizeDestructively();
        Compound u = Task.taskable(t);
        if (u == null)
            return null;

        FluentTask x = make();

        x.setTerm(u);

        return x;
    }

    public static FluentTask make() {
        return new FluentTask();
    }

    @Override
    public final DefaultTask<T> setEternal() {
        setOccurrenceTime(Stamp.ETERNAL);
        return this;
    }


    @Override
    public final int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            return this.hash = rehash();
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

            //hash test has probably already occurred, coming from a HashMap
            //if (hashCode() != that.hashCode()) return false;

            return equivalentTo((Sentence) that, true, true, true, true, false);
        }
        return false;
    }

    @Override
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


    @Override
    public Reference<Task> getParentTaskRef() {
        return parentTask;
    }

    @Override
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
    public Task getBestSolution() {
        return dereference(bestSolution);
    }

    /**
     * Set the best-so-far solution for a Question or Goal, and report answer
     * for input question
     *
     * @param judg The solution to be remembered
     */
    @Override
    public final void setBestSolution(final Task judg, final Memory memory) {
        if(Global.TESTING && this.isInput()) {
            System.out.println(this);
            System.out.println("has new best solution:");
            System.out.println(judg);
        }
        bestSolution = reference(judg);
        //InternalExperience.experienceFromBelief(memory, this, judg);
    }



    /**
     * append an entry to this task's log history
     * useful for debugging but can also be applied to meta-analysis
     * ex: an entry might be a String describing a change in the story/history
     * of the Task and the reason for it.
     */
    @Override
    public final void log(final Object entry) {
        if (!Global.DEBUG_TASK_LOG)
            return;

        //TODO parameter for max history length, although task history should not grow after they are crystallized with a concept
        if (this.log == null)
            this.log = Global.newArrayList(1);

        this.log.add(entry);
    }

    @Override
    public final List getLog() {
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

    public final void setParentTask(Task parentTask) {
        this.parentTask = reference(parentTask);
        invalidate();
    }

    public final void setParentBelief(Task parentBelief) {
        this.parentBelief = reference(parentBelief);
        invalidate();
    }

    /**
     * Get the parent belief of a task
     *
     * @return The belief from which the task is derived
     */
    @Override
    final public Task getParentBelief() {
        return dereference(parentBelief);
    }



    @Override
    final public Sentence<T> name() {
        return this;
    }

    @Override
    @Deprecated
    public String toString() {
        return appendTo(null, null).toString();
    }


    @Override
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

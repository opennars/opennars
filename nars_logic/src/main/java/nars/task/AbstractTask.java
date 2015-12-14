package nars.task;

import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.budget.Item;
import nars.concept.Concept;
import nars.nal.nal7.Interval;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.Tense;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Stamp;
import nars.truth.Truth;
import nars.util.data.Util;
import nars.util.data.array.LongArrays;

import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static nars.Global.dereference;
import static nars.Global.reference;

/**
 * Default Task implementation
 * TODO move all mutable methods to MutableTask and call this ImmutableTask
 */
public abstract class AbstractTask extends Item<Task>
        implements Task, Temporal {

    /** content term of this task */
    private Compound term;


    private char punctuation;

    private Truth truth;

    private long[] evidentialSet = LongArrays.EMPTY_ARRAY;

    private long creationTime = Tense.TIMELESS;
    private long occurrenceTime = Tense.ETERNAL;
    private int duration = Tense.TIMELESS;

    /**
     * Task from which the Task is derived, or null if input
     */
    private transient Reference<Task> parentTask; //should this be transient? we may want a Special kind of Reference that includes at least the parent's Term
    /**
     * Belief from which the Task is derived, or null if derived from a theorem
     */
    private transient Reference<Task> parentBelief;


    private transient int hash;

    /**
     * TODO move to SolutionTask subclass
     * For Question and Goal: best solution found so far
     */
    private transient Reference<Task> bestSolution;


    private List log = null;

    /** flag used for anticipatable derivation */
    protected boolean anticipate = false;


    public AbstractTask(Compound term, char punctuation, Truth truth, Budget bv, Task parentTask, Task parentBelief, Task solution) {
        this(term, punctuation, truth,
                bv.getPriority(),
                bv.getDurability(),
                bv.getQuality(),
                parentTask, parentBelief,
                solution);
    }

    public AbstractTask(Compound term, char punc, Truth truth, float p, float d, float q) {
        this(term, punc, truth, p, d, q, (Task) null, null, null);
    }

    public AbstractTask(Compound term, char punc, Truth truth, float p, float d, float q, Task parentTask, Task parentBelief, Task solution) {
        this(term, punc, truth,
                p, d, q,
                Global.reference(parentTask),
                reference(parentBelief),
                reference(solution)
        );
    }

    /** copy/clone constructor */
    public AbstractTask(Task task) {
        this(task.getTerm(), task.getPunctuation(), task.getTruth(),
                task.getPriority(), task.getDurability(), task.getQuality(),
                task.getParentTaskRef(), task.getParentBeliefRef(), task.getBestSolutionRef());
    }

    @Override
    public Task getTask() {
        return this;
    }

    void setTime(long creation, long occurrence) {
        setCreationTime(creation);
        setOccurrenceTime(occurrence);
    }

    protected final void setTerm(Compound t) {
        if (term!=t) {
            term = t;
            invalidate();
        }
    }


    public AbstractTask(Compound term, char punctuation, Truth truth, float p, float d, float q, Reference<Task> parentTask, Reference<Task> parentBelief, Reference<Task> solution) {
        super(p, d, q);
        this.truth = truth;
        this.punctuation = punctuation;
        this.term = term;
        this.parentTask = parentTask;
        this.parentBelief = parentBelief;
        bestSolution = solution;
    }

    @Override
    public final Task normalize(Memory memory) {

//        if (hash != 0) {
//            /* already validated */
//            return this;
//        }

        if (isDeleted())
            return null;

        Compound t = getTerm();
        if (!t.levelValid( memory.nal() ))
            return null;


        char punc = getPunctuation();
        if (punc == 0)
            throw new RuntimeException("Punctuation must be specified before generating a default budget");

        if (!isCommand()) {
            ensureValidParentTaskRef();
        }

        //noinspection IfStatementWithTooManyBranches
        if (isJudgmentOrGoal()) {

        } else if (isQuestOrQuestion()) {
            if (truth!=null)
                throw new RuntimeException("quests and questions must have null truth");
        } else if (isCommand()) {
            //..
        } else {
            throw new RuntimeException("invalid punctuation: " + punc);
        }

        if (t == null) throw new RuntimeException("null term");
        Term tNorm = t.normalized();
        if (tNorm == null)
            throw new RuntimeException("term not normalized");



        t = Task.validTaskTerm(tNorm);
        if (t == null) {
            throw new RuntimeException("invalid sentence term: " + tNorm);
        }

        updateEvidence();

        if (truth == null && isJudgmentOrGoal()) {
            //apply the default truth value for specified punctuation
            truth = new DefaultTruth(punc, memory);
        }


        // if a task has an unperceived creationTime,
        // set it to the memory's current time here,
        // and adjust occurenceTime if it's not eternal

        if (getCreationTime() <= Tense.TIMELESS) {
            long now = memory.time();
            long oc = getOccurrenceTime();
            if (oc != Tense.ETERNAL)
                oc += now;

            setTime(now, oc);
        }

        if (t instanceof Sequence)  {
            long[] offset = new long[1];
            Term st = ((Sequence)t).cloneRemovingSuffixInterval(offset);
            t = Task.validTaskTerm(st);
            if (t == null)
                return null; //it was reduced to something which is invalid as a task term
            if (!isEternal())
                occurrenceTime -= offset[0];
        }



        //---- VALID TASK BEYOND THIS POINT

        /** NaN quality is a signal that a budget's values need initialized */
        if (Float.isNaN(getQuality())) {
            //HACK for now just assume that only MutableTask supports unbudgeted input
            memory.applyDefaultBudget((MutableTask)this);
        }

        //obtain shared copy of term
        setTerm((Compound)memory.index.getTerm(t));

        setDuration(
            memory.duration() //assume the default perceptual duration?
        );


        //finally, assign a unique stamp if none specified (input)
        if (getEvidence().length== 0) {
            setEvidence(memory.newStampSerial());

            //this actually means it arrived from unknown origin.
            //we'll clarify what null evidence means later.
            //if data arrives via a hardware device, can a virtual
            //task be used as the parent when it generates it?
            //doesnt everything originate from something else?
            if (log == null)
                log("Input");
        }

        hash = rehash();

        onNormalized(memory);

        return this;
    }

    /** can be overridden in subclasses to handle this event */
    protected void onNormalized(Memory m) {

    }

    protected final void setPunctuation(char punctuation) {
        if (this.punctuation!=punctuation) {
            this.punctuation = punctuation;
            invalidate();
        }
    }

    /** includes: evidentialset, occurrencetime, truth, term, punctuation */
    private final int rehash() {

        int h = Objects.hash(
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
    public final void onConcept(Concept c) {

        //intermval generally contains unique information that should not be replaced
        //if (term instanceof TermMetadata)
            //return;

        //if debug, check that they are equal..
        //term = (Compound) c.getTerm(); //HACK the cast
    }

    @Override
    public final Compound getTerm() {
        return term;
    }

    @Override
    public Truth getTruth() {
        return truth;
    }

    @Override
    public void setTruth(Truth t) {
        if (!Objects.equals(truth, t)) {
            truth = t;
            invalidate();
        }
    }

    @Override
    public final boolean isAnticipated() {
        return isJudgmentOrGoal() && (anticipate || isInput());
    }

    @Override
    public Task setEvidence(long... evidentialSet) {
        this.evidentialSet = evidentialSet;
        invalidate();
        return this;
    }

    @Override
    public final boolean isDouble() {
        return getParentBelief() != null && getParentTask() != null;
    }
    @Override
    public final boolean isSingle() {
        return getParentBelief()==null && getParentTask()!=null ;
    }

    @Override
    public final void setDuration(int duration) {
        /*if (this.duration!=Stamp.TIMELESS)
            throw new RuntimeException(this + " has corrupted duration");*/
        if (duration < 0)
            throw new RuntimeException(this + " negative duration");

        Term term = this.term;

        term.setDuration(duration); //HACK int<->long stuff

        int d;
        d = term instanceof Interval ? ((Interval) term).duration() : duration;
        this.duration = d;
    }

    @Override
    public void log(List historyToCopy) {
        if (!Global.DEBUG_TASK_LOG)
            return;

        if (historyToCopy != null) {
            if (log == null) log = Global.newArrayList(historyToCopy.size());
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
        Term t = term;
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
            tc = Truth.compare(otruth, truth);
            if (tc!=0) return tc;

        }

        tc = Long.compare( getOccurrenceTime(),
                o.getOccurrenceTime() );
        if (tc!=0) return tc;



        long[] e1 = getEvidence();
        long[] e2 = o.getEvidence();
        return Util.compare(e1, e2);
    }

    @Override
    public final Task setCreationTime(long creationTime) {
        if ((this.creationTime <= Tense.TIMELESS) && (occurrenceTime > Tense.TIMELESS)) {
            //use the occurrence time as the delta, now that this has a "finite" creationTime
            setOccurrenceTime(occurrenceTime + creationTime);
        }
        //if (this.creationTime != creationTime) {
        this.creationTime = creationTime;
            //does not need invalidated since creation time is not part of hash
        //}
        return this;
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

            long[] zipped = Stamp.zip(as, bs);
            long[] uniques = Stamp.toSetArray(zipped);

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
    public void setOccurrenceTime(long o) {
        if (o != occurrenceTime) {
            occurrenceTime = o;
            invalidate();
        }
    }

    @Override
    public final void setEternal() {
        setOccurrenceTime(Tense.ETERNAL);
    }


    @Override
    public final int hashCode() {
        if (hash == 0) {
            rehash();
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
    public final boolean equals(Object that) {
        if (this == that) return true;
        if (that instanceof Task) {

            //hash test has probably already occurred, coming from a HashMap
            //if (hashCode() != that.hashCode()) return false;

            return equivalentTo((Task) that, true, true, true, true, false);
        }
        return false;
    }

    @Override
    public final boolean equivalentTo(Task that, boolean punctuation, boolean term, boolean truth, boolean stamp, boolean creationTime) {

        if (this == that) return true;

        char thisPunc = getPunctuation();

        if (stamp) {
            //uniqueness includes every aspect of stamp except creation time
            //<patham9> if they are only different in creation time, then they are the same
            if (!equalStamp(that, true, creationTime, true))
                return false;
        }

        if (truth) {
            Truth thisTruth = getTruth();
            if (thisTruth == null) {
                //equal punctuation will ensure thatTruth is also null
            } else {
                if (!thisTruth.equals(that.getTruth())) return false;
            }
        }


        if (term) {
            if (!this.term.equals(that.getTerm())) return false;
        }

        if (punctuation) {
            if (thisPunc != that.getPunctuation()) return false;
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
    public final boolean equalStamp(Task s, boolean evidentialSet, boolean creationTime, boolean occurrenceTime) {
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
    public final void setBestSolution(Task judg, Memory memory) {
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
    public final void log(Object entry) {
        if (!Global.DEBUG_TASK_LOG)
            return;

        //TODO parameter for max history length, although task history should not grow after they are crystallized with a concept
        if (log == null)
            log = Global.newArrayList(1);

        log.add(entry);
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
    }
    public final void setParents(Reference<Task> parentTask, Reference<Task> parentBelief) {
        this.parentTask = parentTask;
        this.parentBelief = parentBelief;
    }

    public final void setParentBelief(Task parentBelief) {
        this.parentBelief = reference(parentBelief);
    }

    /**
     * Get the parent belief of a task
     *
     * @return The belief from which the task is derived
     */
    @Override
    public final Task getParentBelief() {
        return dereference(parentBelief);
    }



    @Override
    public final Task name() {
        return this;
    }

    @Override
    @Deprecated
    public String toString() {
        return appendTo(null, null).toString();
    }

    @Override
    public long start() {
        return occurrenceTime;
    }

    @Override
    public long end() {
        return occurrenceTime + duration;
    }

    @Override
    public void discountConfidence() {
        setTruth(getTruth().discountConfidence());
    }




}

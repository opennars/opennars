/*
 * Task.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.task;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.AbstractMemory;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.budget.Budgeted;
import nars.budget.Item;
import nars.nal.nal7.Sequence;
import nars.nal.nal8.ImmediateOperation;
import nars.nal.nal8.Operation;
import nars.op.mental.InternalExperience;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.TermMetadata;
import nars.truth.ProjectedTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;
import nars.truth.Truthed;
import nars.util.data.Util;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.*;

import static nars.Global.dereference;
import static nars.Global.reference;

/**
 * A task to be processed, consists of a Sentence and a BudgetValue.
 * A task references its parent and an optional causal factor (usually an Operation instance).  These are implemented as WeakReference to allow forgetting via the
 * garbage collection process.  Otherwise, Task ancestry would grow unbounded,
 * violating the assumption of insufficient resources (AIKR).
 * <p>
 * TODO decide if the Sentence fields need to be Reference<> also
 */
public interface Task<T extends Compound> extends Sentence<T>, Budgeted, Truthed {


    @JsonSerialize(using = ToStringSerializer.class)
    public static class DefaultTask<T extends Compound> extends Item<Sentence<T>> implements Task<T>, Serializable, JsonSerializable {
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
         * The sentence of the Task
         */
        @Deprecated public final Sentence<T> sentence = this;

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
        private Reference<Sentence> bestSolution;

        /**
         * TODO move to DesiredTask subclass
         * causal factor if executed; an instance of Operation
         */
        private Operation cause;

        private List<String> history = null;

        /** indicates this Task can be used in Temporal induction
         */
        private boolean temporallyInductable = true;

        public DefaultTask(T term, final char punctuation, final Truth truth, final Budget bv, final Task parentTask, final Task parentBelief, final Sentence solution) {
            this(term, punctuation, truth,
                    bv != null ? bv.getPriority() : 0,
                    bv != null ? bv.getDurability() : 0,
                    bv != null ? bv.getQuality() : 0,
                    parentTask, parentBelief, solution);
        }

        public DefaultTask(T term, final char punc, final Truth truth, final float p, final float d, final float q) {
            this(term, punc, truth, p, d, q, (Task)null, null, null);
        }

        public DefaultTask(T term, final char punc, final Truth truth, final float p, final float d, final float q, final Task parentTask, final Task parentBelief, Sentence solution) {
            this(term, punc, truth,
                    p,d,q,
                    Global.reference(parentTask),
                    reference(parentBelief),
                    reference(solution)
            );
        }

        public DefaultTask(T term, final char punctuation, final Truth truth, final float p, final float d, final float q, final Reference<Task> parentTask, final Reference<Task> parentBelief, final Reference<Sentence> solution) {
            super(p, d, q);
            //super(term, punctuation, truth, p, d, q);

            this.punctuation = punctuation;

            boolean isQuestionOrQuest = isQuestion() || isQuest();
            if (isQuestionOrQuest) {
                this.truth = null;
            }
            else if ( truth == null ) {
                throw new RuntimeException("Judgment and Goal sentences require non-null truth value");
            }
            else {
                this.truth = truth;
            }

            if (term instanceof Sequence) {
                this.term = (T) ((Sequence)term).cloneRemovingSuffixInterval();
            }
            else {
                this.term = term;
            }

            invalidateHash();


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

        private int getHash() {
            //stamp (evidentialset, occurrencetime), truth, term, punctuation

            int hashStamp = Util.hash(Arrays.hashCode(getEvidentialSet()), (int)this.getOccurrenceTime());

            final int truthHash = (getTruth() != null) ? getTruth().hashCode() : 0;

            return (Util.hash(hashStamp, getTerm().hashCode(), truthHash) * 31) + getPunctuation();
        }

        public void setTermShared(final T equivalentInstance) {{

            //intermval generally contains unique information that should not be replaced
            if (this.term instanceof TermMetadata)
                return;

            //if debug, check that they are equal..

            this.term = equivalentInstance;
        }

        @Override
        public Sentence setCreationTime(long creationTime) {
            if ((this.creationTime <= Stamp.TIMELESS) && (this.occurrenceTime>Stamp.TIMELESS)) {
                //use the occurrence time as the delta, now that this has a "finite" creationTime
                this.occurrenceTime = this.occurrenceTime + creationTime;
            }
            this.creationTime = creationTime;
            invalidateHash();
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
                return equivalentTo((Sentence)that, true, true, true, true, false);
            }
            return false;
        }


    }




//    /**
//     * Constructor for an activated task
//     *
//     * @param s            The sentence
//     * @param b            The budget
//     * @param parentTask   The task from which this new task is derived
//     * @param parentBelief The belief from which this new task is derived
//     * @param solution     The belief to be used in future logic
//     */
//    public Task(final Sentence<T> s, final Budget b, final Task parentTask, final Sentence parentBelief, final Sentence solution) {
//        this(s, b, parentTask == null ? null : Global.reference(parentTask), parentBelief, solution);
//    }
//
//    public Task(T term, char punc, Truth truth, AbstractStamper stamp, final Budget b, final Task parentTask, final Sentence parentBelief, final Sentence solution) {
//        this(new Sentence(term, punc, truth, stamp), b, parentTask == null ? null : Global.reference(parentTask), parentBelief, solution);
//    }


//    @Override
//    public Task clone() {
//
//        if (sentence == null)
//            return this;
//
//        return new Task(sentence.clone(), this, parentTask, parentBelief, bestSolution);
//    }

//    public <X extends Compound> Task<X> clone(final Sentence<X> replacedSentence) {
//        return new Task(replacedSentence, this, parentTask, parentBelief, bestSolution);
//    }

    /** clones this Task with a new Term */
    @Override
    public <X extends Compound> Task<X> clone(final X t) {
        return clone(t, true);
    }

    public <X extends Compound> Task<X> clone(X t, boolean cloneEvenIfTruthEqual) {
        return clone(t, getTruth(), cloneEvenIfTruthEqual);
    }

    public Task cloneEternal() {
        return clone(getTerm(), TruthFunctions.eternalize(getTruth()), Stamp.ETERNAL);
    }

    public <X extends Compound> Task<X> clone(X t, Truth newTruth) {
        return clone(t, newTruth, true);
    }

    public Task clone(long newOccurrenceTime) {
        return clone(getTerm(), getTruth(), newOccurrenceTime);
    }

    /** clones this Task with a new Term and truth  */
    public Task clone(Compound newTerm, Truth newTruth, boolean cloneEvenIfTruthEqual) {
        return clone(newTerm, newTruth, getOccurrenceTime());
    }

    public <X extends Compound> Task<X> clone(X t, Truth newTruth, long occ) {
        return clone(t, newTruth, occ, true);
    }

    public Task clone(Compound t, Truth newTruth, long occ, boolean cloneEvenIfTruthEqual) {
        if (newTruth instanceof ProjectedTruth) {
            long target = ((ProjectedTruth) newTruth).getTargetTime();
            if (occ!=target) {
                cloneEvenIfTruthEqual = true;
                occ = target;
            }
        }

        if (!cloneEvenIfTruthEqual) {
            if (occ == getOccurrenceTime() && getTruth().equals(newTruth) && getTerm().equals(t))
                return this;
        }

        Task tt = new Task(t, getPunctuation(), newTruth,
                getPriority(), getDurability(), getQuality(),
                parentTask, parentBelief, bestSolution
        );
        tt.setTemporalInducting(isTemporalInductable());
        tt.setCause(getCause());

        //tt.setLastForgetTime(getLastForgetTime());

        tt.setEvidentialSet(getEvidentialSet());
        tt.setCyclic(isCyclic());

        tt.setCreationTime(getCreationTime());
        tt.setOccurrenceTime(occ);
        tt.log(getHistory());
        return tt;
    }

    /** clones this Task with a new truth */
    public Task<T> clone(Truth newTruth, boolean cloneEvenIfTruthEqual) {
        if (!cloneEvenIfTruthEqual) {
            if (getTruth().equals(newTruth)) return this;
        }
        return clone(getTerm(), newTruth, getOccurrenceTime());
    }



//    @Override
//    public boolean equals(final Object obj) {
//        if (obj == this) return true;
//        if (obj instanceof Sentence) {
//            Task t = (Task) obj;
//            return super.equals(t);// && equalParents(t);
//        }
//        return false;
//    }

    public boolean equalParents(Task t) {
        Task p = getParentTask();
        Task tp = t.getParentTask();
        if (p == null) {
            return (tp == null);
        } else {
            return p.equals(tp);
        }
    }

//    private int parentHash() {
//        Task parent = getParentTask();
//        if (parent!=null)
//            return parent.hashCode();
//        return 0;
//    }


    /**
     * Check if a Task is a direct input
     *
     * @return Whether the Task is derived from another task
     */
    @Override
    public boolean isInput() {

        return parentTask == null && cause == null;
    }

//    public boolean aboveThreshold() {
//        return budget.aboveThreshold();
//    }
/*    public boolean aboveThreshold(float additionalPriority) {
        return budget.aboveThreshold(additionalPriority);
    }*/

    /**
     * Check if a Task is derived by a StructuralRule
     *
     * @return Whether the Task is derived by a StructuralRule
     */
//    public boolean isStructural() {
//        return (parentBelief == null) && (parentTask != null);
//    }


    /**
     * Get the best-so-far solution for a Question or Goal
     *
     * @return The stored Sentence or null
     */
    public Sentence getBestSolution() {
        return dereference(bestSolution);
    }

    /**
     * Set the best-so-far solution for a Question or Goal, and report answer
     * for input question
     *
     * @param judg The solution to be remembered
     */
    public void setBestSolution(final AbstractMemory memory, final Sentence judg) {
        InternalExperience.experienceFromBelief(memory, this, judg);
        bestSolution = reference(judg);
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
    @Deprecated
    public String toString() {
        return appendTo(null,null).toString();
    }


    public StringBuilder toString(@Nullable Memory memory) {
        return appendTo(null, memory);
    }

    @Override
    public StringBuilder appendTo(StringBuilder sb, @Nullable Memory memory) {
        if (sb == null) sb = new StringBuilder();
        return toString(sb, memory, false);
    }

    public boolean hasParent(Task t) {
        if (getParentTask() == null)
            return false;
        Task p = getParentTask();
        do {
            Task n = p.getParentTask();
            if (n == null) break;
            if (n.equals(t))
                return true;
            p = n;
        } while (true);
        return false;
    }

    public Task getRootTask() {
        if (getParentTask() == null) {
            return null;
        }
        Task p = getParentTask();
        do {
            Task n = p.getParentTask();
            if (n == null) break;
            p = n;
        } while (true);
        return p;
    }

    /**
     * generally, op will be an Operation instance
     */
    public Task setCause(final Operation op) {
        if (op!=null) {
            if (this.equals(op.getTask()))
                return this; //dont set the cause to itself
        }

        this.cause = op;

        return this;
    }

    /**
     * the causing Operation, or null if not applicable.
     */
    public Operation getCause() {
        return cause;
    }

    public String getExplanation() {
        StringBuilder sb = new StringBuilder();
        return getExplanation(sb).toString();
    }

    public StringBuilder getExplanation(StringBuilder temporary) {
        temporary.setLength(0);
        getExplanation(this, 0, temporary);
        return temporary;
    }

    protected static void getExplanation(Task task, int indent, StringBuilder sb) {
        //TODO StringBuilder

        for (int i = 0; i < indent; i++)
            sb.append("  ");

        task.appendTo(sb).append(" history=").append(task.getHistory());

        if (task.getCause() != null)
            sb.append(" cause=").append(task.getCause());

        if (task.getBestSolution() != null) {
            if (!task.getTerm().equals(task.getBestSolution().getTerm())) {
                sb.append(" solution=");
                task.getBestSolution().appendTo(sb);
            }
        }

        Task pt = task.getParentTask();

        Sentence pb = task.getParentBelief();
        if (pb != null) {
            if (pt != null && pb.equals(pt)) {

            } else {
                sb.append(" parentBelief=");
                task.getParentBelief().appendTo(sb);
            }
        }
        sb.append('\n');

        if (pt != null) {
            getExplanation(pt, indent + 1, sb);
        }
    }

    public Truth getDesire() {
        return getTruth();
    }


//    /**
//     * Get a String representation of the Task
//     *
//     * @return The Task as a String
//     */
//    @Override
//    public String toStringLong() {
//        final StringBuilder s = new StringBuilder();
//        s.append(super.toString()).append(' ').append(sentence.stamp.name());
//
//        Task pt = getParentTask();
//        if (pt != null) {
//            s.append("  \n from task: ").append(pt.toStringExternal());
//            if (parentBelief != null) {
//                s.append("  \n from belief: ").append(parentBelief.toString());
//            }
//        }
//        if (bestSolution != null) {
//            s.append("  \n solution: ").append(bestSolution.toString());
//        }
//        return s.toString();
//    }


//    /** returns the goal term for this task, which may be either the predicate of a forward implication,
//     * an operation.  if neither, returns null      */
//    public Term getGoalTerm() {
//        Term t = getContent();
//        if (t instanceof Implication) {
//            Implication i = (Implication)t;
//            if (i.getTemporalOrder() == TemporalRules.ORDER_FORWARD)
//                return i.getPredicate();
//            else if (i.getTemporalOrder() == TemporalRules.ORDER_BACKWARD) {
//                throw new RuntimeException("Term getGoal reversed");
//            }
//        }
//        else if (t instanceof Operation)
//            return t;
//        else if (Executive.isSequenceConjunction(t))
//            return t;
//        
//        return null;
//    }
//


    /**
     * signaling that the Task has ended or discarded
     */
    @Override
    public void delete() {

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


    public static Set<Truthed> getSentences(Iterable<Task> tasks) {


        int size;

        if (tasks instanceof Collection)
            size = ((Collection)tasks).size();
        else
            size = 2;

        Set<Truthed> s = Global.newHashSet(size);
        for (Task t : tasks)
            s.add(t);
        return s;
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
     * a task is considered amnesiac (origin not rememebered) if its parent task has been forgotten (garbage collected via a soft/weakref)
     */
    public boolean isAmnesiac() {
        return !isInput() && getParentTask() == null;
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

    public boolean executeIfImmediate(Memory memory) {
        return false;
    }


    public boolean perceivable(final Memory memory) {
        if (!summaryGreaterOrEqual(memory.param.perceptThreshold))
            return false;


        //confidence threshold filter
        if (getTruth()!=null) {
            if (getTruth().getConfidence() < memory.param.confidenceThreshold.floatValue())
                return false;
        }
        if (!(this instanceof ImmediateOperation.ImmediateTask) && getTerm() == null) {
            throw new RuntimeException(this + " null term");
            //return false;
        }

        if (getEvidentialSet() == null)
            setEvidentialSet(memory.newStampSerial());

        //if a task has an unperceived creationTime,
        // set it to the memory's current time here,
        // and adjust occurenceTime if it's not eternal

        if (getCreationTime() <= Stamp.TIMELESS) {
            final long now = memory.time();
            long oc = getOccurrenceTime();
            if (oc != Stamp.ETERNAL)
                oc += now;

            setTime(now, oc);
        }

        if (getDuration() == 0)
            setDuration(memory.duration());

        return true;
    }

    @Override
    public Sentence<T> getSentence() {
        return this;
    }



    public Task projectTask(final long targetTime, final long currentTime) {

        final ProjectedTruth t = projection(targetTime, currentTime);

        return clone(getTerm(), t, t.getTargetTime());
    }




    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(toString());
    }

    @Override
    public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        serialize(jgen, provider);
    }

    @Override
    public int getTemporalOrder() {
        return getTerm().getTemporalOrder();
    }


    public void setEvidentialSet(long serial) {
        setEvidentialSet(new long[] { serial } );
    }

    public void setTruth(Truth t) {
        this.truth = t;
    }

    public void discountConfidence() {
        setTruth(getTruth().discountConfidence());
    }



}

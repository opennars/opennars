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
package nars.nal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.io.in.Input;
import nars.nal.nal8.Operation;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.StampEvidence;
import nars.nal.task.TaskSeed;
import nars.nal.term.Compound;
import nars.nal.term.Termed;
import nars.op.mental.InternalExperience;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
@JsonSerialize(using = ToStringSerializer.class)
public class Task<T extends Compound> extends Sentence<T> implements Termed, Budget.Budgetable, Truthed, Sentenced, Serializable, JsonSerializable, StampEvidence, Input {


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
    transient public final Reference<Sentence> parentBelief;


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



//    public Task(final Sentence s, final Budget b) {
//        this(s, b, (Task) null, null, null);
//    }
//
//    public Task(final Sentence s, final Budget b, final Task parentTask) {
//        this(s, b, parentTask, null);
//    }
//
//    protected Task() {
//        this(null, null);
//    }


    public Task(T term, final char punctuation, final Truth truth, final Budget bv, final Task parentTask, final Sentence parentBelief, final Sentence solution) {
        this(term, punctuation, truth,
                bv != null ? bv.getPriority() : 0,
                bv != null ? bv.getDurability() : 0,
                bv != null ? bv.getQuality() : 0,
                parentTask, parentBelief, solution);
    }

    public Task(T term, final char punc, final Truth truth, final float p, final float d, final float q) {
        this(term, punc, truth, p, d, q, (Task)null, null, null);
    }

    public Task(T term, final char punc, final Truth truth, final float p, final float d, final float q, final Task parentTask, final Sentence parentBelief, Sentence solution) {
        this(term, punc, truth,
                p,d,q,
                reference(parentTask),
                reference(parentBelief),
                reference(solution)
        );
    }
    public Task(T term, final char punctuation, final Truth truth, final float p, final float d, final float q, final Reference<Task> parentTask, final Reference<Sentence> parentBelief, final Reference<Sentence> solution) {
        super(term, punctuation, truth, p, d, q);


        this.parentTask = parentTask;


        if (parentTask == null)
            addHistory("Input");

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


    }

    public Task(Sentence<T> s, Budget budget, Task parentTask, Sentence parentBelief) {
        this(s.getTerm(), s.punctuation, s.truth, budget, parentTask, parentBelief, null);
    }

    protected Task(char punctuation) {
        super(punctuation);
        this.parentTask = null;
        this.parentBelief = null;
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
    public <X extends Compound> Task<X> clone(X t) {
        return clone(t, getTruth());
    }

    /** clones this Task with a new Term and truth  */
    public <X extends Compound> Task<X> clone(X t, Truth newTruth) {
        return clone(t, newTruth, getOccurrenceTime());
    }

    public <X extends Compound> Task<X> clone(X t, Truth newTruth, long occ) {
        Task tt = new Task(t, getPunctuation(), newTruth,
                getPriority(), getDurability(), getQuality(),
                parentTask, parentBelief, bestSolution
        );
        tt.setTemporalInducting(isTemporalInductable());
        tt.setCyclic(isCyclic());
        tt.setCause(getCause());
        tt.setRevisible(isRevisible());
        tt.setLastForgetTime(getLastForgetTime());
        tt.setEvidentialSet(getEvidentialSet());
        tt.setCreationTime(getCreationTime());
        tt.setOccurrenceTime(occ);
        return tt;
    }

    /** clones this Task with a new truth */
    public Task<T> clone(Truth newTruth) {
        return clone(getTerm(), newTruth);
    }

    @Override
    public Sentence name() {
        return sentence;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Task) {
            Task t = (Task) obj;
            return super.equals(t);// && equalParents(t);
        }
        return false;
    }

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
    public void setBestSolution(final Memory memory, final Sentence judg) {
        InternalExperience.experienceFromBelief(memory, this, judg);
        bestSolution = reference(judg);
    }

    /**
     * Get the parent belief of a task
     *
     * @return The belief from which the task is derived
     */
    public Sentence getParentBelief() {
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
        return appendToString(null,null).toString();
    }


    public StringBuilder toString(Memory memory) {
        return appendToString(null, memory);
    }

    public StringBuilder appendToString(StringBuilder sb, Memory memory) {
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
        getExplanation(this, 0, sb);
        return sb.toString();
    }

    protected static void getExplanation(Task task, int indent, StringBuilder sb) {
        //TODO StringBuilder

        for (int i = 0; i < indent; i++)
            sb.append("  ");

        task.appendToString(sb, null).append(" history=").append(task.getHistory());

        if (task.getCause() != null)
            sb.append(" cause=").append(task.getCause());
        if (task.getBestSolution() != null) {
            if (!task.getTerm().equals(task.getBestSolution().term))
                sb.append(" solution=").append(task.getBestSolution());
        }

        Task pt = task.getParentTask();

        Sentence pb = task.getParentBelief();
        if (pb != null) {
            if (pt != null && pb.equals(pt.sentence)) {

            } else {
                sb.append(" parentBelief=").append(task.getParentBelief()).append(task.getParentBelief().getStamp());
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


    public static Set<Truthed> getSentences(Collection<Task> tasks) {
        Set<Truthed> s = Global.newHashSet(tasks.size());
        for (Task t : tasks)
            s.add(t);
        return s;
    }


    /**
     * optional list of strings explaining the reasons that make up this task's [hi-]story.
     * useful for debugging but can also be applied to meta-analysis
     */
    public void addHistory(String reason) {
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


    public char getPunctuation() {
        return punctuation;
    }


    /**
     * a task is considered amnesiac (origin not rememebered) if its parent task has been forgotten (garbage collected via a soft/weakref)
     */
    public boolean isAmnesiac() {
        return !isInput() && getParentTask() == null;
    }


    public Task addHistory(List<String> historyToCopy) {
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


        //if a task has an unperceived creationTime,
        // set it to the memory's current time here,
        // and adjust occurenceTime if it's not eternal

        if (getCreationTime() == Stamp.UNPERCEIVED) {
            final long now = memory.time();
            long oc = getOccurrenceTime();
            if (oc != Stamp.ETERNAL)
                oc += now;

            setTime(now, oc);
        }

        return true;
    }

    @Override
    public Sentence<T> getSentence() {
        return this;
    }



    public TaskSeed<T> newChild(Memory memory) {
        return new TaskSeed(memory, this).parent(this).budget(this);
    }

    public Task projection(Memory m, final long targetTime, final long currentTime) {

        final Truth newTruth = projection(targetTime, currentTime);

        final boolean eternalizing = (newTruth instanceof TruthFunctions.EternalizedTruthValue);

        long occ = eternalizing ? Stamp.ETERNAL : targetTime;

        return clone(getTerm(), newTruth, occ);
    }

    @Override
    public Task get() {
        return this;
    }

    @Override
    public float getAttention() {
        return 1.0f;
    }

    @Override
    public void serialize(JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeString(toString());
    }

    @Override
    public void serializeWithType(JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
        serialize(jgen, provider);
    }

    public int getTemporalOrder() {
        return getTerm().getTemporalOrder();
    }


    public void setEvidentialSet(long serial) {
        setEvidentialSet(new long[] { serial } );
    }
}

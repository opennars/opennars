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

import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.io.Symbols;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.Stamped;
import nars.nal.nal8.ImmediateOperation;
import nars.nal.nal8.Operation;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Termed;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A task to be processed, consists of a Sentence and a BudgetValue.
 * A task references its parent and an optional causal factor (usually an Operation instance).  These are implemented as WeakReference to allow forgetting via the
 * garbage collection process.  Otherwise, Task ancestry would grow unbounded,
 * violating the assumption of insufficient resources (AIKR).
 *
 * TODO decide if the Sentence fields need to be Reference<> also
 */
public class Task<T extends Compound> extends Item<Sentence<T>> implements Termed,Budget.Budgetable, Stamped {

//    /** placeholder for a forgotten task */
//    public static final Task Forgotten = new Task();

    

    /**
     * The sentence of the Task
     */
    public final Sentence<T> sentence;
    /**
     * Task from which the Task is derived, or null if input
     */
    final Reference<Task> parentTask;
    /**
     * Belief from which the Task is derived, or null if derived from a theorem
     */
    public final Sentence parentBelief;

    /** if hash calculated once creation; if parentTask reference is lost, the hash (which includes its hash) will still be preserved,
     * allowing it to be differentiated from another equal task with a lost parent
     * this may result in duplicate equivalent tasks that can be merged but it preserves their
     * position within a bag. otherwise if the hash suddenly changed, there would be a bag fault.
     */
    private int hash;

    /**
     * For Question and Goal: best solution found so far
     */
    private Sentence bestSolution;
    
    /** causal factor; usually an instance of Operation */
    private Term cause;
    private List<String> history = null;
    private boolean temporalInducted=true;


    /**
     * Constructor for input task
     *
     * @param s The sentence
     * @param b The budget
     */
    public Task(final Sentence s, final Budget b) {
        this(s, b, (Task)null, null, null);
    }
 
    public Task(final Sentence s, final Budget b, final Task parentTask) {
        this(s, b, parentTask, null);        
    }

    protected Task() {
        this(null, null);
    }
    

    /**
     * Constructor for a derived task
     *
     * @param s The sentence
     * @param b The budget
     * @param parentTask The task from which this new task is derived
     * @param parentBelief The belief from which this new task is derived
     */
    public Task(final Sentence<T> s, final Budget b, final Task parentTask, final Sentence parentBelief) {
        this(s, b, parentTask == null ? null : Global.reference(parentTask), parentBelief, null);
    }

    public Task(final Sentence<T> s, final Budget b, final Reference<Task> parentTask, final Sentence parentBelief, Sentence solution) {
        super(b);
        this.sentence = s;
        this.parentTask = parentTask;


        if (parentTask == null)
            addHistory("Input");

        this.parentBelief = parentBelief;
        this.bestSolution = solution;


        if (Global.DEBUG) {
            if ((parentTask!=null && parentTask.get() == null))
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

    /**
     * Constructor for an activated task
     *
     * @param s The sentence
     * @param b The budget
     * @param parentTask The task from which this new task is derived
     * @param parentBelief The belief from which this new task is derived
     * @param solution The belief to be used in future logic
     */
    public Task(final Sentence<T> s, final Budget b, final Task parentTask, final Sentence parentBelief, final Sentence solution) {
        this(s, b, parentTask == null ? null : Global.reference(parentTask), parentBelief, solution);
    }



    public Task(ProtoTask<T> t) {
        this(t.getSentence(), t.getBudget(), t.getParentTask(), t.getParentBelief(), null);
    }

    @Override
    public Task clone() {
        return new Task(sentence, this, parentTask, parentBelief, bestSolution);
    }
    
    public Task clone(final Sentence replacedSentence) {
        return new Task(replacedSentence, this, parentTask, parentBelief, bestSolution);
    }
    
    @Override public Sentence name() {
        return sentence;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Task) {
            Task t = (Task)obj;
            return sentence.equals(t.sentence);// && equalParents(t);
        }
        return false;        
    }

    @Override
    public int hashCode() {
        if (this.hash == 0)
            this.hash = (sentence==null? toString().hashCode() : sentence.hashCode());// + parentHash();
        return hash;
    }

    public boolean equalParents(Task t) {
        Task p = getParentTask();
        Task tp = t.getParentTask();
        if (p == null) {
            return (tp == null);
        }
        else {
            return p.equals(tp);
        }
    }

    private int parentHash() {
        Task parent = getParentTask();
        if (parent!=null)
            return parent.hashCode();
        return 0;
    }


    /**
     * Directly get the creation time of the sentence
     *
     * @return The creation time of the sentence
     */
    public long getCreationTime() {
        return sentence.stamp.getCreationTime();
    }

    public long getOccurrenceTime() {
        return sentence.stamp.getOccurrenceTime();
    }


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
        return bestSolution;
    }

    /**
     * Set the best-so-far solution for a Question or Goal, and report answer
     * for input question
     *
     * @param judg The solution to be remembered
     */
    public void setBestSolution(final Sentence judg) {
        bestSolution = judg;
    }

    /**
     * Get the parent belief of a task
     *
     * @return The belief from which the task is derived
     */
    public Sentence getParentBelief() {
        return parentBelief;
    }

    /**
     * Get the parent task of a task
     *
     * @return The task from which the task is derived
     */
    public Task getParentTask() {
        if (parentTask == null) return null;
        return parentTask.get();
    }

    @Override
    @Deprecated public String toString() {
        return toStringBudgetSentence();
    }


    public StringBuilder toString(Memory memory) {
        return appendToString(null, memory);
    }

    public StringBuilder appendToString(StringBuilder sb, Memory memory) {
        if (sb == null) sb = new StringBuilder();
        sb.append(sentence.toString(memory,true)).append(getBudget());
        return sb;
    }

    public boolean hasParent(Task t) {
        if (getParentTask() == null)
            return false;
        Task p=getParentTask();
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
        Task p=getParentTask();
        do {            
            Task n = p.getParentTask();
            if (n==null) break;
            p = n;
        } while (true);
        return p;
    }

    /** generally, op will be an Operation instance */
    public Task setCause(final Term op) {
        this.cause = op;
        return this;
    }

    /** the causing Operation, or null if not applicable. */
    public Term getCause() {
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

        if (task.getCause()!=null)
            sb.append(" cause=").append(task.getCause());
        if (task.getBestSolution()!=null) {
            if (!task.getTerm().equals(task.getBestSolution().term))
                sb.append(" solution=").append(task.getBestSolution());
        }

        Task pt = task.getParentTask();

        Sentence pb = task.getParentBelief();
        if (pb!=null) {
            if (pt!=null && pb.equals(pt.sentence)) {

            }
            else {
                sb.append(" parentBelief=").append(task.getParentBelief()).append(task.getParentBelief().getStamp());
            }
        }
        sb.append('\n');

        if (pt!=null) {
            getExplanation(pt, indent+1, sb);
        }
    }

    public TruthValue getDesire() { return sentence.truth; }


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


    
    /** signaling that the Task has ended or discarded */
    @Override public void end() {
    }

    /** flag to indicate whether this Event Task participates in tempporal induction */
    public void setParticipateInTemporalInduction(boolean b) {
        this.temporalInducted = b;
    }

    public boolean isParticipatingInTemporalInduction() {
        return temporalInducted;
    }

    
    public static Set<Sentence> getSentences(Iterable<Task> tasks) {
        Set<Sentence> s = new HashSet();
        for (Task t : tasks)
            s.add(t.sentence);
        return s;
    }

    @Override
    public T getTerm() {
        return sentence.getTerm();
    }


    /** optional list of strings explaining the reasons that make up this task's [hi-]story.
     *  useful for debugging but can also be applied to meta-analysis */
    public void addHistory(String reason) {
        if (!Global.TASK_HISTORY)
            return;

        //TODO parameter for max history length, although task history should not grow after they are crystallized with a concept
        if (this.history == null)
            this.history = Global.newArrayList(2);

        this.history.add(reason);
    }

    public List<String> getHistory() {
        return history;
    }

    public boolean equalPunctuations(Task t) {
        return sentence.equalPunctuations(t.sentence);
    }

    public char getPunctuation() {
        return sentence.punctuation;
    }


    /** a task is considered amnesiac (origin not rememebered) if its parent task has been forgotten (garbage collected via a soft/weakref) */
    public boolean isAmnesiac() {
        return !isInput() && getParentTask() == null;
    }

    @Override
    public Stamp getStamp() {
        return sentence.stamp;
    }

    public Task addHistory(List<String> historyToCopy) {
        if (!Global.TASK_HISTORY)
            return this;

        if (historyToCopy!=null) {
            if (this.history==null) this.history = new ArrayList(historyToCopy.size());
            history.addAll(historyToCopy);
        }
        return this;
    }

    /**
     * @return true if it was immediate
     */
    public boolean executeIfImmediate(Memory memory) {
        final Term taskTerm = getTerm();
        if (taskTerm instanceof Operation) {
            Operation o = (Operation) taskTerm;
            o.setTask(this);


            if (o instanceof ImmediateOperation) {
                if (sentence!=null && getPunctuation()!= Symbols.GOAL)
                    throw new RuntimeException("ImmediateOperation " + o + " was not specified with goal punctuation");

                ImmediateOperation i = (ImmediateOperation) getTerm();
                i.execute(memory);
                return true;
            }
            else if (o.getOperator().isImmediate()) {
                if (sentence!=null && getPunctuation()!= Symbols.GOAL)
                    throw new RuntimeException("ImmediateOperator call " + o + " was not specified with goal punctuation");

                o.getOperator().execute(o, memory);
                return true;
            }
        }

        return false;
    }

    public void ensurePerceived(Memory memory) {
        if (sentence!=null) {
            //if a task has an unperceived creationTime,
            // set it to the memory's current time here,
            // and adjust occurenceTime if it's not eternal
            Stamp s = sentence.stamp;
            if (s.getCreationTime() == Stamp.UNPERCEIVED) {
                final long now = memory.time();
                long oc = s.getOccurrenceTime();
                if (oc!=Stamp.ETERNAL)
                    oc += now;
                getStamp().setTime(now, oc);
            }
        }
    }
}

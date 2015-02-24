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
package nars.entity;

import com.google.common.base.Strings;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import nars.core.control.AbstractTask;
import nars.language.CompoundTerm;
import nars.language.Term;
import nars.language.Terms.Termable;
import nars.operator.Operation;

/**
 * A task to be processed, consists of a Sentence and a BudgetValue.
 * A task references its parent and an optional causal factor (usually an Operation instance).  These are implemented as WeakReference to allow forgetting via the
 * garbage collection process.  Otherwise, Task ancestry would grow unbounded,
 * violating the assumption of insufficient resources (AIKR).
 */
public class Task<T extends Term> extends AbstractTask<Sentence<T>> implements Termable {

    /** placeholder for a forgotten task */
    public static final Task Forgotten = new Task();

    

    /**
     * The sentence of the Task
     */
    public final Sentence<T> sentence;
    /**
     * Task from which the Task is derived, or null if input
     */
    final WeakReference<? extends Task> parentTask;
    /**
     * Belief from which the Task is derived, or null if derived from a theorem
     */
    public final Sentence parentBelief;
    /**
     * For Question and Goal: best solution found so far
     */
    private Sentence bestSolution;
    
    /** causal factor; usually an instance of Operation */
    private WeakReference<Term> cause;
    
    
    
    /**
     * Constructor for input task
     *
     * @param s The sentence
     * @param b The budget
     */
    public Task(final Sentence<T> s, final BudgetValue b) {
        this(s, b, (WeakReference)null, null, null);
    }
 
    public Task(final Sentence<T> s, final BudgetValue b, final Task parentTask) {
        this(s, b, parentTask, null);        
    }

    protected Task() {
        this(null, null);
    }
    
    private boolean temporalInducted=true;
    
    /**
     * Constructor for a derived task
     *
     * @param s The sentence
     * @param b The budget
     * @param parentTask The task from which this new task is derived
     * @param parentBelief The belief from which this new task is derived
     */
    public Task(final Sentence<T> s, final BudgetValue b, final Task parentTask, final Sentence parentBelief) {
        this(s, b, new WeakReference(parentTask), parentBelief, null);
    }

    public Task(final Sentence<T> s, final BudgetValue b, final WeakReference<Task> parentTask, final Sentence parentBelief, Sentence solution) {    
        super(b);
        this.sentence = s;
        this.parentTask = parentTask;
        this.parentBelief = parentBelief;
        this.bestSolution = solution;   
    }
    /**
     * Constructor for an activated task
     *
     * @param s The sentence
     * @param b The budget
     * @param parentTask The task from which this new task is derived
     * @param parentBelief The belief from which this new task is derived
     * @param solution The belief to be used in future inference
     */
    public Task(final Sentence<T> s, final BudgetValue b, final Task parentTask, final Sentence parentBelief, final Sentence solution) {
        this(s, b, new WeakReference(parentTask), parentBelief, solution);
    }

    public Task clone() {
        return new Task(sentence, budget, parentTask, parentBelief, bestSolution);
    }
    
    public Task clone(final Sentence replacedSentence) {
        return new Task(replacedSentence, budget, parentTask, parentBelief, bestSolution);
    }
    
    @Override public Sentence name() {
        return sentence;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj instanceof Task) {
            Task t = (Task)obj;            
            return t.sentence.equals(sentence);
        }
        return false;        
    }

    @Override
    public int hashCode() {
        return sentence.hashCode();
    }
    
    //public static boolean isValidTerm(Term t) {
   //     return t instanceof CompoundTerm;
   // }
    
    public static Task make(Sentence s, BudgetValue b, Task parent) {
        return make(s, b, parent, null);
    }
    
    public static Task make(Sentence s, BudgetValue b, Task parent, Sentence belief) {
        Term t = s.term;
        //if (isValidTerm(t)) { sentence wouldnt exist if it wouldnt be valid..
            return new Task(s, b, parent, belief);
        //}
        //return null;
    }
    

    

    /**
     * Directly get the creation time of the sentence
     *
     * @return The creation time of the sentence
     */
    public long getCreationTime() {
        return sentence.stamp.getCreationTime();
    }

    /**
     * Check if a Task is a direct input
     *
     * @return Whether the Task is derived from another task
     */
    public boolean isInput() {
        return parentTask == null;
    }
    
    public boolean aboveThreshold() {
        return budget.aboveThreshold();
    }

    /**
     * Check if a Task is derived by a StructuralRule
     *
     * @return Whether the Task is derived by a StructuralRule
     */
//    public boolean isStructural() {
//        return (parentBelief == null) && (parentTask != null);
//    }
    /**
     * Merge one Task into another
     *
     * @param that The other Task
     */
    @Override
    public Item merge(final Item that) {
        if (getCreationTime() >= ((Task) that).getCreationTime()) {
            return super.merge(that);
        } else {
            return that.merge(this);
        }
    }

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

    /**
     * Get a String representation of the Task
     *
     * @return The Task as a String
     */
    @Override
    public String toStringLong() {
        final StringBuilder s = new StringBuilder();
        s.append(super.toString()).append(' ').append(sentence.stamp.name());
        
        Task pt = getParentTask();
        if (pt != null) {
            s.append("  \n from task: ").append(pt.toStringExternal());
            if (parentBelief != null) {
                s.append("  \n from belief: ").append(parentBelief.toString());
            }
        }
        if (bestSolution != null) {
            s.append("  \n solution: ").append(bestSolution.toString());
        }
        return s.toString();
    }


    public boolean hasParent(Task t) {
        if (getParentTask() == null)
            return false;
        Task p=getParentTask();
        do {            
            Task n = p.getParentTask();
            if (n!=null) {
                if (n.equals(t))
                    return true;
                p = n;
            }
            else
                break;
        } while (p!=null);
        return false;        
    }    
    
    public Task getRootTask() {
        if (getParentTask() == null) {
            return null;
        }
        Task p=getParentTask();
        do {            
            Task n = p.getParentTask();
            if (n!=null)
                p = n;
            else
                break;
        } while (p!=null);
        return p;
    }

    /** generally, op will be an Operation instance */
    public void setCause(final Term op) {
        this.cause = new WeakReference(op);
    }

    /** the causing Operation, or null if not applicable. */
    public Term getCause() {
        if (cause == null) return null;
        return cause.get();
    }

    public String getExplanation() {
        String x = toString() + "\n";
        if (cause!=null)
            x += "  cause=" + cause + "\n";
        if (bestSolution!=null) {
            if (!getTerm().equals(bestSolution.term))
                x += "  solution=" + bestSolution + "\n";
        }
        if (parentBelief!=null)
            x += "  parentBelief=" + parentBelief + " @ " + parentBelief.getCreationTime() + "\n";
        
        Task pt = getParentTask();
        if (pt!=null) {
            x += "  parentTask=" + pt + " @ " + pt.getCreationTime() + "\n";
        
            int indentLevel = 1;
            Task p=getParentTask();
            do {            
                indentLevel++;
                Task n = p.getParentTask();
                if (n!=null) {
                    x += Strings.repeat("  ",indentLevel) + n.toString();
                    p = n;
                }
                else
                    break;
                
            } while (p!=null);
        }
        
        return x;
    }

    public TruthValue getDesire() { return sentence.truth; }
    
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

    /** ends, indicating whether successful completion */
    public void end(boolean success) {

    }
    
    
    /** sets priority to zero, signaling that the Task has ended or discarded */
    @Override public void end() {
        end(false);
    }

    /** flag to indicate whether this Event Task participates in tempporal induction */
    public void setParticipateInTemporalInductionOnSucceedingEvents(boolean b) {
        this.temporalInducted = b;
    }

    public boolean isParticipatingInTemporalInductionOnSucceedingEvents() {
        return temporalInducted;
    }

    
    public static Set<Sentence> getSentences(Collection<Task> tasks) {
        Set<Sentence> s = new HashSet();
        for (Task t : tasks)
            s.add(t.sentence);
        return s;
    }

    @Override
    public T getTerm() {
        return sentence.getTerm();
    }



    
    
}

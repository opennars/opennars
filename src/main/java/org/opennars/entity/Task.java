/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.entity;

import java.io.Serializable;
import org.opennars.storage.Memory;
import org.opennars.language.Term;
import org.opennars.plugin.mental.InternalExperience;

/**
 * A task to be processed, consists of a Sentence and a BudgetValue.
 * A task references its parent and an optional causal factor (usually an Operation instance).  These are implemented as WeakReference to allow forgetting via the
 * garbage collection process.  Otherwise, Task ancestry would grow unbounded,
 * violating the assumption of insufficient resources (AIKR).
 */
public class Task<T extends Term> extends Item<Sentence<T>> implements Serializable  {

    /* The sentence of the Task*/
    public final Sentence<T> sentence;
    /* Belief from which the Task is derived, or null if derived from a theorem*/
    public final  Sentence parentBelief;
    /* For Question and Goal: best solution found so far*/
    private Sentence bestSolution;
    
    /**
     * Constructor for input task and single premise task
     *
     * @param s The sentence
     * @param b The budget
     */ 
    public Task(final Sentence<T> s, final BudgetValue b, boolean isInput) {
        this(s, b, null, null);  
        this.isInput = isInput;
    }

    protected Task() {
        this(null, null, null, null);
    }
    
    private boolean partOfSequenceBuffer = false;
    private boolean observablePrediction = false;
    private boolean isInput = false;
    
    /**
     * Constructor for a derived task
     *
     * @param s The sentence
     * @param b The budget
     * @param parentBelief The belief from which this new task is derived
     */
    public Task(final Sentence<T> s, final BudgetValue b, final Sentence parentBelief) {
        this(s, b, parentBelief, null);
    }
    public Task(final Sentence<T> s, final BudgetValue b, final Sentence parentBelief, Sentence solution) {    
        super(b);
        this.sentence = s;
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
        this(s, b, parentBelief, solution);
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
    
    public static Task make(Sentence s, BudgetValue b, Task parent) {
        return make(s, b, parent, null);
    }
    
    public static Task make(Sentence s, BudgetValue b, Task parent, Sentence belief) {
        Term t = s.term;
        return new Task(s, b, belief);
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
        return isInput;
    }
    
    public boolean aboveThreshold() {
        return budget.aboveThreshold();
    }
    
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
    public void setBestSolution(final Memory memory,final Sentence judg) {
        InternalExperience.InternalExperienceFromBelief(memory, this, judg);
        bestSolution = judg;
    }

    /**
     * Get the parent belief of a task
     *
     * @return The belief from which the task is derived
     */
    public Sentence getParentBelief() {
        if (parentBelief == null) return null;
        return parentBelief;
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
        if (bestSolution != null) {
            s.append("  \n solution: ").append(bestSolution.toString());
        }
        return s.toString();
    }

    /** flag to indicate whether this Event Task participates in tempporal induction */
    public void setElemOfSequenceBuffer(boolean b) {
        this.partOfSequenceBuffer = b;
    }

    public boolean isElemOfSequenceBuffer() {
        return !this.sentence.isEternal() && (this.isInput() || partOfSequenceBuffer);
    }
    
    public void setObservablePrediction(boolean b) {
        this.observablePrediction = b;
    }

    public boolean isObservablePrediction() {
        return this.observablePrediction;
    }

    public T getTerm() {
        return sentence.getTerm();
    }
}

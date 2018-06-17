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

import org.opennars.interfaces.Timable;
import org.opennars.language.Term;
import org.opennars.plugin.mental.InternalExperience;
import org.opennars.storage.Memory;

import java.io.Serializable;

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
    /* Whether the task should go into event bag or not*/
    private boolean partOfSequenceBuffer = false;
    /* Whether it is an input task or not */
    private boolean isInput = false;

    /**
     * Constructor for input task and single premise derived task
     *
     * @param s The sentence
     * @param b The budget
     */ 
    public Task(final Sentence<T> s, final BudgetValue b, EnumType type) {
        this(s, b, null, null);  
        this.isInput = type == EnumType.INPUT;
    }
    
    /***
     * Constructors for double premise derived task 
     * 
     * @param s The sentence
     * @param b The budget
     * @param parentBelief The belief used for deriving the task
     */
    public Task(final Sentence<T> s, final BudgetValue b, final Sentence parentBelief) {
        this(s, b, parentBelief, null);
    }
    
    /***
     * Constructors for solved double premise derived task 
     * 
     * @param s The sentence
     * @param b The budget
     * @param parentBelief The belief used for deriving the task 
     * @param solution The solution to the task
     */
    public Task(final Sentence<T> s, final BudgetValue b, final Sentence parentBelief, final Sentence solution) {
        super(b);
        this.sentence = s;
        this.parentBelief = parentBelief;
        this.bestSolution = solution;   
    }
    
    @Override public Sentence name() {
        return sentence;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj instanceof Task) {
            final Task t = (Task)obj;
            return t.sentence.equals(sentence);
        }
        return false;        
    }

    @Override
    public int hashCode() {
        return sentence.hashCode();
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
    public void setBestSolution(final Memory memory,final Sentence judg, final Timable time) {
        InternalExperience.InternalExperienceFromBelief(memory, this, judg, time);
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
    public void setElemOfSequenceBuffer(final boolean b) {
        this.partOfSequenceBuffer = b;
    }

    public boolean isElemOfSequenceBuffer() {
        return !this.sentence.isEternal() && (this.isInput() || partOfSequenceBuffer);
    }

    public T getTerm() {
        return sentence.getTerm();
    }

    public enum EnumType {
        INPUT,
        DERIVED,
    }
}

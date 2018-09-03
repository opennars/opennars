/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
 *
 * @author Pei Wang
 * @author Patrick Hammer
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
        if(memory.internalExperience != null) {
            InternalExperience.InternalExperienceFromBelief(memory, this, judg, time);
        }
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

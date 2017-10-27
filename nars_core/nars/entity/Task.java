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
import nars.storage.Memory;
import nars.language.Term;
import nars.operator.Operation;
import nars.plugin.mental.InternalExperience;

/**
 * A task to be processed, consists of a Sentence and a BudgetValue.
 * A task references its parent and an optional causal factor (usually an Operation instance).  These are implemented as WeakReference to allow forgetting via the
 * garbage collection process.  Otherwise, Task ancestry would grow unbounded,
 * violating the assumption of insufficient resources (AIKR).
 */
public class Task<T extends Term> extends Item<Sentence<T>>  {

    /** placeholder for a forgotten task */
    public static final Task Forgotten = new Task();

    /* The sentence of the Task*/
    public final Sentence<T> sentence;
    /* Task from which the Task is derived, or null if input*/
    final WeakReference<Task> parentTask;
    /* Belief from which the Task is derived, or null if derived from a theorem*/
    public final  WeakReference<Sentence> parentBelief;
    /* For Question and Goal: best solution found so far*/
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
    
    private boolean partOfSequenceBuffer = false;
    private boolean observablePrediction = false;
    
    /**
     * Constructor for a derived task
     *
     * @param s The sentence
     * @param b The budget
     * @param parentTask The task from which this new task is derived
     * @param parentBelief The belief from which this new task is derived
     */
    public Task(final Sentence<T> s, final BudgetValue b, final Task parentTask, final Sentence parentBelief) {
        this(s, b, new WeakReference(parentTask), new WeakReference(parentBelief), null);
    }

    public Task(final Sentence<T> s, final BudgetValue b, final WeakReference<Task> parentTask, final WeakReference<Sentence> parentBelief, Sentence solution) {    
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
        this(s, b, new WeakReference(parentTask), new WeakReference(parentBelief), solution);
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
        return parentBelief.get();
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

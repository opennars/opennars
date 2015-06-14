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

import nars.language.Term;

/**
 * A task to be processed, consists of a Sentence and a BudgetValue
 */
public class Task extends AbstractTask {

    /**
     * The sentence of the Task
     */
    public final Sentence sentence;
    /**
     * Task from which the Task is derived, or null if input
     */
    public final Task parentTask;
    /**
     * Belief from which the Task is derived, or null if derived from a theorem
     */
    public final Sentence parentBelief;
    /**
     * For Question and Goal: best solution found so far
     */
    private Sentence bestSolution;
    private final CharSequence key;
    
    /**
     * Constructor for input task
     *
     * @param s The sentence
     * @param b The budget
     */
    public Task(final Sentence s, final BudgetValue b) {
        this(s, b, null, null);
    }
 

    /**
     * Constructor for a derived task
     *
     * @param s The sentence
     * @param b The budget
     * @param parentTask The task from which this new task is derived
     * @param parentBelief The belief from which this new task is derived
     */
    public Task(final Sentence s, final BudgetValue b, final Task parentTask, final Sentence parentBelief) {
        super(b);
        this.sentence = s;
        this.key = sentence.getKey();        
        this.parentTask = parentTask;
        this.parentBelief = parentBelief;
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
    public Task(final Sentence s, final BudgetValue b, final Task parentTask, final Sentence parentBelief, final Sentence solution) {
        this(s, b, parentTask, parentBelief);
        this.bestSolution = solution;
    }

    @Override public CharSequence getKey() {
        return key;
    }


    /**
     * Directly get the content of the sentence
     *
     * @return The content of the sentence
     */
    public Term getContent() {
        return sentence.content;
    }

    /**
     * Directly get the creation time of the sentence
     *
     * @return The creation time of the sentence
     */
    public long getCreationTime() {
        return sentence.stamp.creationTime;
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
    public void merge(final Item that) {
        if (getCreationTime() >= ((Task) that).getCreationTime()) {
            super.merge(that);
        } else {
            that.merge(this);
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
        return parentTask;
    }

    /**
     * Get a String representation of the Task
     *
     * @return The Task as a String
     */
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        s.append(super.toString()).append(' ').append(sentence.stamp);
        if (parentTask != null) {
            s.append("  \n from task: ").append(parentTask.toStringBrief());
            if (parentBelief != null) {
                s.append("  \n from belief: ").append(parentBelief.toString());
            }
        }
        if (bestSolution != null) {
            s.append("  \n solution: ").append(bestSolution.toString());
        }
        return s.toString();
    }

    public Task getRootTask() {
        if (getParentTask() == null) {
            return null;
        }
        Task t, p=this;
        do {            
            t = p;
            p = t.getParentTask();            
        } while (p!=null);
        return t;
    }

    
}

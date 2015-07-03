/*
 * Concept.java
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
package nars.concept;

import com.google.common.base.Function;
import nars.Global;
import nars.Memory;
import nars.Op;
import nars.Symbols;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.budget.Itemized;
import nars.link.*;
import nars.meter.LogicMetrics;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Termed;
import nars.truth.Truth;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterators.*;

abstract public interface Concept extends Termed, Itemized<Term>, Serializable {


    public Bag<Sentence, TaskLink> getTaskLinks();
    public Bag<TermLinkKey, TermLink> getTermLinks();
    public Map<Object, Meta> getMeta();
    public void setMeta(Map<Object, Meta> meta);


    public Memory getMemory();


    TaskLink activateTaskLink(TaskLinkBuilder taskLinkBuilder);

    boolean linkTerms(Budget budgetRef, boolean b);

    TermLink activateTermLink(TermLinkBuilder termLinkBuilder);

    void updateTermLinks();

    void setUsed(long time);

    float getPriority();

    boolean link(Task currentTask);

    default Op operator() {
        return getTerm().operator();
    }



    /**
     * whether a concept's desire exceeds decision threshold
     */
    default public boolean isDesired() {
        return isDesired(getMemory().param.executionThreshold.floatValue());
    }

    default public boolean isDesired(float threshold) {
        Truth desire=this.getDesire();
        if(desire==null) {
            return false;
        }
        return desire.getExpectation() > threshold;
    }

    default public void discountBeliefConfidence() {
        if (hasBeliefs()) {
            discountTaskConfidences(getBeliefs());
        }
    }

    default public void discountGoalConfidence() {
        if (hasGoals()) {
            discountTaskConfidences(getGoals());
        }
    }

    default void discountTaskConfidences(final Iterable<Task> t) {
        t.forEach(x -> x.discountConfidence());
    }


    default public boolean hasGoals() {
        final BeliefTable s = getGoals();
        if (s == null) return false;
        return !s.isEmpty();
    }

    default public boolean hasBeliefs() {
        final BeliefTable s = getBeliefs();
        if (s == null) return false;
        return !s.isEmpty();
    }

    default public boolean hasQuestions() {
        final TaskTable s = getQuestions();
        if (s == null) return false;
        return !s.isEmpty();
    }

    default public boolean hasQuests() {
        final TaskTable s = getQuests();
        if (s == null) return false;
        return !s.isEmpty();
    }

    boolean isConstant();

    /** allows concept state to be locked */
    boolean setConstant(boolean b);

    /** updates the concept-has-questions index if the concept transitions from having no questions to having, or from having to not having */
    default public void onTableUpdated(char punctuation, int originalSize) {
        if (!isActive()) return;

        switch (punctuation) {
            /*case Symbols.GOAL:
                break;*/
            case Symbols.QUESTION:
                if (getQuestions().isEmpty()) {
                    if (originalSize > 0) //became empty
                        getMemory().updateConceptQuestions(this);
                } else {
                    if (originalSize == 0) { //became non-empty
                        getMemory().updateConceptQuestions(this);
                    }
                }
                break;
        }
    }


    public enum State {

        /** created but not added to memory */
        New,

        /** in memory */
        Active,

        /** in sub-concepts */
        Forgotten,

        /** unrecoverable, will be garbage collected eventually */
        Deleted
    }






    default public String toInstanceString() {
        String id = Integer.toString(System.identityHashCode(this), 16);
        return this + "::" + id + "." + getState().toString().toLowerCase();
    }


    /** like Map.put for storing data in meta map
     *  @param value if null will perform a removal
     * */
    default Meta put(Object key, Meta value) {
        if (getMeta() == null) setMeta(Global.newHashMap());

        if (value != null) {
            Meta removed = getMeta().put(key, value);
            if (removed!=value) {
                value.onState(this, getState());
            }
            return removed;
        }
        else
            return getMeta().remove(key);
    }

    /** like Map.gett for getting data stored in meta map */
    default public <C extends Meta> C get(Object key) {
        if (getMeta() == null) return null;
        return (C) getMeta().get(key);
    }

    /**
     * Get the current overall desire value. TODO to be refined
     */
    default public Truth getDesire() {
        if (!hasGoals()) {
            return null;
        }
        Truth topValue = getGoals().top().getTruth();
        return topValue;
    }

    public BeliefTable getBeliefs();
    public BeliefTable getGoals();

    public TaskTable getQuestions();
    public TaskTable getQuests();

    public State getState();
    public Concept setState(State nextState);

    public long getCreationTime();
    public long getDeletionTime();

    default public boolean isDeleted() {
        return getState() == Concept.State.Deleted;
    }

    default public boolean isNew() { return getState() == State.New;     }
    default public boolean isActive() {
        return getState() == State.Active;
    }
    default public boolean isForgotten() {
        return getState() == State.Forgotten;
    }


    public void delete();

//    /** debugging utility */
//    default public void ensureNotDeleted() {
//        if (isDeleted())
//            throw new RuntimeException("Deleted concept should not activate TermLinks");
//    }



    default public boolean ensureActiveFor(String activity) {
        if (!this.isActive() && !this.isForgotten()) {
            System.err.println(activity + " fail: " + this + " (state=" + getState() + ')');
            new Exception().printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @param nal  reasoning context it is being processed in
     * @param task The task to be processed
     * @return whether it was processed
     */
    default public boolean process(final TaskProcess nal) {


        if (!ensureActiveFor("TaskProcess")) return false;

        final Task task = nal.getCurrentTask();

        if (!processable(task)) {
            getMemory().removed(task, "Filtered by Concept");
            return false;
        }

        //share the same Term instance for fast comparison and reduced memory usage (via GC)
        task.setTermInstance((Compound) getTerm());

        final char type = task.sentence.punctuation;
        final LogicMetrics logicMeter = getMemory().logic;

        switch (type) {
            case Symbols.JUDGMENT:

                if (hasBeliefs() && isConstant())
                    return false;

                if (!processBelief(nal, task))
                    return false;

                logicMeter.JUDGMENT_PROCESS.hit();
                break;
            case Symbols.GOAL:
                if (!processGoal(nal, task))
                    return false;
                logicMeter.GOAL_PROCESS.hit();
                break;
            case Symbols.QUESTION:
                processQuest(nal, task);
                logicMeter.QUESTION_PROCESS.hit();
                break;
            case Symbols.QUEST:
                processQuestion(nal, task);
                logicMeter.QUESTION_PROCESS.hit();
                break;
            default:
                throw new RuntimeException("Invalid sentence type: " + task);
        }

        return true;
    }

    public boolean processBelief(TaskProcess nal, Task task);

    public boolean processGoal(TaskProcess nal, Task task);

    public Task processQuestion(TaskProcess nal, Task task);

    default public Task processQuest(TaskProcess nal, Task task) {
        return processQuestion(nal, task);
    }


    /**
     * by default, any Task is valid to be processed
     */
    default public boolean processable(Task t) {
        return true;
    }


//    /** returns the best belief of the specified types */
//    default public Task getStrongestBelief(boolean eternal, boolean nonEternal) {
//        return getBeliefs().top(eternal, nonEternal);
//    }
//
//
//    default public Task getStrongestGoal(boolean eternal, boolean nonEternal) {
//        return getGoals().top(eternal, nonEternal);
//    }


    /**
     * Methods to be implemented by Concept meta instances
     */
    public static interface Meta {

        /** called before the state changes to the given nextState */
        public void onState(Concept c, State nextState);

    }



    default public Iterator<? extends Termed> adjacentTermables(boolean termLinks, boolean taskLinks) {
        if (termLinks && taskLinks) {
            return concat(
                    this.getTermLinks().iterator(), this.getTaskLinks().iterator()
            );
        }
        else if (termLinks) {
            return this.getTermLinks().iterator();
        }
        else if (taskLinks) {
            return this.getTaskLinks().iterator();
        }

        return null;
    }

    default public void print(PrintStream out) {
        print(out, true, true, true, true);
    }

    /** prints a summary of all termlink, tasklink, etc.. */
    default public void print(PrintStream out, boolean showbeliefs, boolean showgoals, boolean showtermlinks, boolean showtasklinks) {
        final String indent = "\t";
        long now = getMemory().time();

        out.println("CONCEPT: " + toInstanceString() + " @ " + now);

        if (showbeliefs) {
            out.print(" Beliefs:");
            if (getBeliefs().isEmpty()) out.println(" none");
            else out.println();
            for (Task s : getBeliefs()) {
                out.print(indent);
                out.println((int) (getBeliefs().rank(s, now) * 100.0) + "%: " + s);
            }
        }

        if (showgoals) {
            out.print(" Goals:");
            if (getGoals().isEmpty()) out.println(" none");
            else out.println();
            for (Task s : getGoals()) {
                out.print(indent);
                out.println((int) (getGoals().rank(s, now) * 100.0) + "%: " + s);
            }
        }

        if (showtermlinks) {

            out.println(" TermLinks:");
            for (TLink t : getTermLinks()) {
                out.print(indent);
                TLink.print(t, out);
                out.println();
            }

        }

        if (showtasklinks) {
            out.println(" TaskLinks:");
            for (TLink t : getTaskLinks()) {
                out.print(indent);
                TLink.print(t, out);
                out.println();
            }
        }

        out.println();
    }


    default public Iterator<Term> adjacentTerms(boolean termLinks, boolean taskLinks) {
        return transform(adjacentTermables(termLinks, taskLinks), new Function<Termed, Term>() {
            @Override
            public Term apply(final Termed term) {
                return term.getTerm();
            }
        });
    }

    default public Iterator<Concept> adjacentConcepts(boolean termLinks, boolean taskLinks) {
        final Iterator<Concept> termToConcept = transform(adjacentTerms(termLinks, taskLinks), new Function<Termed, Concept>() {
            @Override
            public Concept apply(final Termed term) {
                return getMemory().concept(term.getTerm());
            }
        });
        return filter(termToConcept, Concept.class); //should remove null's (unless they never get included anyway), TODO Check that)
    }

//    public Task getTask(boolean hasQueryVar, long occTime, Truth truth, List<Task>... lists);
//
//    default public Task getTask(Sentence query, List<Task>... lists) {
//        return getTask(query.hasQueryVar(), query.getOccurrenceTime(), query.getTruth(), lists);
//    }

}

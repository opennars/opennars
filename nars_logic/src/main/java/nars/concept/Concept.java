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
import nars.bag.Bag;
import nars.budget.Budget;
import nars.budget.Itemized;
import nars.link.*;
import nars.premise.Premise;
import nars.premise.PremiseGenerator;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.truth.Truth;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.collect.Iterators.*;

abstract public interface Concept extends Termed, Itemized<Term>, Serializable {


    Bag<Sentence, TaskLink> getTaskLinks();
    Bag<TermLinkKey, TermLink> getTermLinks();

    Map<Object, Object> getMeta();
    void setMeta(Map<Object, Object> meta);


    Memory getMemory();


    TaskLink activateTaskLink(TaskLinkBuilder taskLinkBuilder);

    boolean linkTerms(Budget budgetRef, boolean b);

    TermLink activateTermLink(TermLinkBuilder termLinkBuilder);

    void updateLinks();

    @Override
    float getPriority();

    boolean link(Task currentTask);

    default Op operator() {
        return getTerm().op();
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



    default public float getDesireExpectation() {
        Truth d = getDesire();
        if (d!=null) return d.getExpectation();
        return 0;
    }





    public TermLinkBuilder getTermLinkBuilder();


    default public String toInstanceString() {
        String id = Integer.toString(System.identityHashCode(this), 16);
        return this + "::" + id;
    }


    /** like Map.put for storing data in meta map
     *  @param value if null will perform a removal
     * */
    default Object put(Object key, Object value) {

        Map<Object, Object> currMeta = getMeta();

        if (value != null) {

            if (currMeta == null) setMeta(currMeta = Global.newHashMap());

            return currMeta.put(key, value);
        }
        else {
            if (currMeta!=null)
                return currMeta.remove(key);
            else
                return null;
        }

    }

    /** like Map.gett for getting data stored in meta map */
    default public <C> C get(Object key) {
        final Map<Object, Object> m = getMeta();
        if (m == null) return null;
        return (C) m.get(key);
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

    /** satisfaction/success metric:
     * if desire exists, returns 1.0 / (1 + Math.abs(belief - desire))
     *  otherwise zero */
    default public float getSuccess() {
        if (hasBeliefs() && hasGoals()) {

            Truth d = getDesire();
            float de = d.getExpectation();


            if (d == null) return 0;
            Truth b = getBeliefs().top().getTruth();
            float be = b.getExpectation();


            return 1.0f / (1.0f + Math.abs(be - de));
        }

        return 0;
    }

    public BeliefTable getBeliefs();
    public BeliefTable getGoals();

    public TaskTable getQuestions();
    public TaskTable getQuests();



    public long getCreationTime();

    @Override
    public void delete();

//    /** debugging utility */
//    default public void ensureNotDeleted() {
//        if (isDeleted())
//            throw new RuntimeException("Deleted concept should not activate TermLinks");
//    }




    public boolean processBelief(Premise nal, Task task);

    public boolean processGoal(Premise nal, Task task);

    public Task processQuestion(Premise nal, Task task);

    default public Task processQuest(Premise nal, Task task) {
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
        long now = getMemory().time();

        out.println("CONCEPT: " + toInstanceString() + " @ " + now);

        final String indent = "\t";
        if (showbeliefs) {
            out.print(" Beliefs:");
            if (getBeliefs().isEmpty()) out.println(" none");
            else out.println();
            for (Task s : getBeliefs()) {
                out.print(indent);
                out.println(s);
                //out.println((int) (getBeliefs().rank(s, now) * 100.0) + "%: " + s);
            }
        }

        if (showgoals) {
            out.print(" Goals:");
            if (getGoals().isEmpty()) out.println(" none");
            else out.println();
            for (Task s : getGoals()) {
                out.print(indent);
                out.println(s);
                //out.println((int) (getGoals().rank(s, now) * 100.0) + "%: " + s);
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

    default public long time() {
        return getMemory().time();
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

    PremiseGenerator getPremiseGenerator();

//    public Task getTask(boolean hasQueryVar, long occTime, Truth truth, List<Task>... lists);
//
//    default public Task getTask(Sentence query, List<Task>... lists) {
//        return getTask(query.hasQueryVar(), query.getOccurrenceTime(), query.getTruth(), lists);
//    }

}

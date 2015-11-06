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

import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import infinispan.com.google.common.collect.Iterators;
import nars.Global;
import nars.Memory;
import nars.Premise;
import nars.bag.Bag;
import nars.budget.Itemized;
import nars.concept.util.BeliefTable;
import nars.concept.util.TaskTable;
import nars.link.*;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.truth.Truth;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterators.concat;

public interface Concept extends Termed, Itemized<Term> {



    Bag<Task, TaskLink> getTaskLinks();
    Bag<TermLinkKey, TermLink> getTermLinks();

    Map<Object, Object> getMeta();
    void setMeta(Map<Object, Object> meta);


    Memory getMemory();




//    boolean linkTerms(Budget budgetRef, boolean updateTLinks);
//
    TermLink activateTermLink(TermLinkBuilder termLinkBuilder);
//      boolean link(Task currentTask);


    @Override
    float getPriority();


    /** attempts to fill the supplied array with next termlinks
     *  from this concept's bag.
     */
    default int nextTermLinks(int dur, long now, float termLinkForgetDurations, TermLink[] result) {
        return getTermLinks().forgetNext(
                termLinkForgetDurations * dur,
                result,
                now,
                0 /* additional */);
    }
    default int nextTaskLinks(int dur, long now, float taskLinkForgetDurations, TaskLink[] result) {
        return getTaskLinks().forgetNext(
                taskLinkForgetDurations * dur,
                result,
                now,
                0 /* additional */);
    }

    default void discountBeliefConfidence() {
        if (hasBeliefs()) {
            discountTaskConfidences(getBeliefs());
        }
    }

    default void discountGoalConfidence() {
        if (hasGoals()) {
            discountTaskConfidences(getGoals());
        }
    }

    default void discountTaskConfidences(final Iterable<Task> t) {
        t.forEach(Task::discountConfidence);
    }


    default boolean hasGoals() {
        final BeliefTable s = getGoals();
        return (s != null) && !s.isEmpty();
    }

    default boolean hasBeliefs() {
        final BeliefTable s = getBeliefs();
        return (s != null) && !s.isEmpty();
    }

    default boolean hasQuestions() {
        final TaskTable s = getQuestions();
        return (s != null) && !s.isEmpty();
    }

    default boolean hasQuests() {
        final TaskTable s = getQuests();
        if (s == null) return false;
        return !s.isEmpty();
    }

    boolean isConstant();

    /** allows concept state to be locked */
    boolean setConstant(boolean b);



    default float getDesireExpectation() {
        Truth d = getDesire();
        if (d!=null) return d.getExpectation();
        return 0;
    }





    TermLinkBuilder getTermLinkBuilder();


    default String toInstanceString() {
        String id = Integer.toString(System.identityHashCode(this), 16);
        return this + "::" + id + ' ' + getBudget().toBudgetString();
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
    default <C> C get(Object key) {
        final Map<Object, Object> m = getMeta();
        if (m == null) return null;
        return (C) m.get(key);
    }

    /**
     * Get the current overall desire value. TODO to be refined
     */
    default Truth getDesire() {
        if (!hasGoals()) {
            return null;
        }
        Truth topValue = getGoals().top().getTruth();
        return topValue;
    }

    /** satisfaction/success metric:
     * if desire exists, returns 1.0 / (1 + Math.abs(belief - desire))
     *  otherwise zero */
    default float getSuccess() {
        if (hasBeliefs() && hasGoals()) {

            Truth d = getDesire();
            if (d == null) return 0;

            float de = d.getExpectation();

            Truth b = getBeliefs().top().getTruth();
            float be = b.getExpectation();


            return 1.0f / (1.0f + Math.abs(be - de));
        }

        return 0;
    }

    BeliefTable getBeliefs();
    BeliefTable getGoals();

    TaskTable getQuestions();
    TaskTable getQuests();



    long getCreationTime();


//    /** debugging utility */
//    default public void ensureNotDeleted() {
//        if (isDeleted())
//            throw new RuntimeException("Deleted concept should not activate TermLinks");
//    }




    boolean processBelief(Premise nal);

    boolean processGoal(Premise nal);

    boolean processQuestion(Premise nal);

    boolean processQuest(Premise nal);





//    /** returns the best belief of the specified types */
//    default public Task getStrongestBelief(boolean eternal, boolean nonEternal) {
//        return getBeliefs().top(eternal, nonEternal);
//    }
//
//
//    default public Task getStrongestGoal(boolean eternal, boolean nonEternal) {
//        return getGoals().top(eternal, nonEternal);
//    }


    default Iterator<? extends Termed> getTermedAdjacents(boolean termLinks, boolean taskLinks) {
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

    default void print(PrintStream out) {
        print(out, true, true, true, true);
    }

    /** prints a summary of all termlink, tasklink, etc.. */
    default void print(PrintStream out, boolean showbeliefs, boolean showgoals, boolean showtermlinks, boolean showtasklinks) {
        long now = time();

        out.println("CONCEPT: " + toInstanceString() + " @ " + now);

        final String indent = "  \t";
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
                out.print(t.getBudget().toBudgetString());
                out.print(" ");
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

    default long time() {
        return getMemory().time();
    }

//    default Iterator<Term> adjacentTerms(boolean termLinks, boolean taskLinks) {
//        return transform(adjacentTermables(termLinks, taskLinks), Termed::getTerm);
//    }

//    default Iterator<Concept> adjacentConcepts(boolean termLinks, boolean taskLinks) {
//        final Iterator<Concept> termToConcept = transform(adjacentTerms(termLinks, taskLinks), new Function<Termed, Concept>() {
//            @Override
//            public Concept apply(final Termed term) {
//                return getMemory().concept(term.getTerm());
//            }
//        });
//        return filter(termToConcept, Concept.class); //should remove null's (unless they never get included anyway), TODO Check that)
//    }



    List<TermLinkTemplate> getTermLinkTemplates();

    final static Ordering<Task> taskCreationTime = new Ordering<Task>() {
        @Override
        public int compare(Task left, Task right) {
            return Longs.compare(
                    left.getCreationTime(),
                    right.getCreationTime());
        }
    };

    default Iterator<Task> iterateTasks(boolean onbeliefs, boolean ongoals, boolean onquestions, boolean onquests) {
        Iterator<Task> b1, b2, b3, b4;

        TaskTable beliefs = onbeliefs ? getBeliefs() : null;
        TaskTable goals =   ongoals ? getGoals() : null ;
        TaskTable questions = onquestions ?  getQuestions() : null;
        TaskTable quests = onquests ? getQuests() : null;

        b1 = beliefs!=null ? beliefs.iterator() : Iterators.emptyIterator();
        b2 = goals!=null ? goals.iterator() : Iterators.emptyIterator();
        b3 = questions!=null ? questions.iterator() : Iterators.emptyIterator();
        b4 = quests!=null ? quests.iterator() : Iterators.emptyIterator();
        return Iterators.concat(b1, b2, b3, b4);

    }

    void setMemory(Memory m);

    void setCreationTime(long l);


//    public Task getTask(boolean hasQueryVar, long occTime, Truth truth, List<Task>... lists);
//
//    default public Task getTask(Sentence query, List<Task>... lists) {
//        return getTask(query.hasQueryVar(), query.getOccurrenceTime(), query.getTruth(), lists);
//    }

}

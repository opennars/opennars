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

import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import nars.Global;
import nars.NAR;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.concept.util.BeliefTable;
import nars.concept.util.TaskTable;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.truth.Truth;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.collect.Iterators.concat;

public interface Concept extends Termed, Supplier<Term> {

    Bag<Task> getTaskLinks();
    Bag<Termed> getTermLinks();

    Map<Object, Object> getMeta();
    void setMeta(Map<Object, Object> meta);


//    boolean linkTerms(Budget budgetRef, boolean updateTLinks);
//
//    TermLink activateTermLink(TermLinkBuilder termLinkBuilder);
//      boolean link(Task currentTask);


    @Override
    default Term get() { return term(); }


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

    default void discountTaskConfidences(Iterable<Task> t) {
        t.forEach(Task::discountConfidence);
    }


    default boolean hasGoals() {
        BeliefTable s = getGoals();
        return (s != null) && !s.isEmpty();
    }

    default boolean hasBeliefs() {
        BeliefTable s = getBeliefs();
        return (s != null) && !s.isEmpty();
    }

    default boolean hasQuestions() {
        TaskTable s = getQuestions();
        return (s != null) && !s.isEmpty();
    }

    default boolean hasQuests() {
        TaskTable s = getQuests();
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


    default String toInstanceString() {
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
            return currMeta != null ? currMeta.remove(key) : null;
        }

    }

    /** like Map.gett for getting data stored in meta map */
    default <C> C get(Object key) {
        Map<Object, Object> m = getMeta();
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
        return getGoals().top().getTruth();
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



//    /** debugging utility */
//    default public void ensureNotDeleted() {
//        if (isDeleted())
//            throw new RuntimeException("Deleted concept should not activate TermLinks");
//    }




    boolean processBelief(Task task, NAR nar);

    boolean processGoal(Task task, NAR nar);

    boolean processQuestion(Task task, NAR nar);

    boolean processQuest(Task task, NAR nar);





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
                    getTermLinks().iterator(), getTaskLinks().iterator()
            );
        }
        if (termLinks) {
            return getTermLinks().iterator();
        }
        if (taskLinks) {
            return getTaskLinks().iterator();
        }

        return null;
    }

    default void print() {
        print(System.out);
    }

    default void print(PrintStream out) {
        print(out, true, true, true, true);
    }

    /** prints a summary of all termlink, tasklink, etc.. */
    default void print(PrintStream out, boolean showbeliefs, boolean showgoals, boolean showtermlinks, boolean showtasklinks) {

        out.println("concept: " + toInstanceString());

        String indent = "  \t";
        if (showbeliefs) {
            out.print(" Beliefs:");
            if (getBeliefs().isEmpty()) out.println(" none");
            else {out.println();
            getBeliefs().forEach(s -> {
                out.print(indent); out.println(s);
            });}
            out.print(" Questions:");
            if (getQuestions().isEmpty()) out.println(" none");
            else {out.println();
            getQuestions().forEach(s -> {
                out.print(indent); out.println(s);
            });}
        }

        if (showgoals) {
            out.print(" Goals:");
            if (getGoals().isEmpty()) out.println(" none");
            else {out.println();
            getGoals().forEach(s -> {
                out.print(indent);
                out.println(s);
            });}
            out.print(" Quests:");
            if (getQuestions().isEmpty()) out.println(" none");
            else {out.println();
                getQuests().forEach(s -> {
                    out.print(indent); out.println(s);
                });}
        }

        if (showtermlinks) {

            out.println(" TermLinks:");
            getTermLinks().forEachEntry(b-> {
                out.print(indent);
                out.print(b.get() + " " + b.toBudgetString());
                out.print(" ");
            });
        }

        if (showtasklinks) {
            out.println(" TaskLinks:");
            getTaskLinks().forEachEntry(b-> {
                out.print(indent);
                out.print(b.get() + " " + b.toBudgetString());
                out.print(" ");
            });
        }

        out.println();
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



    Termed[] getTermLinkTemplates();

    Ordering<Task> taskCreationTime = new Ordering<Task>() {
        @Override
        public int compare(Task left, Task right) {
            return Longs.compare(
                    left.getCreationTime(),
                    right.getCreationTime());
        }
    };

    default Iterator<Task> iterateTasks(boolean onbeliefs, boolean ongoals, boolean onquestions, boolean onquests) {

        TaskTable beliefs = onbeliefs ? getBeliefs() : null;
        TaskTable goals =   ongoals ? getGoals() : null ;
        TaskTable questions = onquestions ?  getQuestions() : null;
        TaskTable quests = onquests ? getQuests() : null;

        Iterator<Task> b1 = beliefs != null ? beliefs.iterator() : Iterators.emptyIterator();
        Iterator<Task> b2 = goals != null ? goals.iterator() : Iterators.emptyIterator();
        Iterator<Task> b3 = questions != null ? questions.iterator() : Iterators.emptyIterator();
        Iterator<Task> b4 = quests != null ? quests.iterator() : Iterators.emptyIterator();
        return Iterators.concat(b1, b2, b3, b4);

    }



    /**
     * process a task in this concept
     * @return true if process affected the concept (ie. was inserted into a belief table)
     */
    boolean process(Task task, NAR nar);

    /** attempt insert a tasklink into this concept's tasklink bag
     *  return true if successfully inserted
     * */
    boolean link(Task task, float scale, NAR nar);

    boolean linkTemplates(Budget budget, float scale, NAR nar);


    /**
     *
     * @param currentTask task with a term equal to this concept's
     * @param previousTask task with a term equal to another concept's
     * @return number of links created (0, 1, or 2)
     */
    default int crossLink(Task currentTask, Task previousTask, float scale, NAR nar) {
        Compound otherTerm = previousTask.term();
        if (otherTerm.equals(get())) return 0; //self

        int count = 0;
        count += link(previousTask, scale, nar) ? 1 : 0;

        Concept other = nar.concept(otherTerm);
        if (other == null) return 0;

        count += other.link(currentTask, scale, nar) ? 1 : 0;

        return count;

    }



//    public Task getTask(boolean hasQueryVar, long occTime, Truth truth, List<Task>... lists);
//
//    default public Task getTask(Sentence query, List<Task>... lists) {
//        return getTask(query.hasQueryVar(), query.getOccurrenceTime(), query.getTruth(), lists);
//    }

}

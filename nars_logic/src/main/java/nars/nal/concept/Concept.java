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
package nars.nal.concept;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.nal.*;
import nars.nal.stamp.Stamp;
import nars.nal.term.Term;
import nars.nal.term.Termed;
import nars.nal.tlink.*;

import java.io.PrintStream;
import java.util.*;

import static com.google.common.collect.Iterators.*;
import static nars.nal.UtilityFunctions.or;

abstract public interface Concept extends Termed, Itemized<Term> {


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

    default NALOperator operator() {
        return getTerm().operator();
    }

    /**
     * Select a belief to interact with the given task in logic
     * <p>
     * get the first qualified one
     * <p>
     * only called in RuleTables.rule
     *
     * @param task The selected task
     * @return The selected isBelief
     */
    default public Sentence getBelief(final NAL nal, final Task task) {
        if (!hasBeliefs()) return null;

        final Stamp taskStamp = task.sentence.stamp;
        final long currentTime = getMemory().time();
        long occurrenceTime = taskStamp.getOccurrenceTime();

        final int b = getBeliefs().size();
        for (int i = 0; i < b; i++) {
            Sentence belief = getBeliefs().get(i).sentence;

            //if (task.sentence.isEternal() && belief.isEternal()) return belief;

            Sentence projectedBelief = belief.projection(occurrenceTime, currentTime);
            if (projectedBelief.getOccurrenceTime()!=belief.getOccurrenceTime()) {
                nal.singlePremiseTask(projectedBelief, task);
            }

            return projectedBelief;     // return the first satisfying belief
        }
        return null;
    }

    /**
     * whether a concept's desire exceeds decision threshold
     */
    default public boolean isDesired() {
        return isDesired(getMemory().param.decisionThreshold.floatValue());
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
            for (final Task s : getBeliefs())
                s.getTruth().discountConfidence();
        }
    }

    default public void discountGoalConfidence() {
        if (hasGoals()) {
            for (final Task s : getGoals()) {
                s.getTruth().discountConfidence();
            }
        }
    }

    boolean isConstant();

    /** allows concept state to be locked */
    boolean setConstant(boolean b);

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
        Truth topValue = getGoals().get(0).getTruth();
        return topValue;
    }

    public List<Task> getGoals();
    public boolean hasGoals();

    public List<Task> getBeliefs();
    public boolean hasBeliefs();

    public List<Task> getQuestions();
    public boolean hasQuestions();

    public List<Task> getQuests();
    public boolean hasQuests();

    public State getState();
    public long getCreationTime();
    public long getDeletionTime();
    public Concept setState(State nextState);

    default public boolean isDeleted() {
        return getState() == Concept.State.Deleted;
    }

    default public boolean isActive() {
        return getState() == State.Active;
    }
    default public boolean isForgotten() {
        return getState() == State.Forgotten;
    }


    public void delete();

    default public boolean ensureActiveFor(String activity) {
        if (!this.isActive()) {
            System.err.println(activity + " fail: " + this + " (state=" + getState() + ')');
            new Exception().printStackTrace();
            return false;
        }
        return true;
    }

    public boolean process(final TaskProcess nal);

    /** returns the best belief of the specified types */
    default public Task getStrongestBelief(boolean eternal, boolean nonEternal) {
        return getStrongestTask(getBeliefs(), eternal, nonEternal);
    }


    default public Task getStrongestGoal(boolean eternal, boolean nonEternal) {
        return getStrongestTask(getGoals(), eternal, nonEternal);
    }

    /** temporary until goal is separated into goalEternal, goalTemporal */
    @Deprecated default public Sentence getStrongestSentence(List<Task> table, boolean eternal, boolean temporal) {
        for (Task t : table) {
            Sentence s = t.sentence;
            boolean e = s.isEternal();
            if (e && eternal) return s;
            if (!e && temporal) return s;
        }
        return null;
    }
    /** temporary until goal is separated into goalEternal, goalTemporal */
    @Deprecated default public Task getStrongestTask(final List<Task> table, final boolean eternal, final boolean temporal) {
        for (Task t : table) {
            boolean e = t.isEternal();
            if (e && eternal) return t;
            if (!e && temporal) return t;
        }
        return null;
    }

    public static Sentence getStrongestSentence(List<Task> table) {
        Task t = getStrongestTask(table);
        if (t!=null) return t.sentence;
        return null;
    }

    public static Task getStrongestTask(List<Task> table) {
        if (table == null) return null;
        if (table.isEmpty()) return null;
        return table.get(0);
    }

    default public Task getStrongestBelief() {
        if (hasBeliefs())
            return getStrongestBelief(true, true);
        return null;
    }

    /**
     * Methods to be implemented by Concept meta instances
     */
    public static interface Meta {

        /** called before the state changes to the given nextState */
        public void onState(Concept c, State nextState);

    }

    public static final class TermLinkNoveltyFilter implements Predicate<TermLink> {

        TaskLink taskLink;
        private long now;
        private int noveltyHorizon;
        private int recordLength;

        public void set(TaskLink t, long now, int noveltyHorizon, int recordLength) {
            this.taskLink = t;
            this.now = now;
            this.noveltyHorizon = noveltyHorizon;
            this.recordLength = recordLength;
        }

        @Override
        public boolean apply(TermLink termLink) {
            return taskLink.novel(termLink, now, noveltyHorizon, recordLength);
        }
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
                out.println((int) (rankBelief(s, now) * 100.0) + "%: " + s);
            }
        }

        if (showgoals) {
            out.print(" Goals:");
            if (getGoals().isEmpty()) out.println(" none");
            else out.println();
            for (Task s : getGoals()) {
                out.print(indent);
                out.println((int) (rankBelief(s, now) * 100.0) + "%: " + s);
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

    public float rankBelief(final Sentence s, final long now);

    default public float rankBelief(final Task s, final long now) {
        return rankBelief(s.sentence, now);
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


}

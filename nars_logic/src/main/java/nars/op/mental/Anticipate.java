/*
 * Believe.java
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


package nars.op.mental;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.Interval;
import nars.nal.nal7.Tense;
import nars.task.FluentTask;
import nars.task.Task;
import nars.term.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import objenome.solver.evolve.grammar.Symbol;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 something expected did not happen
 anticipation will generate a negative event as consequence
 */
public class Anticipate {

    final static Truth expiredTruth = new DefaultTruth(0.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    final static Budget expiredBudget = new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(expiredTruth));

    final Multimap<Compound,TaskTime> anticipations = LinkedHashMultimap.create();

    /** buffers the terms of new incoming tasks */
    final Set<Compound> newTaskTerms = Global.newHashSet(16);
    private final NAR nar;

    private Memory memory;
    private final boolean debug = false;
    private long nextUpdateTime = -1;

    public Anticipate(NAR nar) {
        this.nar = nar;
        this.memory = nar.memory;

        nar.memory.eventCycleEnd.on(c -> {
           updateAnticipations();
        });
    }

    public double DEFAULT_CONFIRMATION_EXPECTATION = 0.6;
    public void anticipate(Compound term, long occurrenceTime, Task t) {

        if(t.getTruth().getExpectation() < DEFAULT_CONFIRMATION_EXPECTATION || t.getPunctuation() != Symbols.JUDGMENT) {
            return;
        }

        long now = memory.time();

        if (memory.time() > occurrenceTime) //its about the past..
            return;

        if (debug)
            System.err.println("Anticipating " + term + " in " + (occurrenceTime - now));

        TaskTime tt = new TaskTime(t, t.getCreationTime());
        anticipations.put(term, tt);
        updateNextRequiredTime(tt, now);
    }

    protected void deriveDidntHappen(Compound prediction, TaskTime tt) {

        long expectedOccurrenceTime = tt.getOccurrenceTime();

        //it did not happen, so the time of when it did not
        //happen is exactly the time it was expected

        if (debug)
            System.err.println("Anticipation Negated " + tt.task);

        final Task derived = new FluentTask<>(prediction)
                        .punctuation(Symbols.JUDGMENT)
                        .truth(expiredTruth)
                        .budget(tt.getBudget())
                        .time(memory.time(), expectedOccurrenceTime)
                        .parent(tt.task, null);

        nar.input(derived);
    }

    /** called each cycle to update calculations of anticipations */
    protected void updateAnticipations() {

        long now = nar.memory.time();

        if (anticipations.isEmpty()) return;

        int news = newTaskTerms.size(), dids = 0, didnts =0, expireds = 0;

        //1. filter anticipations which occurred
        for (Compound t : newTaskTerms) {

            Collection<TaskTime> a = anticipations.get(t);
            if (a == null) continue;

            Iterator<TaskTime> tti = a.iterator();
            while (tti.hasNext()) {

                TaskTime tt = tti.next();

                if (now > tt.expiredate) {
                    tti.remove();
                    expireds++;
                    continue;
                }

                if (!tt.didNotAlreadyOccurr(now)) {
                    //it happened, general control will do the rest
                    tti.remove();
                    dids++;
                }
            }
        }

        newTaskTerms.clear();

        //2. derive from remaining anticipations

        Iterator<Map.Entry<Compound, TaskTime>> it = anticipations.entries().iterator();
        while (it.hasNext()) {

            Map.Entry<Compound, TaskTime> t = it.next();
            Compound term = t.getKey();
            TaskTime tt = t.getValue();

            if (now > tt.expiredate) {
                deriveDidntHappen(term, tt);

                it.remove();

                didnts++;
            }
            else {
                updateNextRequiredTime(tt, now);
            }
        }

        if (debug)
            System.err.println(now + ": Anticipations: pending=" + anticipations.size() + " newTasks=" + news + ", dids=" + dids + " , didnts=" + didnts + ", expired=" + expireds);
    }

    private void updateNextRequiredTime(final TaskTime tt, final long now) {
        final long nextRequiredUpdate = tt.expiredate;
        if ((nextUpdateTime == -1) || (nextUpdateTime > nextRequiredUpdate) && (nextRequiredUpdate > now)) {
            nextUpdateTime = nextRequiredUpdate;
        }
    }

    public void anticipate(Task t) {
        anticipate(t.getTerm(), t.getOccurrenceTime(), t);
    }

    /** Prediction point vector / centroid of a group of Tasks
     *      time a prediction is made (creationTime), and
     *      tme it is expected (ocurrenceTime) */
    public static class TaskTime {

        /** all data is from task */
        final public Task task;

        /** cached locally, same value as in task */
        final public long occurrTime;
        public long creationTime;

        /** cached locally, same value as in task */
        final public long expiredate;
        private final int hash;

        public TaskTime(Task task, long creationTime) {
            super();
            this.task = task;
            this.creationTime = task.getCreationTime();
            this.occurrTime = task.getOccurrenceTime();
            this.hash = (int)(31 * creationTime + occurrTime);
            expiredate = getOccurrenceTime()+task.duration();
        }

        public float getPriority() { return task.getPriority(); }

        /** when the prediction happened */
        public long getCreationTime() { return creationTime; }

        /** when the event is expected */
        public long getOccurrenceTime() { return occurrTime; }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            TaskTime t = (TaskTime)obj;
            return creationTime == t.creationTime  &&  occurrTime == t.occurrTime;
        }

        public Budget getBudget() {
            return task.getBudget();
        }
    }
}
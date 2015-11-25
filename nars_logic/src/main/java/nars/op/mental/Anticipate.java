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
import nars.task.FluentTask;
import nars.task.Task;
import nars.term.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.*;

/**
 something expected did not happen
 anticipation will generate a negative event as consequence
 */
public class Anticipate {

    public double DEFAULT_CONFIRMATION_EXPECTATION = 0.51;
    public static double TOLERANCE_DIV=5.0;

    final static Truth expiredTruth = new DefaultTruth(0.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    final static Budget expiredBudget = new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(expiredTruth));

    final Multimap<Compound,TaskTime> anticipations = LinkedHashMultimap.create();

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

        nar.memory.eventInput.on(c -> {
            mayHaveHappenedAsExpected(c);
        });

        nar.memory.eventDerived.on(c -> {
            mayHaveHappenedAsExpected(c);
        });
    }


    public void anticipate(Task t) {

        if(t.getTruth().getExpectation() < DEFAULT_CONFIRMATION_EXPECTATION || t.getPunctuation() != Symbols.JUDGMENT) {
            return;
        }

        long now = memory.time();

        if (memory.time() > t.getOccurrenceTime()) //its about the past..
            return;

        if (debug)
            System.err.println("Anticipating " + t.getTerm() + " in " + (t.getOccurrenceTime() - now));

        TaskTime tt = new TaskTime(t, t.getCreationTime());
        System.out.println("anticipating: "+tt.task.getTerm().toString());
        anticipations.put(t.getTerm(), tt);
    }

    protected void deriveDidntHappen(Compound prediction, TaskTime tt) {

        System.out.println("did not happen: "+prediction.toString());
        long expectedOccurrenceTime = tt.occurrTime;

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

    protected void mayHaveHappenedAsExpected(Task c) {
        if(!c.isInput()) {
            return; //it's not a input task, the system is not allowed to convince itself about the state of affairs ^^
        }
        if(!c.isEternal()) {
            Collection<TaskTime> res = anticipations.get(c.getTerm());
            ArrayList<TaskTime> ToRemove = new ArrayList<TaskTime>();
            for(TaskTime tt : res) {
                if(tt.inTime(c.getOccurrenceTime()) && !c.equals(tt.task) && tt.task.getTruth().getExpectation() > DEFAULT_CONFIRMATION_EXPECTATION) {
                    ToRemove.add(tt);
                    happeneds++;
                    System.out.println("happened as expected: "+tt.task.getTerm().toString());
                }
            }
            for(TaskTime tt : ToRemove) {
                anticipations.get(c.getTerm()).remove(tt);
            }
        }
    }

    /** called each cycle to update calculations of anticipations */
    int happeneds = 0, didnts = 0;

    protected void updateAnticipations() {

        long now = nar.memory.time();

        if (anticipations.isEmpty()) return;

        Iterator<Map.Entry<Compound, TaskTime>> it = anticipations.entries().iterator();

        while (it.hasNext()) {

            Map.Entry<Compound, TaskTime> t = it.next();
            Compound term = t.getKey();
            TaskTime tt = t.getValue();

            if (tt.tooLate(now)) {
                deriveDidntHappen(term, tt);
                it.remove();
                didnts++;
            }
        }

        if (debug)
            System.err.println(now + ": Anticipations: pending=" + anticipations.size() + " happened=" + happeneds + " , didnts=" + didnts);
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
        private final int hash;
        public double tolerance = 0;

        public TaskTime(Task task, long creationTime) {
            super();
            this.task = task;
            this.creationTime = task.getCreationTime();
            this.occurrTime = task.getOccurrenceTime();
            this.hash = (int)(31 * creationTime + occurrTime);
            //expiredate in relation how long we predicted forward
            long prediction_time = this.occurrTime - this.creationTime;
            tolerance = prediction_time/TOLERANCE_DIV;
        }

        public boolean tooLate(long occur) {
            return occur > occurrTime + TOLERANCE_DIV;
        }

        public boolean inTime(long occur) {
            return occur > occurrTime - TOLERANCE_DIV && occur < occurrTime + TOLERANCE_DIV;
        }

        public float getPriority() { return task.getPriority(); }

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
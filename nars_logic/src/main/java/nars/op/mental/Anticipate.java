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
import nars.*;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.Parallel;
import nars.nal.nal7.Sequence;
import nars.task.FluentTask;
import nars.task.Task;
import nars.term.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 something expected did not happen
 anticipation will generate a negative event as consequence
 */
public class Anticipate {

    public float DEFAULT_CONFIRMATION_EXPECTATION = 0.51f;
    public static float TOLERANCE_DIV=0.5f; //can even take 2 times as long its fine

    final static Truth expiredTruth = new DefaultTruth(0.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    final static Budget expiredBudget = new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(expiredTruth));
    final static float priorityBoostOnDisappointment = 1.5f;

    final Multimap<Compound,TaskTime> anticipations = LinkedHashMultimap.create();

    private final NAR nar;
    private Memory memory;
    private final boolean debug = false;

    public Anticipate(NAR nar) {
        this.nar = nar;
        this.memory = nar.memory;

        nar.memory.eventCycleEnd.on(c -> updateAnticipations());
        nar.memory.eventInput.on(this::onInput);
    }

    public void onInput(Task t) {
        if (t.isInput())
               mayHaveHappenedAsExpected(t);
      }

    public void anticipate(Task t) {

        Compound tt = t.getTerm();
        long now = memory.time();
        long occ = t.getOccurrenceTime();

        if (t.getPunctuation() != Symbols.JUDGMENT || t.getTruth().getExpectation() < DEFAULT_CONFIRMATION_EXPECTATION) {
            return; //Besides that the task has to be a judgement, if the truth expectation is below confirmation expectation,
        }           //the truth value of the incoming event was too low to confirm that the expected event has happened.

        if(nar.concept(tt).get(Anticipate.class) == null || now > occ) { //it's not observable, or about thee future
            return;                                            //in the former case CWA can not be applied in general
        }                                                      //and in the latter case anticipation is pointless

        if (debug)
            System.err.println("Anticipating " + tt + " in " + (t.getOccurrenceTime() - now));

        TaskTime taskTime = new TaskTime(t, t.getCreationTime());

        if(true || testing) {
            String s = "anticipating: "+taskTime.task.getTerm().toString();
            System.out.println(s);
            teststring += s + "\n";
        }

        anticipations.put(tt, taskTime);
    }

    protected void deriveDidntHappen(Compound prediction, TaskTime tt) {

        if(true || testing) {
            String s = "did not happen: " + prediction.toString();
            System.out.println(s);
            teststring += s + "\n";
        }

        long expectedOccurrenceTime = tt.occurrTime;

        //it did not happen, so the time of when it did not
        //happen is exactly the time it was expected

        if (debug)
            System.err.println("Anticipation Negated " + tt.task);

        Budget expiredBoostedBudget = expiredBudget.clone();
        expiredBoostedBudget.setPriority(priorityBoostOnDisappointment*expiredBoostedBudget.getPriority());

        final Task derived = new FluentTask<>(prediction)
                .belief()
                .truth(expiredTruth.getFrequency(), expiredTruth.getConfidence())
                .budget(expiredBoostedBudget)
                .time(memory.time(), expectedOccurrenceTime)
                .parent(tt.task, null)
                .because("Absent Anticipated Event")
                ;

        nar.input(derived);
    }

    List<TaskTime> toRemove = Global.newArrayList();


    protected void mayHaveHappenedAsExpected(Task c) {

        if(!c.isInput() || c.isEternal() || !c.isJudgment()) {
            return; //it's not a input task, the system is not allowed to convince itself about the state of affairs ^^
        }

        toRemove.clear();

        long cOccurr = c.getOccurrenceTime();

        for(TaskTime tt : anticipations.get(c.getTerm())) {

            if(tt.inTime(cOccurr) && !c.equals(tt.task) &&
                    c.getTruth().getExpectation() > DEFAULT_CONFIRMATION_EXPECTATION) {

                toRemove.add(tt);

                happeneds++;
                if(true || testing) {
                    String s = "happened as expected: "+tt.task.getTerm().toString();
                    System.out.println(s);
                    teststring += s + "\n";
                }
            }
        }

        toRemove.forEach(tt -> anticipations.remove(c.getTerm(),tt));
    }

    /** called each cycle to update calculations of anticipations */
    int happeneds = 0, didnts = 0;

    public static boolean testing = false;
    public static String teststring = "";
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
    public static final class TaskTime {

        /** all data is from task */
        final public Task task;

        /** cached locally, same value as in task */
        final public long occurrTime;
        public long creationTime;

        /** cached locally, same value as in task */
        private final int hash;
        public float tolerance = 0;

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
            return occur > occurrTime + tolerance;
        }

        public boolean inTime(long occur) {
            return occur > occurrTime - tolerance && occur < occurrTime + tolerance;
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
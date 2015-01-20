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


package nars.operator.mental;


import java.util.*;

import nars.core.Events;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.core.Parameters;
import nars.event.AbstractReaction;
import nars.io.Symbols;
import nars.logic.BudgetFunctions;
import nars.logic.NAL;
import nars.logic.entity.*;
import nars.logic.nal4.Product;
import nars.logic.nal5.Conjunction;
import nars.logic.nal7.Interval;
import nars.logic.nal7.TemporalRules;
import nars.logic.nal8.Operation;
import nars.operator.ReactiveOperator;

/**
 * Operator that creates a judgment with a given statement
 */
public class Anticipate extends ReactiveOperator implements Mental {

    final static TruthValue expiredTruth = new TruthValue(0.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
    final static BudgetValue expiredBudget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(expiredTruth));


    final Deque<TaskTimes> anticipations = new ArrayDeque();

    final LinkedHashSet<Task> newTasks = new LinkedHashSet();

    NAL nal;
    AbstractReaction reaction;

    //a parameter which tells whether NARS should know if it anticipated or not
    //in one case its the base functionality needed for NAL8 and in the other its a mental NAL9 operator
    boolean operatorEnabled = true;
    private Memory memory;


    public Anticipate() {
        super("^anticipate");
    }

    @Override
    public Class[] getEvents() {
        return new Class[]{
                Events.TaskDeriveFuture.class,
                Events.InduceSucceedingEvent.class,
                Events.CycleEnd.class
        };
    }

    public void anticipate(Term term, long occurenceTime, Task t) {
        if (memory == null)
            memory = nal.memory;

        if (term instanceof Conjunction && term.getTemporalOrder() != TemporalRules.ORDER_NONE) {
            return;
        }

        if (memory.time() > occurenceTime) //its about the past..
            return;

        anticipations.add(new TaskTimes(memory.time(), occurenceTime) );

        if (operatorEnabled) {

            Operation op = (Operation) Operation.make(Product.make(term), this);

            TruthValue truth = new TruthValue(1.0f, 0.90f);

            Stamp st;
            if (t == null)
                st = new Stamp(memory);
            else
                st = t.sentence.stamp.clone().setOccurrenceTime(memory.time());


            Task newTask = new Task(
                    new Sentence<>(op, Symbols.JUDGMENT_MARK, truth, st),
                    new BudgetValue(
                        Parameters.DEFAULT_JUDGMENT_PRIORITY * InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL,
                        Parameters.DEFAULT_JUDGMENT_DURABILITY * InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL,
                        BudgetFunctions.truthToQuality(truth))
            );
            memory.addNewTask(newTask, "Perceived Anticipation");
        }
    }

    protected void deriveDidntHappen(Task prediction, long expectedOccurenceTime) {

        //it did not happen, so the time of when it did not
        //happen is exactly the time it was expected
        //todo analyze, why do i need to substract duration here? maybe it is just accuracy thing

        Task task = new Task(
                new Sentence<>(prediction.getTerm(),
                        Symbols.JUDGMENT_MARK,
                        expiredTruth,
                        new Stamp(nal.memory).
                                setOccurrenceTime(
                                        expectedOccurenceTime -
                                                nal.memory.param.duration.get())),
                expiredBudget
        );

        System.out.println("Anticipation Negated" + task);
        nal.derivedTask(task, false, true, null, null);

        //should this happen before derivedTask?  it might get stuck in a loop if derivation proceeds before this sets
        task.setParticipateInTemporalInduction(true);
    }

    /** called each cycle to update calculations of anticipations */
    protected void updateAnticipations() {

        if (anticipations.isEmpty()) return;

        long now = nal.memory.time();
        long dur = nal.memory.getDuration();

        //share stamps created by tasks in this cycle

        boolean hasNewTasks = !newTasks.isEmpty();

        System.out.println("Anticipations=" + anticipations.size() + " NewPredictions=" + newTasks.size());
        Iterator<TaskTimes> tti = anticipations.iterator();
        while (tti.hasNext()) {

            TaskTimes tt = tti.next();

            if (!tt.relevant(now, dur)) {
                tti.remove();
                continue;
            }
            long aTime = tt.occurrTime;
            long predictionstarted = tt.creationTime;

            //Not necessary since such Prediction vectors will not be created
//            //maybe ths should never have been created in the first place
//            if (aTime < predictionstarted) { //its about the past..
//                aei.remove();
//                continue;
//            }

            //lets say a and <(&/,a,+4) =/> b> leaded to prediction of b with specific occurence time
            //this indicates that this interval can be reconstructed by looking by when the prediction
            //happened and for what time it predicted, Only when the happening would already lead to <(&/,a,+5) =/> b>
            //we are allowed to apply CWA already, i think this is the perfect time to do this
            //since there is no way anymore that the observation would support <(&/,a,+4) =/> b> at this time,
            //also this way it is not applied to early, it seems to be the perfect time to me,
            //making hopeExpirationWindow parameter entirely osbolete
            Interval Int = Interval.interval(aTime - predictionstarted, nal.memory);
            //ok we know the magnitude now, let's now construct a interval with magnitude one higher
            //(this we can skip because magnitudeToTime allows it without being explicitly constructed)
            //ok, and what predicted occurence time would that be? because only if now is bigger or equal, didnt happen is true
            double expiredate = predictionstarted + Interval.magnitudeToTime(Int.magnitude + 1, nal.memory.param.duration);
            //

            boolean didntHappen = (now >= expiredate);
            boolean maybeHappened = hasNewTasks && !didntHappen;

            if ((!didntHappen) && (!maybeHappened))
                continue;

            Set<Task> knownPredictions = tt.tasks;



            Iterator<Task> ii = knownPredictions.iterator();
            while (ii.hasNext()) {
                Task aTerm = ii.next();

                boolean remove = false;

                if (didntHappen) {
                    deriveDidntHappen(aTerm, aTime);
                    remove = true;
                }

                if (maybeHappened) {
                    if (newTasks.remove(aTerm)) {
                        //it happened, temporal induction will do the rest          
                        remove = true;
                        hasNewTasks = !newTasks.isEmpty();
                    }
                }

                if (remove)
                    ii.remove();
            }

            if (knownPredictions.isEmpty()) {
                //remove this time entry because its terms have been emptied
                tti.remove();
            }
        }

        newTasks.clear();
    }

    @Override
    public void event(Class event, Object[] args) {

        if (event == Events.TaskDeriveFuture.class) {

            Task newEvent = (Task) args[0];
            this.nal = (NAL)args[1];
            anticipate(newEvent);

        } else if (event == Events.InduceSucceedingEvent.class) {

            Task newEvent = (Task) args[0];
            this.nal = (NAL)args[1];
            if (newEvent.sentence.truth != null)
                newTasks.add(newEvent); //new: always add but keep truth value in mind

        } else if (nal != null && event == CycleEnd.class) {

            updateAnticipations();

        }

    }


    //*
    // * To create a judgment with a given statement
    // * @param args Arguments, a Statement followed by an optional tense
    // * @param memory The memory in which the operation is executed
    // * @return Immediate results as Tasks
    //  *
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        if (operation != null)
            return null; //not as mental operator but as fundamental principle

        anticipate(args[0], memory.time() + memory.getDuration(), operation.getTask());

        return null;
    }





    public boolean isOperatorEnabled() {
        return operatorEnabled;
    }

    public void setOperatorEnabled(boolean val) {
        operatorEnabled = val;
    }

    private void anticipate(Task t) {
        anticipate(t.getTerm(), t.getOcurrenceTime(), t);
    }


    /** Prediction point vector / centroid of a group of Tasks
     *      time a prediction is made (creationTime), and
     *      tme it is expected (ocurrenceTime) */
    public static class TaskTimes {
        public long creationTime; //2014 and this is still the best way to define a data structure that simple?
        public long occurrTime;

        Set<Task> tasks = new LinkedHashSet();

        public TaskTimes(long creationTime, long occurrTime) {
            this.creationTime = creationTime; //when the prediction happened
            this.occurrTime = occurrTime; //when the event is expected
        }

        public boolean relevant(long now, long duration) {
            return !(occurrTime < now - duration/2);
        }
    }
}
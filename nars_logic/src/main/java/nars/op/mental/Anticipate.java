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
import nars.Events.CycleEnd;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.event.NARReaction;
import nars.nal.*;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.Interval;
import nars.nal.nal7.TemporalRules;
import nars.nal.stamp.Stamp;
import nars.nal.term.Term;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Operator that creates a judgment with a given statement
 *
    b is expected, but b suddenly enters the system with low frequency?

 Fine, no need to apply closed world assumption

 "what I predicted to observe but I didnt observe didn't happen"
 because it was observed is expected but b does not happen?

 this means NARS predicted to experience something which did not occur

 and as such anticipation will now generate a negative event

 * "if I predicted to observe it but I didnt observe it is evidence that I didn't observe it" makes sense, while the general assumption often taken in AI is not: "what I didn't observe didn't happen"
 *
 */
public class Anticipate extends NARReaction implements Mental {

    final static Truth expiredTruth = new DefaultTruth(0.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
    final static Budget expiredBudget = new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(expiredTruth));


    final Multimap<Term,TaskTime> anticipations = LinkedHashMultimap.create();

    /** buffers the terms of new incoming tasks */
    final Set<Term> newTaskTerms = Global.newHashSet(16);

    NAL nal;
    nars.event.NARReaction reaction;

    //a parameter which tells whether NARS should know if it anticipated or not
    //in one case its the base functionality needed for NAL8 and in the other its a mental NAL9 operate
    boolean operatorEnabled = false;
    private Memory memory;
    private boolean debug = false;
    private long nextUpdateTime = -1;


    /*public Anticipate() {
        super("^anticipate");
    }*/
    public Anticipate(NAR nar) {
        super(nar, Events.TaskDeriveFuture.class,
                Events.InduceSucceedingEvent.class,
                Events.CycleEnd.class);
    }


    public void anticipate(Term term, long occurenceTime, Task t) {
        if (memory == null)
            memory = nal.memory;

        if (term instanceof Conjunction && term.getTemporalOrder() != TemporalRules.ORDER_NONE) {
            return;
        }

        if(t.sentence.truth.getExpectation()< Global.DEFAULT_CONFIRMATION_EXPECTATION) {
            return;
        }

        long now = memory.time();

        if (memory.time() > occurenceTime) //its about the past..
            return;

        if (debug)
            System.err.println("Anticipating " + term + " in " + (occurenceTime - now));

        TaskTime tt = new TaskTime(t, memory.param.duration);
        anticipations.put(term, tt);
        updateNextRequiredTime(tt, now);

        /*
        Move this to a separate Operator class
        if (operatorEnabled) {

            Operation op = (Operation) Operation.make(Product.make(term), this);

            TruthValue truth = new TruthValue(1.0f, 0.90f);

            Stamp st;
            if (t == null)
                st = new Stamp(memory);
            else
                st = t.sentence.stamp.clone().setOccurrenceTime(memory.time());


            Task newTask = new Task(
                    new Sentence<>(op, Symbols.JUDGMENT, truth, st),
                    new BudgetValue(
                        Parameters.DEFAULT_JUDGMENT_PRIORITY * InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL,
                        Parameters.DEFAULT_JUDGMENT_DURABILITY * InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL,
                        BudgetFunctions.truthToQuality(truth))
            );
            memory.addNewTask(newTask, "Perceived Anticipation");
        }
        */
    }

    protected void deriveDidntHappen(Term prediction, TaskTime tt) {

        long expectedOccurenceTime = tt.getOccurrenceTime();

        //it did not happen, so the time of when it did not
        //happen is exactly the time it was expected
        //todo analyze, why do i need to substract duration here? maybe it is just accuracy thing

        Task task = new Task(
                new Sentence<>(prediction,
                        Symbols.JUDGMENT,
                        expiredTruth,
                        new Stamp(nal.memory, expectedOccurenceTime /*- nal.memory.param.duration.get()*/ )),
                //expiredBudget
                tt.getBudget().clone(),
                tt.task
        );

        if (debug)
            System.err.println("Anticipation Negated " + task);

        nal.deriveTask(task, false, true, null, false);

        //should this happen before derivedTask?  it might get stuck in a loop if derivation proceeds before this sets
        task.setParticipateInTemporalInduction(true);
    }

    /** called each cycle to update calculations of anticipations */
    protected void updateAnticipations() {

        long now = nal.memory.time();

        if (anticipations.isEmpty()) return;

        int dur = nal.memory.duration();

        int news = newTaskTerms.size(), dids = 0, didnts =0, expireds = 0;

        //1. filter anticipations which occurred
        for (Term t : newTaskTerms) {

            Collection<TaskTime> a = anticipations.get(t);
            if (a == null) continue;

            Iterator<TaskTime> tti = a.iterator();
            while (tti.hasNext()) {

                TaskTime tt = tti.next();

                long tOccurrs = tt.occurrTime;

                if (now > tOccurrs - dur/2) {
                    tti.remove();
                    expireds++;
                    continue;
                }

                if (!tt.didNotAlreadyOccurr(now)) {
                    //it happened, temporal induction will do the rest
                    tti.remove();
                    dids++;
                }
            }

        }

        newTaskTerms.clear();

        if ((nextUpdateTime == -1) || (nextUpdateTime > now)) return;

        nextUpdateTime = -1;

        //2. derive from remaining anticipations

        Iterator<Map.Entry<Term, TaskTime>> it = anticipations.entries().iterator();
        while (it.hasNext()) {

            Map.Entry<Term, TaskTime> t = it.next();
            Term term = t.getKey();
            TaskTime tt = t.getValue();

            if (tt.didNotAlreadyOccurr(now)) {
                deriveDidntHappen(term, tt);

                it.remove();

                didnts++;
            }
            else {
                updateNextRequiredTime(tt, now);
            }
        }


        if (debug)
            System.err.println(now + ": Anticipations: pending=" + anticipations.size() + " newTasks=" + news + ", dids=" + dids + " , didnts=" + didnts + ", expired=" + expireds + ", nextUpdate=" + nextUpdateTime);


    }

    private void updateNextRequiredTime(final TaskTime tt, final long now) {
        final long nextRequiredUpdate = tt.expiredate;
        if ((nextUpdateTime == -1) || (nextUpdateTime > nextRequiredUpdate) && (nextRequiredUpdate > now)) {
            nextUpdateTime = nextRequiredUpdate;
        }
    }

//    @Override
//    public void onEnabled(NAR n) {
//            newTaskTerms.clear();
//            anticipations.clear();
//    }
//
//    @Override
//    public void onDisabled(NAR n) {
//
//    }

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
                newTaskTerms.add(newEvent.getTerm()); //new: always add but keep truth value in mind

        } else if (nal != null && event == CycleEnd.class) {

            updateAnticipations();

        }

    }


    //TODO move this to a separate operate class
    //*
    // * To create a judgment with a given statement
    // * @param args Arguments, a Statement followed by an optional tense
    // * @param memory The memory in which the operation is executed
    // * @return Immediate results as Tasks
    //  *
    /*
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        if (operation != null)
            return null; //not as mental operate but as fundamental principle

        anticipate(args[0], memory.time() + memory.getDuration(), operation.getTask());

        return null;
    }
    */





    public boolean isOperatorEnabled() {
        return operatorEnabled;
    }

    public void setOperatorEnabled(boolean val) {
        operatorEnabled = val;
    }

    private void anticipate(Task t) {
        anticipate(t.getTerm(), t.getOccurrenceTime(), t);
    }


    /** Prediction point vector / centroid of a group of Tasks
     *      time a prediction is made (creationTime), and
     *      tme it is expected (ocurrenceTime) */
    public static class TaskTime {

        /** all data is from task */
        final public Task task;

        /** cached locally, same value as in task */
        final public long creationTime; //2014 and this is still the best way to define a data structure that simple
        final public long occurrTime;

        /** cached locally, same value as in task */
        final public long expiredate;
        private final int hash;

        public TaskTime(Task task, Interval.AtomicDuration dura) {
            super();
            this.task = task;
            this.creationTime = task.getCreationTime();
            this.occurrTime = task.getOccurrenceTime();
            this.hash = (int)(31 * creationTime + occurrTime);

            //lets say a and <(&/,a,+4) =/> b> leaded to prediction of b with specific occurence time
            //this indicates that this interval can be reconstructed by looking by when the prediction
            //happened and for what time it predicted, Only when the happening would already lead to <(&/,a,+5) =/> b>
            //we are allowed to apply CWA already, i think this is the perfect time to do this
            //since there is no way anymore that the observation would support <(&/,a,+4) =/> b> at this time,
            //also this way it is not applied to early, it seems to be the perfect time to me,
            //making hopeExpirationWindow parameter entirely osbolete
            int m = Interval.magnitude(getOccurrenceTime() - getCreationTime(), dura);

            //ok we know the magnitude now, let's now construct a interval with magnitude one higher
            //(this we can skip because magnitudeToTime allows it without being explicitly constructed)
            //ok, and what predicted occurence time would that be? because only if now is bigger or equal, didnt happen is true
            expiredate = getCreationTime() + Interval.cycles(m + 1, dura);
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

        public boolean didNotAlreadyOccurr(long now) {
            return now >= expiredate;
        }

        public boolean relevant(long now, int durationCycles) {
            return TemporalRules.concurrent(now, occurrTime, durationCycles);
        }

        public Budget getBudget() {
            return task;
        }
    }
}
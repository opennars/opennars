/*
 * Operator.java
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

package nars.nal.nal8;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.nal.nal8.decide.DecideAboveDecisionThreshold;
import nars.nal.nal8.decide.Decider;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Atom;
import nars.term.Term;
import nars.util.event.Reaction;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * An individual operate that can be execute by the system, which can be either
 * inside NARS or outside it, in another system or device.
 * <p>
 * This is the only file to modify when registering a new operate into NARS.
 * <p>
 * An instance of an Operator must not be shared by multiple Memory
 * since it will be associated with a particular one.  Create a separate one for each
 */
abstract public class OpReaction implements Function<Operation,List<Task>>, Reaction<Term,Operation>, Serializable {


    public final Term term;

    transient protected NAR nar;


    @Override
    public String toString() {
        return "^" + term.toString();
    }

    public OpReaction(Term term) {
        if (term == null) {
            term = Atom.the(getClass().getSimpleName());
        }
        this.term = term;
    }

    public OpReaction(String name) {
        this.term = Atom.the(name);
    }

    public boolean setEnabled(NAR n, boolean enabled) {
        if (enabled)
            this.nar = n;
        else
            this.nar = null;
        return enabled;
    }

    /**
     * use the class name as the operator name
     */
    public OpReaction() {
        String className = getClass().getSimpleName();
        this.term = Atom.the(className);
    }


    public final NAR nar() {
        return nar;
    }

    public Decider decider() {
        return DecideAboveDecisionThreshold.the;
    }


    @Override
    public void event(Term event, Operation o) {
        if (o.getTask().isCommand() || decider().test(o)) {
            execute(o);
        }
    }

    /**
     * Required method for every operate, specifying the corresponding
     * operation
     *
     * @return The direct collectable results and feedback of the
     * reportExecution
     */
    @Deprecated /* just use the apply method */
    protected abstract boolean execute(Operation input);



    public final Term getTerm() {
        return term;
    }


    /*
    <patham9_> when a goal task is processed, the following happens: In order to decide on whether it is relevant for the current situation, at first it is projected to the current time, then it is revised with previous "desires", then it is checked to what extent this projected revised desire is already fullfilled (which revises its budget) , if its below satisfaction threshold then it is pursued, if its an operation it is additionally checked if
    <patham9_> executed
    <patham9_> the consequences of this, to give examples, are a lot:
    <patham9_> 1 the system wont execute something if it has a strong objection against it. (example: it wont jump down again 5 meters if it previously observed that this damages it, no matter if it thinks about that situation again or not)
    <patham9_> 2. the system wont lose time with thoughts about already satisfied goals (due to budget shrinking proportional to satisfaction)
    <patham9_> 3. the system wont execute and pursue what is already satisfied
    <patham9_> 4. the system wont try to execute and pursue things in the current moment which are "sheduled" to be in the future.
    <patham9_> 5. the system wont pursue a goal it already pursued for the same reason (due to revision, it is related to 1)
    */

    //abstract public boolean decide(final Operation op);

//    protected void executed(Operation op, Task... feedback) {
//        executed(op, Lists.newArrayList(feedback));
//    }
    /**
     * called after execution completed
     */
    protected void executed(Operation op, List<Task> feedback) {

        final NAR n = nar();

        //Display a message in the output stream to indicate the reportExecution of an operation
        n.memory.eventExecute.emit(new ExecutionResult(op, feedback, n.memory()));


        if (!op.getTask().isCommand()) {
            noticeExecuted(op);
        }

        //feedback tasks as input
        //should we allow immediate tasks to create feedback?
        if (feedback != null) {
            for (final Task t : feedback) {
                if (t == null) continue;
                t.setCause(op);
                //t.log("Feedback");

                n.input(t);
            }
        }

    }


    /**
     * internal notice of the execution
     */
    protected void noticeExecuted(final Operation operation) {
        final Task opTask = operation.getTask();
        //if (opTask == null) return;


        final Memory memory = nar().memory();

        memory.logic.TASK_EXECUTED.hit();

        nar().input(TaskSeed.make(memory, operation).
                judgment().
                truth(1f, Global.OPERATOR_EXECUTION_CONFIDENCE).
                budget(operation.getTask().getBudget()).
                present(memory).
                parent(opTask).
                cause(operation).
                reason("Executed")
            );

    }

}
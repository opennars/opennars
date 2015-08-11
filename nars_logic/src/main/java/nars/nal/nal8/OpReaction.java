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

import nars.AbstractMemory;
import nars.Events.EXE;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.nal.nal8.decide.DecideAboveDecisionThreshold;
import nars.nal.nal8.decide.Decider;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Term;
import nars.util.event.Reaction;

import java.util.List;

/**
 * An individual operate that can be execute by the system, which can be either
 * inside NARS or outside it, in another system or device.
 * <p>
 * This is the only file to modify when registering a new operate into NARS.
 * <p>
 * An instance of an Operator must not be shared by multiple Memory
 * since it will be associated with a particular one.  Create a separate one for each
 */
abstract public class OpReaction implements Reaction<Term> {


    public final Term term;
    protected NAR nar;

    public OpReaction(Term term) {
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


    public Memory getMemory() {
        if (nar != null)
            return nar.memory;
        return null;
    }

    public Decider decider() {
        return DecideAboveDecisionThreshold.the;
    }


    @Override
    public void event(Term event, Object... args) {
        Operation o = ((Operation) args[0]);
        Concept c = (Concept) args[1];
        Memory m = (Memory) args[2];
        if (decider().decide(c, o)) {
            o = o.inline(getMemory(), false);
            execute(o, c, m);
        }
    }

    /**
     * Required method for every operate, specifying the corresponding
     * operation
     *
     * @param args   Arguments of the operation, both input (constant) and output (variable)
     * @param memory
     * @return The direct collectable results and feedback of the
     * reportExecution
     */
    protected abstract List<Task> execute(Operation input, Memory memory);


    @Override
    public OpReaction clone() {
        //do not clone operators, just use as-is since it's effectively immutable
        return this;
    }

    public Term getTerm() {
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

    abstract public boolean execute(final Operation op, final Concept c, final Memory memory);


    /**
     * called after execution completed
     */
    protected void executed(Operation op, List<Task> feedback, Memory memory) {

        //Display a message in the output stream to indicate the reportExecution of an operation
        memory.emit(EXE.class, new ExecutionResult(op, feedback, memory));


        noticeExecuted(op, memory);

        //feedback tasks as input
        //should we allow immediate tasks to create feedback?
        if (feedback != null) {
            for (final Task t : feedback) {
                if (t == null) continue;
                t.setCause(op);
                t.log("Feedback");

                memory.add(t);
            }
        }

    }


    public boolean isExecutable(final AbstractMemory mem) {
        return true;
    }


    /**
     * internal notice of the execution
     */
    protected void noticeExecuted(final Operation operation, final Memory memory) {
        final Task opTask = operation.getTask();
        //if (opTask == null) return;

        memory.logic.TASK_EXECUTED.hit();

        memory.add(memory.newTask(operation).
                judgment().
                truth(1f, Global.OPERATOR_EXECUTION_CONFIDENCE).
                budget(operation.getTask()).
                present().
                parent(opTask).
                cause(operation).
                reason("Executed").
                get());

    }

}
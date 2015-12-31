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

import nars.nal.nal8.decide.DecideAboveDecisionThreshold;
import nars.nal.nal8.decide.Decider;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;

import java.util.function.Consumer;

/**
 * An operator implementation
 */
public abstract class AbstractOperator implements Consumer<Execution> {


    public final Term operatorTerm;




    @Override
    public String toString() {
        return '^' + operatorTerm.toString();
    }

    public AbstractOperator(Term term) {
        if (term == null) {
            term = Atom.the(getClass().getSimpleName());
        }
        operatorTerm = term;
    }

    public AbstractOperator(String operatorName) {
        this(Atom.the(operatorName));
    }

    public AbstractOperator(Atom operatorTerm) {
        this.operatorTerm = operatorTerm;
    }


    /**
     * use the class name as the operator name
     */
    public AbstractOperator() {
        String className = getClass().getSimpleName();
        operatorTerm = Atom.the(className);
    }



    public Decider decider() {
        return DecideAboveDecisionThreshold.the;
    }


    @Override
    public void accept(Execution execution) {
        Task o = execution.task;
        if (o.isCommand() || decider().test(o)) {
            _execute(execution);
        }
    }

    /**
     * Required method for every operate, specifying the corresponding
     * operation
     *
     * @return The direct collectable results and feedback of the
     * reportExecution
     */
    /**
     * The standard way to carry out an operation, which invokes the execute
     * method defined for the operate, and handles feedback tasks as input
     *
     * @param op     The operate to be executed
     * @return true if successful, false if an error occurred
     */
    public final void _execute(Execution execution) {
        if (async()) {
            //asynch
            execution.nar.execAsync(() -> execute(execution));
        } else {
            //synchronous
            execute(execution);
        }
    }

    public abstract void execute(Execution execution);

//    {
//        try {
//            List<Task> feedback = apply(op);
//            executed(op, feedback);
//        } catch (Exception e) {
//            nar().memory.eventError.emit(e);
//
//            //TODO hack this should be handled by the error handler
//            if (Global.DEBUG) {
//                e.printStackTrace();
//            }
//            return false;
//        }
//
//        return true;
//    }

    /** determines the execution strategy. currently there are only two: synch and async, and if
     * we want to add more we can use a lambda Consumer<Runnable> or something
     */
    public boolean async() { return false; }

    public final Term getOperatorTerm() {
        return operatorTerm;
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




}

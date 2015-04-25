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

import com.google.common.collect.Lists;
import nars.Events.EXE;
import nars.Memory;
import nars.NAR;
import nars.Global;
import nars.operate.IOperator;
import nars.budget.Budget;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.TruthValue;
import nars.nal.stamp.Stamp;
import nars.nal.nal7.Tense;
import nars.operate.io.Echo;

import java.util.Arrays;
import java.util.List;

/**
 * An individual operate that can be execute by the system, which can be either
 * inside NARS or outside it, in another system or device.
 * <p>
 * This is the only file to modify when registering a new operate into NARS.
 *
 * An instance of an Operator must not be shared by multiple Memory
 * since it will be associated with a particular one.  Create a separate one for each
 */
public abstract class Operator extends Term implements IOperator {

    protected NAR nar;
    public static final float executionConfidence = Global.OPERATOR_EXECUTION_CONFIDENCE; //Global.MAX_CONFIDENCE;
    
    //protected Operator() {   super();    }
    
    protected Operator(String name) {
        super(name);
        if (!name.startsWith("^"))
            throw new RuntimeException("Operator name needs ^ prefix");
    }


    @Override public boolean setEnabled(NAR n, boolean enabled) {
        if (enabled)
            this.nar = n;
        else
            this.nar = null;
        return true;
    }

    public Memory getMemory() {
        return nar.memory;
    }

    /**
     * Required method for every operate, specifying the corresponding
     * operation
     *
     * @param args Arguments of the operation, both input (constant) and output (variable)
     * @param memory The memory to work on
     * @return The direct collectable results and feedback of the
     * reportExecution
     */
    protected abstract List<Task> execute(Operation operation, Term[] args, Memory memory);


    @Override
    public Operator clone() {
        //do not clone operators, just use as-is since it's effectively immutable
        return this;
    }


    public static class ExecutionResult {
        public final Operation operation;
        public final Object feedback;

        public ExecutionResult(Operation op, Object feedback) {
            this.operation = op;
            this.feedback = feedback;
        }
        
        public Task getTask() { return operation.getTask(); }

        public Operation getOperation() {
            return operation;
        }

        @Override
        public String toString() {
            if (operation instanceof ImmediateOperation) {
                return operation.toString();
            }
            else {
                Term[] args = operation.getArgumentsRaw();
                Operator operator = operation.getOperator();
                StringBuilder sb = new StringBuilder();

                Budget b = getTask();
                if (b!=null)
                    sb.append(b.toStringExternal()).append(' ');

                sb.append(operator).append('(');
                if (args.length > 0) {
                    String argString = Arrays.toString(args);
                    sb.append(argString.substring(1, argString.length()-1)); //remove '[' and ']'
                }
                sb.append(')');
                if (feedback!=null)
                    sb.append(": ").append(feedback);
                return sb.toString();
            }
        }
        
        
    }

    /**
     * The standard way to carry out an operation, which invokes the execute
     * method defined for the operate, and handles feedback tasks as input
     *
     * @param op The operate to be executed
     * @param memory The memory on which the operation is executed
     * @return true if successful, false if an error occurred
     */
    public final boolean execute(final Operation op, final Memory memory) {

        if(!op.isExecutable(memory)) {
            return false;
        }

        final Term[] args = op.getArguments().term;

        List<Task> feedback;
        try {
            feedback = execute(op, args, memory);
        }
        catch (Exception e) {
            feedback = Lists.newArrayList(new Echo(getClass(), e.toString()).newTask());
            e.printStackTrace();
        }

        //Display a message in the output stream to indicate the reportExecution of an operation
        memory.emit(EXE.class, new ExecutionResult(op, feedback));


        //internal notice of the execution
        if (!isImmediate()) {
            executedTask(op, new TruthValue(1f, executionConfidence), memory);
        }

        //feedback tasks as input
        //should we allow immediate tasks to create feedback?
        if (feedback!=null) {
            for (final Task t : feedback) {
                if (t == null) continue;
                t.setCause(op);
                t.addHistory("Feedback");

                memory.input(t);
            }
        }

        return true;

    }


    /**
     * ExecutedTask called in Operator.call
     *
     * @param operation The operation just executed
     */
    public void executedTask(final Operation operation, TruthValue truth, final Memory memory) {
        final Task opTask = operation.getTask();
        memory.logic.TASK_EXECUTED.hit();

        memory.taskAdd(
                memory.newTask(operation).
                        judgment().
                        truth(truth).
                        budget(operation.getTask()).
                        stamp(new Stamp(opTask.getStamp(), memory, Tense.Present)).
                        parent(opTask).
                        get().
                        setCause(operation),
                "Executed");
    }

    public static String addPrefixIfMissing(String opName) {
        if (!opName.startsWith("^"))
            return '^' + opName;
        return opName;
    }


    @Override
    public boolean isExecutable(final Memory mem) {
        return true;
    }


    /** Immediate operators are processed immediately and do not enter the reasoner's memory */
    public boolean isImmediate() { return false; }
}

//        catch (NegativeFeedback n) {
//
//            if (n.freqOcurred >=0 && n.confidenceOcurred >= 0) {
//                memory.executedTask(operation, new TruthValue(n.freqOcurred, n.confidenceOcurred));
//            }
//
//            if (n.freqCorrection >= 0 && n.confCorrection >=0) {
//                //for inputting an inversely frequent goal to counteract a repeat invocation
//                BudgetValue b = operation.getTask().budget;
//                float priority = b.getPriority();
//                float durability = b.getDurability();
//
//                memory.addNewTask(
//                        memory.newTaskAt(operation, Symbols.GOAL, n.freqCorrection, n.confCorrection, priority, durability, (Tense)null),
//                        "Negative feedback"
//                );
//
//            }
//
//            if (!n.quiet) {
//                reportExecution(operation, args, n, memory);
//            }
//        }


//    public static class NegativeFeedback extends RuntimeException {
//
//        /** convenience method for creating a "never again" negative feedback"*/
//        public static NegativeFeedback never(String rule, boolean quiet) {
//            return new NegativeFeedback(rule, 0, executionConfidence,
//                    0, executionConfidence, quiet
//            );
//        }
//        /** convenience method for ignoring an invalid operation; does not recognize that it occurred, and does not report anything*/
//        public static NegativeFeedback ignore(String rule) {
//            return new NegativeFeedback(rule, -1, -1, -1, -1, true);
//        }
//
//        public final float freqCorrection;
//        public final float confCorrection;
//        public final float freqOcurred;
//        public final float confidenceOcurred;
//        public final boolean quiet;
//
//        public NegativeFeedback(String rule, float freqOcurred, float confidenceOccurred, float freqCorrection, float confCorrection, boolean quiet) {
//            super(rule);
//            this.freqOcurred = freqOcurred;
//            this.confidenceOcurred = confidenceOccurred;
//            this.freqCorrection = freqCorrection;
//            this.confCorrection = confCorrection;
//            this.quiet = quiet;
//        }
//    }

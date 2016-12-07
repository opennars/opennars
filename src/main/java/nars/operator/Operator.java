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

package nars.operator;

import nars.NAR;
import nars.config.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Output.EXE;
import nars.language.Product;
import nars.language.Statement;
import nars.language.Term;
import nars.storage.Memory;
import nars.util.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * An individual operator that can be execute by the system, which can be either
 * inside NARS or outside it, in another system or device.
 * <p>
 * This is the only file to modify when registering a new operator into NARS.
 */
public abstract class Operator extends Term implements Plugin {

    public static final float executionConfidence = Parameters.MAX_CONFIDENCE; // 0.9999f;
    
    protected Operator() {   super();    }
    
    protected Operator(String name) {
        super(name);
        if (!name.startsWith("^"))
            throw new RuntimeException("Operator name needs ^ prefix");
    }

    public NAR nar; 
    
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        this.nar = n;
        return true;
    }        
    
    
    /**
     * Required method for every operator, specifying the corresponding
     * operation
     *
     * @param args Arguments of the operation, both input (constant) and output (variable)
     * @param memory The memory to work on
     * @return The direct collectable results and feedback of the
     * reportExecution
     */
    protected abstract List<Task> execute(Operation operation, Term[] args, Memory memory);

    /**
    * The standard way to carry out an operation, which invokes the execute
    * method defined for the operator, and handles feedback tasks as input
    *
    * @param op The operator to be executed
    * @param args The arguments to be taken by the operator
    * @param memory The memory on which the operation is executed
    * @return true if successful, false if an error occurred
    */
    public final boolean call(final Operation operation, final Term[] args, final Memory memory) {
        try {
            List<Task> feedback = execute(operation, args, memory);            
            
            memory.executedTask(operation, new TruthValue(1f,executionConfidence));
            
            reportExecution(operation, args, feedback, memory);
            

            if (feedback!=null) {
                for (final Task t : feedback) {
                    memory.inputTask(t);
                }            
            }
            
            return true;
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
//                        memory.newTask(operation, Symbols.GOAL_MARK, n.freqCorrection, n.confCorrection, priority, durability, (Tense)null), 
//                        "Negative feedback"
//                );
//                
//            }
//            
//            if (!n.quiet) {
//                reportExecution(operation, args, n, memory);
//            }
//        }
        catch (Exception e) {                        
            //reportExecution(operation, args, e, memory);            
        }
        return false;
        
    }
    
   
    public static String operationExecutionString(final Statement operation) {
        Term operator = operation.getPredicate();
        Term arguments = operation.getSubject();
        String argList = arguments.toString().substring(3);         // skip the product prefix "(*,"
        return operator + "(" + argList;        
    }

    @Override
    public Operator clone() {
        //do not clone operators, just use as-is since it's effectively immutable
        return this;
    }

//    /**
//     * Display a message in the output stream to indicate the reportExecution of
//     * an operation
//     * <p>
//     * @param operation The content of the operation to be executed
//     */
    public static void reportExecution(final Operation operation, final Term[] args, Object feedback, final Memory memory) {
        
        final Term opT = operation.getPredicate();
        if(!(opT instanceof Operator)) {
            return;
        }

        if (memory.emitting(EXE.class)) {
            final Operator operator = (Operator) opT;
            

            
            if (feedback instanceof Exception)
                feedback = feedback.getClass().getSimpleName() + ": " + ((Throwable)feedback).getMessage();
            
            memory.emit(EXE.class, 
                    new ExecutionResult(operation, feedback));
        }
    }
    
    public static class ExecutionResult {
        private final Operation operation;
        private final Object feedback;

        public ExecutionResult(Operation op, Object feedback) {
            this.operation = op;
            this.feedback = feedback;
        }
        
        public Task getTask() { return operation.getTask(); }
        

        
        @Override
        public String toString() {
            BudgetValue b = null;
            if (getTask() != null) {
                b = getTask().budget;
            }
            Term[] args = operation.getArguments().term;
            Operator operator = operation.getOperator();
            
            return ((b != null) ? (b.toStringExternal() + " ") : "") + 
                        operator + "(" + Arrays.toString(args) + ")=" + feedback;
        }
        
        
    }

    public final boolean call(final Operation op, final Memory memory) {
        if(!op.isExecutable(memory)) {
            return false;
        }
        Product args = op.getArguments();
        return call(op, args.term, memory);
    }
    

    public static String addPrefixIfMissing(String opName) {
        if (!opName.startsWith("^"))
            return '^' + opName;
        return opName;
    }

    
//    public static class NegativeFeedback extends RuntimeException {
//
//        /** convenience method for creating a "never again" negative feedback"*/
//        public static NegativeFeedback never(String reason, boolean quiet) {
//            return new NegativeFeedback(reason, 0, executionConfidence, 
//                    0, executionConfidence, quiet
//            );
//        }
//        /** convenience method for ignoring an invalid operation; does not recognize that it occurred, and does not report anything*/
//        public static NegativeFeedback ignore(String reason) {
//            return new NegativeFeedback(reason, -1, -1, -1, -1, true);
//        }        
//        
//        public final float freqCorrection;
//        public final float confCorrection;
//        public final float freqOcurred;
//        public final float confidenceOcurred;
//        public final boolean quiet;
//    
//        public NegativeFeedback(String reason, float freqOcurred, float confidenceOccurred, float freqCorrection, float confCorrection, boolean quiet) {
//            super(reason);
//            this.freqOcurred = freqOcurred;
//            this.confidenceOcurred = confidenceOccurred;
//            this.freqCorrection = freqCorrection;
//            this.confCorrection = confCorrection;
//            this.quiet = quiet;
//        }
//    }
    
}


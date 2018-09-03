/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.operator;

import org.opennars.entity.BudgetValue;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.interfaces.Timable;
import org.opennars.io.events.OutputHandler.EXE;
import org.opennars.language.Product;
import org.opennars.language.Statement;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.plugin.Plugin;
import org.opennars.storage.Memory;

import java.util.Arrays;
import java.util.List;
import org.opennars.io.events.OutputHandler.ERR;
import org.opennars.main.MiscFlags;

/**
 * An individual operator that can be execute by the system, which can be either
 * inside NARS or outside it, in another system or device.
 * <p>
 * This is the only file to modify when registering a new operator into NARS.
 */
public abstract class Operator extends Term implements Plugin {

    public float executionConfidence;
    
    protected Operator() {   super();    }
    
    protected Operator(final String name) {
        super(name);
        if (!name.startsWith("^"))
            throw new IllegalStateException("Operator name needs ^ prefix");
    }

    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        this.executionConfidence = n.narParameters.DEFAULT_JUDGMENT_CONFIDENCE;
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
    protected abstract List<Task> execute(Operation operation, Term[] args, Memory memory, final Timable time);

    /**
    * The standard way to carry out an operation, which invokes the execute
    * method defined for the operator, and handles feedback tasks as input
    *
    * @param operation The operator to be executed
    * @param args The arguments to be taken by the operator
    * @param memory The memory on which the operation is executed
    * @param time used to retrieve the time
    * @return true if successful, false if an error occurred
    */
    public final boolean call(final Operation operation, final Term[] args, final Memory memory, final Timable time) {
        List<Task> feedback = null;
        try {
            feedback = execute(operation, args, memory, time);
        }
        catch(Exception ex) {//peripherie, maybe used incorrectly, failure is unavoidable
            if(MiscFlags.SHOW_EXECUTION_ERRORS) {
                memory.event.emit(ERR.class, ex);
            }
            if(!MiscFlags.EXECUTION_ERRORS_CONTINUE) {
                throw new IllegalStateException("Execution error:\n", ex);
            }
        }

        if(feedback == null || feedback.isEmpty()) { //null operator case
            memory.executedTask(time, operation, new TruthValue(1f,executionConfidence, memory.narParameters));
        }

        reportExecution(operation, args, feedback, memory);


        if (feedback!=null) {
            for (final Task t : feedback) {
                memory.inputTask(time, t);
            }
        }

        return true;
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
    }
    
   
    public static String operationExecutionString(final Statement operation) {
        final Term operator = operation.getPredicate();
        final Term arguments = operation.getSubject();
        final String argList = arguments.toString().substring(3);         // skip the product prefix "(*,"
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

        public ExecutionResult(final Operation op, final Object feedback) {
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
            final Term[] args = operation.getArguments().term;
            final Operator operator = operation.getOperator();
            
            return ((b != null) ? (b.toStringExternal() + " ") : "") + 
                        operator + "(" + Arrays.toString(args) + ")=" + feedback;
        }
        
        
    }

    public final boolean call(final Operation op, final Memory memory, final Timable time) {
        if(!op.isExecutable(memory)) {
            return false;
        }
        final Product args = op.getArguments();
        return call(op, args.term, memory, time);
    }
    

    public static String addPrefixIfMissing(final String opName) {
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


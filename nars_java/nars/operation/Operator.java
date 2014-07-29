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

package nars.operation;

import java.util.*;

import nars.language.*;
import nars.entity.Task;
import nars.storage.Memory;

/**
 * An individual operator that can be execute by the system, though implemented
 * outside NARS.
 * <p>
 * This is the only file to modify when adding a new operator into NARS.
 */
public abstract class Operator extends Term {
    public Operator(String name) {
        super(name);
    }
    
    /**
     * Required method for every operation, specifying the operation
     * @param task The task with the arguments to be passed to the operator
     * @return The direct collectable results and feedback of the reportExecution
     */
    public abstract ArrayList<Task> execute(Task task);

    /**
     * Execute an operation, then handle feedback
     * @param task The task to be executed
     * @param memory
     */
    public void call(Task task, Memory memory) {
        ArrayList<Task> feedback = execute(task);
        reportExecution((Statement) task.getContent());
//        Memory.executedTask(task);
        if (feedback != null) {
            for (Task t : feedback) {
                memory.inputTask(t);
            }
        }
    }
    

    /**
     * Register all built-in operators in the Memory
     * <p>
     * The only method to modify when adding a new operator into NARS.
     * An operator name should contain at least two characters after '^'.
     * @return A Map between Operator name and object
     */
    public static void loadDefaultOperators(Memory memory) {
        memory.registerOperator(new Wait("^wait"));
        
        /* operators for tasks */
//        table.put("^believe", new Believe("^believe"));     // accept a statement with a default truth-value
//        table.put("^want", new Want("^want"));              // accept a statement with a default desire-value
//        table.put("^wonder", new Wonder("^wonder"));        // find the truth-value of a statement
//        table.put("^assess", new Assess("^assess"));        // find the desire-value of a statement
//        /* operators for internal perceptions */
//        table.put("^consider", new Consider("^consider"));  // find the most active concept
//        table.put("^remind", new Remind("^remind"));        // create/activate a concept
//        table.put("^wait", new Wait("^wait"));              // wait for a certain number of clock cycle
        /*
         * observe          // process a new task (Channel ID: optional?)
         * think            // carry out a working cycle
         * do               // turn a statement into a goal
         *
         * possibility      // return the possibility of a term
         * doubt            // decrease the confidence of a belief
         * hesitate         // decrease the confidence of a goal
         *
         * feel             // the overall happyness, average solution quality, and predictions
         * busy             // the overall business
         *
         * tell             // output a judgment (Channel ID: optional?)
         * ask              // output a question/quest (Channel ID: optional?)
         * demand           // output a goal (Channel ID: optional?)
         *
         * count            // count the number of elements in a set
         * arithmatic       // + - * /
         * comparisons      // < = >
         * inference        // binary inference
         *
         * assume           // local assumption ???
         * name             // turn a compount term into an atomic term ???
         * ???              // rememberAction the history of the system? excutions of operatons?
         */
        
        /* operators for testing examples */
//        table.put("^go-to", new GoTo("^go-to"));
//        table.put("^pick", new Pick("^pick"));
//        table.put("^open", new Open("^open"));
//        table.put("^break", new Break("^break"));
//        table.put("^drop", new Drop("^drop"));
//        table.put("^throw", new Throw("^throw"));
//        table.put("^strike", new Strike("^strike"));

    }
    
    /**
     * Display a message in the output stream to indicate the reportExecution of an operation
     * <p>
     * @param operation The content of the operation to be executed
     */
    protected void reportExecution(Statement operation) {
        Term operator = operation.getPredicate();
        Term arguments = operation.getSubject();
        String argList = arguments.toString().substring(3);         // skip the product prefix "(*,"
        System.out.println("EXECUTE: " + operator + "(" + argList);
    }
}


/*
 * Wait.java
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

import java.util.ArrayList;

import nars.entity.Task;
import nars.language.*;
import nars.storage.Memory;

/**
 * A class used in testing only.
 */
public class Wait extends Operator {

    public Wait(String name) {
        super(name);
    }

    /**
     * Execute an operation, then handle feedback
     * @param task The task to be executed
     */
    public void call(Task task, Memory memory) {
        execute(task);
    }

    public ArrayList<Task> execute(Task task) {
//        Statement content = (Statement) task.getContent();
//        String delta = ((CompoundTerm) content.getSubject()).componentAt(0).getName();
//        int targetTime = (int) task.getSentence().getCreationTime() + Integer.parseInt(delta);
//        if (Center.getTime() < targetTime) {
//            Memory.activatedTask(task.getBudget(), task.getSentence(), false);
//        } else {
//            reportExecution(content);
//            Memory.executedTask(task);
//        }
        return null;
    }

//    public Term createOperation(int distance) {
//        Term t = new Term(Integer.toString(distance));
//        ArrayList<Term> argument = new ArrayList<Term>();
//        argument.add(t);
//        Term p = Product.make(argument);
//        return Inheritance.make(p, this);
//    }
//
//    public static int getWaitingTime(Term term) {
//        ArrayList<Term> list = term.parseOperation("^wait");
//        if (list != null) {
//            return Integer.parseInt(list.get(1).toString());
//        } else {
//            return 0;
//        }
//    }
//
//    public static int getWaitingTime(Term t, boolean front) {
//        if (t instanceof CompoundTerm) {
//            if (t instanceof Inheritance) {
//                return getWaitingTime(t);
//            } else if ((t instanceof Conjunction) && Conjunction.isSequence(t)) {
//                if (front) {
//                    return getWaitingTime(((Conjunction) t).componentAt(0));
//                } else {
//                    return getWaitingTime(((Conjunction) t).componentAt(((Conjunction) t).size()-1));
//                }
//            }
//        }
//        return 0;
//    }
}


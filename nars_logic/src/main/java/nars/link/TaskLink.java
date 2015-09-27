/*
 * TaskLink.java
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
package nars.link;

import nars.budget.Budget;
import nars.budget.Item;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;

/**
 * Reference to a Task.
 * <p>
 * The rule to separate a Task and a TaskLink is that the same Task can be
 * linked from multiple Concepts, with different BudgetValue.
 * <p>
 * TaskLinks are unique according to the Task they reference
 */
public class TaskLink extends Item<Sentence> implements TLink<Task> {

    /**
     * The Task linked
     */
    public final Task targetTask;


    protected TaskLink(Task t, Budget v) {
        super(v);

        if (t == null)
            throw new RuntimeException(this + " null task");

        this.targetTask = t;
    }




    @Override
    public final Sentence name() { return targetTask; }




    //    @Override
//    public int hashCode() {
//        return getSentence().hashCode();
//    }

    @Override
    public final int hashCode() {
        return targetTask.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        //if (obj == this) return true;
        //return false;
        //throw new RuntimeException("tasklinks should be compared by their sentences, not directly");

        if (obj == this) return true;


        //if (obj instanceof TaskLink) {
        TaskLink t = (TaskLink) obj;
        return targetTask.equals(t.targetTask);
        //}
        //return false;
//
//            /*if (Global.TASK_LINK_UNIQUE_BY_INDEX)
//                return TermLinkTemplate.prefix(type, index, false) + Symbols.TLinkSeparator + task.sentence.name();
//            else*/
//        }
//        return false;
    }

//    /**
//     * Get one index by level
//     *
//     * @param i The index level
//     * @return The index value
//     */
//    @Override
//    public final short getIndex(final int i) {
//        final short[] index = this.index;
//        if ((index != null) && (i < index.length)) {
//            return index[i];
//        } else {
//            return -1;
//        }
//    }



    @Override
    public String toString() {
        return name().toString();
    }


    /**
     * Get the target Task
     *
     * @return The linked Task
     */
    @Override
    public final Term getTerm() {
        return targetTask.getTerm();
    }

    public final Task getTask() {
        return targetTask;
    }





}

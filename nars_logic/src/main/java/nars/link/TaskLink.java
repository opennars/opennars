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
import nars.task.Sentenced;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import oracle.jrockit.jfr.Recording;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reference to a Task.
 * <p>
 * The rule to separate a Task and a TaskLink is that the same Task can be
 * linked from multiple Concepts, with different BudgetValue.
 * <p>
 * TaskLinks are unique according to the Task they reference
 */
public class TaskLink extends Item<Sentence> implements TLink<Task>, Termed, Sentenced {

    /**
     * The Task linked
     */
    public final Task targetTask;
    private final int recordLength;
    private float lastFireTime = -1; //float to include the "subcycle"




//
//
//    /** allows re-use of the Recording object since it would otherwise be instantiated frequently */
//    public static class RecordingList extends CircularArrayList<Recording> {
//
//        public RecordingList(int capacity) {
//            super(capacity);
//        }
//
//
//        public void add(final TermLink t, final long time) {
//            add(new Recording(t, time));
//        }
//
//    }




    /**
     * The type of tlink, one of the above
     */
    public final short type;

    /**
     * The index of the component in the component list of the compound, may have up to 4 levels
     */
    public final short[] index;


    protected TaskLink(Task t, Budget v, short[] index, short type, int recordLength) {
        super(v);
        this.targetTask = t;
        this.recordLength = recordLength;
        this.index = index;
        this.type = type;
    }

    public TaskLink(final Task t, final Budget v, int recordLength) {
        this(t, v, null, TermLink.SELF, recordLength);
    }

    /**
     * Constructor
     * <p>
     *
     * @param t        The target Task
     * @param template The TermLink template
     * @param v        The budget
     */
    public TaskLink(final Task t, final TermLinkTemplate template, final Budget v, int recordLength) {
        this(t, v, template.index, template.type, recordLength);
    }


    @Override
    public Sentence name() { return getSentence(); }


//    @Override
//    public int hashCode() {
//        return getSentence().hashCode();
//    }



    @Override
    public final boolean equals(final Object obj) {
        //if (obj == this) return true;
        //return false;
        //throw new RuntimeException("tasklinks should be compared by their sentences, not directly");

        if (obj == this) return true;
        if (obj instanceof TaskLink) {
            TaskLink t = (TaskLink) obj;
            return getSentence().equals(t.getSentence());
        }
        return false;
//
//            /*if (Global.TASK_LINK_UNIQUE_BY_INDEX)
//                return TermLinkTemplate.prefix(type, index, false) + Symbols.TLinkSeparator + task.sentence.name();
//            else*/
//        }
//        return false;
    }

    /**
     * Get one index by level
     *
     * @param i The index level
     * @return The index value
     */
    @Override
    public final short getIndex(final int i) {
        if ((index != null) && (i < index.length)) {
            return index[i];
        } else {
            return -1;
        }
    }

    public float getLastFireTime() {
        return lastFireTime;
    }

    public void setFired(final float now) {
        this.lastFireTime = now;
    }


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
    public Term getTarget() {
        return targetTask.getTerm();
    }

    public Task getTask() {
        return targetTask;
    }

    @Override
    public void delete() {
//        if (records != null) {
//            records.clear();
//            records = null;
//        }
    }

    @Override
    public Term getTerm() {
        return getTarget();
    }

    @Override
    public Sentence getSentence() {
        return targetTask.sentence;
    }


}

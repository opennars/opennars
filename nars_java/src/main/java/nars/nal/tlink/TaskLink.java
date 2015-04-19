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
package nars.nal.tlink;

import com.gs.collections.api.block.procedure.primitive.ObjectLongProcedure;
import com.gs.collections.api.map.primitive.ObjectLongMap;
import com.gs.collections.impl.map.mutable.primitive.ObjectLongHashMap;
import nars.Global;
import nars.budget.Budget;
import nars.io.Symbols;
import nars.nal.Item;
import nars.nal.Sentence;
import nars.nal.Sentence.Sentenced;
import nars.nal.Task;
import nars.nal.Terms.Termable;
import nars.nal.term.Term;

import java.util.List;

/**
 * Reference to a Task.
 * <p>
 * The rule to separate a Task and a TaskLink is that the same Task can be
 * linked from multiple Concepts, with different BudgetValue.
 * 
 * TaskLinks are unique according to the Task they reference
 */
public class TaskLink extends Item<String> implements TLink<Task>, Termable, Sentenced {

    /**
     * The Task linked
     */
    public final Task targetTask;

    private String name;

    /** time when records table was last filtered for old entries;
     *  used to prevent repeated filtering on the same time cycle (nothing will have changed)
     */
    transient private long lastClean = -1;

    public static String key(short type, short[] index, Task task) {
        if (Global.TASK_LINK_UNIQUE_BY_INDEX)
            return TermLinkTemplate.prefix(type, index, false) + Symbols.TLinkSeparator + task.sentence.name();
        else
            return task.sentence.name();
    }



    /** stores the last access time that this tasklink was fired with a given termlink */
    ObjectLongHashMap<TermLink> records;
    

    
    /** The type of tlink, one of the above */
    public final short type;

    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;


    public TaskLink(final Task t, final Budget v) {
        super(v);
        this.type = TermLink.SELF;
        this.index = null;

        this.targetTask = t;

        this.name = null;
    }

    /**
     * Constructor
     * <p>
     *
     * @param t The target Task
     * @param template The TermLink template
     * @param v The budget
     */
    public TaskLink(final Task t, final TermLinkTemplate template, final Budget v) {
        super(v);
        this.type = template.type;
        this.index = template.index;
        
        this.targetTask = t;
        
        this.name = null;
    }


    @Override
    public String name() {
        if (name == null) {
            this.name = key(type, index, targetTask);
        }
        return name;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    public ObjectLongMap<TermLink> getRecords() {
        return records;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof TaskLink) {
            TaskLink t = (TaskLink)obj;
            //return t.name().equals(name());
            return t.targetTask.equals(targetTask);
        }
        return false;
    }    
    
    /**
     * Get one index by level
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

    /**
     * To check whether a TaskLink should use a TermLink, return false if they
     * interacted recently
     * <p>
     * called in TermLinkBag only
     *
     * @param termLink The TermLink to be checked
     * @param currentTime The current time
     * @return Whether they are novel to each other
     */
    public boolean novel(final TermLink termLink, final long currentTime, int noveltyHorizon, int recordLength) {


        final Term bTerm = termLink.target;
        if (bTerm.equals(targetTask.sentence.term)) {            
            return false;
        }

        if (noveltyHorizon == 0) return true;

        if (records==null) {
            //records = new ArrayDeque(recordLength);
            //records = new LinkedList();
            records = new ObjectLongHashMap<>(recordLength);
        }

        //TODO remove old entries from records if recordLength < records.size()  -- for dynamic adjusting of novelty parameters

        final long minTime = currentTime - noveltyHorizon;

        boolean novelEnough;

        long lastTime = records.getIfAbsentPut(termLink, currentTime);
        novelEnough = lastTime < minTime;


        //(attempt to) keep recordedLinks queue a maximum finite size
        if (!records.isEmpty() && lastClean<currentTime && records.size()  > recordLength) {

            recordsFilter.removeOlderThan(minTime);

            //if the records list continues to exceed the length,
            // remove values by sorted earliest time
            // this will remove all entries which have the target
            // value
            while (records.size() > recordLength) {
                recordsFilter.removeOlderThan(records.min() + 1);
            }

            lastClean=currentTime;
        }


        return novelEnough;
    }

    class RecordsFilter implements ObjectLongProcedure<TermLink> {

        List<TermLink> toForget = Global.newArrayList();
        private long minTime = -1;

        public void removeOlderThan(long anyBeforeOrAtTime) {

            this.minTime = anyBeforeOrAtTime;

            records.forEachKeyValue(this);

            int n = toForget.size();
            for (int i = 0; i < n; i++)
                records.removeKey(toForget.get(i));
            toForget.clear();
        }

        @Override
        public void value(TermLink key, long value) {
            if (value < minTime) toForget.add(key);
        }


    }

    public final RecordsFilter recordsFilter = new RecordsFilter();

    @Override
    public String toString() {
        return name().toString();
    }
    

    /**
    * Get the target Task
    *
    * @return The linked Task
    */
    @Override public Task getTarget() {
        return getTask();
    }

    public Task getTask() { return targetTask; }

    @Override
    public void end() {
        if (records !=null) {
            records.clear();
            records = null;
        }
    }

    @Override
    public Term getTerm() {
        return getTarget().getTerm();
    }

    @Override
    public Sentence getSentence() {   return getTarget().sentence;  }

    
    
    
}

/** Remember the TermLinks, and when they has been used
 //     * recently with this TaskLink
 //     * Equality (& hashCode) tests only the termlink, useful for
 //     * fast comparison in a set.
 //     * */
//    final static class Recording {
//
//        public final TermLink link;
//        long time;
//
//        public Recording(TermLink link, long time) {
//            this.link = link;
//            this.time = time;
//        }
//
//        @Override
//        public int hashCode() {
//            return link.hashCode();
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            return link.equals(obj);
//        }
//
//        public long getTime() {
//            return time;
//        }
//
//
//        public void setTime(long t) {
//            this.time = t;
//        }
//
//        @Override
//        public String toString() {
//            return link + "@" + time;
//        }
//    }
//
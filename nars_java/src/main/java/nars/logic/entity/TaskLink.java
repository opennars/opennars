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
package nars.logic.entity;

import nars.logic.Terms.Termable;
import nars.logic.entity.Sentence.Sentenceable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Reference to a Task.
 * <p>
 * The reason to separate a Task and a TaskLink is that the same Task can be
 * linked from multiple Concepts, with different BudgetValue.
 * 
 * TaskLinks are unique according to the Task they reference
 */
public class TaskLink extends Item<Task> implements TLink<Task>, Termable, Sentenceable {

    /**
     * The Task linked
     */
    public final Task targetTask;

    private final int recordLength;



    /* Remember the TermLinks, and when they has been used recently with this TaskLink */
    public final static class Recording {
    
        public final TermLink link;
        long time;

        public Recording(TermLink link, long time) {
            this.link = link;
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        
        public void setTime(long t) {
            this.time = t;
        }
        
    }
    
    final Deque<Recording> records;
    

    
    /** The type of link, one of the above */    
    public final short type;

    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;



    public TaskLink(final Task t, final BudgetValue v, int recordLength) {
        super(v);
        this.type = TermLink.SELF;
        this.index = null;

        this.targetTask = t;

        this.recordLength = recordLength;
        this.records = new ArrayDeque(recordLength);
    }

    /**
     * Constructor
     * <p>
     * only called in Memory.continuedProcess
     *
     * @param t The target Task
     * @param template The TermLink template
     * @param v The budget
     */
    public TaskLink(final Task t, final TermLink.TermLinkTemplate template, final BudgetValue v, int recordLength) {
        super(v);
        this.type = template.type;
        this.index = template.index;
        
        this.targetTask = t;
        
        this.recordLength = recordLength;
        this.records = new ArrayDeque(recordLength);
        
    }


    @Override
    public int hashCode() {        
        return targetTask.hashCode();                
    }

    @Override
    public Task name() {
        return targetTask;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof TaskLink) {
            TaskLink t = (TaskLink)obj;
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
    public boolean novel(final TermLink termLink, final long currentTime, int noveltyHorizon) {
        final Term bTerm = termLink.target;
        if (bTerm.equals(targetTask.sentence.term)) {            
            return false;
        }

        if (noveltyHorizon == 0) return true;

        //iterating the FIFO deque from oldest (first) to newest (last)
        Iterator<Recording> ir = records.iterator();
        while (ir.hasNext()) {
            Recording r = ir.next();
            if (termLink.equals(r.link)) {
                if (currentTime < r.getTime() + noveltyHorizon) {
                    //too recent, not novel
                    return false;
                } else {
                    //happened long enough ago that we have forgotten it somewhat, making it seem more novel
                    r.setTime(currentTime);
                    ir.remove();
                    records.addLast(r);
                    return true;
                }
            }
            else if (currentTime > r.getTime() + noveltyHorizon) {
                //remove a record which will not apply to any other link
                ir.remove();
            }
        }
        
        
        //keep recordedLinks queue a maximum finite size
        while (records.size() + 1 >= recordLength) records.removeFirst();
        
        // add knowledge reference to recordedLinks
        records.addLast(new Recording(termLink, currentTime));
        
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " " + getTarget().sentence.stamp;
    }
    
    public String toStringBrief() {
        return super.toString();
    }

    /**
    * Get the target Task
    *
    * @return The linked Task
    */
    @Override public Task getTarget() {
        return targetTask;
    }

    @Override
    public void end() {
        records.clear();
    }

    @Override
    public Term getTerm() {
        return getTarget().getTerm();
    }

    @Override
    public Sentence getSentence() {   return getTarget().sentence;  }

    
    
    
}

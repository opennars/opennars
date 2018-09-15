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
package org.opennars.entity;

import org.opennars.language.Term;
import org.opennars.main.Parameters;

import java.io.Serializable;
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
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class TaskLink extends Item<Task> implements TLink<Task>, Serializable {

    /**
     * The Task linked. The "target" field in TermLink is not used here.
     */
    public final Task targetTask;
    private final int recordLength;
    
    
    /* Remember the TermLinks, and when they has been used recently with this TaskLink */
    public final static class Recording implements Serializable {
    
        public final TermLink link;
        long time;

        public Recording(final TermLink link, final long time) {
            this.link = link;
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        
        public void setTime(final long t) {
            this.time = t;
        }
        
    }
    
    public final Deque<Recording> records;
    

    
    /** The type of link, one of the above */    
    public final short type;

    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;

    
    /**
     * Constructor
     * <p>
     * only called in Memory.continuedProcess
     *
     * @param t The target Task
     * @param template The TermLink template
     * @param v The budget
     */
    public TaskLink(final Task t, final TermLink template, final BudgetValue v, final int recordLength) {
        super(v);
        this.type =
                template == null ? 
                        TermLink.SELF : 
                        template.type;
        this.index =
                
                template == null ?
                        null : 
                        template.index
        ;
        
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
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (obj instanceof TaskLink) {
            final TaskLink t = (TaskLink)obj;
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
    public boolean novel(final TermLink termLink, final long currentTime, final Parameters narParameters) {
        return novel(termLink, currentTime, narParameters, false);
    }

    public boolean novel(final TermLink termLink, final long currentTime, final Parameters narParameters, final boolean transformTask) {
        final Term bTerm = termLink.target;
        if (!transformTask && bTerm.equals(targetTask.sentence.term)) {            
            return false;
        }
        final TermLink linkKey = termLink.name();
        int next, i;
                
        //iterating the FIFO deque from oldest (first) to newest (last)
        final Iterator<Recording> ir = records.iterator();
        while (ir.hasNext()) {
            final Recording r = ir.next();
            if (linkKey.equals(r.link)) {
                if (currentTime < r.getTime() + narParameters.NOVELTY_HORIZON) {
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
        }
        
        
        //keep recordedLinks queue a maximum finite size
        while (records.size() + 1 >= recordLength) records.removeFirst();
        
        // add knowledge reference to recordedLinks
        records.addLast(new Recording(linkKey, currentTime));
        
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
    
    public Term getTerm() {
        return getTarget().getTerm();
    }
}

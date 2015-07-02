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

import java.util.*;

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




    /* Remember the TermLinks, and when they has been used recently with this TaskLink */
    public final static class Recording {

        public final TermLink link;
        float time;

        public Recording(TermLink link, float time) {
            this.time = time;
            this.link = link;
        }

        public float getTime() {
            return time;
        }


        public boolean setTime(float t) {
            if (this.time!=t) {
                this.time = t;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return link + "@" + time;
        }

        public Term getTerm() {
            return link.getTerm();
        }

        public void setRemoved() {
            setTime(Float.NaN);
        }
    }

    Map<Term,Recording> records;
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

    public Map<Term,Recording> newRecordSet() {
        //TODO use more efficient collection?

        return new LinkedHashMap<Term,Recording>(recordLength) {
            protected boolean removeEldestEntry(Map.Entry<Term,Recording> eldest) {
                if (size() > recordLength) {

                    eldest.getValue().setRemoved();
                    return true;
                }
                return false;
            }
        };
    }




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


    @Override
    public int hashCode() {
        return getSentence().hashCode();
    }

    public Collection<Recording> getRecords() {
        if (records == null) return Collections.EMPTY_LIST;
        return records.values();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        return false;
        //throw new RuntimeException("tasklinks should be compared by their sentences, not directly");
//        if (obj == this) return true;
//        if (obj instanceof TaskLink) {
//            TaskLink t = (TaskLink) obj;
//            return getSentence().equals(t.getSentence());
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

    public boolean valid(TermLink t) {
        return !(t.getTarget().equals(getTerm()));
    }

    /** returns the record associated with a termlink, or null if none exists (or if no records exist) */
    public Recording get(final TermLink termLink) {
        final Term bTerm = termLink.getTarget();

        if (records == null) {
            return null;
        }

        Recording r = records.get(bTerm);
        if (r == null) {
            return null;
        }
        else {
            return r;
        }
    }

    public void put(final TermLink t, final float now) {
        put(new Recording(t, now));
    }

    public void put(final Recording r, final float now) {
        //if the time has changed, then actually insert it.
        //this works because if the recordlink has been removed by
        //the collection, it will have its time set to NaN
        if (r.setTime(now))
            put(r);
    }

    protected void put(final Recording r) {
        if (records == null)
            records = newRecordSet();

        records.put(r.getTerm(), r);
    }

//    /**
//     * To check whether a TaskLink should use a TermLink, return false if they
//     * interacted recently
//     * <p>
//     * called in TermLinkBag only
//     *
//     * @param termLink    The TermLink to be checked
//     * @param currentTime The current time
//     * @return (float) the novelty of the termlink, 0 = entirely non-novel (already processed in this cycle), 1 = totally novel (as far as its limited memory remembers)
//     */
//    public float novel(final TermLink termLink, final long currentTime, int noveltyHorizon, int recordLength) {
//
//        final Term bTerm = termLink.getTarget().getTerm();
//        if (bTerm.equals(targetTask.sentence.term)) {
//            return 0;
//        }
//
//        if ((lastFireTime == -1) || (noveltyHorizon == 0)) return 1; //noveltyHorizon==0: everything novel
//
//        if (records == null) {
//            //records = new ArrayDeque(recordLength);
//            //records = new LinkedList();
//            records = new RecordingList(recordLength);
//        }
//
//
//
//        final long minTime = lastFireTime - noveltyHorizon;
//
//        long newestRecordTime =
//                (records.isEmpty()) ?
//                        currentTime : records.getLast().time;
//
//        long age = 0;
//
//        if (newestRecordTime <= minTime) {
//            //just erase the entire record list because its newest entry is older than the noveltyHorizon
//            //faster than iterating and removing individual entries (in the following else condition)
//            records.clear();
//        } else {
//            //iterating the FIFO deque from oldest (first) to newest (last)
//            //  this awkward for-loop with the CircularArrayList replaced an ArrayDeque version because ArrayDeque does not provide indexed access and this required using its Iterator which involved an allocation.  this should be less expensive and it is a critical section
//            int size = records.size();
//            for (int i = 0; i < size; i++) {
//                Recording r = records.get(i);
//
//                if (termLink.termLinkEquals(r.link, true)) {
//                    records.removeFast(i);
//                    return r;
//
////                    if (minTime < rtime) {
////                        //too recent, not novel
////                        return false;
////                    } else {
////                        //happened long enough ago that we have forgotten it somewhat, making it seem more novel
////                        records.removeFast(i);
////                        addRecord(r.setTime(currentTime));
////                        return true;
////                    }
//                } else if (minTime > rtime) {
//                    //remove a record which will not apply to any other tlink
//
//                    records.remove(i);
//                    i--; //skip back one so the next iteration will be at the element after the one removed
//                    size--;
//                }
//            }
//
//
//            //keep recordedLinks queue a maximum finite size
//            int toRemove = (records.size() + 1) - recordLength;
//            for (int i = 0; i < toRemove; i++)
//                records.removeFirstFast();
//
//        }
//
//        // add knowledge reference to recordedLinks
//        records.add(termLink, currentTime);
//
//
//        return n;
//    }

//    protected void addRecord(Recording r) {
//        records.addLast(r);
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
    public Term getTarget() {
        return targetTask.getTerm();
    }

    public Task getTask() {
        return targetTask;
    }

    @Override
    public void delete() {
        if (records != null) {
            records.clear();
            records = null;
        }
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

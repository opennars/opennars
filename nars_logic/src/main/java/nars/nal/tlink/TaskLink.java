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

import nars.budget.Budget;
import nars.nal.Item;
import nars.nal.Sentence;
import nars.nal.Sentenced;
import nars.nal.Task;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import nars.nal.term.Termed;
import nars.util.data.CircularArrayList;

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
    private final Concept concept;


    /* Remember the TermLinks, and when they has been used recently with this TaskLink */
    final static class Recording {

        public TermLink link;
        public long time;

        public Recording(TermLink link, long time) {
            this.link = link;
            this.time = time;
        }

        public long getTime() {
            return time;
        }


        public Recording setTime(long t) {
            this.time = t;
            return this;
        }

        @Override
        public String toString() {
            return link + "@" + time;
        }
    }

    RecordingList records;


    /** allows re-use of the Recording object since it would otherwise be instantiated frequently */
    public static class RecordingList extends CircularArrayList<Recording> {

        public RecordingList(int capacity) {
            super(capacity);
        }


        public void add(final TermLink t, final long time) {
            add(new Recording(t, time));
        }

    }

    /**
     * The type of tlink, one of the above
     */
    public final short type;

    /**
     * The index of the component in the component list of the compound, may have up to 4 levels
     */
    public final short[] index;


    public TaskLink(final Concept c, final Task t, final Budget v) {
        super(v);
        this.concept = c;
        this.type = TermLink.SELF;
        this.index = null;

        this.targetTask = t;

    }

    /**
     * Constructor
     * <p>
     *
     * @param t        The target Task
     * @param template The TermLink template
     * @param v        The budget
     */
    public TaskLink(final Concept c, final Task t, final TermLinkTemplate template, final Budget v) {
        super(v);
        this.concept = c;
        this.type = template.type;
        this.index = template.index;

        this.targetTask = t;

    }


    @Override
    public Sentence name() { return getSentence(); }


    @Override
    public int hashCode() {
        return getSentence().hashCode();
    }

    public CircularArrayList<Recording> getRecords() {
        return records;
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

    /**
     * To check whether a TaskLink should use a TermLink, return false if they
     * interacted recently
     * <p>
     * called in TermLinkBag only
     *
     * @param termLink    The TermLink to be checked
     * @param currentTime The current time
     * @return Whether they are novel to each other
     */
    public boolean novel(final TermLink termLink, final long currentTime, int noveltyHorizon, int recordLength) {

        final Term bTerm = termLink.getTarget().getTerm();
        if (bTerm.equals(targetTask.sentence.term)) {
            return false;
        }

        if (noveltyHorizon == 0) return true;

        if (records == null) {
            //records = new ArrayDeque(recordLength);
            //records = new LinkedList();
            records = new RecordingList(recordLength);
        }

        final long minTime = currentTime - noveltyHorizon;

        long newestRecordTime =
                (records.isEmpty()) ?
                        currentTime : records.getLast().time;

        if (newestRecordTime <= minTime) {
            //just erase the entire record list because its newest entry is older than the noveltyHorizon
            //faster than iterating and removing individual entries (in the following else condition)
            records.clear();
        } else {
            //iterating the FIFO deque from oldest (first) to newest (last)
            //  this awkward for-loop with the CircularArrayList replaced an ArrayDeque version because ArrayDeque does not provide indexed access and this required using its Iterator which involved an allocation.  this should be less expensive and it is a critical section
            int size = records.size();
            for (int i = 0; i < size; i++) {
                Recording r = records.get(i);
                final long rtime = r.getTime();

                if (termLink.termLinkEquals(r.link, true)) {
                    if (minTime < rtime) {
                        //too recent, not novel
                        return false;
                    } else {
                        //happened long enough ago that we have forgotten it somewhat, making it seem more novel
                        records.removeFast(i);
                        addRecord(r.setTime(currentTime));
                        return true;
                    }
                } else if (minTime > rtime) {
                    //remove a record which will not apply to any other tlink

                    records.remove(i);
                    i--; //skip back one so the next iteration will be at the element after the one removed
                    size--;
                }
            }


            //keep recordedLinks queue a maximum finite size
            int toRemove = (records.size() + 1) - recordLength;
            for (int i = 0; i < toRemove; i++)
                records.removeFirstFast();

        }

        // add knowledge reference to recordedLinks
        records.add(termLink, currentTime);

        return true;
    }

    protected void addRecord(Recording r) {
        records.addLast(r);
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
        return concept.getTerm();
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
        return getTarget().getTerm();
    }

    @Override
    public Sentence getSentence() {
        return targetTask.sentence;
    }


}

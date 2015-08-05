package nars.premise;
//
//import com.gs.collections.api.tuple.Pair;
//import com.gs.collections.impl.tuple.Tuples;
//import nars.Memory;
//import nars.Param;
//import nars.bag.Bag;
//import nars.concept.Concept;
//import nars.link.TaskLink;
//import nars.link.TermLink;
//import nars.link.TermLinkKey;
//import nars.task.Task;
//import nars.term.Term;
//
//import java.util.*;
//

/**
 * Uses a Record short-term-memory list of recently fired termlinks
 * for each tasklink
 */

//TODO reimplement and make non-abstract

abstract public class NoveltyRecordPremiseGenerator implements PremiseGenerator {

    //noveltyHorizon.set(0.7f/termLinksPerCycle);
    //termLinkRecordLength.set(8);

//    /**
//     * this value is multiplied by the size of the termlink bag to determine
//     * how long ago a termlink will be considered completely novel for pairing
//     * with a tasklink during fire,
//     *
//     * a value of 1.0 then means that it should take N cycles before a term
//     * is considered completely novel to a Tasklink.
//     * */
//    public AtomicDouble noveltyHorizon = new AtomicDouble();
//
//    /** probability that a completely non-novel termlink/tasklink pair (older than novelty horizon) will be selected */
//    public static float NOVELTY_FLOOR = 0.05f;
}


//
//    final static float minNovelty = Param.NOVELTY_FLOOR;
//
//    private float noveltyHorizon;
//    private int numTermLinks; //total # of tasklinks in the bag
//
//    private float noveltyDuration;
//
//    final Memory memory;
//
//    public NoveltyRecordPremiseSelector(Memory memory) {
//        this.memory = memory;
//    }
//
//    LinkedHashSet<Pair<Task,Term>> records = new LinkedHashSet();
//
//    /**
//     * now is float because it is calculated as the fraction of current time + 1/(termlinks matched), thus including the subcycle
//     */
//    public void set(final float noveltyHorizon, final int numTermLinksInBag) {
//        this.noveltyHorizon = noveltyHorizon;
//        this.numTermLinks = numTermLinksInBag;
//
//
//        /** proportional to an amount of cycles it should take a fired termlink
//         * to be considered novel.
//         * there needs to be at least 2 termlinks to use the novelty filter.
//         * if there is one termlink, there is nothing to prioritize it against.
//         * */
//        this.noveltyDuration = (noveltyHorizon *
//                Math.max(0, numTermLinksInBag - 1));
//    }
//
//
//    public boolean add(Pair<Task, Term> pair) {
//        if (records.a)
//    }
//
//    public boolean test(TaskLink taskLink, TermLink termLink, long now, Random rng) {
//        if (noveltyDuration == 0) {
//            //this will happen in the case of one termlink,
//            //in which case there is no other option so duration
//            //will be zero
//            return true;
//        }
//
//        if (!PremiseSelector.validTermLinkTarget(taskLink, termLink))
//            return false;
//
//        Pair<Task, Term> pair = pair(taskLink, termLink);
//        Recording r = add(pair);
//        if (r == null) {
//            taskLink.put(termLink, now);
//            return true;
//        } else {
//            boolean result;
//
//            //determine age (non-novelty) factor
//            float lft = taskLink.getLastFireTime();
//            if (lft == -1) {
//                //this is its first fire
//                result = true;
//            } else {
//
//                float timeSinceLastFire = lft - r.getTime();
//                float factor = noveltyFactor(timeSinceLastFire, minNovelty, noveltyDuration);
//
//                if (factor <= 0) {
//                    result = false;
//                } else if (factor >= 1f) {
//                    result = true;
//                } else {
//                    float f = rng.nextFloat();
//                    result = (f < factor);
//                }
//            }
//
//
//            if (result) {
//                taskLink.put(r, now);
//                return true;
//            } else {
//                return false;
//            }
//
//        }
//
//    }
//
//    public static Pair<Task, Term> pair(TaskLink taskLink, TermLink termLink) {
//        return Tuples.pair(taskLink.getTask(), termLink.getTarget());
//    }
//
//    public static float noveltyFactor(final float timeSinceLastFire, final float minNovelty, final float noveltyDuration) {
//
//
//        if (timeSinceLastFire <= 0)
//            return minNovelty;
//
//        float n = Math.max(0,
//                Math.min(1f,
//                        timeSinceLastFire /
//                                noveltyDuration));
//
//
//        n = (minNovelty) + (n * (1.0f - minNovelty));
//
//        return n;
//
//    }
//
//
//    /**
//     * Replace default to prevent repeated logic, by checking TaskLink
//     *
//     * @param taskLink The selected TaskLink
//     * @param time     The current time
//     * @return The selected TermLink
//     */
//    public TermLink nextTermLink(Concept c, TaskLink taskLink) {
//
//        final float noveltyHorizon = memory.param.noveltyHorizon.floatValue();
//
//        final int links = c.getTermLinks().size();
//        if (links == 0) return null;
//
//        int toMatch = memory.param.termLinkMaxMatched.get();
//
//        //optimization case: if there is only one termlink, we will never get anything different from calling repeatedly
//        if (links == 1) toMatch = 1;
//
//        Bag<TermLinkKey, TermLink> tl = c.getTermLinks();
//
//        set(noveltyHorizon, tl.size());
//
//        final long now = memory.time();
//
//        Random rng = memory.random;
//
//        for (int i = 0; (i < toMatch); i++) {
//
//            final TermLink termLink = tl.forgetNext();
//
//            if (termLink != null) {
//                if (test(taskLink, termLink, now, rng)) {
//                    return termLink;
//                }
//            } else {
//                break;
//            }
//
//        }
//
//        return null;
//    }
//
//
//
//
//
//
//    /** returns the record associated with a termlink, or null if none exists (or if no records exist) */
//    public Recording get(final TermLink termLink) {
//        final Term bTerm = termLink.getTarget();
//
//        if (records == null) {
//            return null;
//        }
//
//        Recording r = records.get(bTerm);
//        if (r == null) {
//            return null;
//        }
//        else {
//            return r;
//        }
//    }
//
//    public Collection<Recording> getRecords() {
//        if (records == null) return Collections.EMPTY_LIST;
//        return records.values();
//    }
//    /* Remember the TermLinks, and when they has been used recently with this TaskLink */
//    public final static class Recording {
//
//        public final Pair<Task, Term> pair;
//        float time;
//
//        public Recording(TaskLink task, TermLink link, float time) {
//            pair = Tuples.pair(task.getTask(), link.getTarget());
//            this.time = time;
//        }
//
//        public float getTime() {
//            return time;
//        }
//
//
//        public boolean setTime(float t) {
//            if (this.time!=t) {
//                this.time = t;
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            return pair.equals(obj);
//        }
//
//        @Override
//        public int hashCode() {
//            return pair.hashCode();
//        }
//
//        @Override
//        public String toString() {
//            return term + "@" + time;
//        }
//
//        public Term getTerm() {
//            return term.getTerm();
//        }
//
//        public void setRemoved() {
//            setTime(Float.NaN);
//        }
//    }
//
//    public Map<Term,Recording> newRecordSet() {
//        //TODO use more efficient collection?
//
//        return new LinkedHashMap<Term,Recording>(recordLength) {
//            protected boolean removeEldestEntry(Map.Entry<Term,Recording> eldest) {
//                if (size() > recordLength) {
//
//                    eldest.getValue().setRemoved();
//                    return true;
//                }
//                return false;
//            }
//        };
//    }
//
//
//    public void put(final TermLink t, final float now) {
//        put(new Recording(t, now));
//    }
//
//    public void put(final Recording r, final float now) {
//        //if the time has changed, then actually insert it.
//        //this works because if the recordlink has been removed by
//        //the collection, it will have its time set to NaN
//        if (r.setTime(now))
//            put(r);
//    }
//
//    protected void put(final Recording r) {
//        if (records == null)
//            records = newRecordSet();
//
//        records.put(r.getTerm(), r);
//    }
//
////    /**
////     * To check whether a TaskLink should use a TermLink, return false if they
////     * interacted recently
////     * <p>
////     * called in TermLinkBag only
////     *
////     * @param termLink    The TermLink to be checked
////     * @param currentTime The current time
////     * @return (float) the novelty of the termlink, 0 = entirely non-novel (already processed in this cycle), 1 = totally novel (as far as its limited memory remembers)
////     */
////    public float novel(final TermLink termLink, final long currentTime, int noveltyHorizon, int recordLength) {
////
////        final Term bTerm = termLink.getTarget().getTerm();
////        if (bTerm.equals(targetTask.sentence.term)) {
////            return 0;
////        }
////
////        if ((lastFireTime == -1) || (noveltyHorizon == 0)) return 1; //noveltyHorizon==0: everything novel
////
////        if (records == null) {
////            //records = new ArrayDeque(recordLength);
////            //records = new LinkedList();
////            records = new RecordingList(recordLength);
////        }
////
////
////
////        final long minTime = lastFireTime - noveltyHorizon;
////
////        long newestRecordTime =
////                (records.isEmpty()) ?
////                        currentTime : records.getLast().time;
////
////        long age = 0;
////
////        if (newestRecordTime <= minTime) {
////            //just erase the entire record list because its newest entry is older than the noveltyHorizon
////            //faster than iterating and removing individual entries (in the following else condition)
////            records.clear();
////        } else {
////            //iterating the FIFO deque from oldest (first) to newest (last)
////            //  this awkward for-loop with the CircularArrayList replaced an ArrayDeque version because ArrayDeque does not provide indexed access and this required using its Iterator which involved an allocation.  this should be less expensive and it is a critical section
////            int size = records.size();
////            for (int i = 0; i < size; i++) {
////                Recording r = records.get(i);
////
////                if (termLink.termLinkEquals(r.link, true)) {
////                    records.removeFast(i);
////                    return r;
////
//////                    if (minTime < rtime) {
//////                        //too recent, not novel
//////                        return false;
//////                    } else {
//////                        //happened long enough ago that we have forgotten it somewhat, making it seem more novel
//////                        records.removeFast(i);
//////                        addRecord(r.setTime(currentTime));
//////                        return true;
//////                    }
////                } else if (minTime > rtime) {
////                    //remove a record which will not apply to any other tlink
////
////                    records.remove(i);
////                    i--; //skip back one so the next iteration will be at the element after the one removed
////                    size--;
////                }
////            }
////
////
////            //keep recordedLinks queue a maximum finite size
////            int toRemove = (records.size() + 1) - recordLength;
////            for (int i = 0; i < toRemove; i++)
////                records.removeFirstFast();
////
////        }
////
////        // add knowledge reference to recordedLinks
////        records.add(termLink, currentTime);
////
////
////        return n;
////    }
//
////    protected void addRecord(Recording r) {
////        records.addLast(r);
////    }
//
//
//
//
//
//
//}

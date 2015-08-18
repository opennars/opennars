/*
 * Stamp.java
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
 * GNU General Pbulic License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.task.stamp;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.task.Sentence;

import java.io.Serializable;
import java.util.Arrays;

import static nars.nal.nal7.TemporalRules.*;

/** TODO divide this into a Stamp and Timed interfaces,
 *  with a subclass of Time additionally responsible for NAL7+ occurenceTime
 */

public interface Stamp extends Cloneable, Serializable {

    /**
     * default for atemporal events
     * means "always" in Judgment/Question, but "current" in Goal/Quest
     */
    public static final long ETERNAL = Integer.MIN_VALUE;
    /**
     * flag for an unknown time, or as-yet-un-perceived time,
     * signalling a missing value to set to some default
     * if eventually perceived or derived
     */
    public static final long TIMELESS = Integer.MIN_VALUE + 1;

    /*** zips two evidentialBase arrays into a new one */
    static long[] zip(final long[] a, final long[] b) {

        final int baseLength = Math.min(a.length + b.length, Global.MAXIMUM_EVIDENTAL_BASE_LENGTH);

        long[] c = new long[baseLength];

        int firstLength = a.length;
        int secondLength = b.length;

        int i2 = 0, j =0;
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143
        while (i2 < secondLength && j < baseLength) {
            c[j++] = b[i2++];
        }
        int i1 = 0;
        while (i1 < firstLength && j < baseLength) {
            c[j++] = a[i1++];
        }
        return c;
    }

    public static long getOccurrenceTime(long creationTime, final Tense tense, Memory m) {
        return getOccurrenceTime(creationTime, tense, m.duration());
    }

    public static long getOccurrenceTime(long creationTime, final Tense tense, final int duration) {

        if (creationTime == Stamp.TIMELESS) {
            //in this case, occurenceTime must be considered relative to whatever creationTime will be set when perceived
            //so we base it at zero to make this possible
            creationTime = 0;
        }

        if (tense == null)
            return Stamp.ETERNAL;

        switch (tense) {
            case Present:
                return creationTime;
            case Past:
                return creationTime - duration;
            case Future:
                return creationTime + duration;
            default:
            //case Unknown:
            //case Eternal:
                return Stamp.ETERNAL;
        }
    }

    public static long[] toSetArray(final long[] x) {
        final int l = x.length;

        if (l < 2)
            return x;


        //1. copy evidentialBase and sort it
        long[] sorted = Arrays.copyOf(x, l);
        Arrays.sort(sorted);

        //2. count unique elements
        long lastValue = -1;
        int uniques = 0; //# of unique items
        final int sLen = sorted.length;

        for (int i = 0; i < sLen; i++) {
            long v = sorted[i];
            if (lastValue != v)
                uniques++;
            lastValue = v;
        }

        if (uniques == sLen) {
            //if no duplicates, just return it
            return sorted;
        }

        //3. de-duplicate
        lastValue = -1;
        long[] deduplicated = new long[uniques];
        uniques = 0;
        for (int i = 0; i < sLen; i++) {
            long v = sorted[i];
            if (lastValue != v)
                deduplicated[uniques++] = v;
            lastValue = v;
        }
        return deduplicated;
    }

    /** true if there are any common elements; assumes the arrays are sorted and contain no duplicates */
    public static boolean overlapping(final long[] a, final long[] b) {

        /** TODO there may be additional ways to exit early from this loop */

        for (long x : a) {
            for (long y : b) {
                if (x == y) {
                    return true;
                }
                else if (y > x) {
                    //any values after y in b will not be equal to x
                    break;
                }
            }
        }
        return false;
    }

    public static boolean overlapping(final Sentence a, final Sentence b) {

        if (a == b) return true;

        return overlapping(a.getEvidence(), b.getEvidence());
    }





//    public static long[] toSetArrayHeap(final long[] x) {
//        final int l = x.length;
//
//        if (l < 2)
//            return x;
//
//        long[] y = Arrays.copyOf(x, l);
//
//        int duplicates = LongDeduplicatingHeapSort.sort(y);
//        if (duplicates == 0)
//            return y;
//        else {
//            return Arrays.copyOfRange(y, duplicates, l);
//        }
//    }

    public long getCreationTime();

    public Stamp setCreationTime(long t);

    public int getDuration();

    public long getOccurrenceTime();

    public Stamp setOccurrenceTime(long t);



    /*
     private static boolean equalBases(long[] base1, long[] base2) {
     if (base1.baseLength != base2.baseLength) {
     return false;
     }
     for (long n1 : base1) {
     boolean found = false;
     for (long n2 : base2) {
     if (n1 == n2) {
     found = true;
     }
     }
     if (!found) {
     return false;
     }
     }
     return true;
     }
     */

    default public boolean before(Stamp s, int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return order(s.getOccurrenceTime(), getOccurrenceTime(), duration) == TemporalRules.ORDER_BACKWARD;
    }

    /*
    public Stamp cloneWithNewCreationTime(long newCreationTime) {
        return new Stamp(this, newCreationTime, getOccurrenceTime());
    }

    public Stamp cloneWithNewOccurrenceTime(final long newOcurrenceTime) {
        return new Stamp(this, getCreationTime(), newOcurrenceTime);
    }

    public Stamp cloneEternal() {
        return cloneWithNewOccurrenceTime(ETERNAL);
    }*/

    /** true if this instance is after 's' */
    default public boolean after(Stamp s, int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return TemporalRules.after(s.getOccurrenceTime(), getOccurrenceTime(), duration);
    }

    default public float getOriginality() {
        return 1.0f / (getEvidence().length + 1);
    }
//
//    /**
//     * Get the occurrenceTime of the truth-value
//     *
//     * @return The occurrence time
//     */
//    public String getOccurrenceTimeString() {
//        if (isEternal()) {
//            return "";
//        } else {
//            return appendOcurrenceTime(new StringBuilder()).toString();
//        }
//    }

    default public boolean isEternal() {
        return getOccurrenceTime() == ETERNAL;
    }

    /**
     * Check if two stamps contains the same types of content
     * <p>
     * NOTE: hashcode will include within it the creationTime & occurrenceTime, so if those are not to be compared then avoid comparing hash
     *
     * @param s The Stamp to be compared
     * @return Whether the two have contain the same evidential base
     */
    default public boolean equalStamp(final Stamp s, final boolean evidentialSet, final boolean creationTime, final boolean occurrenceTime) {
        if (this == s) return true;

        /*if (hash && (!occurrenceTime || !evidentialSet))
            throw new RuntimeException("Hash equality test must be followed by occurenceTime and evidentialSet equality since hash incorporates them");

        if (hash)
            if (hashCode() != s.hashCode()) return false;*/
        if (creationTime)
            if (getCreationTime() != s.getCreationTime()) return false;
        if (occurrenceTime)
            if (getOccurrenceTime() != s.getOccurrenceTime()) return false;
        if (evidentialSet) {
            //iterate in reverse; the ending of the evidence chain is more likely to be different
            /*final long[] a = getEvidentialSet();
            final long[] b = s.getEvidentialSet();
            //if (evidentialHash!=s.evidentialHash)
            //    return false;
            if (a.length != b.length) return false;
            for (int i = a.length - 1; i >= 0; i--)
                if (a[i] != b[i]) return false;*/
            return Arrays.equals(getEvidence(), s.getEvidence());
        }


        return true;
    }

    /**
     * Get the occurrenceTime of the truth-value
     *
     * @return The occurrence time
     */

//    @Deprecated public Stamp setOccurrenceTime(long l) {
//        this.occurrenceTime = l;
//        this.hash = 0;
//        return this;
//    }

//    public void setCreationTime(long c) {
//        this.creationTime = c;
//        this.hash = 0;
//    }




    default public StringBuilder appendOccurrenceTime(final StringBuilder sb) {
        if (getOccurrenceTime() != ETERNAL) {
            int estTimeLength = 10; /* # digits */
            sb.ensureCapacity(estTimeLength);
            sb.append(Long.toString(getCreationTime()));

            long relOc = (getOccurrenceTime() - getCreationTime());
            if (relOc >= 0)
                sb.append('+'); //+ sign if positive or zero, negative sign will be added automatically in converting the int to string:
            sb.append(relOc);
        }
        return sb;
    }

    default public String getTense(final long currentTime, final int duration) {

        if (isEternal()) {
            return "";
        }

        switch (TemporalRules.order(currentTime, getOccurrenceTime(), duration)) {
            case ORDER_FORWARD:
                return Symbols.TENSE_FUTURE;
            case ORDER_BACKWARD:
                return Symbols.TENSE_PAST;
            default:
                return Symbols.TENSE_PRESENT;
        }
    }

    default public CharSequence stampAsStringBuilder() {

        final long[] ev = getEvidence();
        final int len = ev != null ? ev.length : 0;
        final int estimatedInitialSize = 8 + (len * 3);

        final StringBuilder buffer = new StringBuilder(estimatedInitialSize);
        buffer.append(Symbols.STAMP_OPENER);

        if (getCreationTime() == Stamp.TIMELESS) {
            buffer.append('?');
        } else if (!isEternal()) {
            appendOccurrenceTime(buffer);
        } else {
            buffer.append(getCreationTime());
        }
        buffer.append(Symbols.STAMP_STARTER).append(' ');
        for (int i = 0; i < len; i++) {

            buffer.append(Long.toString(ev[i], 36));
            if (i < (len - 1)) {
                buffer.append(Symbols.STAMP_SEPARATOR);
            }
        }

        buffer.append(Symbols.STAMP_CLOSER); //.append(' ');

        //this is for estimating an initial size of the stringbuffer
        //System.out.println(baseLength + " " + derivationChain.size() + " " + buffer.baseLength());

        return buffer;


    }

    /** deduplicated and sorted version of the evidentialBase.
     * this can always be calculated deterministically from the evidentialBAse
     * since it is the deduplicated and sorted form of it. */
    abstract public long[] getEvidence();

    abstract public Stamp setEvidence(long... evidentialSet);

    public boolean isCyclic();

    /**
     * Get a String form of the Stamp for display Format: {creationTime [:
     * eventTime] : evidentialBase}
     *
     * @return The Stamp as a String
     */
    /*
     final static String stampOpenerSpace = " " + Symbols.STAMP_OPENER;
     final static String spaceStampStarterSpace = " " + Symbols.STAMP_STARTER + " ";
     final static String stampCloserSpace = Symbols.STAMP_CLOSER + " ";

     @Override
     public String toString() {
     if (toStringCache == null) {
     int numBases = evidentialBase.size();
     final StringBuilder b = new StringBuilder(8+numBases*5 // TODO properly estimate this //);

     b.append(stampOpenerSpace).append(creationTime)
     .append(spaceStampStarterSpace);

     int i = 0;
     for (long eb : evidentialBase) {
     b.append(Long.toString(eb));
     if (i++ < (numBases - 1)) {
     b.append(Symbols.STAMP_SEPARATOR);
     } else {
     b.append(stampCloserSpace);
     }
     }
     toStringCache = b.toString();
     }
     return toStringCache;
     }
     */



    abstract void setCyclic(boolean cyclic);

    default public boolean isInput() {
        return false;
    }


    /*public int getDuration() {
        return duration;
    }*/


    //String toStringCache = null; //holds pre-allocated symbol for toString()

    void applyToStamp(Stamp target);

    //    static boolean isCyclic(final long[] eb, long[] es) {
//        if (eb == null) {
//            throw new RuntimeException("evidentialBase null");
//        }
//        if (es == null) {
//            throw new RuntimeException("evidentialSet null");
//        }
//
//        //if the evidential set contains duplicates, it will be of a smaller size then the original evidence
//        return es.length != eb.length;
//
//
//        /*
//        final int stampLength = evidentialBase.length;
//        for (int i = 0; i < stampLength; i++) {
//            final long baseI = evidentialBase[i];
//            for (int j = 0; j < stampLength; j++) {
//                if ((i != j) && (baseI == evidentialBase[j])) {
//                    return true;
//                }
//            }
//        }
//        return false;
//        */
//
//    }


    default Stamp setTime(long creation, long occurrence) {
        setCreationTime(creation);
        setOccurrenceTime(occurrence);
        return this;
    }


    Stamp setDuration(int duration);
}


    /*

        final int baseLength = Math.min(firstBase.length + secondBase.length, Parameters.MAXIMUM_EVIDENTAL_BASE_LENGTH);
        //long[] evidentialBase = new long[baseLength];

        int firstLength = firstBase.length;
        int secondLength = secondBase.length;

        LongHashSet h = new LongHashSet(firstLength + secondLength);

        int i2;
        int i1 = i2 = 0;



        //Store the value negative so sort order will be reversed. then negative again to restore the original value
        while (i2 < secondLength && (h.size() < baseLength)) {
            h.add( -secondBase[i2++] );
        }
        while (i1 < firstLength && (h.size() < baseLength)) {
            h.add( -firstBase[i1++] );
        }

        long[] evidentialBase = h.toSortedArray();
        for (int i = 0; i < evidentialBase.length; i++) {
            evidentialBase[i] *= -1;
        }

        return new Stamp(evidentialBase, creationTime, occurenceTime, duration);
    }
    */

///** Deduplicating heap sort for long[] arrays.
// *  WARNING doesnt seem to work
// * */
//public static class LongDeduplicatingHeapSort {
//
//
//    /**
//     * Standard heapsort.
//     * @param a an array of Comparable items.
//     * @return how many duplicates were removed
//     */
//    public static int sort( long[ ] a )
//    {
//        int duplicates = 0;
//
//
//        int al = a.length;
//
//        for( int i = al / 2; i >= 0; ) { /* buildHeap */
//
//            int dd = percDown(a, i, al);
//
//            boolean dupLimit = ( duplicates >= al-1);
//
//            if ((dd > 0) && (!dupLimit)) {
//                duplicates += dd;
//                dupLimit = ( duplicates >= al-1);
//            }
//
//
//            //proceed only if there was no duplicate, or if we have reached the duplicate limit
//            if ((dd == 0) || dupLimit) {
//                i--;
//            }
//        }
//
//        for( int i = al - 1; i > 0;  ) {
//
//            swapReferences( a, 0, i );  /* deleteMax */
//
//            int dd = percDown( a, 0, i );
//
//            boolean dupLimit = ( duplicates >= al-1);
//
//            if ((dd > 0) && (!dupLimit)) {
//                duplicates += dd;
//                dupLimit = ( duplicates >= al-1);
//            }
//
//
//            //proceed only if there was no duplicate, or if we have reached the duplicate limit
//            if ((dd == 0) || dupLimit) {
//                i--;
//            }
//            else {
//                //go backwards
//                if (i < al - 1)
//                    i++;
//            }
//
//        }
//
//        return duplicates;
//    }
//
//    /**
//     * Internal method for heapsort.
//     * @param i the index of an item in the heap.
//     * @return the index of the left child.
//     */
//    private static int leftChild( final int i ) {
//        return 2 * i + 1;
//    }
//
//    /**
//     * Internal method for heapsort that is used in
//     * deleteMax and buildHeap.
//     * @param a an array of Comparable items.
//     * @index i the position from which to percolate down.
//     * @int n the logical size of the binary heap.
//     * @return how many duplicates were removed
//     */
//    private static int percDown( long [] a, int i, int n )
//    {
//        int c;
//        long tmp;
//        int dups = 0;
//
//        for( tmp = a[ i ]; leftChild( i ) < n; i = c ) {
//            c = leftChild(i);
//
//
//
//            if( c != n-1 ) {
//                final long ac1 = a[c+1];
//
//                int c1 = Long.compare(a[c], ac1 );
//                if ((c1 == 0) && (a[c]!=-1)) {
//                    a[c] = -1;
//                    //dups++;
//                    return 1;
//                }
//
//                if (c1 < 0 )
//                    c++;
//            }
//
//
//            int c2 = Long.compare(tmp, a[c]);
//            if ((c2 == 0) && (tmp!=-1)) {
//                tmp = -1;
//                return 1;
//                //dups++;
//            }
//
//            if( c2 >= 0 ) {
//                break;
//            }
//
//            a[i] = a[c];
//        }
//
//        a[ i ] = tmp;
//
//        return 0;
//    }
//
//
//    /**
//     * Method to swap to elements in an array.
//     * @param a an array of objects.
//     * @param index1 the index of the first object.
//     * @param index2 the index of the second object.
//     */
//    public static final void swapReferences( final long[ ] a, final int index1, final int index2 ) {
//        final long tmp = a[ index1 ];
//        a[ index1 ] = a[ index2 ];
//        a[ index2 ] = tmp;
//    }
//}




//
//    public Stamp(final long[] evidentialBase, final long creationTime, final long occurenceTime, final int duration) {
//        super();
//        this.creationTime = creationTime;
//        this.occurrenceTime = occurenceTime;
//        this.duration = duration;
//        this.evidentialBase = evidentialBase;
//    }
//
//    protected Stamp(final long serial, final long creationTime, final long occurenceTime, final int duration) {
//        this(new long[]{serial}, creationTime, occurenceTime, duration);
//    }
//
//
//
//    public Stamp(final Stamp parent, final Memory memory, final Tense tense) {
//        this(parent, memory.time(), getOccurrenceTime(memory.time(), tense, memory.duration()));
//    }
//
//
//    public Stamp(Operation operation, Memory memory, Tense tense) {
//        this(operation.getTask().sentence.getStamp(), memory, tense);
//    }

//    /**
//     * Generate a new stamp identical with a given one
//     *
//     * @param parent The stamp to be cloned
//     */
//    protected Stamp(final Stamp parent) {
//        this(parent, parent.creationTime, parent.occurrenceTime);
//    }

//    public Stamp(final Stamp parent, final long creationTime, final long occurenceTime) {
//        this(parent.evidentialBase, creationTime, occurenceTime, parent.duration);
//    }
//

//    /**
//     * Generate a new stamp for derived sentence by merging the two from parents
//     * the first one is no shorter than the second
//     *
//     * @param first  The first Stamp
//     * @param second The second Stamp
//     */
//    @Deprecated public static Stamp zip(final Stamp first, final Stamp second, final long creationTime, final long occurenceTime) {
//
////        if (first==second) {
////            throw new RuntimeException("Same stamp: " + first);
////        }
//
////        if (first.equals(second, true, true, true, true)) {
////            throw new RuntimeException("Equal stamp: " + first);
////        }
//        //this may not be a problem, but let's deal with that when we use different durations in the same system(s):
//        if (second.duration != first.duration)
//            throw new RuntimeException("Stamp can not be created from parents with different durations: " + first + ", " + second);
//
//
//
//        return new Stamp(zip(first.evidentialBase, second.evidentialBase), creationTime, occurenceTime, first.duration);
//
//
//    }
//
////
//
//    /**
//     * create an original stamp at current memory time, with a tense offset
//     */

//
//    /**
//     * create an original stamp at current memory time, with a specific occurence time
//     */
//    public Stamp(final Memory memory, long occurenceTime) {
//        this(memory, memory.time(), occurenceTime);
//    }
//
//    /**
//     * create an original stamp at current memory time, with a specific creation and occurence time
//     */
//    public Stamp(final Memory memory, long creationTime, long occurenceTime) {
//        this(memory.newStampSerial(), creationTime, occurenceTime, memory.duration());
//    }
//
//    /**
//     * create an original stamp at current memory time, with a specific creation and tense offset
//     */
//    public Stamp(final Memory memory, long creationTime, final Tense tense) {
//        this(memory, creationTime, getOccurrenceTime(creationTime, tense, memory.duration()));
//    }
//    public Stamp(final long[] evidence, Memory memory, long creationTime, final Tense tense) {
//        this(evidence, creationTime,  getOccurrenceTime(creationTime, tense, memory.duration()), memory.duration());
//    }

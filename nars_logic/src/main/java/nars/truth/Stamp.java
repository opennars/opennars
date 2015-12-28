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
package nars.truth;

import nars.Global;

import java.util.Arrays;

/**
 * TODO divide this into a Stamp and Timed interfaces,
 * with a subclass of Time additionally responsible for NAL7+ occurenceTime
 */

public interface Stamp  {

    /***
     * zips two evidentialBase arrays into a new one
     */
    static long[] zip(long[] a, long[] b) {

        int baseLength = Math.min(a.length + b.length, Global.MAXIMUM_EVIDENTAL_BASE_LENGTH);

        long[] c = new long[baseLength];

        int firstLength = a.length;
        int secondLength = b.length;

        int i2 = 0, j = 0;
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

    static long[] toSetArray(long[] x) {
        int l = x.length;

        if (l < 2)
            return x;


        //1. copy evidentialBase and sort it
        long[] sorted = Arrays.copyOf(x, l);
        Arrays.sort(sorted);

        //2. count unique elements
        long lastValue = -1;
        int uniques = 0; //# of unique items
        int sLen = sorted.length;

        for (long v : sorted) {
            if (lastValue != v)
                uniques++;
            lastValue = v;
        }

        if (uniques == sLen) {
            //if no duplicates, just return it
            return sorted;
        }

        //3. de-duplicate
        long lastValue2 = -1;
        long[] deduplicated = new long[uniques];
        uniques = 0;
        for (long v : sorted) {
            if (lastValue2 != v)
                deduplicated[uniques++] = v;
            lastValue2 = v;
        }
        return deduplicated;
    }


    long getCreationTime();

    Stamp setCreationTime(long t);



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

//    default boolean before(Stamp s, int duration) {
//        if (isEternal() || s.isEternal())
//            return false;
//        return order(s.getOccurrenceTime(), getOccurrenceTime(), duration) == TemporalRules.ORDER_BACKWARD;
//    }

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

//    /** true if this instance is after 's' */
//    default boolean after(Stamp s, int duration) {
//        if (isEternal() || s.isEternal())
//            return false;
//        return TemporalRules.after(s.getOccurrenceTime(), getOccurrenceTime(), duration);
//    }

    default float getOriginality() {
        if (getEvidence() == null)
            throw new RuntimeException(this + " has null evidence");
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

    /**
     * deduplicated and sorted version of the evidentialBase.
     * this can always be calculated deterministically from the evidentialBAse
     * since it is the deduplicated and sorted form of it.
     */
    long[] getEvidence();

    Stamp setEvidence(long... evidentialSet);


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

//    default boolean isInput() {
//        return false;
//    }


    /*public int getDuration() {
        return duration;
    }*/


    //String toStringCache = null; //holds pre-allocated symbol for toString()

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

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
package nars.nal.stamp;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;

import java.io.Serializable;
import java.util.Arrays;

import static nars.nal.nal7.TemporalRules.*;
import static nars.nal.nal7.Tense.*;

/** TODO divide this into a Stamp and Timed interfaces,
 *  with a subclass of Time additionally responsible for NAL7+ occurenceTime
 */

public interface Stamp extends StampEvidence, Cloneable, Serializable {

    /**
     * default for atemporal events
     * means "always" in Judgment/Question, but "current" in Goal/Quest
     */
    public static final long ETERNAL = Integer.MIN_VALUE;
    /**
     * flag for an un-perceived stamp, which signals to be set to the current time if it is eventually perceived
     */
    public static final long UNPERCEIVED = Integer.MIN_VALUE + 1;




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



    abstract public long getCreationTime();

    abstract public int getDuration();

    abstract public long getOccurrenceTime();


    abstract public Stamp cloneWithNewCreationTime(long newCreationTime);
    abstract public Stamp cloneWithNewOccurrenceTime(final long newOcurrenceTime);
    default public Stamp cloneEternal() {
        return cloneWithNewOccurrenceTime(ETERNAL);
    }


    /*** zips two evidentialBase arrays into a new one */
    static long[] zip(final long[] a, final long[] b) {

        final int baseLength = Math.min(a.length + b.length, Global.MAXIMUM_EVIDENTAL_BASE_LENGTH);

        long[] c = new long[baseLength];

        int firstLength = a.length;
        int secondLength = b.length;

        int i1 = 0, i2 = 0, j =0;
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143
        while (i2 < secondLength && j < baseLength) {
            c[j++] = b[i2++];
        }
        while (i1 < firstLength && j < baseLength) {
            c[j++] = a[i1++];
        }
        return c;
    }

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

    public static long getOccurrenceTime(long creationTime, final Tense tense, Memory m) {
        return getOccurrenceTime(creationTime, tense, m.duration());
    }

    public static long getOccurrenceTime(long creationTime, final Tense tense, final int duration) {

        if (creationTime == Stamp.UNPERCEIVED) {
            //in this case, occurenceTime must be considered relative to whatever creationTime will be set when perceived
            //so we base it at zero to make this possible
            creationTime = 0;
        }

        if (tense == Past) {
            return creationTime - duration;
        } else if (tense == Future) {
            return creationTime + duration;
        } else if (tense == Present) {
            return creationTime;
        } else if (tense == Unknown) {
            return Stamp.ETERNAL;
        }

        return Stamp.ETERNAL;
    }


    /** Deduplicating heap sort for long[] arrays.
     *  WARNING doesnt seem to work
     * */
    public static class LongDeduplicatingHeapSort {


        /**
         * Standard heapsort.
         * @param a an array of Comparable items.
         * @return how many duplicates were removed
         */
        public static int sort( long[ ] a )
        {
            int duplicates = 0;


            int al = a.length;

            for( int i = al / 2; i >= 0; ) { /* buildHeap */

                int dd = percDown(a, i, al);

                boolean dupLimit = ( duplicates >= al-1);

                if ((dd > 0) && (!dupLimit)) {
                    duplicates += dd;
                    dupLimit = ( duplicates >= al-1);
                }


                //proceed only if there was no duplicate, or if we have reached the duplicate limit
                if ((dd == 0) || dupLimit) {
                    i--;
                }
            }

            for( int i = al - 1; i > 0;  ) {

                swapReferences( a, 0, i );  /* deleteMax */

                int dd = percDown( a, 0, i );

                boolean dupLimit = ( duplicates >= al-1);

                if ((dd > 0) && (!dupLimit)) {
                    duplicates += dd;
                    dupLimit = ( duplicates >= al-1);
                }


                //proceed only if there was no duplicate, or if we have reached the duplicate limit
                if ((dd == 0) || dupLimit) {
                    i--;
                }
                else {
                    //go backwards
                    if (i < al - 1)
                        i++;
                }

            }

            return duplicates;
        }

        /**
         * Internal method for heapsort.
         * @param i the index of an item in the heap.
         * @return the index of the left child.
         */
        private static int leftChild( final int i ) {
            return 2 * i + 1;
        }

        /**
         * Internal method for heapsort that is used in
         * deleteMax and buildHeap.
         * @param a an array of Comparable items.
         * @index i the position from which to percolate down.
         * @int n the logical size of the binary heap.
         * @return how many duplicates were removed
         */
        private static int percDown( long [] a, int i, int n )
        {
            int c;
            long tmp;
            int dups = 0;

            for( tmp = a[ i ]; leftChild( i ) < n; i = c ) {
                c = leftChild(i);



                if( c != n-1 ) {
                    final long ac1 = a[c+1];

                    int c1 = Long.compare(a[c], ac1 );
                    if ((c1 == 0) && (a[c]!=-1)) {
                        a[c] = -1;
                        //dups++;
                        return 1;
                    }

                    if (c1 < 0 )
                        c++;
                }


                int c2 = Long.compare(tmp, a[c]);
                if ((c2 == 0) && (tmp!=-1)) {
                    tmp = -1;
                    return 1;
                    //dups++;
                }

                if( c2 >= 0 ) {
                    break;
                }

                a[i] = a[c];
            }

            a[ i ] = tmp;

            return 0;
        }


        /**
         * Method to swap to elements in an array.
         * @param a an array of objects.
         * @param index1 the index of the first object.
         * @param index2 the index of the second object.
         */
        public static final void swapReferences( final long[ ] a, final int index1, final int index2 ) {
            final long tmp = a[ index1 ];
            a[ index1 ] = a[ index2 ];
            a[ index2 ] = tmp;
        }
    }

    public static long[] toSetArray(final long[] x) {
        long[] z = toSetArrayOLD(x);

        /*if (x.length!=1) {
            long[] y = toSetArrayHeap(x);

            if (!Arrays.equals(z, y)) {
                System.err.println("inconsistent toSetArray: " + Arrays.toString(x) + "  " + Arrays.toString(y) + "  " + Arrays.toString(z));
            }
        }*/

        return z;
    }

    public static long[] toSetArrayHeap(final long[] x) {
        final int l = x.length;

        if (l < 2)
            return x;

        long[] y = Arrays.copyOf(x, l);

        int duplicates = LongDeduplicatingHeapSort.sort(y);
        if (duplicates == 0)
            return y;
        else {
            return Arrays.copyOfRange(y, duplicates, l);
        }
    }

    public static long[] toSetArrayOLD(final long[] x) {
        final int l = x.length;

        if (l < 2)
            return x;

        long[] set = Arrays.copyOf(x, l);

        //1. copy evidentialBse
        //2. sorted
        //3. count duplicates
        //4. create new array

        Arrays.sort(set);
        long lastValue = -1;
        int j = 0; //# of unique items
        for (int i = 0; i < set.length; i++) {
            long v = set[i];
            if (lastValue != v)
                j++;
            lastValue = v;
        }
        lastValue = -1;
        long[] sorted = new long[j];
        j = 0;
        for (int i = 0; i < set.length; i++) {
            long v = set[i];
            if (lastValue != v)
                sorted[j++] = v;
            lastValue = v;
        }
        return sorted;
    }

    default public boolean before(Stamp s, int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return order(s.getOccurrenceTime(), getOccurrenceTime(), duration) == TemporalRules.ORDER_BACKWARD;
    }

    /** true if this instance is after 's' */
    default public boolean after(Stamp s, int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return TemporalRules.after(s.getOccurrenceTime(), getOccurrenceTime(), duration);
    }

    default public float getOriginality() {
        return 1.0f / (getEvidentialSet().length + 1);
    }

    default public boolean isEternal() {
        return getOccurrenceTime() == ETERNAL;
    }



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



    /**
     * Check if two stamps contains the same types of content
     * <p>
     * NOTE: hashcode will include within it the creationTime & occurrenceTime, so if those are not to be compared then avoid comparing hash
     *
     * @param s The Stamp to be compared
     * @return Whether the two have contain the same evidential base
     */
    default public boolean equalStamp(final Stamp s, final boolean hash, final boolean evidentialBase, final boolean creationTime, final boolean occurrenceTime) {
        if (this == s) return true;

        if (hash && (!occurrenceTime || !evidentialBase))
            throw new RuntimeException("Hash equality test must be followed by occurenceTime and evidentialSet equality since hash incorporates them");

        if (hash)
            if (hashCode() != s.hashCode()) return false;
        if (creationTime)
            if (getCreationTime() != s.getCreationTime()) return false;
        if (occurrenceTime)
            if (getOccurrenceTime() != s.getOccurrenceTime()) return false;
        if (evidentialBase) {
            //iterate in reverse; the ending of the evidence chain is more likely to be different
            final long[] a = getEvidentialSet();
            final long[] b = s.getEvidentialSet();
            //if (evidentialHash!=s.evidentialHash)
            //    return false;
            if (a.length != b.length) return false;
            for (int i = a.length - 1; i >= 0; i--)
                if (a[i] != b[i]) return false;
        }


        return true;
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




    default public StringBuilder appendOcurrenceTime(final StringBuilder sb) {
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

    default public CharSequence stampAsStringBuilder() {

        long[] evidentialBase = getEvidentialBase();

        final int baseLength = evidentialBase.length;
        final int estimatedInitialSize = 8 + (baseLength * 3);

        final StringBuilder buffer = new StringBuilder(estimatedInitialSize);
        buffer.append(Symbols.STAMP_OPENER);
        if (!isEternal()) {
            appendOcurrenceTime(buffer);
        } else {
            buffer.append(getCreationTime());
        }
        buffer.append(Symbols.STAMP_STARTER).append(' ');
        for (int i = 0; i < baseLength; i++) {
            buffer.append(Long.toString(evidentialBase[i], 16));
            if (i < (baseLength - 1)) {
                buffer.append(Symbols.STAMP_SEPARATOR);
            }
        }

        buffer.append(Symbols.STAMP_CLOSER).append(' ');

        //this is for estimating an initial size of the stringbuffer
        //System.out.println(baseLength + " " + derivationChain.size() + " " + buffer.baseLength());

        return buffer;


    }


    public Stamp setCreationTime(long c);
    public Stamp setOccurrenceTime(long o);
    public Stamp setDuration(int d);

    /** should not call this directly, but use setEvidence() */
    public Stamp setEvidentialBase(long[] b);

    /** default implementation here is just to ignore the cached value
     * because an implementation can generate one anyway.
     * but if the implementation wants to store it they can trust
     * this will be called with a precomputed value that matches the
     * evidentialBase provided in a previous call.
     *
     *
     */
    default Stamp setEvidentialSet(long[] evidentialSetCached) {  return this;  }

    default Stamp setEvidence(long[] evidentialBase, long[] evidentialSet) {
        setEvidentialBase(evidentialBase);
        setEvidentialSet(evidentialSet);
        return this;
    }

    default Stamp setTime(long creation, long occurrence) {
        setCreationTime(creation);
        setOccurrenceTime(occurrence);
        return this;
    }


    /*public int getDuration() {
        return duration;
    }*/


    //String toStringCache = null; //holds pre-allocated symbol for toString()

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


    static boolean isCyclic(final StampEvidence x) {
        long[] eb = x.getEvidentialBase();
        switch (eb.length) {
            case 0:
            case 1:
                return false;
            case 2:
                return eb[0] != eb[1];
            default:
                return isCyclic(eb, x.getEvidentialSet());
        }
    }

    static boolean isCyclic(final long[] eb, long[] es) {
        //if the evidential set contains duplicates, it will be of a smaller size then the original evidence
        return es.length != eb.length;


        /*
        final int stampLength = evidentialBase.length;
        for (int i = 0; i < stampLength; i++) {
            final long baseI = evidentialBase[i];
            for (int j = 0; j < stampLength; j++) {
                if ((i != j) && (baseI == evidentialBase[j])) {
                    return true;
                }
            }
        }
        return false;
        */

    }


    public static boolean evidentialSetOverlaps(final Stamp a, final Stamp b) {
        /** TODO since these are sorted, we can compare these faster by
            iterating both arrays simultaneously skipping ahead when one
            has a higher value than the other until the shorter reaches the end

            or at least compare a triangular half of the matrix with these
            2 loops
         */
        for (long l : a.getEvidentialSet()) {
            for (long h : b.getEvidentialSet()) {
                if (l == h) {
                    return true;
                }
            }
        }
        return false;
    }



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

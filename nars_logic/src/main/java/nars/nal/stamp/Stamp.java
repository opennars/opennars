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

import nars.Memory;
import nars.Global;
import nars.Symbols;
import nars.nal.NAL;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;

import java.util.Arrays;

import static nars.nal.nal7.TemporalRules.*;
import static nars.nal.nal7.Tense.*;

public class Stamp implements Cloneable, NAL.StampBuilder, Stamped {

    /**
     * default for atemporal events
     * means "always" in Judgment/Question, but "current" in Goal/Quest
     */
    public static final long ETERNAL = Integer.MIN_VALUE;
    /**
     * flag for an un-perceived stamp, which signals to be set to the current time if it is eventually perceived
     */
    public static final long UNPERCEIVED = Integer.MIN_VALUE + 1;
    /**
     * serial numbers. not to be modified after Stamp constructor has initialized it
     */
    public final long[] evidentialBase;
    /**
     * duration (in cycles) which any contained intervals are measured by
     */
    private final int duration;
    /**
     * creation time of the stamp
     */
    private long creationTime;
    /**
     * estimated occurrence time of the event
     * TODO: make this final?
     */
    private long occurrenceTime;

    /**
     * used when the occurrence time cannot be estimated, means "unknown"
     */
    //public static final long UNKNOWN = Integer.MAX_VALUE;
    /**
     * caches evidentialBase as a set for comparisons and hashcode.
     * stores the unique Long's in-order for efficiency
     */
    private long[] evidentialSet = null;


    /**
     * cache of hashcode of evidential base
     */
    transient private int hash;
    transient private int evidentialHash; //hash which includes only evidentialSet, not creation/occurenceTimes


    public Stamp(final long[] evidentialBase, final long creationTime, final long occurenceTime, final int duration) {
        super();
        this.creationTime = creationTime;
        this.occurrenceTime = occurenceTime;
        this.duration = duration;
        this.evidentialBase = evidentialBase;
    }

    protected Stamp(final long serial, final long creationTime, final long occurenceTime, final int duration) {
        this(new long[]{serial}, creationTime, occurenceTime, duration);
    }



    public Stamp(final Stamp parent, final Memory memory, final Tense tense) {
        this(parent, memory.time(), getOccurrenceTime(memory.time(), tense, memory.duration()));
    }


    public Stamp(Operation operation, Memory memory, Tense tense) {
        this(operation.getTask().sentence.getStamp(), memory, tense);
    }

    /**
     * Generate a new stamp identical with a given one
     *
     * @param parent The stamp to be cloned
     */
    protected Stamp(final Stamp parent) {
        this(parent, parent.creationTime, parent.occurrenceTime);
    }

    public Stamp(final Stamp parent, final long creationTime, final long occurenceTime) {
        this(parent.evidentialBase, creationTime, occurenceTime, parent.duration);
    }




    /**
     * Generate a new stamp for derived sentence by merging the two from parents
     * the first one is no shorter than the second
     *
     * @param first  The first Stamp
     * @param second The second Stamp
     */
    public static Stamp zip(final Stamp first, final Stamp second, final long creationTime, final long occurenceTime) {

//        if (first==second) {
//            throw new RuntimeException("Same stamp: " + first);
//        }

//        if (first.equals(second, true, true, true, true)) {
//            throw new RuntimeException("Equal stamp: " + first);
//        }

        final long[] firstBase = first.evidentialBase;
        final long[] secondBase = second.evidentialBase;

        int duration = first.duration;
        //this may not be a problem, but let's deal with that when we use different durations in the same system(s):
        if (second.duration != first.duration)
            throw new RuntimeException("Stamp can not be created from parents with different durations: " + first + ", " + second);


        final int baseLength = Math.min(firstBase.length + secondBase.length, Global.MAXIMUM_EVIDENTAL_BASE_LENGTH);
        long[] evidentialBase = new long[baseLength];

        int firstLength = firstBase.length;
        int secondLength = secondBase.length;

        int i1 = 0, i2 = 0, j =0;
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143
        while (i2 < secondLength && j < baseLength) {
            evidentialBase[j++] = secondBase[i2++];
        }
        while (i1 < firstLength && j < baseLength) {
            evidentialBase[j++] = firstBase[i1++];
        }

        return new Stamp(evidentialBase, creationTime, occurenceTime, duration);


    }



    /**
     * create an original stamp at current memory time, with a tense offset
     */
    public Stamp(final Memory memory, final Tense tense) {
        this(memory, memory.time(), tense);
    }

    /**
     * create an original stamp at current memory time, with a specific occurence time
     */
    public Stamp(final Memory memory, long occurenceTime) {
        this(memory, memory.time(), occurenceTime);
    }

    /**
     * create an original stamp at current memory time, with a specific creation and occurence time
     */
    public Stamp(final Memory memory, long creationTime, long occurenceTime) {
        this(memory.newStampSerial(), creationTime, occurenceTime, memory.duration());
    }

    /**
     * create an original stamp at current memory time, with a specific creation and tense offset
     */
    public Stamp(final Memory memory, long creationTime, final Tense tense) {
        this(memory, creationTime, getOccurrenceTime(creationTime, tense, memory.duration()));
    }
    public Stamp(final long[] evidence, Memory memory, long creationTime, final Tense tense) {
        this(evidence, creationTime,  getOccurrenceTime(creationTime, tense, memory.duration()), memory.duration());
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
        }

        return Stamp.ETERNAL;
    }

    public static long[] toSetArray(final long[] x) {
        long[] set = x.clone();

        if (x.length < 2)
            return set;

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

    public boolean before(Stamp s, int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return order(s.occurrenceTime, occurrenceTime, duration) == TemporalRules.ORDER_BACKWARD;
    }

    public boolean after(Stamp s, int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return order(s.occurrenceTime, occurrenceTime, duration) == TemporalRules.ORDER_FORWARD;
    }

    public float getOriginality() {
        return 1.0f / (evidentialBase.length + 1);
    }

    public boolean isEternal() {
        return occurrenceTime == ETERNAL;
    }

    @Override
    public Stamp build() {
        return this;
    }

    /**
     * Clone a stamp
     *
     * @return The cloned stamp
     */
    @Override
    public Stamp clone() {
        return new Stamp(this);
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
     * Get a number from the evidentialBase by index, called in this class only
     *
     * @param i The index
     * @return The number at the index
     */
    long get(final int i) {
        return evidentialBase[i];
    }

    /**
     * Convert the evidentialBase into a set
     *
     * @return The TreeSet representation of the evidential base
     */
    public long[] toSet() {
        if (evidentialSet == null) {
            this.evidentialSet = toSetArray(evidentialBase);
            this.evidentialHash = Arrays.hashCode(evidentialSet);
            this.hash = 0;
        }

        return evidentialSet;
    }

    @Override
    public boolean equals(final Object that) {
        throw new RuntimeException("Use other equals() method");
    }

    /**
     * Check if two stamps contains the same types of content
     * <p>
     * NOTE: hashcode will include within it the creationTime & occurrenceTime, so if those are not to be compared then avoid comparing hash
     *
     * @param s The Stamp to be compared
     * @return Whether the two have contain the same evidential base
     */
    public boolean equals(final Stamp s, final boolean hash, final boolean evidentialBase, final boolean creationTime, final boolean occurrenceTime) {
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
            final long[] a = toSet();
            final long[] b = s.toSet();
            if (evidentialHash!=s.evidentialHash)
                return false;
            if (a.length != b.length) return false;
            for (int i = a.length - 1; i >= 0; i--)
                if (a[i] != b[i]) return false;
        }


        return true;
    }

    /**
     * The hash code of Stamp; incorporates evidentialHash and occurenceTime (but not creationTime)
     *
     * @return The hash code
     */
    public final int hashCode() {
        if (evidentialSet == null)
            toSet();
        if (hash == 0) {
            hash = (int)(evidentialHash + 31 * occurrenceTime);
        }
        return hash;
    }

    public Stamp cloneWithNewCreationTime(long newCreationTime) {
        return new Stamp(this, newCreationTime, getOccurrenceTime());
    }

    public Stamp cloneWithNewOccurrenceTime(final long newOcurrenceTime) {
        return new Stamp(this, getCreationTime(), newOcurrenceTime);
    }

    public Stamp cloneEternal() {
        return cloneWithNewOccurrenceTime(ETERNAL);
    }

    /**
     * Get the occurrenceTime of the truth-value
     *
     * @return The occurrence time
     */
    public long getOccurrenceTime() {
        return occurrenceTime;
    }

    @Deprecated public Stamp setOccurrenceTime(long l) {
        this.occurrenceTime = l;
        this.hash = 0;
        return this;
    }

    public void setEternal() {
        setOccurrenceTime(ETERNAL);
    }

    public StringBuilder appendOcurrenceTime(final StringBuilder sb) {
        if (occurrenceTime != ETERNAL) {
            int estTimeLength = 10; /* # digits */
            sb.ensureCapacity(estTimeLength);
            sb.append(Long.toString(creationTime));

            long relOc = (occurrenceTime - creationTime);
            if (relOc >= 0)
                sb.append('+'); //+ sign if positive or zero, negative sign will be added automatically in converting the int to string:
            sb.append(relOc);


        }
        return sb;
    }

    public String getTense(final long currentTime, final int duration) {

        if (isEternal()) {
            return "";
        }

        switch (TemporalRules.order(currentTime, occurrenceTime, duration)) {
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

    public CharSequence name() {

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

    @Override
    public String toString() {
        return name().toString();
    }

    /**
     * @return the creationTime
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * sets the creation time; used to set input tasks with the actual time they enter Memory
     */
    public void setTime(long creation, long occurrence) {
        this.creationTime = creation;
        this.occurrenceTime = occurrence;
        this.hash = 0;
    }

    public int getDuration() {
        return duration;
    }


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
    @Override
    public Stamp getStamp() {
        return this;
    }

    public boolean isCyclic() {
        long[] es = toSet();
        return es.length!=evidentialBase.length; //if the evidential set contains duplicates, it will be of a smaller size then the original evidence
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

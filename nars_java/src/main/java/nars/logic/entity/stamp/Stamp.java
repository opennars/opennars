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
package nars.logic.entity.stamp;

import nars.core.Memory;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.logic.NAL;
import nars.logic.nal7.TemporalRules;
import nars.logic.nal7.Tense;
import nars.logic.nal8.Operation;

import java.util.Arrays;
import java.util.Collection;

import static nars.logic.nal7.TemporalRules.*;
import static nars.logic.nal7.Tense.*;


public class Stamp implements Cloneable, NAL.StampBuilder {


    /**
     * serial numbers. not to be modified after Stamp constructor has initialized it
     */
    public final long[] evidentialBase;


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
     * default for atemporal events
     * means "always" in Judgment/Question, but "current" in Goal/Quest     
     */
    public static final long ETERNAL = Integer.MIN_VALUE;

    /**
     * used when the occurrence time cannot be estimated, means "unknown"
     */
    //public static final long UNKNOWN = Integer.MAX_VALUE;

    
    /** caches evidentialBase as a set for comparisons and hashcode.
        stores the unique Long's in-order for efficiency
     */    
    private long[] evidentialSet = null;
    


    
    /** cache of hashcode of evidential base */
    transient private int evidentialHash;



    public boolean before(Stamp s, int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return order(s.occurrenceTime, occurrenceTime, duration) == TemporalRules.ORDER_BACKWARD;
    }
    
    public boolean after(Stamp s, int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return order(s.occurrenceTime, occurrenceTime, duration) == TemporalRules.ORDER_FORWARD;        }

    public float getOriginality() {
        return 1.0f / (evidentialBase.length + 1);
    }



    public interface DerivationBuilder {
        Collection build();
    }


    protected Stamp(final long serial, long creationTime) {
        super();

        this.evidentialBase = new long[1];
        this.evidentialBase[0] = serial;

        this.creationTime = creationTime;

    }


    /**
     * Generate a new stamp, with a new serial number, for a new Task
     *
     * @param creationTime Creation time of the stamp
     */
    protected Stamp(final long serial, final long creationTime, final Tense tense, final int duration) {
        this(serial, creationTime);

        setOccurenceTime(tense, duration);

    }

    protected void setOccurenceTime(Tense tense, int duration) {
        if (tense == null) {
            occurrenceTime = ETERNAL;
        } else if (tense == Past) {
            occurrenceTime = creationTime - duration;
        } else if (tense == Future) {
            occurrenceTime = creationTime + duration;
        } else if (tense == Present) {
            occurrenceTime = creationTime;
        }
    }

    /**
     * Generate a new stamp identical with a given one
     *
     * @param parent The stamp to be cloned
     */
    private Stamp(final Stamp parent) {
        this(parent, parent.creationTime);
    }

    /**
     * Generate a new stamp from an existing one, with the same evidentialBase
     * but different creation time
     * <p>
     * For single-premise rules
     *
     * @param parent The stamp of the single premise
     */


    public Stamp(final Stamp parent, final Memory memory, final Tense tense) {
        this(parent, memory.time() );

        setOccurenceTime(tense, memory.getDuration());
    }

    public Stamp(Operation operation, Memory memory, Tense tense) {
        this(operation.getTask().sentence.getStamp(), memory, tense);
    }


    public Stamp(final Stamp parent, final long creationTime, final long occurenceTime) {
        this(parent, creationTime);
        this.occurrenceTime = occurenceTime;

    }
    public Stamp(final Stamp parent, final long creationTime) {

        this.evidentialBase = parent.evidentialBase;

        this.creationTime = creationTime;
        this.occurrenceTime = parent.getOccurrenceTime();

    }

    public Stamp(final Stamp first, final Stamp second, final long creationTime) {
        this(first, second, creationTime,
                first.getOccurrenceTime() /* use the creation time of the first task */ );
    }

    /**
     * Generate a new stamp for derived sentence by merging the two from parents
     * the first one is no shorter than the second
     *
     * @param first The first Stamp
     * @param second The second Stamp
     */
    public Stamp(final Stamp first, final Stamp second, final long creationTime, final long occurenceTime) {
        //TODO use iterators instead of repeated first and second .get's?
        
        int i2, j;
        int i1 = i2 = j = 0;

        final long[] firstBase = first.evidentialBase;
        final long[] secondBase = second.evidentialBase;

        final int baseLength = Math.min(firstBase.length + secondBase.length, Parameters.MAXIMUM_EVIDENTAL_BASE_LENGTH);
        this.evidentialBase = new long[baseLength];

        int firstLength = firstBase.length;
        int secondLength = secondBase.length;

        this.creationTime = creationTime;
        this.occurrenceTime = occurenceTime;
        

        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143        
        while (i2 < secondLength && j < baseLength) {
            evidentialBase[j++] = secondBase[i2++];
        }
        while (i1 < firstLength && j < baseLength) {
            evidentialBase[j++] = firstBase[i1++];
        }

    }

    public Stamp(final Memory memory, final Tense tense, long creationTime) {
        this(memory.newStampSerial(), creationTime, tense, memory.param.duration.get());
    }

    /** create stamp at current memory time */
    public Stamp(final Memory memory, final Tense tense) {
        this(memory, tense, memory.time());
    }


    public Stamp(final Memory memory, long creationTime, long occurenceTime) {
        this(memory.newStampSerial(), creationTime);
        this.occurrenceTime = occurenceTime;
    }

    public Stamp(final Memory memory, long occurenceTime) {
        this(memory, memory.time(), occurenceTime);
    }

    
    public boolean isEternal() {
        return occurrenceTime == ETERNAL;
    }

    /** sets the creationTime to a non-value so that it will be set at a later point, ex: after traversing the input queue */
    public void setNotYetPerceived() {
        creationTime = -1;
    }

    /** sets the creation time; used to set input tasks with the actual time they enter Memory */
    public void setCreationTime(long creationTime) {
        long originalCreationTime = this.creationTime;
        this.creationTime = creationTime;

        //shift occurence time relative to the new creation time
        if (occurrenceTime != Stamp.ETERNAL) {
            occurrenceTime = occurrenceTime + (creationTime - originalCreationTime);
        }
    }
    

    @Override
    public Stamp build() {
        return this;
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
     * Clone a stamp
     *
     * @return The cloned stamp
     */
    @Override
    public Stamp clone() {
        return new Stamp(this);
    }


    /**
     * Get a number from the evidentialBase by index, called in this class only
     *
     * @param i The index
     * @return The number at the index
     */
    long get(final int i) {
        return evidentialBase[i];
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

    /**
     * Convert the evidentialBase into a set
     *
     * @return The TreeSet representation of the evidential base
     */
    public long[] toSet() {
        if (evidentialSet == null) {        
            evidentialSet = toSetArray(evidentialBase);
            evidentialHash = Arrays.hashCode(evidentialSet);
        }
        
        return evidentialSet;
    }

    
    @Override public boolean equals(final Object that) {
        throw new RuntimeException("Use other equals() method");
    }
    
    /**
     * Check if two stamps contains the same types of content
     *
     * @param s The Stamp to be compared
     * @return Whether the two have contain the same evidential base
     */
    public boolean equals(Stamp s, final boolean creationTime, final boolean ocurrenceTime, final boolean evidentialBase) {
        if (this == s) return true;

        if (creationTime)
            if (getCreationTime()!=s.getCreationTime()) return false;
        if (ocurrenceTime)
            if (getOccurrenceTime()!=s.getOccurrenceTime()) return false;       
        if (evidentialBase) {
            if (evidentialHash() != s.evidentialHash()) return false;

            //iterate in reverse; the ending of the evidence chain is more likely to be different
            final long[] a = toSet();
            final long[] b = s.toSet();
            if (a.length != b.length) return false;
            for (int i = a.length-1; i >=0; i--)
                if (a[i]!=b[i]) return false;
        }
        

        return true;        
    }
            

    
    /**
     * The hash code of Stamp
     *
     * @return The hash code
     */
    public final int evidentialHash() {
        if (evidentialSet==null)
            toSet();       
        return evidentialHash;
    }

    public Stamp cloneWithNewCreationTime(long newCreationTime) {
        return new Stamp(this, newCreationTime);
    }
    public Stamp cloneWithNewOccurrenceTime(final long newOcurrenceTime) {
        Stamp s = new Stamp(this, getCreationTime(), newOcurrenceTime);
        return s;
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
    
    public void setEternal() {
        occurrenceTime=ETERNAL;
    }

    
    public StringBuilder appendOcurrenceTime(final StringBuilder sb) {
        if (occurrenceTime != ETERNAL) {
            int estTimeLength = 8; /* # digits */
            sb.ensureCapacity(estTimeLength + 1 + 1);
            sb.append('[').append(occurrenceTime).append(']');
        }
        return sb;
    }
            
    /**
     * Get the occurrenceTime of the truth-value
     *
     * @return The occurrence time
     */
    public String getOccurrenceTimeString() {
        if (isEternal()) {
            return "";
        } else {
            return appendOcurrenceTime(new StringBuilder()).toString();
        }
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



    public CharSequence name() {

            final int baseLength = evidentialBase.length;
            final int estimatedInitialSize = 8 + (baseLength * 3);

            final StringBuilder buffer = new StringBuilder(estimatedInitialSize);
            buffer.append(Symbols.STAMP_OPENER).append(getCreationTime());
            if (!isEternal()) {
                buffer.append('|').append(occurrenceTime);
            }
            buffer.append(' ').append(Symbols.STAMP_STARTER).append(' ');
            for (int i = 0; i < baseLength; i++) {
                buffer.append(Long.toString(evidentialBase[i]));
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

}

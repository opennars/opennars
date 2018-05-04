/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import org.opennars.storage.Memory;
import org.opennars.main.Parameters;
import org.opennars.inference.TemporalRules;
import static org.opennars.inference.TemporalRules.ORDER_BACKWARD;
import static org.opennars.inference.TemporalRules.ORDER_FORWARD;
import org.opennars.io.Symbols;
import org.opennars.language.Tense;
import static org.opennars.language.Tense.Future;
import static org.opennars.language.Tense.Past;
import static org.opennars.language.Tense.Present;
import static org.opennars.inference.TemporalRules.order;

public class Stamp implements Cloneable, Serializable {

    /*serial numbers. not to be modified after Stamp constructor has initialized it*/
    public long[] evidentialBase;
    /* evidentialBase baseLength*/
    public int baseLength;
    /*creation time of the stamp*/
    private long creationTime;
    /* estimated occurrence time of the event*/
    private long occurrenceTime;
    /*default for atemporal events means "always" in Judgment/Question, but "current" in Goal/Quest*/
    public static final long ETERNAL = Integer.MIN_VALUE;
    /** caches evidentialBase as a set for comparisons and hashcode, stores the unique Long's in-order for efficiency*/    
    private long[] evidentialSet = null;
    /*Tense of the item*/
    private Tense tense;
    /*True when its a neg confirmation task that was already checked:*/
    public boolean alreadyAnticipatedNegConfirmation = false;
    
    /** caches  */
    CharSequence name = null;
    
    /**
     * derivation chain containing the used premises and conclusions which made
     * deriving the conclusion c possible
     * Uses LinkedHashSet for optimal contains/indexOf performance.
     * TODO use thread-safety for this
     */
    
    /** cache of hashcode of evidential base */
    private int evidentialHash;

    
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
    
    /** used for when the ocrrence time will be set later; so should not be called from externally but through another Stamp constructor */
    protected Stamp(final Tense tense, final long serial) {
        this.baseLength = 1;
        this.evidentialBase = new long[baseLength];
        this.evidentialBase[0] = serial;
        this.tense = tense;
        this.creationTime = -1;
    }
    
    /**
     * Generate a new stamp, with a new serial number, for a new Task
     *
     * @param time Creation time of the stamp
     */
    public Stamp(final long time, final Tense tense, final long serial, final int duration) {    
        this(tense, serial);    
        setCreationTime(time, duration);        
    }

    /**
     * Generate a new stamp identical with a given one
     *
     * @param old The stamp to be cloned
     */
    private Stamp(final Stamp old) {
        this(old, old.creationTime);
    }

    /**
     * Generate a new stamp from an existing one, with the same evidentialBase
     * but different creation time
     * <p>
     * For single-premise rules
     *
     * @param old The stamp of the single premise
     * @param creationTim The current time
     */
    public Stamp(final Stamp old, final long creationTime) {
        this(old, creationTime, old);
    }

    public Stamp(final Stamp old, final long creationTime, final Stamp useEvidentialBase) {        
        this.evidentialBase = useEvidentialBase.evidentialBase;
        this.baseLength = useEvidentialBase.baseLength;
        this.creationTime = creationTime;

        this.occurrenceTime = old.getOccurrenceTime();
    }
    
    /**
     * Generate a new stamp for derived sentence by merging the two from parents
     * the first one is no shorter than the second
     *
     * @param first The first Stamp
     * @param second The second Stamp
     */
    public Stamp(final Stamp first, final Stamp second, final long time) {
        //TODO use iterators instead of repeated first and second .get's?
        
        int i1, i2, j;
        i1 = i2 = j = 0;
        this.baseLength = Math.min(first.baseLength + second.baseLength, Parameters.MAXIMUM_EVIDENTAL_BASE_LENGTH);
        this.evidentialBase = new long[baseLength];

        final long[] firstBase = first.evidentialBase;
        final long[] secondBase = second.evidentialBase;     
        int firstLength = firstBase.length;
        int secondLength = secondBase.length;

        creationTime = time;
        occurrenceTime = first.getOccurrenceTime();    // use the occurrence of task
        
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143        
        while (j < baseLength) {
            if(i2 < secondLength) {
                evidentialBase[j++] = secondBase[i2++];
            }
            if(i1 < firstLength) {
                evidentialBase[j++] = firstBase[i1++];
            }
        }
    }

    public Stamp(final Memory memory, final Tense tense) {
        this(memory.time(), tense, memory.newStampSerial(), Parameters.DURATION);
    }

    /** creates a stamp with default Present tense */
    public Stamp(final Memory memory) {
        this(memory, Tense.Present);
    }
    
    /** Detects evidental base overlaps **/
    public static boolean baseOverlap(long[] base1, long[] base2) {
        HashSet<Long> task_base = new HashSet<>(base1.length + base2.length);
        for(int i=0; i < base1.length; i++) {
            if(task_base.contains(base1[i])) { //can have an overlap in itself already
                return true;
            }
            task_base.add(base1[i]);
        }
        for(int i=0; i < base2.length; i++) {
            if(task_base.contains(base2[i])) {
                return true;
            }
            task_base.add(base2[i]); //also add to detect collision with itself
        }
        return false;
     }
    
    public boolean evidenceIsCyclic() {
        HashSet<Long> task_base = new HashSet<Long>(this.evidentialBase.length);
        for(int i=0; i < this.evidentialBase.length; i++) {
            if(task_base.contains(Long.valueOf(this.evidentialBase[i]))) { //can have an overlap in itself already
                return true;
            }
            task_base.add(this.evidentialBase[i]);
        }
        return false;
    }

    public boolean isEternal() {
        boolean eternalOccurrence = occurrenceTime == ETERNAL;
        
        if (Parameters.DEBUG) {
            if (eternalOccurrence && tense!=Tense.Eternal) {
                throw new RuntimeException("Stamp has inconsistent tense and eternal ocurrenceTime: tense=" + tense);
            }
        }
        
        return eternalOccurrence;
    }
    /** sets the creation time; used to set input tasks with the actual time they enter Memory */
    public void setCreationTime(long time, int duration) {
        creationTime = time;
        
        if (tense == null) {
            occurrenceTime = ETERNAL;
        } else if (tense == Past) {
            occurrenceTime = time - duration;
        } else if (tense == Future) {
            occurrenceTime = time + duration;
        } else if (tense == Present) {
            occurrenceTime = time;
        } else {
            occurrenceTime = time;
        }
        
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
        //2. sort
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
    private long[] toSet() {        
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
     * @param that The Stamp to be compared
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
            if (!Arrays.equals(toSet(), s.toSet())) return false;
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
    
    public Stamp cloneWithNewOccurrenceTime(final long newOcurrenceTime) {
        Stamp s = clone();
        if (newOcurrenceTime == ETERNAL)
            s.tense = Tense.Eternal;
        s.setOccurrenceTime(newOcurrenceTime);
        return s;
    }

    /**
     * Get the occurrenceTime of the truth-value
     *
     * @return The occurrence time
     */
    public long getOccurrenceTime() {
        return occurrenceTime;
    }
    
    /**
     * 
     */
    public void setEternal() {
        occurrenceTime=ETERNAL;
    }
    
    public StringBuilder appendOcurrenceTime(final StringBuilder sb) {
        if (occurrenceTime != ETERNAL) {
            int estTimeLength = 8; /* # digits */
            sb.ensureCapacity(estTimeLength + 1 + 1);
            sb.append('[').append(occurrenceTime).append(']').toString();
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

    public void setOccurrenceTime(final long time) {
        if (occurrenceTime!=time) {
            occurrenceTime = time;
            
            if (time == ETERNAL)
                tense = Tense.Eternal;
                        
            name = null;
        }
    }

    public CharSequence name() {
        if (name == null) {
            
            final int estimatedInitialSize = 10 * baseLength;

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
            name = buffer;
        }
        return name;
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
}

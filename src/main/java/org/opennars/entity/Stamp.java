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

import org.opennars.inference.TemporalRules;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.language.Tense;
import org.opennars.main.MiscFlags;
import org.opennars.storage.Memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static org.opennars.inference.TemporalRules.*;
import static org.opennars.language.Tense.*;
import org.opennars.main.Parameters;

/**
 * Stamps are used to keep track of done derivations
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class Stamp implements Cloneable, Serializable {
    /** serial numbers. not to be modified after Stamp constructor has initialized it*/
    public BaseEntry[] evidentialBase;

    /** the length of @see evidentialBase */
    public int baseLength;

    /** creation time of the stamp */
    private long creationTime;

    /** estimated occurrence time of the event */
    private long occurrenceTime;

    /** default for atemporal events means "always" in Judgment/Question, but "current" in Goal/Quest*/
    public static final long ETERNAL = Integer.MIN_VALUE;

    /** caches evidentialBase as a set for comparisons and hashcode, stores the unique Long's in-order for efficiency*/
    private BaseEntry[] evidentialSet = null;

    /** Tense of the item*/
    private Tense tense;

    /** is it a neg confirmation task that was already checked*/
    public boolean alreadyAnticipatedNegConfirmation = false;
    
    /** caches */
    CharSequence name = null;
    
    /**
     * derivation chain containing the used premises and conclusions which made
     * deriving the conclusion c possible
     * Uses LinkedHashSet for optimal contains/indexOf performance.
     * TODO use thread-safety for this
     */
    
    /** cache of hashcode of evidential base */
    private int evidentialHash;

    
    public boolean before(final Stamp s, final int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return order(s.occurrenceTime, occurrenceTime, duration) == TemporalRules.ORDER_BACKWARD;
    }
    
    public boolean after(final Stamp s, final int duration) {
        if (isEternal() || s.isEternal())
            return false;
        return order(s.occurrenceTime, occurrenceTime, duration) == TemporalRules.ORDER_FORWARD;        }

    public float getOriginality() {
        return 1.0f / (evidentialBase.length + 1);
    }
    
    /** used for when the ocrrence time will be set later; so should not be called from externally but through another Stamp constructor */
    protected Stamp(final Tense tense, final BaseEntry serial) {
        this.baseLength = 1;
        this.evidentialBase = new BaseEntry[baseLength];
        this.evidentialBase[0] = serial;
        this.tense = tense;
        this.creationTime = -1;
    }
    
    /**
     * Generate a new stamp, with a new serial number, for a new Task
     *
     * @param time Creation time of the stamp
     */
    public Stamp(final long time, final Tense tense, final BaseEntry serial, final int duration) {    
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
     * @param creationTime The current time
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
    public Stamp(final Stamp first, final Stamp second, final long time, Parameters narParameters) {
        //TODO use iterators instead of repeated first and second .get's?
        
        int i1, i2, j;
        i1 = i2 = j = 0;
        this.baseLength = Math.min(first.baseLength + second.baseLength, narParameters.MAXIMUM_EVIDENTAL_BASE_LENGTH);
        this.evidentialBase = new BaseEntry[baseLength];

        final BaseEntry[] firstBase = first.evidentialBase;
        final BaseEntry[] secondBase = second.evidentialBase;     
        final int firstLength = firstBase.length;
        final int secondLength = secondBase.length;

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

    public Stamp(final Timable time, final Memory memory, final Tense tense) {
        this(time.time(), tense, memory.newStampSerial(), memory.narParameters.DURATION);
    }

    /** creates a stamp with default Present tense */
    public Stamp(final Timable time, final Memory memory) {
        this(time, memory, Tense.Present);
    }
    
    /** Detects evidental base overlaps **/
    public static boolean baseOverlap(final BaseEntry[] base1, final BaseEntry[] base2) {
        final Set<BaseEntry> task_base = new HashSet<>(base1.length + base2.length);
        for (final BaseEntry aBase1 : base1) {
            if (task_base.contains(aBase1)) { //can have an overlap in itself already
                return true;
            }
            task_base.add(aBase1);
        }
        for (final BaseEntry aBase2 : base2) {
            if (task_base.contains(aBase2)) {
                return true;
            }
            task_base.add(aBase2); //also add to detect collision with itself
        }
        return false;
     }
    
    public boolean evidenceIsCyclic() {
        final Set<BaseEntry> task_base = new HashSet<>(this.evidentialBase.length);
        for (final BaseEntry anEvidentialBase : this.evidentialBase) {
            if (task_base.contains(anEvidentialBase)) { //can have an overlap in itself already
                return true;
            }
            task_base.add(anEvidentialBase);
        }
        return false;
    }

    public boolean isEternal() {
        final boolean eternalOccurrence = occurrenceTime == ETERNAL;
        
        if (MiscFlags.DEBUG) {
            if (eternalOccurrence && tense!=Tense.Eternal) {
                throw new IllegalStateException("Stamp has inconsistent tense and eternal ocurrenceTime: tense=" + tense);
            }
        }
        
        return eternalOccurrence;
    }
    /** sets the creation time; used to set input tasks with the actual time they enter Memory */
    public void setCreationTime(final long time, final int duration) {
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
    
    public static BaseEntry[] toSetArray(final BaseEntry[] x) {
        final BaseEntry[] set = x.clone();
        
        if (x.length < 2)
            return set;
        
        //1. copy evidentialBse
        //2. sort
        //3. count duplicates
        //4. create new array 
        
        Arrays.sort(set);
        BaseEntry lastValue = null;
        int j = 0; //# of unique items
        for (final BaseEntry v : set) {
            if (lastValue == null || !lastValue.equals(v)) {
                j++;
            }
            lastValue = v;
        }
        lastValue = null;
        final BaseEntry[] sorted = new BaseEntry[j];
        j = 0;
        for (final BaseEntry v : set) {
            if (lastValue == null || !lastValue.equals(v)) {
                sorted[j++] = v;
            }
            lastValue = v;
        }
        return sorted;
    }

    /**
     * Convert the evidentialBase into a set
     *
     * @return The NavigableSet representation of the evidential base
     */
    private BaseEntry[] toSet() {        
        if (evidentialSet == null) {        
            evidentialSet = toSetArray(evidentialBase);
            evidentialHash = Arrays.hashCode(evidentialSet);
        }
        
        return evidentialSet;
    }

    
    @Override public boolean equals(final Object that) {
        throw new IllegalStateException("Use other equals() method");
    }
    
    /**
     * Check if two stamps contains the same types of content
     *
     * @param s The Stamp to be compared
     * @return Whether the two have contain the same evidential base
     */
    public boolean equals(final Stamp s, final boolean creationTime, final boolean ocurrenceTime, final boolean evidentialBase) {
        if (this == s) return true;

        if (creationTime)
            if (getCreationTime()!=s.getCreationTime()) return false;
        if (ocurrenceTime)
            if (getOccurrenceTime()!=s.getOccurrenceTime()) return false;       
        if (evidentialBase) {
            if (evidentialHash() != s.evidentialHash()) return false;
            return Arrays.equals(toSet(), s.toSet());
        }
        
        return true;        
    }
    
    /**
     * hash code of Stamp
     *
     * @return hash code
     */
    public final int evidentialHash() {
        if (evidentialSet==null)
            toSet();       
        return evidentialHash;
    }
    
    public Stamp cloneWithNewOccurrenceTime(final long newOcurrenceTime) {
        final Stamp s = clone();
        if (newOcurrenceTime == ETERNAL)
            s.tense = Tense.Eternal;
        s.setOccurrenceTime(newOcurrenceTime);
        return s;
    }

    /**
     * Get the occurrenceTime of the truth-value
     *
     * @return occurrence time
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
            final int estTimeLength = 8; /* # digits */
            sb.ensureCapacity(estTimeLength + 1 + 1);
            sb.append('[').append(occurrenceTime).append(']').toString();
        }
        return sb;
    }
            
    /**
     * Get the occurrenceTime of the truth-value
     *
     * @return occurrence time
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
                buffer.append(evidentialBase[i].toString());
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
     * @return time of creation
     */
    public long getCreationTime() {
        return creationTime;
    }



    /**
     * Element of the evidential base of stamp
     */
    public static class BaseEntry implements Comparable, Serializable {
        public final long narId; //the NAR in which the input evidence was added
        public long getNarId() {
            return narId;
        }
        public final long inputId;
        public long getInputId() {
            return inputId;
        }

        /**
         * The evidential base entry
         *
         * @param narId The id of the NAR the input evidence was obtained from
         * @param inputId The nar-specific input id of the input
         */
        public BaseEntry(long narId, long inputId) {
            this.narId = narId;
            this.inputId = inputId;
        }

        @Override
        public String toString() {
            return "(" + narId + "," + inputId + ")";
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof BaseEntry)){
                return false;
            }
            BaseEntry other_ = (BaseEntry) other;
            return other_.inputId == this.inputId && other_.narId == this.narId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Long.hashCode(narId);
            result = prime * result + Long.hashCode(inputId);
            return result;
        }

        @Override
        public int compareTo(Object o) {
            return Comparator.comparing(BaseEntry::getNarId).thenComparing(BaseEntry::getInputId).compare(this, (BaseEntry) o);
        }
    }
}

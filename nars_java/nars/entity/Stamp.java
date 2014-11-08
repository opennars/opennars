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
package nars.entity;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import nars.core.Memory;
import nars.core.Parameters;
import nars.inference.TemporalRules;
import static nars.inference.TemporalRules.ORDER_BACKWARD;
import static nars.inference.TemporalRules.ORDER_FORWARD;
import static nars.inference.TemporalRules.order;
import nars.io.Symbols;
import nars.language.Tense;
import static nars.language.Tense.Future;
import static nars.language.Tense.Past;
import static nars.language.Tense.Present;
import nars.language.Term;
import nars.language.Terms;


public class Stamp implements Cloneable {


    /**
     * serial numbers. not to be modified after Stamp constructor has initialized it
     */
    public final long[] evidentialBase;

    /**
     * evidentialBase baseLength
     */
    public final int baseLength;

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

    
    private Tense tense;
    
    
    /** caches  */
    transient CharSequence name = null;
    
    /* used for lazily calculating derivationChain on-demand */
    private DerivationBuilder derivationBuilder = null;
    
    
    
    /**
     * derivation chain containing the used premises and conclusions which made
     * deriving the conclusion c possible
     * Uses LinkedHashSet for optimal contains/indexOf performance.
     */
    private Collection<Term> derivationChain;
    
    /** analytics metric */
    transient public final long latency;
    
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
        LinkedHashSet<Term> build();
    }
    
    /** creates a Derivation Chain by collating / zipping 2 Stamps Derivation Chains */
    public static class ZipperDerivationBuilder implements DerivationBuilder {
        private final WeakReference<Stamp> first;
        private final WeakReference<Stamp> second;

        public ZipperDerivationBuilder(Stamp first, Stamp second) {
            this.first = new WeakReference(first);
            this.second = new WeakReference(second);
        }
            
        @Override public LinkedHashSet<Term> build()  {
            Stamp ff = first.get();
            Stamp ss = second.get();
            
            //check if the parent stamps still exist, because they may have been garbage collected
            if ((ff == null) && (ss == null)) {                
                return new LinkedHashSet();
            }
            else {
                //TODO decide if it can use the parent chains directly?
                if (ff == null) {
                    //ss!=null
                    return new LinkedHashSet(ss.getChain());
                }
                else if (ss == null) {
                    //ff!=null                    
                    return new LinkedHashSet(ff.getChain());
                }
            }
                    
            final Collection<Term> chain1 = ff.getChain();
            final Collection<Term> chain2 = ss.getChain();
            
            final Iterator<Term> iter1 = chain1.iterator();
            int i1 = chain1.size() - 1;
            
            final Iterator<Term> iter2 = chain2.iterator();
            int i2 = chain2.size() - 1;

            Set<Term> added = new HashSet();
            //set here is for fast contains() checking
            List<Term> sequence = new ArrayList<>(Parameters.MAXIMUM_EVIDENTAL_BASE_LENGTH);      

            //take as long till the chain is full or all elements were taken out of chain1 and chain2:
            int j = 0;
            while (j < Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH && (i1 >= 0 || i2 >= 0)) {
                if (j % 2 == 0) {//one time take from first, then from second, last ones are more important
                    if (i1 >= 0) {
                        final Term c1i1 = iter1.next();
                        if (!added.add(c1i1)) {
                            sequence.add(c1i1);                        
                        }
                        else {
                            j--; //was double, so we can add one more now
                        }
                        i1--;
                    }
                } else {
                    if (i2 >= 0) {
                        final Term c2i2 = iter2.next();
                        if (!added.add(c2i2)) {
                            sequence.add(c2i2);
                        }
                        else {
                            j--; //was double, so we can add one more now
                        }
                        i2--;
                    }
                }
                j++;
            } 

            if (Parameters.DEBUG) {
                Terms.verifyNonNull(added);
            }

            Collections.reverse(sequence);

            return new LinkedHashSet<>(sequence);
        }                            
    }
    
    /** lazily inherit the derivation from a parent, causing it to cache the derivation also (in case other children get it */
    public static class InheritDerivationBuilder implements DerivationBuilder {
        private final WeakReference<Stamp> parent;

        public InheritDerivationBuilder(Stamp parent) {
            this.parent = new WeakReference(parent);            
        }
        
        @Override public LinkedHashSet<Term> build() {
            if (parent.get() == null) {
                //parent doesnt exist anymore (garbage collected)
                return new LinkedHashSet();
            }
            
            Collection<Term> p = parent.get().getChain();
            if (p instanceof LinkedHashSet)
                return (LinkedHashSet)p;
            else
                return new LinkedHashSet(p);
        }
        
    }
    
    /** used for when the ocrrence time will be set later */
    public Stamp(final Tense tense, final long serial) {
        this.baseLength = 1;
        this.evidentialBase = new long[baseLength];
        this.evidentialBase[0] = serial;
        this.tense = tense;
        this.latency = 0;
        this.creationTime = -1;
        this.derivationBuilder = null;
        this.derivationChain = new LinkedHashSet(Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH);
    }
    
    /**
     * Generate a new stamp, with a new serial number, for a new Task
     *
     * @param time Creation time of the stamp
     */
    public Stamp(final long time, final Tense tense, final long serial, final int duration) {               this(tense, serial);    
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
        this.derivationChain = old.getChain();
        this.latency = this.creationTime - old.latency;
        
        if (derivationChain == null)
            this.derivationBuilder = new InheritDerivationBuilder(old);        
        else
            this.derivationBuilder = null;
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
        
        //calculate latency as the time difference between now and the last created of the 2 input stamps
        this.latency = time - Math.max(first.creationTime, second.creationTime);
        
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143        
        while (i2 < secondLength && j < baseLength) {
            evidentialBase[j++] = secondBase[i2++];
        }
        while (i1 < firstLength && j < baseLength) {
            evidentialBase[j++] = firstBase[i1++];
        }
        
        this.derivationBuilder = new ZipperDerivationBuilder(first, second);

    }

    public Stamp(final Memory memory, final Tense tense) {
        this(memory.time(), tense, memory.newStampSerial(), memory.param.duration.get());
    }

    /** creates a stamp with default Present tense */
    public Stamp(final Memory memory) {
        this(memory, Tense.Present);
    }

    
    public boolean isEternal() {
        return occurrenceTime == ETERNAL;
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
    
    /** for creating the chain lazily */
    protected synchronized void ensureChain() {
        if (this.derivationChain != null) return;
        
        //create chain
        if (derivationBuilder==null)
            throw new RuntimeException("Null derivationChain and derivationBuilder");
        
        this.derivationChain = derivationBuilder.build();
        this.derivationBuilder = null;
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


    /**
     * Get the derivationChain, called from derivedTask in Memory
     * Provides a snapshot copy if in multi-threaded mode.
     * @return The evidentialBase of numbers
     */
    public Collection<Term> getChain() {
        ensureChain();
        
        if (Parameters.THREADS == 1)
            return derivationChain;
        else {
            //unmodifiable list copy
            return Lists.newArrayList(derivationChain);
        }
    }

    /**
     * Add element to the chain.
     *
     * @return The evidentialBase of numbers
     */
    public void chainAdd(final Term t) {
        if (t == null)
            throw new RuntimeException("Chain must contain non-null items");
        
        ensureChain();        
        
        if (derivationChain.size()+1 > Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH) {
            derivationChain.remove(0);
        }

        derivationChain.add(t);
        name = null;
    }
    public void chainRemove(final Term t) {
        if (t == null)
            throw new RuntimeException("Chain must contain non-null items");
        
        ensureChain();

        derivationChain.remove(t);
        name = null;
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
    public boolean equals(Stamp s, final boolean creationTime, final boolean ocurrenceTime, final boolean evidentialBase, final boolean derivationChain) {
        if (this == s) return true;

        if (creationTime)
            if (getCreationTime()!=s.getCreationTime()) return false;
        if (ocurrenceTime)
            if (getOccurrenceTime()!=s.getOccurrenceTime()) return false;       
        if (evidentialBase) {
            if (evidentialHash() != s.evidentialHash()) return false;
            if (!Arrays.equals(toSet(), s.toSet())) return false;
        }
        
        //two beliefs can have two different derivation chains altough they share same evidental bas
        //in this case it shouldnt return true
        if (derivationChain)
            if (!chainEquals(getChain(), s.getChain())) return false;
        
        return true;        
    }
            

    /** necessary because LinkedHashSet.equals does not compare order, only set content */
    public static boolean chainEquals(final Collection<Term> a, final Collection<Term> b) {
        if (a == b) return true;
        
        if ((a instanceof LinkedHashSet) && (b instanceof LinkedHashSet))
            return Iterators.elementsEqual(a.iterator(), b.iterator());        
        else
            return a.equals(b);
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
        Stamp s = clone();
        s.setOccurrenceTime(newOcurrenceTime);
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
        if (occurrenceTime == ETERNAL) {
            return "";
        } else {
            return appendOcurrenceTime(new StringBuilder()).toString();
        }
    }

    public String getTense(final long currentTime, final int duration) {
        
        if (occurrenceTime == Stamp.ETERNAL) {
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
            name = null;
        }
    }


    public CharSequence name() {
        if (name == null) {
            ensureChain();
            
            final int estimatedInitialSize = 10 * (baseLength + derivationChain.size());

            final StringBuilder buffer = new StringBuilder(estimatedInitialSize);
            buffer.append(Symbols.STAMP_OPENER).append(getCreationTime());
            if (occurrenceTime != ETERNAL) {
                buffer.append('|').append(occurrenceTime);
            }
            buffer.append(' ').append(Symbols.STAMP_STARTER).append(' ');
            for (int i = 0; i < baseLength; i++) {
                buffer.append(Long.toString(evidentialBase[i]));
                if (i < (baseLength - 1)) {
                    buffer.append(Symbols.STAMP_SEPARATOR);
                } else {
                    if (derivationChain.isEmpty()) {
                        buffer.append(' ').append(Symbols.STAMP_STARTER).append(' ');
                    }
                }
            }
            int i = 0;
            for (Term t : derivationChain) {
                buffer.append(t);
                if (i < (derivationChain.size() - 1)) {
                    buffer.append(Symbols.STAMP_SEPARATOR);
                }
                i++;
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

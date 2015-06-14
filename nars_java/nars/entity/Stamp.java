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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.language.Tense;
import static nars.language.Tense.Future;
import static nars.language.Tense.Past;
import static nars.language.Tense.Present;
import nars.language.Term;
import nars.storage.Memory;


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
    public final long creationTime;

    /**
     * estimated occurrence time of the event
     * TODO: make this final
     */
    public long occurrenceTime;

    /**
     * default for atemporal events
     * means "always" in Judgment/Question, but "current" in Goal/Quest     
     */
    public static final long ETERNAL = Integer.MIN_VALUE;

    /**
     * used when the occurrence time cannot be estimated, means "unknown"
     */
    public static final long UNKNOWN = Integer.MAX_VALUE;

    /**
     * derivation chain containing the used premises and conclusions which made
     * deriving the conclusion c possible
     * Uses LinkedHashSet for optimal contains/indexOf performance.
     */
    public final LinkedHashSet<Term> derivationChain;


    /** caches hashcode value; only computed on-demand since stamp's hashcode does not seem used in inference (yet) */
    private final Integer hashCode = null;
    
    /** caches evidentialBase as a set for comparisons and hashcode */
    private Set<Long> evidentialSet; 
    //TODO investigate using a BitVector
    
    /**
     * Generate a new stamp, with a new serial number, for a new Task
     *
     * @param time Creation time of the stamp
     */
    public Stamp(final long time, final Tense tense, final long serial) {        
        baseLength = 1;
        evidentialBase = new long[baseLength];
        evidentialBase[0] = serial;
        creationTime = time;
        
        if (tense == null) {
            occurrenceTime = ETERNAL;
        } else if (tense == Past) {
            occurrenceTime = time - Parameters.DURATION;
        } else if (tense == Future) {
            occurrenceTime = time + Parameters.DURATION;
        } else if (tense == Present) {
            occurrenceTime = time;
        } else {
            occurrenceTime = time;
        }
        
        derivationChain = new LinkedHashSet<>(Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH);
    }

    /**
     * Generate a new stamp identical with a given one
     *
     * @param old The stamp to be cloned
     */
    private Stamp(final Stamp old) {
        this.baseLength = old.baseLength;
        this.evidentialBase = old.evidentialBase;
        this.creationTime = old.creationTime;
        this.occurrenceTime = old.getOccurrenceTime();
        this.derivationChain = old.getChain();
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
        this.baseLength = old.baseLength;
        this.evidentialBase = old.evidentialBase;
        this.creationTime = creationTime;
        this.occurrenceTime = old.getOccurrenceTime();
        this.derivationChain = old.getChain();
    }

    public Stamp(final Stamp old, final long creationTime, final Stamp useEvidentialBase) {        
        this.evidentialBase = useEvidentialBase.evidentialBase;
        this.baseLength = useEvidentialBase.baseLength;
        this.creationTime = creationTime;
        this.occurrenceTime = old.getOccurrenceTime();
        this.derivationChain = old.getChain();
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
        
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143        
        while (i2 < secondLength && j < baseLength) {
            evidentialBase[j++] = secondBase[i2++];
        }
        while (i1 < firstLength && j < baseLength) {
            evidentialBase[j++] = firstBase[i1++];
        }
        

        //TODO create Term[] getChainArray() method
        final Term[] chain1 = first.getChain().toArray( new Term[ first.getChain().size() ]);        
        final Term[] chain2 = second.getChain().toArray( new Term[ second.getChain().size() ]);
        
        i1 = chain1.length - 1;
        i2 = chain2.length - 1;

        //take as long till the chain is full or all elements were taken out of chain1 and chain2:
        LinkedHashSet<Term> derivationChain = new LinkedHashSet<>(baseLength); 
        
        j = 0;
        while (j < Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH && (i1 >= 0 || i2 >= 0)) {
            if (j % 2 == 0) {//one time take from first, then from second, last ones are more important
                if (i1 >= 0) {
                    final Term c1i1 = chain1[i1];                         
                    if (!derivationChain.contains(c1i1)) {
                        derivationChain.add(c1i1);
                    } else {
                        j--; //was double, so we can add one more now
                    }
                    i1--;
                }
            } else {
                if (i2 >= 0) {
                    final Term c2i2 = chain2[i2];
                    if (!derivationChain.contains(c2i2)) {
                        derivationChain.add(c2i2);
                    } else {
                       j--; //was double, so we can add one more now
                    }
                    i2--;
                }
            }
            j++;
        } //ok but now the most important elements are at the beginning so let's change that:
        
        //reverse the linkedhashset
        ArrayList<Term> reverseDerivation = new ArrayList(derivationChain);
        Collections.reverse(reverseDerivation);
        
        this.derivationChain = new LinkedHashSet(reverseDerivation);

        creationTime = time;
        occurrenceTime = first.getOccurrenceTime();    // use the occurrence of task
    }

    /**
     * Try to merge two Stamps, return null if have overlap
     * <p>
     * By default, the event time of the first stamp is used in the result
     *
     * @param first The first Stamp
     * @param second The second Stamp
     * @param time The new creation time
     * @return The merged Stamp, or null
     */
    public static Stamp make(final Stamp first, final Stamp second, final long time) {

        //temporarily removed
        /*
         if (equalBases(first.getBase(), second.getBase())) {
         return null;  // do not merge identical bases
         }
         */
//        if (first.baseLength() > second.baseLength()) {
            return new Stamp(first, second, time); // keep the order for projection
//        } else {
//            return new Stamp(second, first, time);
//        }
    }

    public Stamp(final Memory memory, final Tense tense) {
        this(memory.getTime(), tense, memory.newStampSerial());
    }

    public Stamp(final Memory memory) {
        this(memory, Tense.Present);
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
    public Object clone() {
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
     *
     * @return The evidentialBase of numbers
     */
    public LinkedHashSet<Term> getChain() {
        return derivationChain;
    }

    /**
     * Add element to the chain.
     *
     * @return The evidentialBase of numbers
     */
    public void addToChain(final Term T) {
        derivationChain.add(T);
        if (derivationChain.size() > Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH) {
            Term next = derivationChain.iterator().next();
            derivationChain.remove(next); //remove 0th
        }

    }

    /**
     * Convert the evidentialBase into a set
     *
     * @return The TreeSet representation of the evidential base
     */
    private Set<Long> toSet() {
        if (evidentialSet == null) {
            evidentialSet = new HashSet<>(evidentialBase.length);
            for (final Long l : evidentialBase)
                evidentialSet.add(l);
        }
        return evidentialSet;
    }

    /**
     * Check if two stamps contains the same content
     *
     * @param that The Stamp to be compared
     * @return Whether the two have contain the same elements
     */
    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof Stamp)) {
            return false;
        }

        Stamp s = (Stamp)that;

        /*
        if (occurrenceTime!=s.occurrenceTime)
            return false;
        if (creationTime!=s.creationTime)
            return false;
        */
        
        //TODO see if there is a faster way than creating two set's
        final Set<Long> set1 = toSet();
        final Set<Long> set2 = s.toSet();

        //return (set1.containsAll(set2) && set2.containsAll(set1));
        return set1.equals(set2);
    }

    /**
     * The hash code of Stamp
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
//        return Objects.hash(toSet(), creationTime, occurrenceTime);
        //return Objects.hash(Arrays.hashCode(evidentialBase), creationTime, occurrenceTime);    
        return toSet().hashCode();
    }

    public Stamp cloneToTime(long newTime) {
        return new Stamp(this, newTime);
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
     * Get the occurrenceTime of the truth-value
     *
     * @return The occurrence time
     */
    public String getOccurrenceTimeString() {
        if (occurrenceTime == ETERNAL) {
            return "";
        } else {
            String ot = String.valueOf(occurrenceTime);
            return new StringBuilder(ot.length()+2).append('[').append(ot).append(']').toString();
        }
    }

    public String getTense(final long currentTime) {
        
        if (occurrenceTime == Stamp.ETERNAL) {
            return "";
        }

        long timeDiff = occurrenceTime - currentTime;
        
        if (timeDiff > Parameters.DURATION) {
            return Symbols.TENSE_FUTURE;
        } else if (timeDiff < -Parameters.DURATION) {
            return  Symbols.TENSE_PAST;
        } else {
            return Symbols.TENSE_PRESENT;
        }
        
    }

    public void setOccurrenceTime(final long time) {
        occurrenceTime = time;
    }


    public CharSequence name() {
        final int estimatedInitialSize = 10 * (baseLength + derivationChain.size());

        final StringBuilder buffer = new StringBuilder(estimatedInitialSize);
        buffer.append(Symbols.STAMP_OPENER).append(creationTime);
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
        return buffer;
    }

    @Override
    public String toString() {
        
        return name().toString();
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

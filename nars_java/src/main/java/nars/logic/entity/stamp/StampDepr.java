///*
// * Stamp.java
// *
// * Copyright (C) 2008  Pei Wang
// *
// * This file is part of Open-NARS.
// *
// * Open-NARS is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 2 of the License, or
// * (at your option) any later version.
// *
// * Open-NARS is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Pbulic License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
// */
//package nars.logic.entity.stamp;
//
//import com.google.common.collect.Iterators;
//import com.google.common.collect.Lists;
//import nars.core.Memory;
//import nars.core.Parameters;
//import nars.io.Symbols;
//import nars.logic.NAL;
//import nars.logic.nal7.TemporalRules;
//import nars.logic.nal7.Tense;
//
//import java.lang.ref.Reference;
//import java.util.*;
//import java.util.function.Predicate;
//
//import static nars.logic.nal7.TemporalRules.*;
//import static nars.logic.nal7.Tense.*;
//
//
//public class StampDepr<C> implements Cloneable, NAL.StampBuilder<C>, Iterable<C> {
//
//
//    /**
//     * serial numbers. not to be modified after Stamp constructor has initialized it
//     */
//    public final long[] evidentialBase;
//
//    /**
//     * evidentialBase baseLength
//     */
//    public final int baseLength;
//
//    /**
//     * creation time of the stamp
//     */
//    private long creationTime;
//
//    /**
//     * estimated occurrence time of the event
//     * TODO: make this final?
//     */
//    private long occurrenceTime;
//
//    /**
//     * default for atemporal events
//     * means "always" in Judgment/Question, but "current" in Goal/Quest
//     */
//    public static final long ETERNAL = Integer.MIN_VALUE;
//
//    /**
//     * used when the occurrence time cannot be estimated, means "unknown"
//     */
//    //public static final long UNKNOWN = Integer.MAX_VALUE;
//
//
//    /** caches evidentialBase as a set for comparisons and hashcode.
//        stores the unique Long's in-order for efficiency
//     */
//    private long[] evidentialSet = null;
//
//
//    /** caches  */
//    transient CharSequence name = null;
//
//    /* used for lazily calculating derivationChain on-demand */
//    private DerivationBuilder derivationBuilder = null;
//
//
//    final static Collection EmptyDerivationChain = Collections.EMPTY_LIST;
//
//    /**
//     * derivation chain containing the used premises and conclusions which made
//     * deriving the conclusion c possible
//     * Uses LinkedHashSet for optimal contains/indexOf performance.
//     * TODO use thread-safety for this
//     */
//    private Collection<C> derivationChain;
//
//    /** analytics metric */
//    transient public final long latency;
//
//    /** cache of hashcode of evidential base */
//    transient private int evidentialHash;
//
//
//    public boolean before(StampDepr s, int duration) {
//        if (isEternal() || s.isEternal())
//            return false;
//        return order(s.occurrenceTime, occurrenceTime, duration) == TemporalRules.ORDER_BACKWARD;
//    }
//
//    public boolean after(StampDepr s, int duration) {
//        if (isEternal() || s.isEternal())
//            return false;
//        return order(s.occurrenceTime, occurrenceTime, duration) == TemporalRules.ORDER_FORWARD;        }
//
//    public float getOriginality() {
//        return 1.0f / (evidentialBase.length + 1);
//    }
//
//    @Override
//    public Iterator<C> iterator() {
//        return derivationChain.iterator();
//    }
//
//    public interface DerivationBuilder<C> {
//        Collection<C> build();
//    }
//
//    /** array list with an internal set for fast contains() method */
//    public static class FixedArrayListWithSet<T> extends ArrayList<T> {
//
//        final Set<T> index;
//
//        public FixedArrayListWithSet(int size) {
//            super(size);
//            index = Parameters.newHashSet(Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH);
//        }
//
//        public FixedArrayListWithSet(final Collection<T> p) {
//            super(p.size());
//            index = Parameters.newHashSet(Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH);
//            for (final T t : p)
//                add(t);
//        }
//
//        @Override
//        public boolean add(T t) {
//            if (index.add(t)) {
//                return super.add(t);
//            }
//            return false;
//        }
//
//        @Override
//        public boolean contains(Object o) {
//            return index.contains(o);
//        }
//
//        @Override
//        public boolean remove(Object o) {
//            if (index.remove(o)) {
//                return super.remove(o);
//            }
//            return false;
//        }
//
//        @Override public T remove(int index) { throw new RuntimeException("not supported");        }
//
//        @Override public boolean removeAll(Collection<?> c) { throw new RuntimeException("not supported");        }
//
//        @Override public boolean removeIf(Predicate<? super T> filter) { throw new RuntimeException("not supported");        }
//
//        @Override public void add(int index, T element) { throw new RuntimeException("not supported");        }
//
//        @Override public boolean addAll(Collection<? extends T> c) { throw new RuntimeException("not supported");        }
//
//        @Override public boolean addAll(int index, Collection<? extends T> c) { throw new RuntimeException("not supported");        }
//    }
//
//    /** creates a Derivation Chain by collating / zipping 2 Stamps Derivation Chains */
//    public static class ZipperDerivationBuilder<C> implements DerivationBuilder<C> {
//        private final Reference<StampDepr<C>> first;
//        private final Reference<StampDepr<C>> second;
//
//        public ZipperDerivationBuilder(StampDepr<C> first, StampDepr<C> second) {
//            this.first = Parameters.reference(first);
//            this.second = Parameters.reference(second);
//        }
//
//        @Override public Collection<C> build()  {
//            StampDepr ff = first.get();
//            StampDepr ss = second.get();
//
//            //check if the parent stamps still exist, because they may have been garbage collected
//            if ((ff == null) && (ss == null)) {
//                return new LinkedHashSet();
//            }
//            else {
//                //TODO decide if it can use the parent chains directly?
//                if (ff == null) {
//                    //ss!=null
//                    return new LinkedHashSet(ss.getChain());
//                }
//                else if (ss == null) {
//                    //ff!=null
//                    return new LinkedHashSet(ff.getChain());
//                }
//            }
//
//            final Collection<C> chain1 = ff.getChain();
//            final Collection<C> chain2 = ss.getChain();
//
//            final Iterator<C> iter1 = chain1.iterator();
//            int i1 = chain1.size() - 1;
//
//            final Iterator<C> iter2 = chain2.iterator();
//            int i2 = chain2.size() - 1;
//
//            //TODO verify this is pre-sized large enough
//            //Set<Term> added = Parameters.newHashSet(Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH);
//
//            //set here is for fast contains() checking
//            FixedArrayListWithSet<C> sequence = new FixedArrayListWithSet(Parameters.MAXIMUM_EVIDENTAL_BASE_LENGTH);
//
//            //take as long till the chain is full or all elements were taken out of chain1 and chain2:
//            int j = 0;
//            while (j < Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH && (i1 >= 0 || i2 >= 0)) {
//                if (j % 2 == 0) {//one time take from first, then from second, last ones are more important
//                    if (i1 >= 0) {
//                        final C c1i1 = iter1.next();
//                        if (!sequence.add(c1i1)) {
//                            j--; //was double, so we can add one more now
//                        }
//                        i1--;
//                    }
//                } else {
//                    if (i2 >= 0) {
//                        final C c2i2 = iter2.next();
//                        if (!sequence.add(c2i2)) {
//                            j--; //was double, so we can add one more now
//                        }
//                        i2--;
//                    }
//                }
//                j++;
//            }
//
//            /*
//            if (Parameters.DEBUG) {
//                Terms.verifyNonNull(sequence);
//            }
//            */
//
//            Collections.reverse(sequence);
//
//            return sequence;
//        }
//    }
//
//    /** lazily inherit the derivation from a parent, causing it to cache the derivation also (in case other children get it */
//    public static class InheritDerivationBuilder<C> implements DerivationBuilder<C> {
//        private final Reference<StampDepr<C>> parent;
//
//        public InheritDerivationBuilder(StampDepr<C> parent) {
//            this.parent = Parameters.reference(parent);
//        }
//
//        @Override public Collection<C> build() {
//            if (parent.get() == null) {
//                //parent doesnt exist anymore (garbage collected)
//                return new LinkedHashSet();
//            }
//
//            Collection<C> p = parent.get().getChain();
//            return new FixedArrayListWithSet(p);
//        }
//
//    }
//
//
//
//    private static long nextSerial = 0;
//    public static synchronized long newSerial() {
//        return nextSerial++;
//    }
//
//
//    /**
//     * Generate a new stamp, with a new serial number, for a new Task
//     *
//     * @param creationTime Creation time of the stamp
//     */
//    @Deprecated public StampDepr(@Deprecated final long serial, final long creationTime, final Tense tense, final int duration) {
//        super();
//        this.baseLength = 1;
//        this.evidentialBase = new long[baseLength];
//        this.evidentialBase[0] = newSerial();
//        this.latency = 0;
//        this.derivationBuilder = null;
//        this.derivationChain = EmptyDerivationChain;
//
//        this.creationTime = creationTime;
//
//        if (tense == null) {
//            occurrenceTime = ETERNAL;
//        } else if (tense == Past) {
//            occurrenceTime = creationTime - duration;
//        } else if (tense == Future) {
//            occurrenceTime = creationTime + duration;
//        } else if (tense == Present) {
//            occurrenceTime = creationTime;
//        }
//
//    }
//
//    /**
//     * Generate a new stamp identical with a given one
//     *
//     * @param old The stamp to be cloned
//     */
//    private StampDepr(final StampDepr old) {
//        this(old, old.creationTime);
//    }
//
//    /**
//     * Generate a new stamp from an existing one, with the same evidentialBase
//     * but different creation time
//     * <p>
//     * For single-premise rules
//     *
//     * @param old The stamp of the single premise
//     * @param creationTime The current time
//     */
//    public StampDepr(final StampDepr old, final long creationTime) {
//        this(old, creationTime, old);
//    }
//
//    public StampDepr(final StampDepr old, final long creationTime, final long occurenceTime) {
//        this(old, creationTime, occurenceTime, old);
//    }
//
//    public StampDepr(final StampDepr old, final long creationTime, final StampDepr useEvidentialBase) {
//        this(old, creationTime, old.getOccurrenceTime(), useEvidentialBase);
//    }
//
//    public StampDepr(final StampDepr old, final long creationTime, final long occurenceTime, final StampDepr useEvidentialBase) {
//
//        this.evidentialBase = useEvidentialBase.evidentialBase;
//        this.baseLength = useEvidentialBase.baseLength;
//        this.creationTime = creationTime;
//
//        this.occurrenceTime = occurenceTime;
//        this.derivationChain = null;
//        this.latency = this.creationTime - old.latency;
//
//        this.derivationBuilder = new InheritDerivationBuilder(old);
//
//
//    }
//
//    public StampDepr(final StampDepr first, final StampDepr second, final long creationTime) {
//        this(first, second, creationTime,
//                first.getOccurrenceTime() /* use the creation time of the first task */ );
//    }
//
//    /**
//     * Generate a new stamp for derived sentence by merging the two from parents
//     * the first one is no shorter than the second
//     *
//     * @param first The first Stamp
//     * @param second The second Stamp
//     */
//    public StampDepr(final StampDepr first, final StampDepr second, final long creationTime, final long occurenceTime) {
//        //TODO use iterators instead of repeated first and second .get's?
//
//        int i2, j;
//        int i1 = i2 = j = 0;
//        this.baseLength = Math.min(first.baseLength + second.baseLength, Parameters.MAXIMUM_EVIDENTAL_BASE_LENGTH);
//        this.evidentialBase = new long[baseLength];
//
//        final long[] firstBase = first.evidentialBase;
//        final long[] secondBase = second.evidentialBase;
//        int firstLength = firstBase.length;
//        int secondLength = secondBase.length;
//
//        this.creationTime = creationTime;
//        this.occurrenceTime = occurenceTime;
//
//        //calculate latency as the time difference between now and the last created of the 2 input stamps
//        this.latency = creationTime - Math.max(first.creationTime, second.creationTime);
//
//        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143
//        while (i2 < secondLength && j < baseLength) {
//            evidentialBase[j++] = secondBase[i2++];
//        }
//        while (i1 < firstLength && j < baseLength) {
//            evidentialBase[j++] = firstBase[i1++];
//        }
//
//        this.derivationBuilder = new ZipperDerivationBuilder(first, second);
//
//    }
//
//    public StampDepr(final Memory memory, final Tense tense, long creationTime) {
//        this(memory.newStampSerial(), creationTime, tense, memory.param.duration.get());
//    }
//
//    /** create stamp at current memory time */
//    public StampDepr(final Memory memory, final Tense tense) {
//        this(memory, tense, memory.time());
//    }
//
//    /** creates a stamp with default Present tense */
//    @Deprecated public StampDepr(final Memory memory) {
//        this(memory, Tense.Present);
//    }
//
//    public StampDepr(final Memory memory, long creationTime, long occurenceTime) {
//        this(memory);
//        this.creationTime = creationTime;
//        this.occurrenceTime = occurenceTime;
//    }
//
//    public StampDepr(final Memory memory, long occurenceTime) {
//        this(memory);
//        this.occurrenceTime = occurenceTime;
//    }
//
//
//    public boolean isEternal() {
//        return occurrenceTime == ETERNAL;
//    }
//
//    /** sets the creationTime to a non-value so that it will be set at a later point, ex: after traversing the input queue */
//    public void setNotYetPerceived() {
//        creationTime = -1;
//    }
//
//    /** sets the creation time; used to set input tasks with the actual time they enter Memory */
//    public void setCreationTime(long creationTime) {
//        long originalCreationTime = this.creationTime;
//        this.creationTime = creationTime;
//
//        //shift occurence time relative to the new creation time
//        if (occurrenceTime != StampDepr.ETERNAL) {
//            occurrenceTime = occurrenceTime + (creationTime - originalCreationTime);
//        }
//    }
//
//    protected boolean chainIsNullOrEmpty() {
//        return derivationChain == null || derivationChain.isEmpty();
//    }
//
//    /** for creating the chain lazily */
//    protected /* synchronized */ void ensureChain() {
//
//        if (derivationChain == EmptyDerivationChain) {
//            derivationChain = new LinkedHashSet();
//            return;
//        }
//
//        if (this.derivationChain != null) return;
//
//        //create chain
//        if (derivationBuilder==null)
//            throw new RuntimeException("Null derivationChain and derivationBuilder");
//
//        this.derivationChain = derivationBuilder.build();
//        this.derivationBuilder = null;
//    }
//
//    @Override
//    public StampDepr build() {
//        return this;
//    }
//
//    /*
//     private static boolean equalBases(long[] base1, long[] base2) {
//     if (base1.baseLength != base2.baseLength) {
//     return false;
//     }
//     for (long n1 : base1) {
//     boolean found = false;
//     for (long n2 : base2) {
//     if (n1 == n2) {
//     found = true;
//     }
//     }
//     if (!found) {
//     return false;
//     }
//     }
//     return true;
//     }
//     */
//    /**
//     * Clone a stamp
//     *
//     * @return The cloned stamp
//     */
//    @Override
//    public StampDepr clone() {
//        return new StampDepr(this);
//    }
//
//
//    /**
//     * Get a number from the evidentialBase by index, called in this class only
//     *
//     * @param i The index
//     * @return The number at the index
//     */
//    long get(final int i) {
//        return evidentialBase[i];
//    }
//
//
//    /**
//     * Get the derivationChain, called from derivedTask in Memory
//     * Provides a snapshot copy if in multi-threaded mode.
//     * @return The evidentialBase of numbers
//     */
//    public Collection<C> getChain() {
//        ensureChain();
//
//        if (Parameters.THREADS == 1)
//            return derivationChain;
//        else {
//            //modifiable list copy
//            return Lists.newArrayList(derivationChain);
//        }
//    }
//
//    /**
//     * Add element to the chain.
//     *
//     * @return The evidentialBase of numbers
//     */
//    public void chainAdd(final C t) {
//        if (t == null)
//            throw new RuntimeException("Chain must contain non-null items");
//
//        ensureChain();
//
//        if (derivationChain.add(t)) {
//
//            if (derivationChain.size() > Parameters.MAXIMUM_DERIVATION_CHAIN_LENGTH) {
//                //remove first element
//                C first = derivationChain.iterator().next();
//                derivationChain.remove(first);
//            }
//
//            name = null;
//        }
//    }
//    public void chainRemove(final C t) {
//        if (t == null)
//            throw new RuntimeException("Chain must contain non-null items");
//
//        if (chainIsNullOrEmpty())
//            return;
//
//        ensureChain();
//
//        if (derivationChain.remove(t)) {
//            name = null;
//        }
//    }
//
//    public void chainReplace(final C remove, final C add) {
//        chainRemove(remove);
//        chainAdd(add);
//    }
//
//    public static long[] toSetArray(final long[] x) {
//        long[] set = x.clone();
//
//        if (x.length < 2)
//            return set;
//
//        //1. copy evidentialBse
//        //2. sorted
//        //3. count duplicates
//        //4. create new array
//
//        Arrays.sort(set);
//        long lastValue = -1;
//        int j = 0; //# of unique items
//        for (int i = 0; i < set.length; i++) {
//            long v = set[i];
//            if (lastValue != v)
//                j++;
//            lastValue = v;
//        }
//        lastValue = -1;
//        long[] sorted = new long[j];
//        j = 0;
//        for (int i = 0; i < set.length; i++) {
//            long v = set[i];
//            if (lastValue != v)
//                sorted[j++] = v;
//            lastValue = v;
//        }
//        return sorted;
//    }
//
//    /**
//     * Convert the evidentialBase into a set
//     *
//     * @return The TreeSet representation of the evidential base
//     */
//    public long[] toSet() {
//        if (evidentialSet == null) {
//            evidentialSet = toSetArray(evidentialBase);
//            evidentialHash = Arrays.hashCode(evidentialSet);
//        }
//
//        return evidentialSet;
//    }
//
//
//    @Override public boolean equals(final Object that) {
//        throw new RuntimeException("Use other equals() method");
//    }
//
//    /**
//     * Check if two stamps contains the same types of content
//     *
//     * @param s The Stamp to be compared
//     * @return Whether the two have contain the same evidential base
//     */
//    public boolean equals(StampDepr<C> s, final boolean creationTime, final boolean ocurrenceTime, final boolean evidentialBase, final boolean derivationChain) {
//        if (this == s) return true;
//
//        if (creationTime)
//            if (getCreationTime()!=s.getCreationTime()) return false;
//        if (ocurrenceTime)
//            if (getOccurrenceTime()!=s.getOccurrenceTime()) return false;
//        if (evidentialBase) {
//            if (evidentialHash() != s.evidentialHash()) return false;
//
//            //iterate in reverse; the ending of the evidence chain is more likely to be different
//            final long[] a = toSet();
//            final long[] b = s.toSet();
//            if (a.length != b.length) return false;
//            for (int i = a.length-1; i >=0; i--)
//                if (a[i]!=b[i]) return false;
//        }
//
//        //two beliefs can have two different derivation chains altough they share same evidental bas
//        //in this case it shouldnt return true
//        if (derivationChain)
//            if (!chainEquals(getChain(), s.getChain())) return false;
//
//        return true;
//    }
//
//
//    /** necessary because LinkedHashSet.equals does not compare order, only set content */
//    public static <C> boolean chainEquals(final Collection<C> a, final Collection<C> b) {
//        if (a == b) return true;
//
//        //if ((a instanceof LinkedHashSet) && (b instanceof LinkedHashSet))
//            return Iterators.elementsEqual(a.iterator(), b.iterator());
//        /*else
//            return a.equals(b);*/
//    }
//
//    /**
//     * The hash code of Stamp
//     *
//     * @return The hash code
//     */
//    public final int evidentialHash() {
//        if (evidentialSet==null)
//            toSet();
//        return evidentialHash;
//    }
//
//    public StampDepr cloneWithNewCreationTime(long newCreationTime) {
//        return new StampDepr(this, newCreationTime);
//    }
//    public StampDepr cloneWithNewOccurrenceTime(final long newOcurrenceTime) {
//        StampDepr s = new StampDepr(this, getCreationTime(), newOcurrenceTime);
//        return s;
//    }
//    public StampDepr cloneEternal() {
//        return cloneWithNewOccurrenceTime(ETERNAL);
//    }
//
//    /**
//     * Get the occurrenceTime of the truth-value
//     *
//     * @return The occurrence time
//     */
//    public long getOccurrenceTime() {
//        return occurrenceTime;
//    }
//
//    public void setEternal() {
//        occurrenceTime=ETERNAL;
//    }
//
//
//    public StringBuilder appendOcurrenceTime(final StringBuilder sb) {
//        if (occurrenceTime != ETERNAL) {
//            int estTimeLength = 8; /* # digits */
//            sb.ensureCapacity(estTimeLength + 1 + 1);
//            sb.append('[').append(occurrenceTime).append(']');
//        }
//        return sb;
//    }
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
//
//    public String getTense(final long currentTime, final int duration) {
//
//        if (isEternal()) {
//            return "";
//        }
//
//        switch (TemporalRules.order(currentTime, occurrenceTime, duration)) {
//            case ORDER_FORWARD:
//                return Symbols.TENSE_FUTURE;
//            case ORDER_BACKWARD:
//                return Symbols.TENSE_PAST;
//            default:
//                return Symbols.TENSE_PRESENT;
//        }
//    }
//
//
//
//    public CharSequence name() {
//        if (name == null) {
//            ensureChain();
//
//            final int estimatedInitialSize = 10 * (baseLength + derivationChain.size());
//
//            final StringBuilder buffer = new StringBuilder(estimatedInitialSize);
//            buffer.append(Symbols.STAMP_OPENER).append(getCreationTime());
//            if (!isEternal()) {
//                buffer.append('|').append(occurrenceTime);
//            }
//            buffer.append(' ').append(Symbols.STAMP_STARTER).append(' ');
//            for (int i = 0; i < baseLength; i++) {
//                buffer.append(Long.toString(evidentialBase[i]));
//                if (i < (baseLength - 1)) {
//                    buffer.append(Symbols.STAMP_SEPARATOR);
//                } else {
//                    if (derivationChain.isEmpty()) {
//                        buffer.append(' ').append(Symbols.STAMP_STARTER).append(' ');
//                    }
//                }
//            }
//            int i = 0;
//            for (C t : derivationChain) {
//                buffer.append(t);
//                if (i < (derivationChain.size() - 1)) {
//                    buffer.append(Symbols.STAMP_SEPARATOR);
//                }
//                i++;
//            }
//            buffer.append(Symbols.STAMP_CLOSER).append(' ');
//
//            //this is for estimating an initial size of the stringbuffer
//            //System.out.println(baseLength + " " + derivationChain.size() + " " + buffer.baseLength());
//            name = buffer;
//        }
//        return name;
//    }
//
//    @Override
//    public String toString() {
//        return name().toString();
//    }
//
//
//    /**
//     * @return the creationTime
//     */
//    public long getCreationTime() {
//        return creationTime;
//    }
//
//
//
//
//
//
//    //String toStringCache = null; //holds pre-allocated symbol for toString()
//    /**
//     * Get a String form of the Stamp for display Format: {creationTime [:
//     * eventTime] : evidentialBase}
//     *
//     * @return The Stamp as a String
//     */
//    /*
//     final static String stampOpenerSpace = " " + Symbols.STAMP_OPENER;
//     final static String spaceStampStarterSpace = " " + Symbols.STAMP_STARTER + " ";
//     final static String stampCloserSpace = Symbols.STAMP_CLOSER + " ";
//
//     @Override
//     public String toString() {
//     if (toStringCache == null) {
//     int numBases = evidentialBase.size();
//     final StringBuilder b = new StringBuilder(8+numBases*5 // TODO properly estimate this //);
//
//     b.append(stampOpenerSpace).append(creationTime)
//     .append(spaceStampStarterSpace);
//
//     int i = 0;
//     for (long eb : evidentialBase) {
//     b.append(Long.toString(eb));
//     if (i++ < (numBases - 1)) {
//     b.append(Symbols.STAMP_SEPARATOR);
//     } else {
//     b.append(stampCloserSpace);
//     }
//     }
//     toStringCache = b.toString();
//     }
//     return toStringCache;
//     }
//     */
//
//}

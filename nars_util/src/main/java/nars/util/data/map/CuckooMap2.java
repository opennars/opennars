package nars.util.data.map;

/***************************************************************************
   WARNING - SEEMS UNSTABLE

 * File: CuckooMap2.java
 * Author: Keith Schwarz (htiek@cs.stanford.edu)
 * http://www.keithschwarz.com/interesting/code/cuckoo-hashmap/
 * An implementation of a hash map backed by a cuckoo hash table.  Cuckoo hash
 * tables, first described in "Cuckoo Hashing" by Pugh and Rodler, is a hash
 * system with worst-case constant-time lookup and deletion, and amortized
 * expected O(1) insertion.
 *
 * Internally, cuckoo hashing works by maintaining two arrays of some size,
 * along with two universal hash functions f and g.  When an element x is
 * inserted, the value f(x) is computed and the entry is stored in that spot
 * in the first array.  If that spot was initially empty, we are done.
 * Otherwise, the element that was already there (call it y) is "kicked out."
 * We then compute g(y) and store element y at position g(y) in the second
 * array, which may in turn kick out another element, which will be stored in
 * the first array.  This process repeats until either a loop is detected (in
 * which case we pick a new hash function and rehash), or all elements finally
 * come to rest.
 *
 * The original paper by Pugh and Rodler proves a strong bound - for any
 * epsilon greater than zero, if the load factor of a Cuckoo hash table is at
 * most (1/2 - epsilon)n, then both the expected runtime and variance of
 * the expected runtime for an insertion is amortized O(1).  This means that
 * we will always want to keep the load factor at just below 50%, say, at 40%.
 *
 * The main challenge of implementing a cuckoo hash table in Java is that the
 * hash code provided for each object is not drawn from a universal hash
 * function.  To ameliorate this, internally we will choose a universal hash
 * function to apply to the hash code of each element.  This is only a good
 * hash if the hash codes for objects are distributed somewhat uniformly, but
 * we assume that this is the case.  If it isn't true - and in particular, if
 * more than two objects of the type hash to the same value - then all bets
 * are off and the cuckoo hash table will entirely fail.  Internally, the
 * class provides a default hash function (described below), but complex class
 * implementations should provide their own implementation.
 *
 * Our family of universal hash functions is based on the universal hash
 * functions described by Mikkel Thorup in "String Hashing for Linear Probing."
 * We begin by breaking the input number into two values, a lower 16-bit value
 * and an upper 16-bit value (denoted HIGH and LOW), then picking two random
 * 32-bit values A and B (which will remain constant across any one hash
 * function from this family).  We then compute
 *
 *           HashCode = ((HIGH + A) * (LOW * B)) / (2^(32 - k))
 *
 * Where 2^k is the number of buckets we're hashing into.
 */

import java.util.*;

@SuppressWarnings("unchecked") // For array casts
public final class CuckooMap2<K, V> extends AbstractMap<K, V> {
    /* The initial size of each array. */
    private static final int kStartSize = 4;

    /* The maximum load factor, which we arbitrarily decree to be 40%. */
    private static final float kMaxLoadFactor = 0.40f;

    /* The two hash arrays. */
    private Entry<K, V>[][] mArrays = new Entry[2][kStartSize];

    /* The two hash functions. */
    private final HashFunction<? super K>[] mHashFns = new HashFunction[2];

    /* The family of universal hash functions. */
    private final UniversalHashFunction<? super K> mUniversalHashFunction;

    /* The number of entries that are filled in. */
    private int mSize = 0;

    /****************************************************************************
     * File: HashFunction.java
     * Author: Keith Schwarz (htiek@cs.stanford.edu)
     *
     * An object representing a hash function capable of hashing objects of some
     * type.  This allows the notion of a hash function to be kept separate from
     * the object itself and is necessary to provide families of hash functions.
     */
    public interface HashFunction<T> {
        /**
         * Given an object, returns the hash code of that object.
         *
         * @param obj The object whose hash code should be computed.
         * @return The object's hash code.
         */
        int hash(T obj);
    }

    /***************************************************************************
     * File: UniversalHashFunction.java
     * Author: Keith Schwarz (htiek@cs.stanford.edu)
     *
     * An object representing a family of universal hash functions.  The object
     * can then hand back random instances of the universal hash function on an
     * as-needed basis.
     */
    public interface UniversalHashFunction<T> {
        /**
         * Given as input the number of buckets, produces a random hash function
         * that hashes objects of type T into those buckets with the guarantee
         * that
         *<pre>
         *             Pr[h(x) == h(y)] <= |T| / numBuckets, forall x != y
         *</pre>
         * For all hash functions handed back.
         *
         * @param buckets The number of buckets into which elements should be
         *                partitioned.
         * @return A random hash function whose distribution satisfies the above
         *         property.
         */
        HashFunction<T> randomHashFunction(int buckets);
    }

    /**
     * Utility class representing a default hash function, as described above.
     */
    private static final class DefaultHashFunction<T> implements HashFunction<T> {
        private final int mA, mB;  // Coefficients for this hash function
        private final int mLgSize; // Log of the size of the hash tables.

        /**
         * Constructs a new hash function using the specified coefficients and
         * the log of the number of buckets in the hash table.
         *
         * @param a The first coefficient
         * @param b The second coefficient
         * @param lgSize The base-two log of the number of buckets.
         */
        public DefaultHashFunction(int a, int b, int lgSize) {
            mA = a;
            mB = b;
            mLgSize = lgSize;
        }

        /**
         * Given an object, evaluates its hash code.
         *
         * @param obj The object whose hash code should be evaluated.
         * @return Its hash code.
         */
        @Override
        public int hash(T obj) {
            /* If the object is null, just evaluate to zero. */
            if (obj == null) return 0;

            /* Otherwise, split its hash code into upper and lower bits. */
            int objHash = obj.hashCode();
            int upper = objHash >>> 16;
            int lower = objHash & (0xFFFF);

            /* Return the pairwise product of those bits, shifted down so that
             * only lgSize bits remain in the output.
             */
            return (upper * mA + lower * mB) >>> (32 - mLgSize);
        }
    }

    /**
     * Utility class representing a default generator of universal hash
     * functions.  This class is hardcoded to assume that the number of
     * buckets is a perfect power of two, though in general the
     * UniversalHashFunction contract says nothing of this.
     */
    private static final class DefaultUniversalHashFunction<T> implements UniversalHashFunction<T> {
        /* A random-number generator for producing the hash functions. */
        private final Random mRandom = new Random();

        /**
         * Produces a HashFunction from the given bucket size.
         *
         * @param numBuckets The number of buckets to use.
         */
        @Override
        public HashFunction<T> randomHashFunction(int numBuckets) {
            /* Compute the base-2 logarithm of the number of buckets.  This
             * value is the number of bits required to hold the number of
             * buckets, but we want one minus this value because we want to
             * know the number of bits necessary to index any of these buckets.
             * This is given by the log minus one, and so we start a counter
             * at -1 and keep bumping it as many times as we can divide by two.
             */
            int lgBuckets = -1;
            for (; numBuckets > 0; numBuckets >>>= 1)
                ++lgBuckets;

            /* Return a default hash function initialized with random values
             * and the log of the number of buckets.
             */
            return new DefaultHashFunction<>(mRandom.nextInt(), mRandom.nextInt(),
                    lgBuckets);
        }
    }

    /**
     * Creates a new, empty CuckooMap2 using a default family of universal
     * hash functions.  Note that this is in general NOT SAFE unless you can
     * positively guarantee that no two distinct objects have distinct hash
     * codes.  This will be true for objects that don't explicitly override
     * hashCode(), and certain numeric wrappers like Integer, but not for more
     * complex types like String.
     */
    public CuckooMap2() {
        /* Set us up with a default universal hash function. */
        this(new DefaultUniversalHashFunction());
    }

    /**
     * Creates a new, empty CuckooMap2 using the specified family of
     * universal hash functions.
     *
     * @param fn The family of universal hash functions to use.
     */
    public CuckooMap2(UniversalHashFunction<? super K> fn) {
        /* Confirm that the family of hash functions is not null; we can't use
         * it if it is.
         */
        if (fn == null)
            throw new NullPointerException("Universal hash function must be non-null.");

        /* Store the family for later use. */
        mUniversalHashFunction = fn;

        /* Set up the hash functions. */
        generateHashFunctions();
    }

    /**
     * Adds a new key/value pair to the hash map.  If the key already existed,
     * its old value is displaced and the new value is written in its stead.
     *
     * @param key The key to insert.
     * @param value Its associated value
     * @return The value that was originally associated with this key, or null
     *         if no value was associated with it.
     */
    @Override
    public V put(K key, V value) {
        /* Check whether this value already exists.  If so, just displace its
         * old value and hand it back.
         */
        for (int i = 0; i < 2; ++i) {
            /* Compute the hash code, then look up the entry there. */
            int hash = mHashFns[i].hash(key);
            Entry<K, V> entry = mArrays[i][hash];

            /* If the entry matches, we found what we're looking for. */
            if (entry != null && isEqual(entry.getKey(), key)) {
                /* Cache the value so we can return it, then clobber it
                 * with the new value.
                 */
                V result = entry.getValue();
                entry.setValue(value);
                return result;
            }
        }

        /* The value is not in the hash table, so we're going to have to
         * insert it.
         *
         * If we need to grow the hash table, do so here.  The maximum load
         * is given by the total number of array elements scaled by the
         * maximum load factor.
         */
        if (size() >= kMaxLoadFactor * mArrays[0].length * 2)
            grow();

        /* Otherwise, continously try to insert the value into the hash table,
         * rehashing whenever that fails.
         */
        Entry<K, V> toInsert = new SimpleEntry<>(key, value);
        while (true) {
            /* Add the entry to the table, then see what element was
             * ultimately displaced.
             */
            toInsert = tryInsertEntry(toInsert);

            /* If nothing ended up displaced, we're done. */
            if (toInsert == null) break;

            /* Otherwise, rehash and try again. */
            rehash();
        }

        /* We just added an entry, so increase our recorded size. */
        ++mSize;

        /* Nothing was associated with this value. */
        return null;
    }

    /**
     * Given an Entry, tries to insert that entry into the hash table, taking
     * several iterations if necessary.  The return value is the last entry
     * that was displaced, which will be null if the element was inserted
     * correctly and will be some arbitrary other entry otherwise.
     *
     * @param toInsert The entry to insert into the hash table.
     * @return The last displaced entry, or null if all collisions were
     *         resolved.
     */
    private Entry<K, V> tryInsertEntry(Entry<K, V> toInsert) {
        /* Starting at the initial position, bounce back and forth between the
         * hash tables trying to insert the value.  During this process, keep
         * a counter that keeps growing until it reaches the a value above the
         * size.  If this is ever hit, we give up and return the element that
         * was last bounced.
         *
         * We also use numTries as an odd/even counter so we know which hash
         * table we're inserting into.
         */
        for (int numTries = 0; numTries < size() + 1; ++numTries) {
            /* Compute the hash code and see what's at that position. */
            int hash = mHashFns[numTries % 2].hash(toInsert.getKey());
            Entry<K, V> entry = mArrays[numTries % 2][hash];

            /* If the entry is null, the slot is open and we just write the
             * element there.
             */
            if (entry == null) {
                mArrays[numTries % 2][hash] = toInsert;

                /* The last displaced entry was indeed null. */
                return null;
            }

            /* Otherwise displace this element with the element to insert,
             * then try inserting the bumped element into the other array.
             */
            mArrays[numTries % 2][hash] = toInsert;
            toInsert = entry;
        }

        return toInsert;
    }

    /**
     * Utility function which, given two keys, returns whether they are equal,
     * including the special case where both keys are null.
     *
     * @param one The first object to compare (or null).
     * @param two The second object to compare (or null).
     * @return Whether the two objects are equal.
     */
    private static <T> boolean isEqual(T one, T two) {
        /* If both are null, they're equal. */
        if (one == null && two == null) return true;

        /* Otherwise, if either are null, they're not equal. */
        if (one == null || two == null) return false;

        /* Otherwise, they're equal if they say that they are. */
        return one.equals(two);
    }

    /**
     * Utility function to choose new hash functions for the hash table.
     */
    private void generateHashFunctions() {
        /* Create two new hash functions using the log size of the buckets and
         * two random integers.
         */
        for (int i = 0; i < 2; ++i)
            mHashFns[i] = mUniversalHashFunction.randomHashFunction(mArrays[0].length);
    }

    /**
     * Utility function to rehash all of the elements in the hash table.  This
     * does NOT grow the size of the hash tables; rather, it recomputes the
     * hash values for all of the entries according to some new hash function.
     */
    private void rehash() {
        /* Begin by creating an array of elements suitable for holding all the
         * elements in the hash table.  We need to do this here, since we're
         * going to be mucking around with the contents of the arrays and
         * otherwise have no way of tracking what values got inserted.
         */
        EntrySet var = entrySet();
        Entry<K, V>[] values = var.toArray(new Entry[var.size()]);

        /* Continuously spin, trying to add more and more values to the table.
         * If at any point we can't add something, pick new hash functions and
         * start over.
         */
        reinsert: while (true) {
            /* Clear all the arrays. */
            for (int i = 0; i < 2; ++i)
                Arrays.fill(mArrays[i], null);

            /* Pick two new hash functions. */
            generateHashFunctions();

            /* Try adding everything. */
            for (Entry<K, V> entry: values) {
                /* If we can't insert the value successfully, rehash again. */
                if (tryInsertEntry(entry) != null)
                    continue reinsert;
            }

            /* If we made it here, we successfully inserted everything and are
             * done.
             */
            break;
        }
    }

    /**
     * Grows the hash table, doubling the size of each array and picking a new
     * hash function.
     */
    private void grow() {
        /* Hold on to the old arrays; we'll need this so that later on we can
         * scan over and add everything to the new array.
         */
        Entry<K, V>[][] oldArrays = mArrays;

        /* Reallocate the arrays twice as large as they are now. */
        mArrays = new Entry[2][mArrays[0].length * 2];

        /* Now, we need to reinsert everything after picking a new hash
         * function.  To do this, we'll cheat a bit.  First, we'll write back
         * all the old elements to the array in an arbitrary order.  Next,
         * we'll invoke rehash() to pick a new, good hash function.  This is a
         * bit hacky, but it works perfectly well.  Moreover, since we know
         * that the load factor is no greater than 50%, we're guaranteed that
         * everything fits into the first array.
         */
        int writePoint = 0;
        for (int i = 0; i < 2; ++i)
            for (Entry<K, V> entry: oldArrays[i])
                if (entry != null) // Only write valid entries.
                    mArrays[0][writePoint++] = entry;

        /* Rehash the array to put everything back in the right place. */
        rehash();
    }

    /**
     * Returns the number of elements in the hash map.
     *
     * @return The number of elements in the hash map.
     */
    @Override
    public int size() {
        return mSize;
    }

    /**
     * Returns whether the hash map is empty.
     *
     * @return Whether the hash map is empty.
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Removes all entries from the hash map.
     */
    @Override
    public void clear() {
        /* We could just remove everything, but it's easier to just blast the
         * entire data structure by just resetting to default values.
         */
        mArrays = new Entry[2][kStartSize];
        mSize = 0;
        generateHashFunctions();
    }

    /**
     * Returns whether the specified key is contained in the hash map.  If the
     * key is not of a type derived from K, throws a ClassCastException.
     *
     * @param key The key to look up.
     * @return Whether that key is contained in the hash map.
     * @throws ClassCastException If the key is of the wrong type.
     */
    @Override
    public boolean containsKey(Object key) {
        /* Check both locations where the object could be. */
        for (int i = 0; i < 2; ++i) {
            int hash = mHashFns[i].hash((K)key);
            if (mArrays[i][hash] != null && isEqual(mArrays[i][hash].getKey(), key))
                return true;
        }

        /* Didn't find it. */
        return false;
    }

    /**
     * Returns the value associated with the given key.  If the key is not a
     * key in the map, returns null as a sentinel.  If the key is not of a
     * type derived from K, throws a ClassCastException.
     *
     * @param key The key to look up.
     * @return The value associated with the key, or null if there is none.
     * @throws ClassCastException If the key is of the wrong type.
     */
    @Override
    public V get(Object key) {
        /* Check both locations where the object could be. */
        for (int i = 0; i < 2; ++i) {
            int hash = mHashFns[i].hash((K)key);
            if (mArrays[i][hash] != null && isEqual(mArrays[i][hash].getKey(), key))
                return mArrays[i][hash].getValue();
        }

        /* Didn't find it. */
        return null;
    }

    /**
     * Deletes the specified key from the map, if it exists.
     *
     * @param key The key to remove.
     * @return The value associated with the key, or null if there is none.
     * @throws ClassCastException If the key is of the wrong type.
     */
    @Override
    public V remove(Object key) {
        /* Check both locations where the object could be. */
        for (int i = 0; i < 2; ++i) {
            int hash = mHashFns[i].hash((K)key);
            if (mArrays[i][hash] != null && isEqual(mArrays[i][hash].getKey(), key)) {
                /* Cache the value to return. */
                V result = mArrays[i][hash].getValue();

                /* Wipe this element from the array. */
                mArrays[i][hash] = null;

                /* Drop the number of elements, since we just removed
                 * something.
                 */
                --mSize;
                return result;
            }
        }

        /* Didn't find it. */
        return null;
    }

    /**
     * A utility class representing the set of entries contained in this
     * hash map.
     */
    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        /**
         * Returns the number of entries in the set, which is the same as the
         * number of entries in the map.
         *
         * @return The number of entries in this set.
         */
        @Override
        public int size() {
            return CuckooMap2.this.size();
        }

        /**
         * Given an Entry, returns whether that entry is stored in the set of
         * entries.
         *
         * @param entry The entry to look up.
         * @return Whether it is contained in the set of entries.
         * @throws ClassCastException If the entry is not of type Entry or its
         *                            key and value are the wrong type.
         */
        @Override
        public boolean contains(Object entry) {
            /* Check if the object is null; it's certainly not here if that's
             * true.
             */
            if (entry == null) return false;

            /* Cast it to an Entry<?, ?> and see if the key is contained. */
            Entry<?, ?> realEntry = (Entry) entry;
            if (!containsKey(realEntry.getKey()))
                return false;

            /* Get the value and check if it matches. */
            V value = get(realEntry.getKey());
            return CuckooMap2.isEqual(value, realEntry.getValue());
        }

        /**
         * Removes the specified entry from this entry set (and, consequently,
         * from the hash map.  If the entry is of the wrong type, throws a
         * ClassCastException.
         *
         * @param entry The entry to remove.
         * @return Whether the entry was removed.
         * @throws ClassCastException If the entry is of the wrong type.
         */
        @Override
        public boolean remove(Object entry) {
            /* If the entry isn't here, then we can't remove it.  This also
             * filters out null and the case where the specific entry isn't
             * here.
             */
            if (!contains(entry)) return false;

            /* Recover the original entry, then remove its key from the map. */
            Entry<?, ?> realEntry = (Entry) entry;
            CuckooMap2.this.remove(realEntry.getKey());

            /* We did remove something. */
            return true;
        }

        /**
         * Clears this set, and consequently, the hash map.
         */
        @Override
        public void clear() {
            CuckooMap2.this.clear();
        }

        /**
         * A utility class representing an iterator that can traverse the
         * entries in the map.
         */
        public final class MapIterator implements Iterator<Entry<K, V>> {
            /* The next index to consider, expressed as a pair of a table index
             * and an offset.  The next table index will be 2 if there are no
             * more elements to consider.
             */
            private int mNextTable = 0, mNextIndex = 0;

            /* The last value we visited, so we can call remove(). */
            private Entry<K, V> mLast = null;

            /**
             * Creates a new MapIterator to traverse the hash map.
             */
            public MapIterator() {
                /* Figure out what the next location will be. */
                stageNext();
            }

            /**
             * Returns whether there are more elements to visit.
             *
             * @return Whether there are more elements to visit.
             */
            @Override
            public boolean hasNext() {
                /* We have a next element as long as the next location to visit
                 * isn't past the end of the arrays.
                 */
                return mNextTable != 2;
            }

            /**
             * Returns the next element in the sequence.  If there are no more
             * elements to visit, throws a NoSuchElementException.
             *
             * @return The next element in the sequence.
             * @throws NoSuchElementException If there are no more elements.
             */
            @Override
            public Entry<K, V> next() {
                if (!hasNext())
                    throw new NoSuchElementException("Out of elements.");

                /* Cache the value we're going to return. */
                Entry<K, V> result = mArrays[mNextTable][mNextIndex];

                /* Advance what index to consider next (so the next staging
                 * doesn't pick up the same element), then stage the next
                 * value.
                 */
                ++mNextIndex;
                stageNext();

                /* Store this value so we can remove it later. */
                mLast = result;

                return result;
            }

            /**
             * Removes the last element that was visited.  If this is not
             * called after next(), or is invoked twice, throws an
             * IllegalStateException.
             *
             * @throws IllegalStateException If there is no element to remove.
             */
            @Override
            public void remove() {
                /* Check if something is staged for removal and fail if there
                 * isn't.
                 */
                if (mLast == null)
                    throw new IllegalStateException("No element staged.");

                /* Tell owner to remove the element. */
                EntrySet.this.remove(mLast);

                /* Unstage the element so that we can't remove it again. */
                mLast = null;
            }

            /**
             * Queues up the next element so a call to next() can find it, or
             * detects that no more elements remain.
             */
            private void stageNext() {
                /* Pick up where we left off. */
                for (; mNextTable < 2; ++mNextTable) {
                    /* Scan across this table looking for something. */
                    for (; mNextIndex < mArrays[0].length; ++mNextIndex)
                        if (mArrays[mNextTable][mNextIndex] != null)
                            return;

                    /* If we didn't find it, reset the offset and check the
                     * next table.
                     */
                    mNextIndex = 0;
                }
            }
        }

        /**
         * Returns an iterator that can traverse the elements of the EntrySet.
         *
         * @return An iterator that can traverse the elements of the EntrySet.
         */
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new MapIterator();
        }
    }

    /**
     * Returns a Set containing an Entry for every key/value pair in the map.
     *
     * @return A Set containing an Entry for every key/value pair in the map.
     */
    @Override
    public EntrySet entrySet() {
        return new EntrySet();
    }
}
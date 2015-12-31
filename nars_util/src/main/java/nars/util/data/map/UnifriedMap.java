package nars.util.data.map;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function0;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.block.predicate.Predicate2;
import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.block.procedure.Procedure2;
import com.gs.collections.api.block.procedure.primitive.ObjectIntProcedure;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.map.MapIterable;
import com.gs.collections.api.map.MutableMap;
import com.gs.collections.api.map.UnsortedMapIterable;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.block.factory.Functions;
import com.gs.collections.impl.block.factory.Predicates;
import com.gs.collections.impl.block.procedure.MapCollectProcedure;
import com.gs.collections.impl.factory.Maps;
import com.gs.collections.impl.factory.Sets;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.AbstractMutableMap;
import com.gs.collections.impl.parallel.BatchIterable;
import com.gs.collections.impl.set.mutable.UnifiedSet;
import com.gs.collections.impl.tuple.ImmutableEntry;
import com.gs.collections.impl.tuple.Tuples;
import com.gs.collections.impl.utility.ArrayIterate;
import com.gs.collections.impl.utility.Iterate;
import net.jcip.annotations.NotThreadSafe;

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.*;

/*
 * Copyright 2015 Goldman Sachs, OpenNARS

 * Originally licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * UnifriedMap is an attempt at some optimizations of UnifiedMap
 * <p>
 * UnifiedMap stores key/value pairs in a single array, where alternate slots are keys and values.  This is nicer to CPU caches as
 * consecutive memory addresses are very cheap to access.  Entry objects are not stored in the table like in java.util.HashMap.
 * Instead of trying to deal with collisions in the main array using Entry objects, we put a special object in
 * the key slot and put a regular Object[] in the value slot. The array contains the key value pairs in consecutive slots,
 * just like the main array, but it's a linear list with no hashing.
 * <p>
 * The final result is a Map implementation that's leaner than java.util.HashMap and faster than Trove's THashMap.
 * The best of both approaches unified together, and thus the name UnifriedMap.
 */

@NotThreadSafe
@SuppressWarnings("ObjectEquality")
public class UnifriedMap<K, V> extends AbstractMutableMap<K, V>
        implements Externalizable, BatchIterable<V> {

    protected static final Object NULL_KEY = new Object() {
        @Override
        public boolean equals(Object obj) {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public int hashCode() {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public String toString() {
            return "UnifriedMap.NULL_KEY";
        }
    };

    protected static final Object CHAINED_KEY = new Object() {
        @Override
        public boolean equals(Object obj) {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public int hashCode() {
            throw new RuntimeException("Possible corruption through unsynchronized concurrent modification.");
        }

        @Override
        public String toString() {
            return "UnifriedMap.CHAINED_KEY";
        }
    };

    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

    protected static final int DEFAULT_INITIAL_CAPACITY = 8;

    private static final long serialVersionUID = 1L;

    protected transient Object[] table;

    protected transient int occupied;

    protected float loadFactor = DEFAULT_LOAD_FACTOR;

    protected int maxSize;

    public UnifriedMap() {
        allocate(DEFAULT_INITIAL_CAPACITY << 1);
    }

    public UnifriedMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public UnifriedMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initial capacity cannot be less than 0");
        }
        if (loadFactor <= 0.0) {
            throw new IllegalArgumentException("load factor cannot be less than or equal to 0");
        }
        if (loadFactor > 1.0) {
            throw new IllegalArgumentException("load factor cannot be greater than 1");
        }

        this.loadFactor = loadFactor;
        init(fastCeil(initialCapacity / loadFactor));
    }

    public UnifriedMap(Map<? extends K, ? extends V> map) {
        this(Math.max(map.size(), DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);

        putAll(map);
    }

    @SafeVarargs
    public UnifriedMap(Pair<K, V>... pairs) {
        this(Math.max(pairs.length, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        ArrayIterate.forEach(pairs, new MapCollectProcedure<Pair<K, V>, K, V>(
                this,
                Functions.<K>firstOfPair(),
                Functions.<V>secondOfPair()));
    }

    public static <K, V> UnifriedMap<K, V> newMap() {
        return new UnifriedMap<>();
    }

    public static <K, V> UnifriedMap<K, V> newMap(int size) {
        return new UnifriedMap<>(size);
    }

    public static <K, V> UnifriedMap<K, V> newMap(int size, float loadFactor) {
        return new UnifriedMap<>(size, loadFactor);
    }

    public static <K, V> UnifriedMap<K, V> newMap(Map<? extends K, ? extends V> map) {
        return new UnifriedMap<>(map);
    }

    @SafeVarargs
    public static <K, V> UnifriedMap<K, V> newMapWith(Pair<K, V>... pairs) {
        return new UnifriedMap<>(pairs);
    }

    public static <K, V> UnifriedMap<K, V> newMapWith(Iterable<Pair<K, V>> inputIterable) {
        UnifriedMap<K, V> outputMap = UnifriedMap.newMap();
        for (Pair<K, V> single : inputIterable) {
            outputMap.add(single);
        }
        return outputMap;
    }

    public static <K, V> UnifriedMap<K, V> newWithKeysValues(K key, V value) {
        return new UnifriedMap<K, V>(1).withKeysValues(key, value);
    }

    public static <K, V> UnifriedMap<K, V> newWithKeysValues(K key1, V value1, K key2, V value2) {
        return new UnifriedMap<K, V>(2).withKeysValues(key1, value1, key2, value2);
    }

    public static <K, V> UnifriedMap<K, V> newWithKeysValues(K key1, V value1, K key2, V value2, K key3, V value3) {
        return new UnifriedMap<K, V>(3).withKeysValues(key1, value1, key2, value2, key3, value3);
    }

    public static <K, V> UnifriedMap<K, V> newWithKeysValues(
            K key1, V value1,
            K key2, V value2,
            K key3, V value3,
            K key4, V value4) {
        return new UnifriedMap<K, V>(4).withKeysValues(key1, value1, key2, value2, key3, value3, key4, value4);
    }

    public UnifriedMap<K, V> withKeysValues(K key, V value) {
        putFast(key, value);
        return this;
    }

    public UnifriedMap<K, V> withKeysValues(K key1, V value1, K key2, V value2) {
        putFast(key1, value1);
        putFast(key2, value2);
        return this;
    }

    public UnifriedMap<K, V> withKeysValues(K key1, V value1, K key2, V value2, K key3, V value3) {
        putFast(key1, value1);
        putFast(key2, value2);
        putFast(key3, value3);
        return this;
    }

    public UnifriedMap<K, V> withKeysValues(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        putFast(key1, value1);
        putFast(key2, value2);
        putFast(key3, value3);
        putFast(key4, value4);
        return this;
    }

    @Override
    public UnifriedMap<K, V> clone() {
        return new UnifriedMap<>(this);
    }

    public MutableMap<K, V> newEmpty() {
        return new UnifriedMap<>();
    }

    @Override
    public MutableMap<K, V> newEmpty(int capacity) {
        return UnifriedMap.newMap(capacity);
    }

    private static int fastCeil(float v) {
        int possibleResult = (int) v;
        if (v - possibleResult > 0.0F) {
            possibleResult++;
        }
        return possibleResult;
    }

    protected final int init(int initialCapacity) {
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }

        return allocate(capacity);
    }

    protected final int allocate(int capacity) {
        allocateTable(capacity << 1); // the table size is twice the capacity to handle both keys and values
        computeMaxSize(capacity);

        return capacity;
    }

    protected final Object[] allocateTable(int sizeToAllocate) {
        //if (this.table==null || this.table.length!=sizeToAllocate)
            return table = new Object[sizeToAllocate];
        /*else {
            Arrays.fill(table, null);
        }*/
    }

    protected final void computeMaxSize(int capacity) {
        // need at least one free slot for open addressing
        maxSize = Math.min(capacity - 1, (int) (capacity * loadFactor));
    }

    protected final int index(Object key) {
        return index(key, table.length);
    }

    protected static int index(Object key, int tl) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        int h = key == null ? 0 : key.hashCode();
        h ^= h >>> 20 ^ h >>> 12;
        h ^= h >>> 7 ^ h >>> 4;
        return (h & (tl >> 1) - 1) << 1;
    }

    public final void clear() {

        if (occupied == 0)
            return;

        occupied = 0;
        Arrays.fill(table, null);

        /*Object[] set = this.table;
        for (int i = set.length; i-- > 0; ) {
            set[i] = null;
        }*/
    }

    @Override
    public V put(K key, V value) {
        return (V)putFast(key, value);
    }

    /**
     * puts fast without the useless cast.
     * make sure value is of type V
     */
    public final Object putFast(Object key, Object value) {
        int index = index(key);
        Object[] t = table;
        Object cur = t[index];
        if (cur == null) {
            t[index++] = UnifriedMap.toSentinelIfNull(key);
            t[index] = value;
            if (++occupied > maxSize) {
                rehash(t.length);
            }
            return null;
        }
        if (cur != CHAINED_KEY && nonNullTableObjectEquals(cur, key)) {
            Object result = t[++index];
            t[index] = value;
            return result;
        }
        return chainedPut(key, index, value);
    }

    private Object chainedPut(Object key, int index, Object value) {
        Object[] t = table;
        int tl = t.length;

        if (t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) t[index + 1];
            int cl = chain.length;
            int ms = maxSize;
            for (int i = 0; i < cl; i += 2) {
                if (chain[i] == null) {
                    chain[i++] = UnifriedMap.toSentinelIfNull(key);
                    chain[i] = value;
                    if (++occupied > ms) {
                        rehash(tl);
                    }
                    return null;
                }
                if (nonNullTableObjectEquals(chain[i], key)) {
                    i++;
                    Object result = chain[i];
                    chain[i] = value;
                    return result;
                }
            }
            Object[] newChain = new Object[cl + 4];
            System.arraycopy(chain, 0, newChain, 0, cl);
            t[index + 1] = newChain;
            newChain[cl] = UnifriedMap.toSentinelIfNull(key);
            newChain[cl + 1] = value;
            if (++occupied > ms) {
                rehash(tl);
            }
            return null;
        }
        Object[] newChain =  {
            /*newChain[0] = */t[index],
            /*newChain[1] = */t[index + 1],
            /*newChain[2] = */UnifriedMap.toSentinelIfNull(key),
            /*newChain[3] = */value
        };
        t[index++] = CHAINED_KEY;
        t[index] = newChain;
        if (++occupied > maxSize) {
            rehash(tl);
        }
        return null;
    }

    @Override
    public V updateValue(K key, Function0<? extends V> factory, Function<? super V, ? extends V> function) {
        int index = index(key);
        Object[] t = table;
        Object cur = t[index];
        if (cur == null) {
            t[index] = UnifriedMap.toSentinelIfNull(key);
            V result = function.valueOf(factory.value());
            t[index + 1] = result;
            ++occupied;
            return result;
        }
        if (cur != CHAINED_KEY && nonNullTableObjectEquals(cur, key)) {
            V oldValue = (V) t[index + 1];
            V newValue = function.valueOf(oldValue);
            t[index + 1] = newValue;
            return newValue;
        }
        return chainedUpdateValue(key, index, factory, function);
    }

    private V chainedUpdateValue(K key, int index, Function0<? extends V> factory, Function<? super V, ? extends V> function) {
        Object[] t = table;
        if (t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) t[index + 1];
            for (int i = 0; i < chain.length; i += 2) {
                if (chain[i] == null) {
                    chain[i] = UnifriedMap.toSentinelIfNull(key);
                    V result = function.valueOf(factory.value());
                    chain[i + 1] = result;
                    if (++occupied > maxSize) {
                        rehash(t.length);
                    }
                    return result;
                }
                if (nonNullTableObjectEquals(chain[i], key)) {
                    V oldValue = (V) chain[i + 1];
                    V result = function.valueOf(oldValue);
                    chain[i + 1] = result;
                    return result;
                }
            }
            Object[] newChain = new Object[chain.length + 4];
            System.arraycopy(chain, 0, newChain, 0, chain.length);
            t[index + 1] = newChain;
            newChain[chain.length] = UnifriedMap.toSentinelIfNull(key);
            V result = function.valueOf(factory.value());
            newChain[chain.length + 1] = result;
            if (++occupied > maxSize) {
                rehash(t.length);
            }
            return result;
        }
        Object[] newChain = new Object[4];
        newChain[0] = t[index];
        newChain[1] = t[index + 1];
        newChain[2] = UnifriedMap.toSentinelIfNull(key);
        V result = function.valueOf(factory.value());
        newChain[3] = result;
        t[index] = CHAINED_KEY;
        t[index + 1] = newChain;
        if (++occupied > maxSize) {
            rehash(t.length);
        }
        return result;
    }

    @Override
    public <P> V updateValueWith(K key, Function0<? extends V> factory, Function2<? super V, ? super P, ? extends V> function, P parameter) {
        int index = index(key);
        Object cur = table[index];
        if (cur == null) {
            table[index] = UnifriedMap.toSentinelIfNull(key);
            V result = function.value(factory.value(), parameter);
            table[index + 1] = result;
            ++occupied;
            return result;
        }
        if (cur != CHAINED_KEY && nonNullTableObjectEquals(cur, key)) {
            V oldValue = (V) table[index + 1];
            V newValue = function.value(oldValue, parameter);
            table[index + 1] = newValue;
            return newValue;
        }
        return chainedUpdateValueWith(key, index, factory, function, parameter);
    }

    private <P> V chainedUpdateValueWith(
            K key,
            int index,
            Function0<? extends V> factory,
            Function2<? super V, ? super P, ? extends V> function,
            P parameter) {
        if (table[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) table[index + 1];
            for (int i = 0; i < chain.length; i += 2) {
                if (chain[i] == null) {
                    chain[i] = UnifriedMap.toSentinelIfNull(key);
                    V result = function.value(factory.value(), parameter);
                    chain[i + 1] = result;
                    if (++occupied > maxSize) {
                        rehash(table.length);
                    }
                    return result;
                }
                if (nonNullTableObjectEquals(chain[i], key)) {
                    V oldValue = (V) chain[i + 1];
                    V result = function.value(oldValue, parameter);
                    chain[i + 1] = result;
                    return result;
                }
            }
            Object[] newChain = new Object[chain.length + 4];
            System.arraycopy(chain, 0, newChain, 0, chain.length);
            table[index + 1] = newChain;
            newChain[chain.length] = UnifriedMap.toSentinelIfNull(key);
            V result = function.value(factory.value(), parameter);
            newChain[chain.length + 1] = result;
            if (++occupied > maxSize) {
                rehash(table.length);
            }
            return result;
        }
        Object[] newChain = new Object[4];
        newChain[0] = table[index];
        newChain[1] = table[index + 1];
        newChain[2] = UnifriedMap.toSentinelIfNull(key);
        V result = function.value(factory.value(), parameter);
        newChain[3] = result;
        table[index] = CHAINED_KEY;
        table[index + 1] = newChain;
        if (++occupied > maxSize) {
            rehash(table.length);
        }
        return result;
    }

    @Override
    public V getIfAbsentPut(K key, Function0<? extends V> function) {
        Object[] t = table;

        int index = index(key, t.length);

        Object cur = t[index];

        if (cur == null) {
            V result = function.value();
            t[index] = UnifriedMap.toSentinelIfNull(key);
            t[index + 1] = result;
            if (++occupied > maxSize) {
                rehash(t.length);
            }
            return result;
        }
        if (cur != CHAINED_KEY && nonNullTableObjectEquals(cur, key)) {
            return (V) t[index + 1];
        }
        return chainedGetIfAbsentPut(key, index, function);
    }

    private V chainedGetIfAbsentPut(K key, int index, Function0<? extends V> function) {
        V result = null;
        Object[] t = table;
        if (t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) t[index + 1];
            int i = 0;
            int cl = chain.length;
            for (; i < cl; i += 2) {
                Object c = chain[i];
                if (c == null) {
                    result = function.value();
                    chain[i] = UnifriedMap.toSentinelIfNull(key);
                    chain[i + 1] = result;
                    if (++occupied > maxSize) {
                        rehash(t.length);
                    }
                    break;
                }
                else /* this else and others like it may prevent null keys, dunno */
                    if (nonNullTableObjectEquals(c, key)) {
                    result = (V) chain[i + 1];
                    break;
                }
            }
            if (i == cl) {
                result = function.value();
                Object[] newChain = new Object[cl + 4];
                System.arraycopy(chain, 0, newChain, 0, cl);
                newChain[i] = UnifriedMap.toSentinelIfNull(key);
                newChain[i + 1] = result;
                t[index + 1] = newChain;
                if (++occupied > maxSize) {
                    rehash(t.length);
                }
            }
        } else {
            result = function.value();

            Object[] newChain =  {
                /*newChain[0] =*/ t[index],
                /*newChain[1] =*/ t[index + 1],
                /*newChain[2] =*/ UnifriedMap.toSentinelIfNull(key),
                /*newChain[3] =*/ result
            };

            t[index++] = CHAINED_KEY;
            t[index] = newChain;
            if (++occupied > maxSize) {
                rehash(t.length);
            }
        }
        return result;
    }

    @Override
    public V getIfAbsentPut(K key, V value) {
        int index = index(key);
        Object[] t = table;
        Object cur = t[index];

        if (cur == null) {
            t[index] = UnifriedMap.toSentinelIfNull(key);
            t[index + 1] = value;
            if (++occupied > maxSize) {
                rehash(t.length);
            }
            return value;
        }
        if (cur != CHAINED_KEY && nonNullTableObjectEquals(cur, key)) {
            return (V) t[index + 1];
        }
        return chainedGetIfAbsentPut(key, index, value);
    }

    private V chainedGetIfAbsentPut(K key, int index, V value) {
        V result = value;
        Object[] t = table;
        int tl = t.length;
        if (t[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) t[index + 1];
            int i = 0;
            int cl = chain.length;
            for (; i < cl; i += 2) {
                if (chain[i] == null) {
                    chain[i] = UnifriedMap.toSentinelIfNull(key);
                    chain[i + 1] = value;
                    if (++occupied > maxSize) {
                        rehash(tl);
                    }
                    break;
                }
                if (nonNullTableObjectEquals(chain[i], key)) {
                    result = (V) chain[i + 1];
                    break;
                }
            }
            if (i == cl) {
                Object[] newChain = new Object[cl + 4];
                System.arraycopy(chain, 0, newChain, 0, cl);
                newChain[i] = UnifriedMap.toSentinelIfNull(key);
                newChain[i + 1] = value;
                t[index + 1] = newChain;
                if (++occupied > maxSize) {
                    rehash(tl);
                }
            }
        } else {
            Object[] newChain = new Object[4];
            newChain[0] = t[index];
            newChain[1] = t[index + 1];
            newChain[2] = UnifriedMap.toSentinelIfNull(key);
            newChain[3] = value;
            t[index] = CHAINED_KEY;
            t[index + 1] = newChain;
            if (++occupied > maxSize) {
                rehash(tl);
            }
        }
        return result;
    }

    @Override
    public <P> V getIfAbsentPutWith(K key, Function<? super P, ? extends V> function, P parameter) {
        int index = index(key);
        Object cur = table[index];

        if (cur == null) {
            V result = function.valueOf(parameter);
            table[index] = UnifriedMap.toSentinelIfNull(key);
            table[index + 1] = result;
            if (++occupied > maxSize) {
                rehash(table.length);
            }
            return result;
        }
        if (cur != CHAINED_KEY && nonNullTableObjectEquals(cur, key)) {
            return (V) table[index + 1];
        }
        return chainedGetIfAbsentPutWith(key, index, function, parameter);
    }

    private <P> V chainedGetIfAbsentPutWith(K key, int index, Function<? super P, ? extends V> function, P parameter) {
        V result = null;
        if (table[index] == CHAINED_KEY) {
            Object[] chain = (Object[]) table[index + 1];
            int i = 0;
            for (; i < chain.length; i += 2) {
                if (chain[i] == null) {
                    result = function.valueOf(parameter);
                    chain[i] = UnifriedMap.toSentinelIfNull(key);
                    chain[i + 1] = result;
                    if (++occupied > maxSize) {
                        rehash(table.length);
                    }
                    break;
                }
                if (nonNullTableObjectEquals(chain[i], key)) {
                    result = (V) chain[i + 1];
                    break;
                }
            }
            if (i == chain.length) {
                result = function.valueOf(parameter);
                Object[] newChain = new Object[chain.length + 4];
                System.arraycopy(chain, 0, newChain, 0, chain.length);
                newChain[i] = UnifriedMap.toSentinelIfNull(key);
                newChain[i + 1] = result;
                table[index + 1] = newChain;
                if (++occupied > maxSize) {
                    rehash(table.length);
                }
            }
        } else {
            result = function.valueOf(parameter);
            Object[] newChain = new Object[4];
            newChain[0] = table[index];
            newChain[1] = table[index + 1];
            newChain[2] = UnifriedMap.toSentinelIfNull(key);
            newChain[3] = result;
            table[index] = CHAINED_KEY;
            table[index + 1] = newChain;
            if (++occupied > maxSize) {
                rehash(table.length);
            }
        }
        return result;
    }

    public int getCollidingBuckets() {
        int count = 0;
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the number of JVM words that is used by this map.  A word is 4 bytes in a 32bit VM and 8 bytes in a 64bit
     * VM. Each array has a 2 word header, thus the formula is:
     * words = (internal table length + 2) + sum (for all chains (chain length + 2))
     *
     * @return the number of JVM words that is used by this map.
     */
    public int getMapMemoryUsedInWords() {
        int headerSize = 2;
        int sizeInWords = table.length + headerSize;
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                sizeInWords += headerSize + ((Object[]) table[i + 1]).length;
            }
        }
        return sizeInWords;
    }

    protected void rehash(int newCapacity) {
        Object[] old = table;
        int oldLength = old.length;

        allocate(newCapacity);
        occupied = 0;

        for (int i = 0; i < oldLength; i += 2) {
            Object cur = old[i];
            if (cur == CHAINED_KEY) {
                Object[] chain = (Object[]) old[i + 1];
                int clen = chain.length;
                for (int j = 0; j < clen; ) {
                    Object cj = chain[j++];
                    if (cj != null) {
                        putFast(nonSentinel(cj), chain[j]);
                    }
                    j++;
                }
            } else if (cur != null) {
                putFast(nonSentinel(cur), old[i + 1]);
            }
        }
    }

    public final V get(Object key) {

        Object[] t = table;
        int index = index(key, t.length);
        Object cur = t[index];

        Object v = null;

        if (cur != null) {
            Object val = t[index + 1];

            if (cur == CHAINED_KEY) {
                 v = getFromChain((Object[]) val, key);
            }

            else if (nonNullTableObjectEquals(cur, key)) {
                v =  val;
            }

        }

        return (V)v;
    }

    private static Object getFromChain(Object[] chain, Object key) {
        int cl = chain.length;
        for (int i = 0; i < cl; i += 2) {
            Object k = chain[i];
            if (k == null) {
                return null;
            }
            if (nonNullTableObjectEquals(k, key)) {
                return chain[i + 1];
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        Object[] t = table;
        int index = index(key, t.length);
        Object cur = t[index];

        if (cur == null) {
            return false;
        }

        boolean curIsChain = cur == CHAINED_KEY;
        if ((!curIsChain) && nonNullTableObjectEquals(cur, key)) {
            return true;
        }

        return curIsChain && chainContainsKey((Object[]) t[index + 1], key);
    }

    private static boolean chainContainsKey(Object[] chain, Object key) {
        int cl = chain.length;
        for (int i = 0; i < cl; i += 2) {
            Object k = chain[i];
            if (k == null) {
                return false;
            }
            if (nonNullTableObjectEquals(k, key)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsValue(Object value) {
        Object[] t = table;
        int tl = t.length;
        for (int i = 0; i < tl; i += 2) {
            if (t[i] == CHAINED_KEY) {
                if (chainedContainsValue((Object[]) t[i + 1], value)) {
                    return true;
                }
            } else if (t[i] != null) {
                //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
                if (Objects.equals(value, t[i + 1])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean chainedContainsValue(Object[] chain, Object value) {
        for (int i = 0; i < chain.length; i += 2) {
            if (chain[i] == null) {
                return false;
            }
            //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
            if (Objects.equals(value, chain[i + 1])) {
                return true;
            }
        }
        return false;
    }

    public void forEachKeyValue(Procedure2<? super K, ? super V> procedure) {
        if (isEmpty())
            return;

        for (int i = 0; i < table.length; i += 2) {
            Object[] t = table;
            Object cur = t[i];
            if (cur == CHAINED_KEY) {
                chainedForEachEntry((Object[]) t[i + 1], procedure);
            } else if (cur != null) {
                procedure.value((K) nonSentinel(cur), (V) t[i + 1]);
            }
        }
    }

    public <E> MutableMap<K, V> collectKeysAndValues(
            Iterable<E> iterable,
            Function<? super E, ? extends K> keyFunction,
            Function<? super E, ? extends V> valueFunction) {
        Iterate.forEach(iterable, new MapCollectProcedure<>(this, keyFunction, valueFunction));
        return this;
    }

    public final V removeKey(K key) {
        return remove(key);
    }

    private void chainedForEachEntry(Object[] chain, Procedure2<? super K, ? super V> procedure) {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) {
                return;
            }
            procedure.value((K) nonSentinel(cur), (V) chain[i + 1]);
        }
    }

    public final int getBatchCount(int batchSize) {
        return Math.max(1, table.length / 2 / batchSize);
    }

    public void batchForEach(Procedure<? super V> procedure, int sectionIndex, int sectionCount) {
        Object[] t = table;
        int tl = t.length;
        int sectionSize = tl / sectionCount;
        int start = sectionIndex * sectionSize;
        int end = sectionIndex == sectionCount - 1 ? tl : start + sectionSize;
        if (start % 2 == 0) {
            start++;
        }
        for (int i = start; i < end; i += 2) {
            Object value = t[i];
            if (value instanceof Object[]) {
                chainedForEachValue((Object[]) value, procedure);
            } else if (value == null && t[i - 1] != null || value != null) {
                procedure.value((V) value);
            }
        }
    }

    @Override
    public final void forEachKey(Procedure<? super K> procedure) {
        if (isEmpty()) return;

        Object[] t = table;
        int l = t.length;
        for (int i = 0; i < l; i += 2) {
            Object cur = t[i];
            if (cur == CHAINED_KEY) {
                chainedForEachKey((Object[]) t[i + 1], procedure);
            } else if (cur != null) {
                procedure.value((K) nonSentinel(cur));
            }
        }
    }

    private void chainedForEachKey(Object[] chain, Procedure<? super K> procedure) {
        int cl = chain.length;
        for (int i = 0; i < cl; i += 2) {
            Object cur = chain[i];
            if (cur == null) {
                return;
            }
            procedure.value((K) nonSentinel(cur));
        }
    }

    @Override
    public void forEachValue(Procedure<? super V> procedure) {
        if (isEmpty())
            return;

        Object[] t = table;

        int l = t.length;

        for (int i = 0; i < l; ) {
            Object cur = t[i++];
            Object curVal = t[i++];
            if (cur == CHAINED_KEY) {
                chainedForEachValue((Object[]) curVal, procedure);
            } else if (cur != null) {
                procedure.value((V) curVal);
            }
        }
    }

    private static <V> void chainedForEachValue(Object[] chain, Procedure<? super V> procedure) {
        int l = chain.length;
        for (int i = 0; i < l; ) {
            Object cur = chain[i++];
            if (cur == null) {
                return;
            }
            procedure.value((V) chain[i++]);
        }
    }

    @Override
    public final boolean isEmpty() {
        return occupied == 0;
    }

    public final void putAll(Map<? extends K, ? extends V> map) {
        if (map instanceof UnifriedMap<?, ?>) {

            UnifriedMap<K, V> umap = (UnifriedMap<K, V>) map;

            if (isEmpty())
                copyMapToEmpty(umap);
            else
                copyMap(umap);

        } else if (map instanceof UnsortedMapIterable) {
            MapIterable<K, V> mapIterable = (MapIterable<K, V>) map;
            mapIterable.forEachKeyValue((Procedure2<K, V>) this::put);
        } else {
            for (Entry<? extends K, ? extends V> entry : getEntrySetFrom(map)) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    private Set<? extends Entry<? extends K, ? extends V>> getEntrySetFrom(Map<? extends K, ? extends V> map) {
        Set<? extends Entry<? extends K, ? extends V>> entries = map.entrySet();
        if (entries != null) {
            return entries;
        }
        if (map.isEmpty()) {
            return Sets.immutable.<Entry<K, V>>of().castToSet();
        }
        throw new IllegalStateException("Entry set was null and size was non-zero");
    }

    /** if this table is empty, completely overwrite it with the values from another map */
    protected final void copyMapToEmpty(UnifriedMap<K, V> unifiedMap) {

        Object[] st = unifiedMap.table; //src
        int nextLen = st.length;

        Object[] tt = table; //target (this)
        if (tt.length!= nextLen)
            tt = allocateTable(nextLen);

        if ((occupied = unifiedMap.occupied) > 0)
            System.arraycopy(st, 0, tt, 0, nextLen);
        //else it should remain all null

    }

    protected final void copyMap(UnifriedMap<K, V> unifiedMap) {
        Object[] ut = unifiedMap.table;

        int l = ut.length;

        for (int i = 0; i < l; ) {
            Object cur = ut[i++];
            Object uu = ut[i++];
            if (cur == CHAINED_KEY) {
                copyChain((Object[]) uu);
            } else if (cur != null) {
                putFast(nonSentinel(cur), uu);
            }
        }
    }

    private void copyChain(Object[] chain) {
        int cl = chain.length;
        for (int j = 0; j < cl; ) {
            Object cur = chain[j++];
            if (cur == null) {
                break;
            }
            putFast(nonSentinel(cur), chain[j++]);
        }
    }

    public V remove(Object key) {
        int index = index(key);
        Object[] t = table;
        Object cur = t[index];
        if (cur != null) {
            Object val = t[index + 1];
            if (cur == CHAINED_KEY) {
                return removeFromChain((Object[]) val, key, index);
            }
            if (nonNullTableObjectEquals(cur, key)) {
                t[index++] = null;
                t[index] = null;
                occupied--;
                return (V) val;
            }
        }
        return null;
    }

    private V removeFromChain(Object[] chain, Object key, int index) {
        int cl = chain.length;
        for (int i = 0; i < cl; i += 2) {
            Object k = chain[i];
            if (k == null) {
                return null;
            }
            if (nonNullTableObjectEquals(k, key)) {
                V val = (V) chain[i + 1];
                overwriteWithLastElementFromChain(chain, index, i);
                return val;
            }
        }
        return null;
    }

    private void overwriteWithLastElementFromChain(Object[] chain, int index, int i) {
        int j = chain.length - 2;
        for (; j > i; j -= 2) {
            if (chain[j] != null) {
                chain[i] = chain[j];
                chain[i + 1] = chain[j + 1];
                break;
            }
        }
        chain[j] = null;
        chain[j + 1] = null;
        if (j == 0) {
            Object[] t = table;
            t[index++] = null;
            t[index] = null;
        }
        occupied--;
    }

    public final int size() {
        return occupied;
    }

    public final Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    public final Set<K> keySet() {
        return new KeySet();
    }

    public final Collection<V> values() {
        return new ValuesCollection();
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Map)) {
            return false;
        }

        Map<?, ?> other = (Map<?, ?>) object;
        if (size() != other.size()) {
            return false;
        }

        Object[] table = this.table;

        int l = table.length;

        for (int i = 0; i < l; i += 2) {

            Object cur = table[i];
            if (cur == CHAINED_KEY) {
                if (!chainedEquals((Object[]) table[i + 1], other)) {
                    return false;
                }
            } else if (cur != null) {
                Object key = nonSentinel(cur);
                Object value = table[i + 1];
                Object otherValue = other.get(key);
                //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
                if (!Objects.equals(otherValue, value) || (value == null && otherValue == null && !other.containsKey(key))) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean chainedEquals(Object[] chain, Map<?, ?> other) {
        int cl = chain.length;
        for (int i = 0; i < cl; i += 2) {
            Object cur = chain[i];
            if (cur == null) {
                return true;
            }
            Object key = nonSentinel(cur);
            Object value = chain[i + 1];
            Object otherValue = other.get(key);
            //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
            if (!Objects.equals(otherValue, value) || (value == null && otherValue == null && !other.containsKey(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int hashCode = 0;
        Object[] t = table;
        int tl = t.length;
        for (int i = 0; i < tl; ) {
            Object cur = t[i];
            i++;
            if (cur == CHAINED_KEY) {
                hashCode += chainedHashCode((Object[]) t[i]);
            } else if (cur != null) {
                Object value = t[i];
                hashCode += (cur == NULL_KEY ? 0 : cur.hashCode()) ^ (value == null ? 0 : value.hashCode());
            }
            i++;
        }
        return hashCode;
    }

    private static int chainedHashCode(Object[] chain) {
        int hashCode = 0;
        int cl = chain.length;
        for (int i = 0; i < cl; i += 2) {
            Object cur = chain[i];
            if (cur == null) {
                return hashCode;
            }
            Object value = chain[i + 1];
            hashCode += (cur == NULL_KEY ? 0 : cur.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');

        forEachKeyValue(new Procedure2<K, V>() {
            private boolean first = true;

            public void value(K key, V value) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }

                builder.append(key == UnifriedMap.this ? "(this Map)" : key);
                builder.append('=');
                builder.append(value == UnifriedMap.this ? "(this Map)" : value);
            }
        });

        builder.append('}');
        return builder.toString();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        loadFactor = in.readFloat();
        init(Math.max((int) (size / loadFactor) + 1,
                DEFAULT_INITIAL_CAPACITY));
        for (int i = 0; i < size; i++) {
            put((K) in.readObject(), (V) in.readObject());
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size());
        out.writeFloat(loadFactor);
        for (int i = 0; i < table.length; i += 2) {
            Object o = table[i];
            if (o != null) {
                if (o == CHAINED_KEY) {
                    writeExternalChain(out, (Object[]) table[i + 1]);
                } else {
                    out.writeObject(nonSentinel(o));
                    out.writeObject(table[i + 1]);
                }
            }
        }
    }

    private void writeExternalChain(ObjectOutput out, Object[] chain) throws IOException {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) {
                return;
            }
            out.writeObject(nonSentinel(cur));
            out.writeObject(chain[i + 1]);
        }
    }

    @Override
    public void forEachWithIndex(ObjectIntProcedure<? super V> objectIntProcedure) {
        int index = 0;
        for (int i = 0; i < table.length; i += 2) {
            Object cur = table[i];
            if (cur == CHAINED_KEY) {
                index = chainedForEachValueWithIndex((Object[]) table[i + 1], objectIntProcedure, index);
            } else if (cur != null) {
                objectIntProcedure.value((V) table[i + 1], index++);
            }
        }
    }

    private int chainedForEachValueWithIndex(Object[] chain, ObjectIntProcedure<? super V> objectIntProcedure, int index) {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) {
                return index;
            }
            objectIntProcedure.value((V) chain[i + 1], index++);
        }
        return index;
    }

    @Override
    public <P> void forEachWith(Procedure2<? super V, ? super P> procedure, P parameter) {
        for (int i = 0; i < table.length; i += 2) {
            Object cur = table[i];
            if (cur == CHAINED_KEY) {
                chainedForEachValueWith((Object[]) table[i + 1], procedure, parameter);
            } else if (cur != null) {
                procedure.value((V) table[i + 1], parameter);
            }
        }
    }

    private <P> void chainedForEachValueWith(
            Object[] chain,
            Procedure2<? super V, ? super P> procedure,
            P parameter) {
        for (int i = 0; i < chain.length; i += 2) {
            Object cur = chain[i];
            if (cur == null) {
                return;
            }
            procedure.value((V) chain[i + 1], parameter);
        }
    }

    @Override
    public <R> MutableMap<K, R> collectValues(Function2<? super K, ? super V, ? extends R> function) {
        UnifriedMap<K, R> target = UnifriedMap.newMap();
        target.loadFactor = loadFactor;
        target.occupied = occupied;
        target.allocate(table.length >> 1);

        for (int i = 0; i < target.table.length; i += 2) {
            target.table[i] = table[i];

            if (table[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) table[i + 1];
                Object[] chainedTargetTable = new Object[chainedTable.length];
                for (int j = 0; j < chainedTargetTable.length; j += 2) {
                    if (chainedTable[j] != null) {
                        chainedTargetTable[j] = chainedTable[j];
                        chainedTargetTable[j + 1] = function.value((K) nonSentinel(chainedTable[j]), (V) chainedTable[j + 1]);
                    }
                }
                target.table[i + 1] = chainedTargetTable;
            } else if (table[i] != null) {
                target.table[i + 1] = function.value((K) nonSentinel(table[i]), (V) table[i + 1]);
            }
        }

        return target;
    }

    @Override
    public Pair<K, V> detect(Predicate2<? super K, ? super V> predicate) {
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) table[i + 1];
                for (int j = 0; j < chainedTable.length; j += 2) {
                    if (chainedTable[j] != null) {
                        K key = (K) nonSentinel(chainedTable[j]);
                        V value = (V) chainedTable[j + 1];
                        if (predicate.accept(key, value)) {
                            return Tuples.pair(key, value);
                        }
                    }
                }
            } else if (table[i] != null) {
                K key = (K) nonSentinel(table[i]);
                V value = (V) table[i + 1];

                if (predicate.accept(key, value)) {
                    return Tuples.pair(key, value);
                }
            }
        }

        return null;
    }

    @Override
    public V detect(Predicate<? super V> predicate) {
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) table[i + 1];
                for (int j = 0; j < chainedTable.length; j += 2) {
                    if (chainedTable[j] != null) {
                        V value = (V) chainedTable[j + 1];
                        if (predicate.accept(value)) {
                            return value;
                        }
                    }
                }
            } else if (table[i] != null) {
                V value = (V) table[i + 1];

                if (predicate.accept(value)) {
                    return value;
                }
            }
        }

        return null;
    }

    @Override
    public <P> V detectWith(Predicate2<? super V, ? super P> predicate, P parameter) {
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) table[i + 1];
                for (int j = 0; j < chainedTable.length; j += 2) {
                    if (chainedTable[j] != null) {
                        V value = (V) chainedTable[j + 1];
                        if (predicate.accept(value, parameter)) {
                            return value;
                        }
                    }
                }
            } else if (table[i] != null) {
                V value = (V) table[i + 1];

                if (predicate.accept(value, parameter)) {
                    return value;
                }
            }
        }

        return null;
    }

    @Override
    public V detectIfNone(Predicate<? super V> predicate, Function0<? extends V> function) {
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) table[i + 1];
                for (int j = 0; j < chainedTable.length; j += 2) {
                    if (chainedTable[j] != null) {
                        V value = (V) chainedTable[j + 1];
                        if (predicate.accept(value)) {
                            return value;
                        }
                    }
                }
            } else if (table[i] != null) {
                V value = (V) table[i + 1];

                if (predicate.accept(value)) {
                    return value;
                }
            }
        }

        return function.value();
    }

    @Override
    public <P> V detectWithIfNone(
            Predicate2<? super V, ? super P> predicate,
            P parameter,
            Function0<? extends V> function) {
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) table[i + 1];
                for (int j = 0; j < chainedTable.length; j += 2) {
                    if (chainedTable[j] != null) {
                        V value = (V) chainedTable[j + 1];
                        if (predicate.accept(value, parameter)) {
                            return value;
                        }
                    }
                }
            } else if (table[i] != null) {
                V value = (V) table[i + 1];

                if (predicate.accept(value, parameter)) {
                    return value;
                }
            }
        }

        return function.value();
    }

    private boolean shortCircuit(
            Predicate<? super V> predicate,
            boolean expected,
            boolean onShortCircuit,
            boolean atEnd) {
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) table[i + 1];
                for (int j = 0; j < chainedTable.length; j += 2) {
                    if (chainedTable[j] != null) {
                        V value = (V) chainedTable[j + 1];
                        if (predicate.accept(value) == expected) {
                            return onShortCircuit;
                        }
                    }
                }
            } else if (table[i] != null) {
                V value = (V) table[i + 1];

                if (predicate.accept(value) == expected) {
                    return onShortCircuit;
                }
            }
        }

        return atEnd;
    }

    private <P> boolean shortCircuitWith(
            Predicate2<? super V, ? super P> predicate,
            P parameter,
            boolean expected,
            boolean onShortCircuit,
            boolean atEnd) {
        for (int i = 0; i < table.length; i += 2) {
            if (table[i] == CHAINED_KEY) {
                Object[] chainedTable = (Object[]) table[i + 1];
                for (int j = 0; j < chainedTable.length; j += 2) {
                    if (chainedTable[j] != null) {
                        V value = (V) chainedTable[j + 1];
                        if (predicate.accept(value, parameter) == expected) {
                            return onShortCircuit;
                        }
                    }
                }
            } else if (table[i] != null) {
                V value = (V) table[i + 1];

                if (predicate.accept(value, parameter) == expected) {
                    return onShortCircuit;
                }
            }
        }

        return atEnd;
    }

    @Override
    public boolean anySatisfy(Predicate<? super V> predicate) {
        return shortCircuit(predicate, true, true, false);
    }

    @Override
    public <P> boolean anySatisfyWith(Predicate2<? super V, ? super P> predicate, P parameter) {
        return shortCircuitWith(predicate, parameter, true, true, false);
    }

    @Override
    public boolean allSatisfy(Predicate<? super V> predicate) {
        return shortCircuit(predicate, false, false, true);
    }

    @Override
    public <P> boolean allSatisfyWith(Predicate2<? super V, ? super P> predicate, P parameter) {
        return shortCircuitWith(predicate, parameter, false, false, true);
    }

    @Override
    public boolean noneSatisfy(Predicate<? super V> predicate) {
        return shortCircuit(predicate, true, false, true);
    }

    @Override
    public <P> boolean noneSatisfyWith(Predicate2<? super V, ? super P> predicate, P parameter) {
        return shortCircuitWith(predicate, parameter, true, false, true);
    }

    protected class KeySet implements Set<K>, Serializable, BatchIterable<K> {
        private static final long serialVersionUID = 1L;

        public boolean add(K key) {
            throw new UnsupportedOperationException("Cannot call add() on " + getClass().getSimpleName());
        }

        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException("Cannot call addAll() on " + getClass().getSimpleName());
        }

        public void clear() {
            UnifriedMap.this.clear();
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean containsAll(Collection<?> collection) {
            for (Object aCollection : collection) {
                if (!containsKey(aCollection)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return UnifriedMap.this.isEmpty();
        }

        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        public boolean remove(Object key) {
            int oldSize = occupied;
            UnifriedMap.this.remove(key);
            return occupied != oldSize;
        }

        public boolean removeAll(Collection<?> collection) {
            int oldSize = occupied;
            collection.forEach(UnifriedMap.this::remove);
            return oldSize != occupied;
        }

        public void putIfFound(Object key, Map<K, V> other) {
            int index = index(key);
            Object cur = table[index];
            if (cur != null) {
                Object val = table[index + 1];
                if (cur == CHAINED_KEY) {
                    putIfFoundFromChain((Object[]) val, (K) key, other);
                    return;
                }
                if (nonNullTableObjectEquals(cur, (K) key)) {
                    other.put((K) nonSentinel(cur), (V) val);
                }
            }
        }

        private void putIfFoundFromChain(Object[] chain, K key, Map<K, V> other) {
            for (int i = 0; i < chain.length; i += 2) {
                Object k = chain[i];
                if (k == null) {
                    return;
                }
                if (nonNullTableObjectEquals(k, key)) {
                    other.put((K) nonSentinel(k), (V) chain[i + 1]);
                }
            }
        }

        public boolean retainAll(Collection<?> collection) {
            int retainedSize = collection.size();
            UnifriedMap<K, V> retainedCopy = new UnifriedMap<>(retainedSize, loadFactor);
            for (Object key : collection) {
                putIfFound(key, retainedCopy);
            }
            if (retainedCopy.size() < size()) {
                maxSize = retainedCopy.maxSize;
                occupied = retainedCopy.occupied;
                table = retainedCopy.table;
                return true;
            }
            return false;
        }

        public int size() {
            return UnifriedMap.this.size();
        }

        public void forEach(Procedure<? super K> procedure) {
            forEachKey(procedure);
        }

        public int getBatchCount(int batchSize) {
            return UnifriedMap.this.getBatchCount(batchSize);
        }

        public void batchForEach(Procedure<? super K> procedure, int sectionIndex, int sectionCount) {
            Object[] map = table;
            int sectionSize = map.length / sectionCount;
            int start = sectionIndex * sectionSize;
            int end = sectionIndex == sectionCount - 1 ? map.length : start + sectionSize;
            if (start % 2 != 0) {
                start++;
            }
            for (int i = start; i < end; i += 2) {
                Object cur = map[i];
                if (cur == CHAINED_KEY) {
                    chainedForEachKey((Object[]) map[i + 1], procedure);
                } else if (cur != null) {
                    procedure.value((K) nonSentinel(cur));
                }
            }
        }

        protected void copyKeys(Object[] result) {
            Object[] table = UnifriedMap.this.table;
            int count = 0;
            for (int i = 0; i < table.length; i += 2) {
                Object x = table[i];
                if (x != null) {
                    if (x == CHAINED_KEY) {
                        Object[] chain = (Object[]) table[i + 1];
                        for (int j = 0; j < chain.length; j += 2) {
                            Object cur = chain[j];
                            if (cur == null) {
                                break;
                            }
                            result[count++] = nonSentinel(cur);
                        }
                    } else {
                        result[count++] = nonSentinel(x);
                    }
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Set) {
                Set<?> other = (Set<?>) obj;
                if (other.size() == size()) {
                    return containsAll(other);
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hashCode = 0;
            Object[] table = UnifriedMap.this.table;
            for (int i = 0; i < table.length; i += 2) {
                Object x = table[i];
                if (x != null) {
                    if (x == CHAINED_KEY) {
                        Object[] chain = (Object[]) table[i + 1];
                        for (int j = 0; j < chain.length; j += 2) {
                            Object cur = chain[j];
                            if (cur == null) {
                                break;
                            }
                            hashCode += cur == NULL_KEY ? 0 : cur.hashCode();
                        }
                    } else {
                        hashCode += x == NULL_KEY ? 0 : x.hashCode();
                    }
                }
            }
            return hashCode;
        }

        @Override
        public String toString() {
            return Iterate.makeString(this, "[", ", ", "]");
        }

        public Object[] toArray() {
            int size = UnifriedMap.this.size();
            Object[] result = new Object[size];
            copyKeys(result);
            return result;
        }

        public <T> T[] toArray(T[] result) {
            int size = UnifriedMap.this.size();
            if (result.length < size) {
                result = (T[]) Array.newInstance(result.getClass().getComponentType(), size);
            }
            copyKeys(result);
            if (size < result.length) {
                result[size] = null;
            }
            return result;
        }

        protected Object writeReplace() {
            UnifiedSet<K> replace = UnifiedSet.newSet(UnifriedMap.this.size());
            for (int i = 0; i < table.length; i += 2) {
                Object cur = table[i];
                if (cur == CHAINED_KEY) {
                    chainedAddToSet((Object[]) table[i + 1], replace);
                } else if (cur != null) {
                    replace.add((K) nonSentinel(cur));
                }
            }
            return replace;
        }

        private void chainedAddToSet(Object[] chain, UnifiedSet<K> replace) {
            for (int i = 0; i < chain.length; i += 2) {
                Object cur = chain[i];
                if (cur == null) {
                    return;
                }
                replace.add((K) nonSentinel(cur));
            }
        }
    }

    protected abstract class PositionalIterator<T> implements Iterator<T> {
        protected int count;
        protected int position;
        protected int chainPosition;
        protected boolean lastReturned;

        public final boolean hasNext() {
            return count < size();
        }

        public void remove() {
            if (!lastReturned) {
                throw new IllegalStateException("next() must be called as many times as remove()");
            }
            count--;
            occupied--;

            if (chainPosition != 0) {
                removeFromChain();
                return;
            }

            int pos = position - 2;
            Object cur = table[pos];
            if (cur == CHAINED_KEY) {
                removeLastFromChain((Object[]) table[pos + 1], pos);
                return;
            }
            table[pos] = null;
            table[pos + 1] = null;
            position = pos;
            lastReturned = false;
        }

        protected void removeFromChain() {
            Object[] chain = (Object[]) table[position + 1];
            int pos = chainPosition - 2;
            int replacePos = chainPosition;
            while (replacePos < chain.length - 2 && chain[replacePos + 2] != null) {
                replacePos += 2;
            }
            chain[pos] = chain[replacePos];
            chain[pos + 1] = chain[replacePos + 1];
            chain[replacePos] = null;
            chain[replacePos + 1] = null;
            chainPosition = pos;
            lastReturned = false;
        }

        protected void removeLastFromChain(Object[] chain, int tableIndex) {
            int pos = chain.length - 2;
            while (chain[pos] == null) {
                pos -= 2;
            }
            if (pos == 0) {
                table[tableIndex] = null;
                table[tableIndex + 1] = null;
            } else {
                chain[pos] = null;
                chain[pos + 1] = null;
            }
            lastReturned = false;
        }
    }

    protected class KeySetIterator extends PositionalIterator<K> {
        protected K nextFromChain() {
            Object[] chain = (Object[]) table[position + 1];
            Object cur = chain[chainPosition];
            chainPosition += 2;
            if (chainPosition >= chain.length
                    || chain[chainPosition] == null) {
                chainPosition = 0;
                position += 2;
            }
            lastReturned = true;
            return (K) nonSentinel(cur);
        }

        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException("next() called, but the iterator is exhausted");
            }
            count++;
            Object[] table = UnifriedMap.this.table;
            if (chainPosition != 0) {
                return nextFromChain();
            }
            while (table[position] == null) {
                position += 2;
            }
            Object cur = table[position];
            if (cur == CHAINED_KEY) {
                return nextFromChain();
            }
            position += 2;
            lastReturned = true;
            return (K) nonSentinel(cur);
        }
    }

    protected class EntrySet implements Set<Entry<K, V>>, Serializable, BatchIterable<Entry<K, V>> {

        private static final long serialVersionUID = 1L;

        private final transient WeakReference<UnifriedMap<K, V>> holder = new WeakReference<>(UnifriedMap.this);

        public boolean add(Entry<K, V> entry) {
            throw new UnsupportedOperationException("Cannot call add() on " + getClass().getSimpleName());
        }

        public boolean addAll(Collection<? extends Entry<K, V>> collection) {
            throw new UnsupportedOperationException("Cannot call addAll() on " + getClass().getSimpleName());
        }

        public final void clear() {
            UnifriedMap.this.clear();
        }

        public final boolean containsEntry(Entry<?, ?> entry) {
            return getEntry(entry) != null;
        }

        private Entry<K, V> getEntry(Entry<?, ?> entry) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            int index = index(key);

            Object[] tt = table;

            Object cur =        tt[index++];
            if (cur == null) {
                return null;
            }
            else {
                Object curValue = tt[index];

                if (cur == CHAINED_KEY) {
                    return chainGetEntry((Object[]) curValue, key, value);
                }
                if (nonNullTableObjectEquals(cur, key)) {
                    if (Objects.equals(value, curValue)) {
                        return ImmutableEntry.of((K) nonSentinel(cur), (V) curValue);
                    }
                }
                return null;
            }
        }

        private Entry<K, V> chainGetEntry(Object[] chain, Object key, Object value) {
            for (int i = 0; i < chain.length; i += 2) {
                Object cur = chain[i];
                if (cur == null) {
                    return null;
                }
                if (nonNullTableObjectEquals(cur, key)) {
                    Object curValue = chain[i + 1];
                    //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
                    if (Objects.equals(value, curValue)) {
                        return ImmutableEntry.of((K) nonSentinel(cur), (V) curValue);
                    }
                }
            }
            return null;
        }

        public boolean contains(Object o) {
            return o instanceof Entry && containsEntry((Entry<?, ?>) o);
        }

        public boolean containsAll(Collection<?> collection) {
            for (Object obj : collection) {
                if (!contains(obj)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return UnifriedMap.this.isEmpty();
        }

        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetIterator(holder);
        }

        public boolean remove(Object e) {
            if (!(e instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) e;
            K key = (K) entry.getKey();
            V value = (V) entry.getValue();

            int index = index(key);

            Object cur = table[index];
            if (cur != null) {
                Object val = table[index + 1];
                if (cur == CHAINED_KEY) {
                    return removeFromChain((Object[]) val, key, value, index);
                }
                //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
                if (nonNullTableObjectEquals(cur, key) && Objects.equals(value, val)) {
                    table[index] = null;
                    table[index + 1] = null;
                    occupied--;
                    return true;
                }
            }
            return false;
        }

        private boolean removeFromChain(Object[] chain, K key, V value, int index) {
            for (int i = 0; i < chain.length; i += 2) {
                Object k = chain[i];
                if (k == null) {
                    return false;
                }
                if (nonNullTableObjectEquals(k, key)) {
                    V val = (V) chain[i + 1];
                    //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
                    if (Objects.equals(val, value)) {
                        overwriteWithLastElementFromChain(chain, index, i);
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean removeAll(Collection<?> collection) {
            boolean changed = false;
            for (Object obj : collection) {
                if (remove(obj)) {
                    changed = true;
                }
            }
            return changed;
        }

        public boolean retainAll(Collection<?> collection) {
            int retainedSize = collection.size();
            UnifriedMap<K, V> retainedCopy = new UnifriedMap<>(retainedSize, loadFactor);

            collection.stream().filter(obj -> obj instanceof Entry).forEach(obj -> {
                Entry<?, ?> otherEntry = (Entry<?, ?>) obj;
                Entry<K, V> thisEntry = getEntry(otherEntry);
                if (thisEntry != null) {
                    retainedCopy.put(thisEntry.getKey(), thisEntry.getValue());
                }
            });
            if (retainedCopy.size() < size()) {
                maxSize = retainedCopy.maxSize;
                occupied = retainedCopy.occupied;
                table = retainedCopy.table;
                return true;
            }
            return false;
        }

        public final int size() {
            return UnifriedMap.this.size();
        }

        public final void forEach(Procedure<? super Entry<K, V>> procedure) {
            Object[] tt = table;
            int len = tt.length;

            for (int i = 0; i < len; i += 2) {

                Object cur = tt[i];
                if (cur == CHAINED_KEY) {
                    chainedForEachEntry((Object[]) tt[i + 1], procedure);
                } else if (cur != null) {
                    procedure.value(ImmutableEntry.of((K) nonSentinel(cur), (V) tt[i + 1]));
                }
            }
        }

        private void chainedForEachEntry(Object[] chain, Procedure<? super Entry<K, V>> procedure) {
            for (int i = 0; i < chain.length; i += 2) {
                Object cur = chain[i];
                if (cur == null) {
                    return;
                }
                procedure.value(ImmutableEntry.of((K) nonSentinel(cur), (V) chain[i + 1]));
            }
        }

        public final int getBatchCount(int batchSize) {
            return UnifriedMap.this.getBatchCount(batchSize);
        }

        public final void batchForEach(Procedure<? super Entry<K, V>> procedure, int sectionIndex, int sectionCount) {
            Object[] map = table;
            int sectionSize = map.length / sectionCount;
            int start = sectionIndex * sectionSize;
            int end = sectionIndex == sectionCount - 1 ? map.length : start + sectionSize;
            if (start % 2 != 0) {
                start++;
            }
            for (int i = start; i < end; i += 2) {
                Object cur = map[i];
                if (cur == CHAINED_KEY) {
                    chainedForEachEntry((Object[]) map[i + 1], procedure);
                } else if (cur != null) {
                    procedure.value(ImmutableEntry.of((K) nonSentinel(cur), (V) map[i + 1]));
                }
            }
        }

        protected void copyEntries(Object[] result) {
            Object[] table = UnifriedMap.this.table;
            int count = 0;
            for (int i = 0; i < table.length; i += 2) {
                Object x = table[i];
                if (x != null) {
                    if (x == CHAINED_KEY) {
                        Object[] chain = (Object[]) table[i + 1];
                        for (int j = 0; j < chain.length; j += 2) {
                            Object cur = chain[j];
                            if (cur == null) {
                                break;
                            }
                            result[count++] =
                                    new WeakBoundEntry<>((K) nonSentinel(cur), (V) chain[j + 1], holder);
                        }
                    } else {
                        result[count++] = new WeakBoundEntry<>((K) nonSentinel(x), (V) table[i + 1], holder);
                    }
                }
            }
        }

        public Object[] toArray() {
            Object[] result = new Object[UnifriedMap.this.size()];
            copyEntries(result);
            return result;
        }

        public <T> T[] toArray(T[] result) {
            int size = UnifriedMap.this.size();
            if (result.length < size) {
                result = (T[]) Array.newInstance(result.getClass().getComponentType(), size);
            }
            copyEntries(result);
            if (size < result.length) {
                result[size] = null;
            }
            return result;
        }

        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
//            in.defaultReadObject();
//            this.holder = new WeakReference<UnifriedMap<K, V>>(UnifriedMap.this);
            throw new RuntimeException("unimpl");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Set) {
                Set<?> other = (Set<?>) obj;
                if (other.size() == size()) {
                    return containsAll(other);
                }
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return UnifriedMap.this.hashCode();
        }
    }

    protected class EntrySetIterator extends PositionalIterator<Entry<K, V>> {
        private final WeakReference<UnifriedMap<K, V>> holder;

        protected EntrySetIterator(WeakReference<UnifriedMap<K, V>> holder) {
            this.holder = holder;
        }

        protected Entry<K, V> nextFromChain() {
            Object[] chain = (Object[]) table[position + 1];

            int chainPosition = this.chainPosition;
            Object cur = chain[chainPosition];
            Object value = chain[chainPosition + 1];
            chainPosition += 2;
            if (chainPosition >= chain.length
                    || chain[chainPosition] == null) {
                chainPosition = 0;
                position += 2;
            }
            this.chainPosition = chainPosition; //save
            lastReturned = true;
            return new WeakBoundEntry<>((K) nonSentinel(cur), (V) value, holder);
        }

        public Entry<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("next() called, but the iterator is exhausted");
            }
            count++;
            Object[] table = UnifriedMap.this.table;
            if (chainPosition != 0) {
                return nextFromChain();
            }
            while (table[position] == null) {
                position += 2;
            }
            Object cur = table[position];
            Object value = table[position + 1];
            if (cur == CHAINED_KEY) {
                return nextFromChain();
            }
            position += 2;
            lastReturned = true;
            return new WeakBoundEntry<>((K) nonSentinel(cur), (V) value, holder);
        }
    }

    protected static class WeakBoundEntry<K, V> implements Map.Entry<K, V> {
        protected final K key;
        protected V value;
        protected final WeakReference<UnifriedMap<K, V>> holder;

        protected WeakBoundEntry(K key, V value, WeakReference<UnifriedMap<K, V>> holder) {
            this.key = key;
            this.value = value;
            this.holder = holder;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            this.value = value;
            UnifriedMap<K, V> map = holder.get();
            if (map != null && map.containsKey(key)) {
                return map.put(key, value);
            }
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                Entry<?, ?> other = (Entry<?, ?>) obj;
                K otherKey = (K) other.getKey();
                V otherValue = (V) other.getValue();
                //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
                //        if (value == null) {
//            if (other == null) {
//                return true;
//            }
//        } else if (other == value || value.equals(other)) {
//            return true;
//        }
//        return false;
                return Objects.equals(key, otherKey)
                        && Objects.equals(value, otherValue);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    protected class ValuesCollection extends ValuesCollectionCommon<V>
            implements Serializable, BatchIterable<V> {
        private static final long serialVersionUID = 1L;

        public void clear() {
            UnifriedMap.this.clear();
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public boolean containsAll(Collection<?> collection) {
            // todo: this is N^2. if c is large, we should copy the values to a set.
            return Iterate.allSatisfy(collection, Predicates.in(this));
        }

        public boolean isEmpty() {
            return UnifriedMap.this.isEmpty();
        }

        public Iterator<V> iterator() {
            return new ValuesIterator();
        }

        public boolean remove(Object o) {
            // this is so slow that the extra overhead of the iterator won't be noticeable
            if (o == null) {
                for (Iterator<V> it = iterator(); it.hasNext(); ) {
                    if (it.next() == null) {
                        it.remove();
                        return true;
                    }
                }
            } else {
                for (Iterator<V> it = iterator(); it.hasNext(); ) {
                    V o2 = it.next();
                    if (o == o2 || o2.equals(o)) {
                        it.remove();
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean removeAll(Collection<?> collection) {
            // todo: this is N^2. if c is large, we should copy the values to a set.
            boolean changed = false;

            for (Object obj : collection) {
                if (remove(obj)) {
                    changed = true;
                }
            }
            return changed;
        }

        public boolean retainAll(Collection<?> collection) {
            boolean modified = false;
            Iterator<V> e = iterator();
            while (e.hasNext()) {
                if (!collection.contains(e.next())) {
                    e.remove();
                    modified = true;
                }
            }
            return modified;
        }

        public int size() {
            return UnifriedMap.this.size();
        }

        public void forEach(Procedure<? super V> procedure) {
            forEachValue(procedure);
        }

        public int getBatchCount(int batchSize) {
            return UnifriedMap.this.getBatchCount(batchSize);
        }

        public void batchForEach(Procedure<? super V> procedure, int sectionIndex, int sectionCount) {
            UnifriedMap.this.batchForEach(procedure, sectionIndex, sectionCount);
        }

        protected void copyValues(Object[] result) {
            int count = 0;
            for (int i = 0; i < table.length; i += 2) {
                Object x = table[i];
                if (x != null) {
                    if (x == CHAINED_KEY) {
                        Object[] chain = (Object[]) table[i + 1];
                        for (int j = 0; j < chain.length; j += 2) {
                            Object cur = chain[j];
                            if (cur == null) {
                                break;
                            }
                            result[count++] = chain[j + 1];
                        }
                    } else {
                        result[count++] = table[i + 1];
                    }
                }
            }
        }

        public Object[] toArray() {
            int size = UnifriedMap.this.size();
            Object[] result = new Object[size];
            copyValues(result);
            return result;
        }

        public <T> T[] toArray(T[] result) {
            int size = UnifriedMap.this.size();
            if (result.length < size) {
                result = (T[]) Array.newInstance(result.getClass().getComponentType(), size);
            }
            copyValues(result);
            if (size < result.length) {
                result[size] = null;
            }
            return result;
        }

        protected Object writeReplace() {
            FastList<V> replace = FastList.newList(UnifriedMap.this.size());
            for (int i = 0; i < table.length; i += 2) {
                Object cur = table[i];
                if (cur == CHAINED_KEY) {
                    chainedAddToList((Object[]) table[i + 1], replace);
                } else if (cur != null) {
                    replace.add((V) table[i + 1]);
                }
            }
            return replace;
        }

        private void chainedAddToList(Object[] chain, FastList<V> replace) {
            for (int i = 0; i < chain.length; i += 2) {
                Object cur = chain[i];
                if (cur == null) {
                    return;
                }
                replace.add((V) chain[i + 1]);
            }
        }

        @Override
        public String toString() {
            return Iterate.makeString(this, "[", ", ", "]");
        }
    }

    protected class ValuesIterator extends PositionalIterator<V> {
        protected V nextFromChain() {
            Object[] chain = (Object[]) table[position + 1];
            V val = (V) chain[chainPosition + 1];
            chainPosition += 2;
            if (chainPosition >= chain.length
                    || chain[chainPosition] == null) {
                chainPosition = 0;
                position += 2;
            }
            lastReturned = true;
            return val;
        }

        public V next() {
            if (!hasNext()) {
                throw new NoSuchElementException("next() called, but the iterator is exhausted");
            }
            count++;
            Object[] table = UnifriedMap.this.table;
            if (chainPosition != 0) {
                return nextFromChain();
            }
            while (table[position] == null) {
                position += 2;
            }
            Object cur = table[position];
            Object val = table[position + 1];
            if (cur == CHAINED_KEY) {
                return nextFromChain();
            }
            position += 2;
            lastReturned = true;
            return (V) val;
        }
    }


    private static Object nonSentinel(Object key) {
        return key == NULL_KEY ? null : key;
    }

    private static Object toSentinelIfNull(Object key) {
        if (key == null) {
            return NULL_KEY;
        }
        return key;
    }

    private static boolean nonNullTableObjectEquals(Object cur, Object key) {
        return cur == key || (cur == NULL_KEY ? key == null : cur.equals(key));
    }

    @Override
    public ImmutableMap<K, V> toImmutable() {
        return Maps.immutable.withAll(this);
    }
}

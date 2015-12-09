package nars.util.data.map;

import java.io.Serializable;
import java.util.*;

/**
 * Optimised for very small data sets, allowing compact size and fast puts at
 * the expense of O(n) lookups. This implementation may store duplicate entries
 * for the same key. TODO: explain more.
 *
 * @author The Stajistics Project
 */
public class FastPutsArrayMap<K, V> extends AbstractMap<K, V> implements Serializable {

    private static final int UNKNOWN_SIZE = 0; // Zero so that the size is unknown after deserialization

    protected final ArrayList<Entry<K, V>> entries;
    protected transient int size = UNKNOWN_SIZE;
    protected transient Set<Entry<K, V>> entrySet = null;

    public FastPutsArrayMap() {
        this(4);
    }

    public FastPutsArrayMap(int initialCapacity) {
        entries = new ArrayList<>(initialCapacity);
    }

    public FastPutsArrayMap(Map<K, V> map) {
        this(map.size());
        putAll(map);
    }

    public void compact() {

        // Iterate forwards looking for duplicate entries and deleting the old ones
        Entry<K, V> e;
        Entry<K, V> possibleDup;
        for (Iterator<Entry<K, V>> itr = entries.iterator(); itr.hasNext();) {
            e = itr.next();
            possibleDup = getEntry(e.getKey()); // Returns the newest entry
            if (possibleDup != null && e != possibleDup) {
                itr.remove();
            }
        }

        size = entries.size(); // We now know the size

        // Compact the underlying array
        entries.trimToSize();
    }

    @Override
    public int size() {
        if (entries.isEmpty()) {
            return 0;
        }

        if (size > 0) {
            return size;
        }

        compact();
        return size;
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean containsValue(Object aValue) {
        V value;
        if (aValue == null) {
            for (int i = entries.size() - 1; i >= 0; i--) {
                value = entries.get(i).getValue();
                if (value == null) {
                    return true;
                }
            }
        } else {
            for (int i = entries.size() - 1; i >= 0; i--) {
                value = entries.get(i).getValue();
                if (aValue.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    @Override
    public V get(Object key) {
        Entry<K, V> e = getEntry(key);
        if (e != null) {
            return e.getValue();
        }
        return null;
    }

    protected Entry<K, V> getEntry(Object key) {
        Entry<K, V> e;

        // Search in reverse so we find the most up to date value in the event of duplicates
        for (int i = entries.size() - 1; i >= 0; i--) {
            e = entries.get(i);
            if (keyEquals(e.getKey(), key)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Note: contract broken! Always returns null.
     */
    @Override
    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        entries.add(new SimpleEntry<>(key, value));
        size = UNKNOWN_SIZE;
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            return null;
        }

        Entry<K, V> e;
        V value = null;

        //TODO avoid ListIterator by just doing for loop then removal

        // Search in reverse so we find the most up to date value in the event of duplicates
        ListIterator<Entry<K, V>> itr = entries.listIterator(entries.size());
        while (itr.hasPrevious()) {
            e = itr.previous();
            if (keyEquals(e.getKey(), key)) {
                if (value == null) {
                    value = e.getValue(); // Keep the newest value
                }
                itr.remove();
                size = UNKNOWN_SIZE;
                // Keep searching for duplicates
            }
        }

        return value;
    }

    public boolean keyEquals(K a, Object b) {
        return a.equals(b);
    }

    @Override
    public void clear() {
        entries.clear();
        size = 0;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new AbstractSet<Entry<K, V>>() {
                @Override
                public Iterator<Entry<K, V>> iterator() {
                    compact();
                    return entries.iterator();
                }

                @Override
                public int size() {
                    return FastPutsArrayMap.this.size();
                }
            };
        }

        return entrySet;
    }
}

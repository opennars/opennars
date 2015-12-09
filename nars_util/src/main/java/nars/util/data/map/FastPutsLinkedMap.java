package nars.util.data.map;

import java.io.Serializable;
import java.util.*;

/**
 * @author The Stajistics Project
 */
public class FastPutsLinkedMap<K, V> extends AbstractMap<K, V> implements Serializable {

    private final transient LinkedEntry<K, V> header = new LinkedEntry<>(null, null, null, null);

    {
        header.next = header;
        header.prev = header;
    }

    private transient int size = 0;
    private transient Set<Map.Entry<K, V>> entrySet = null;

    public FastPutsLinkedMap() {
    }

    public FastPutsLinkedMap(Map<? extends K, ? extends V> map) {
        putAll(map);
    }

    public void compact() {
        int size = 0;

        LinkedEntry<K, V> entry = header.next;
        LinkedEntry<K, V> newerEntry;
        LinkedEntry<K, V> next;
        while (entry != header) {
            size++;
            next = entry.next;
            newerEntry = getEntry(entry.getKey());
            if (newerEntry != null && newerEntry != entry) {
                remove(entry);
                size--;
            }
            entry = next;
        }

        this.size = size;
    }

    @Override
    public void clear() {
        header.next = header.prev = header;
        size = 0;
    }

    @Override
    public boolean isEmpty() {
        return header.prev == header && header.next == header;
    }

    @Override
    public int size() {
        if (isEmpty()) {
            return 0;
        }

        if (size > 0) {
            return size;
        }

        compact(); // Updates size

        return size;
    }

    @Override
    public V get(Object key) {
        LinkedEntry<K, V> entry = getEntry(key);
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        size = 0; // reset
        LinkedEntry<K, V> newEntry = new LinkedEntry<>(key, value, header, header.prev);
        newEntry.prev.next = newEntry;
        newEntry.next.prev = newEntry;
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V remove(Object key) {
        LinkedEntry<K, V> entry = getEntry(key);
        if (entry != null) {
            size = 0; // reset
            V value = entry.getValue();
            do {
                remove(entry);
                entry = getEntry(key);
            } while (entry != null);
            return value;
        }
        return null;
    }

    protected LinkedEntry<K, V> getEntry(Object key) {
        LinkedEntry<K, V> entry = header.prev;
        while (entry != header) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
            entry = entry.prev;
        }

        return null;
    }

    private void remove(LinkedEntry<K, V> e) {
        if (e == header) {
            throw new NoSuchElementException();
        }
        e.prev.next = e.next;
        e.next.prev = e.prev;
        e.next = e.prev = null;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new AbstractSet<Map.Entry<K, V>>() {
                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    compact();

                    List<Map.Entry<K, V>> list = new ArrayList<>(size());
                    for (LinkedEntry<K, V> entry = header.next; entry != null && entry != header; entry = entry.next) {
                        list.add(entry);
                    }
                    return list.iterator();
                }

                @Override
                public int size() {
                    return FastPutsLinkedMap.this.size();
                }
            };
        }

        return entrySet;
    }

    /* NESTED CLASSES */
    public static final class LinkedEntry<K, V> extends SimpleEntry<K, V> {

        private transient LinkedEntry<K, V> next;
        private transient LinkedEntry<K, V> prev;

        private LinkedEntry(K key, V value, LinkedEntry<K, V> next, LinkedEntry<K, V> prev) {
            super(key, value);
            this.next = next;
            this.prev = prev;
        }
    }
}

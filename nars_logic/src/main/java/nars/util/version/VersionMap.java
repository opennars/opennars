package nars.util.version;

import org.jgrapht.util.ArrayUnenforcedSet;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


public final class VersionMap<X,Y> extends AbstractMap<X, Y>  {

    private final Versioning context;
    private final Map<X, Versioned<Y>> map;

    public VersionMap(Versioning context) {
        this(context, new LinkedHashMap<>());
    }

    public VersionMap(Versioning context, Map<X, Versioned<Y>/*<Y>*/> map) {
        super();
        this.context = context;
        this.map = map;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public Y remove(Object key) {
        Versioned<Y> x = map.remove(key);
        if (x == null) return null;
        return x.get();
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new RuntimeException("unimpl yet");
    }

    @Override
    public void clear() {
        throw new RuntimeException("unimpl yet");
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** avoid using this if possible because it involves transforming the entries from the internal map to the external form */
    @Override public Set<Entry<X, Y>> entrySet() {
        ArrayUnenforcedSet<Entry<X,Y>> e = new ArrayUnenforcedSet<>(size());
        map.forEach( (k, v) -> {
            Y vv = v.get();
            e.add(new AbstractMap.SimpleEntry<>(k, vv));
        });
        return e;
    }


    /**
     * records an assignment operation
     * follows semantics of set()
     */
    public Y put(X key, Y value) {
        getOrCreateIfAbsent(key).set(value);
        return null;
    }

    /** follows semantics of thenSet() */
    public Versioning thenPut(X key, Y value) {
        getOrCreateIfAbsent(key).thenSet(value);
        return context;
    }

    public Versioned getOrCreateIfAbsent(X key) {
        return map.computeIfAbsent(key,
                RemovingVersionedEntry::new);
    }

    /** this implementation removes itself from the map when it is reverted to
     *  times prior to its appearance in the map */
    final class RemovingVersionedEntry extends Versioned<Y> {

        private final X key;

        public RemovingVersionedEntry(X key) {
            super(context);
            this.key = key;
        }

        @Override
        boolean revertNext(int before) {
            boolean v = super.revertNext(before);
            if (size == 0)
                removeFromMap();
            return v;
        }

        public void clear() {
            super.clear();
            removeFromMap();
        }

        private void removeFromMap() {
            VersionMap.this.remove(key);
        }
    }


    public final Y get(/*X*/Object key) {
        Versioned<Y> v = version((X) key);
        if (v!=null) return v.get();
        return null;
    }

    public Y get(X key, Supplier<Y> ifAbsentPut) {
        //TODO use compute... Map methods
        Y o = get(key);
        if (o == null) {
            o = ifAbsentPut.get();
            put(key, o);
        }
        return o;
    }

    public final Versioned<Y> version(X key) {
        return map.computeIfPresent(key, (k, v) -> {
            if (v == null || v.isEmpty())
                return null;
            return v;
        });
    }
}

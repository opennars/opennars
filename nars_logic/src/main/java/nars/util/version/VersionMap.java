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
        this(context, new LinkedHashMap<X, Versioned<Y>>());
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
            e.add(new AbstractMap.SimpleEntry<>(k, v.get()));
        });
        return e;
    }


    /**
     * records an assignment operation
     */
    public Y put(X key, Y value) {
        Versioned v = map.computeIfAbsent(key,
                (k) -> new Versioned(context));
        v.set(value);
        return null;
    }

    public Versioning putWith(X key, Y value) {
        Versioned v = map.computeIfAbsent(key,
                (k) -> new Versioned(context));
        v.set(value);
        return context;
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

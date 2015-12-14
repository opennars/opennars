package nars.bag.impl;


import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;


public class MapCacheBag<V> extends AbstractCacheBag<V>  {

    public final Map<V,V> data;

    public MapCacheBag(Map<V,V> data) {
        this.data = data;
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public V get(V key) {
        return data.get(key);
    }

    @Override
    public Object remove(V key) {
        return data.remove(key);
    }

    @Override
    public V put(V k) {
        return data.put(k, k);
    }

    @Override
    public int size() {
        return data.size();
    }

    public Iterator<V> iterator() {
        return data.values().iterator();
    }

    /** provides a direct method to forEach,
     * in case the map implements a better
     * alternative to iterator()'s default */
    public void forEach(Consumer<? super V> consumer) {
        data.forEach((k,v) -> consumer.accept(v));
    }

}

package nars.db;


import nars.bag.impl.CacheBag;
import nars.nal.Itemized;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by me on 6/2/15.
 */
public class HazelCacheBag<K, V extends Itemized<K>> extends CacheBag<K,V>  {

    private final Map<K, V> concepts;
    private final String userID;

    public HazelCacheBag(String userID, String channel) {

        this.userID = userID;

        concepts = null;//instance.getMap(channel);


    }
    @Override
    public void clear() {
        //throw new RuntimeException("unable to clear() shared concept bag " + concepts);
    }

    @Override
    public V get(K key) {
        return concepts.get(key);
    }

    @Override
    public V take(K key) {
        return concepts.remove(key);
    }

    @Override
    public void put(V v) {

        concepts.put(v.name(), v);
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Iterator<V> iterator() {
        return concepts.values().iterator();
    }

    @Override
    public String toString() {
        return super.toString() + ":" + userID + "x" + concepts.size();
    }
}

package nars.bag.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.util.Iterator;

/**
 * Index of stored Items (ex: concepts) which is optimized for
 * random access storage and retrieval and not prioritized active processing.
 * not really a bag, maybe it should be called ItemCache
 * 
 * http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/cache/package-summary.html*
 */
public class GuavaCacheBag<K,V> extends AbstractCacheBag<K,V> implements RemovalListener<K,V>, Iterable<V> {

    public final Cache<K, V> data;
    //public final Observed<V> removed = new Observed();


    public static <K,V> GuavaCacheBag<K,V> make(int maxSize) {
        return new GuavaCacheBag(CacheBuilder.newBuilder()
                .maximumSize(maxSize));
    }


    public static <K,V> GuavaCacheBag<K,V> makeSoftValues() {
        return new GuavaCacheBag(CacheBuilder.newBuilder()

                //.maximumSize(capacity)

                .softValues());



                //.expireAfterWrite(10, TimeUnit.MINUTES)
               /*.weakVeys()
               .weakValues()
               .weigher(null)*/


    }


    public GuavaCacheBag(CacheBuilder<K, V> data) {

        data.removalListener(this);
        this.data = data.build();

    }


    /** empty contents */
    @Override
    public void clear() {
        data.invalidateAll();
        data.cleanUp();
    }


    @Override
    public final V get(Object key) {
        if (key == null)
            throw new RuntimeException("null");
        return data.getIfPresent(key);
    }

    @Override
    public V remove(K key) {
        Cache<K,V> d = data;
        V v = d.getIfPresent(key);
        if (v !=null) {
            d.invalidate(v);
            d.cleanUp();
            return v;
        }
        return null;
    }

    @Override
    public V put(K k, V v) {
        if (v == null)
            throw new RuntimeException("null");
        data.put(k, v);
        return null; //assume it was inserted
    }

    @Override
    public int size() {
        return (int) data.size();
    }

    @Override
    public void onRemoval(RemovalNotification<K, V> rn) {
        //RemovalCause cause = rn.getCause();

//        if (cause==RemovalCause.SIZE || cause==RemovalCause.COLLECTED) {
//            V v = rn.getValue();
//            /*if (v!=null)
//                getOnRemoval().accept(v);*/
//        }
    }



    @Override
    public Iterator<V> iterator() {
        return data.asMap().values().iterator();
    }
}

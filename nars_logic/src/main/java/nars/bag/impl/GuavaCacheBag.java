package nars.bag.impl;

import com.google.common.cache.*;
import nars.budget.Itemized;

import java.util.Iterator;

/**
 * Index of stored Items (ex: concepts) which is optimized for
 * random access storage and retrieval and not prioritized active processing.
 * not really a bag, maybe it should be called ItemCache
 * 
 * http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/cache/package-summary.html*
 */
public class GuavaCacheBag<K, V extends Itemized<K>> extends AbstractCacheBag<K, V> implements RemovalListener<K, V>, Iterable<V> {

    public final Cache<K, V> data;
    //public final Observed<V> removed = new Observed();


    public static <K,V extends Itemized<K>> GuavaCacheBag<K,V> make(int maxSize) {
        return new GuavaCacheBag(CacheBuilder.newBuilder()
                .maximumSize(maxSize));
    }


    public static <K,V extends Itemized<K>> GuavaCacheBag<K,V> makeSoftValues() {
        return new GuavaCacheBag(CacheBuilder.newBuilder()

                //.maximumSize(capacity)

                .softValues());



                //.expireAfterWrite(10, TimeUnit.MINUTES)
               /*.weakKeys()
               .weakValues()
               .weigher(null)*/


    }

    public GuavaCacheBag(CacheBuilder<K, V> data) {
        super();

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
    public V get(K key) {
        return data.getIfPresent(key);
    }

    @Override
    public V remove(K key) {
        V v = data.getIfPresent(key);
        if (v !=null) {
            data.invalidate(v);
            data.cleanUp();
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
        RemovalCause cause = rn.getCause();

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

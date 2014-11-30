package nars.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import nars.entity.Item;

/**
 * Index of stored Items (ex: concepts) which is optimized for
 * random access storage and retrieval and not prioritized active processing.
 * 
 http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/cache/package-summary.html* 
 */
public class CacheBag<K, I extends Item<K>> implements RemovalListener<K,I> {
    public final Cache<K, I> data;

    public CacheBag(int capacity) {
        
       data = CacheBuilder.newBuilder()
            .maximumSize(capacity)
            //.expireAfterWrite(10, TimeUnit.MINUTES)
               /*.weakKeys()
               .weakValues()
               .weigher(null)*/
        .removalListener(this)
        .build();
    }
    
    public I get(K key) {
        return data.getIfPresent(key);
    }
    
    public I take(K key) {
        I i = data.getIfPresent(key);
        if (i!=null) {
            data.invalidate(i);
            data.cleanUp();
            return i;
        }
        return null;
    }
    
    public void add(I i) {        
        data.put(i.name(), i);        
    }

    public long size() {
        return data.size();
    }

    @Override
    public void onRemoval(RemovalNotification<K, I> rn) {
        if (rn.getCause()==RemovalCause.SIZE)
            rn.getValue().end();
    }
    
    
}

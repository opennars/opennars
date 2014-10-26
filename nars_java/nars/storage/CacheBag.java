package nars.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import nars.entity.Item;

/**
 * Index of stored Items (ex: concepts) which is optimized for
 * random access storage and retrieval and not prioritized active processing.
 * 
 http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/cache/package-summary.html* 
 */
public class CacheBag<K, I extends Item<K>> {
    public final Cache<K, I> data;

    public CacheBag(int capacity) {
        
       data = CacheBuilder.newBuilder()
            .maximumSize(capacity)
            //.expireAfterWrite(10, TimeUnit.MINUTES)
               /*.weakKeys()
               .weakValues()
               .weigher(null)*/
        //.removalListener(MY_LISTENER)
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
    
    
}

package nars.util.data.map;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides a size-limited map of keys to values.
 * <p>
 * When the maximum size limit has been reached, the least-recently accessed entry will be removed whenever a new entry is added.
 *
 * @param <K>
 *           the type of keys maintained by this map
 * @param <V>
 *           the type of mapped values
 *
 * from: https://github.com/webber-s/oakgp/blob/master/src/main/java/org/oakgp/util/CacheMap.java
 */
public final class FixedSizeMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;


    /**
     * @param maxSize
     *           the maximum size restriction to enforce on the returned map
     * @see #createCache(int)
     */
    private FixedSizeMap(int maxSize) {
        super(maxSize, 0.1f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

//    /**
//     * Returns a size-limited map of keys to values.
//     *
//     * @param maxSize
//     *           the maximum size restriction to enforce on the returned map
//     * @param <K>
//     *           the type of keys maintained by this map
//     * @param <V>
//     *           the type of mapped values
//     * @return a size-limited map of keys to values
//     */
//    public static <K, V> Map<K, V> createCache(int maxSize) {
//        CacheMap<K, V> m = new CacheMap<>(maxSize);
//        return Collections.synchronizedMap(m);
//    }

}
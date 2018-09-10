package org.opennars.util;

import org.opennars.language.Term;

import java.util.HashMap;
import java.util.Map;

/**
 * Optimized Map&lt;Term,Term&gt;
 */
public class FastTermTermMap {
    protected Map<Term, Term> map = new HashMap<>();

    protected CachedElementByKey[] cachedEntries;

    public FastTermTermMap() {
        cachedEntries = new CachedElementByKey[2];
    }

    public void put(final Term key, final Term value) {
        for(int cacheIdx=0;cacheIdx<cachedEntries.length;cacheIdx++) {
            if (cachedEntries[cacheIdx] == null) {
                // the slot is free, this means that we can cache here
                cachedEntries[cacheIdx] = new CachedElementByKey(key, value);

                // we return because we put it already into the cache and there is no reason to put it into the slowpath map
                return;
            }
            else {
                // the slot is already used, we need to check if we have to override the value if keys match
                if (key.equals(cachedEntries[cacheIdx].key)) {
                    cachedEntries[cacheIdx] = new CachedElementByKey(key, value);

                    // we return because we stored the new value
                    return;
                }
            }
        }

        map.put(key, value);
    }

    public boolean containsKey(final Term key) {
        // first we check in the cache
        for (final CachedElementByKey iCached : cachedEntries) {
            if (iCached == null) {
                continue;
            }

            if (iCached.key.equals(key)) {
                return true;
            }
        }

        // slow path
        return map.containsKey(key);
    }

    public Term get(final Term key) {
        // first we check in the cache
        for (final CachedElementByKey iCached : cachedEntries) {
            if (iCached == null) {
                continue;
            }

            if (iCached.key.equals(key)) {
                return iCached.value;
            }
        }

        // slow path
        return map.get(key);
    }

    public boolean isEmpty() {
        for (final CachedElementByKey iCached : cachedEntries) {
            if (iCached == null) {
                continue;
            }

            return false;
        }

        return map.isEmpty();
    }

    public int size() {
        int elementsInCache = 0;

        for (final CachedElementByKey iCached : cachedEntries) {
            if (iCached == null) {
                continue;
            }

            elementsInCache++;
        }

        return map.size() + elementsInCache;
    }

    public void clear() {
        map.clear();

        cachedEntries = new CachedElementByKey[2];
    }

    public void merge(final FastTermTermMap source) {
        // TODO< optimize >

        // we need to invalidate the cache because source may override some cached entries and we don't want to pay for the extra logic
        invalidateCache();

        for(final Term c : source.map.keySet()) {
            map.put(c, source.get(c));
        }

        // we need to add the cachedEntries of source too
        for(final CachedElementByKey iCachedOfSource : source.cachedEntries) {
            if (iCachedOfSource != null) {
                map.put(iCachedOfSource.key, iCachedOfSource.value);
            }
        }
    }

    /**
     * invalidates the cache
     */
    private void invalidateCache() {
        // move all cached values into the "real" map
        for(final CachedElementByKey iCachedOfSource : cachedEntries) {
            if (iCachedOfSource != null) {
                map.put(iCachedOfSource.key, iCachedOfSource.value);
            }
        }

        cachedEntries = new CachedElementByKey[2];
    }

    private static class CachedElementByKey {
        public final Term key;
        public final Term value;

        public CachedElementByKey(final Term key, final Term value) {
            this.key = key;
            this.value = value;
        }
    }
}

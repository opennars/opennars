package org.opennars.util;

import org.opennars.language.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Optimized Map&lt;Term,Term&gt;
 */
public class FastTermTermMap {
    protected Map<Term, Term> map = new HashMap<>();

    protected List<CachedElementByKey> cachedEntries = new ArrayList<>();

    public FastTermTermMap() {
    }

    public void put(final Term key, final Term value) {
        for(int cacheIdx=0;cacheIdx<cachedEntries.size();cacheIdx++) {
            // the slot is already used, we need to check if we have to override the value if keys match
            if (key.equalsFast(cachedEntries.get(cacheIdx).key)) {
                cachedEntries.set(cacheIdx, new CachedElementByKey(key, value));

                // we return because we stored the new value
                return;
            }
        }

        // we can cache it if there is any space left to cache
        if (cachedEntries.size() < 2) {
            cachedEntries.add(new CachedElementByKey(key, value));
            return;
        }

        map.put(key, value);
    }

    public boolean containsKey(final Term key) {
        // first we check in the cache
        for (final CachedElementByKey iCached : cachedEntries) {
            if (iCached.key.equalsFast(key)) {
                return true;
            }
        }

        // slow path
        return map.containsKey(key);
    }

    public Term get(final Term key) {
        // first we check in the cache
        for (final CachedElementByKey iCached : cachedEntries) {
            if (iCached.key.equalsFast(key)) {
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
        return map.size() + cachedEntries.size();
    }

    public void clear() {
        map.clear();

        cachedEntries = new ArrayList<>();
    }

    public void merge(final FastTermTermMap source) {
        // TODO< optimize >

        for (CachedElementByKey iCachedEntry : source.cachedEntries) {
            put(iCachedEntry.key, iCachedEntry.value);
        }

        for(final Term c : source.map.keySet()) {
            put(c, source.get(c));
        }
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

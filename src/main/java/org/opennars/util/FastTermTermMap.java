package org.opennars.util;

import org.opennars.language.Term;

import java.util.HashMap;
import java.util.Map;

/**
 * Optimized Map&lt;Term,Term&gt;
 */
public class FastTermTermMap {
    protected Map<Term, Term> map = new HashMap<>();

    /** bitset to accelerate the primary check of a key of the map */
    protected long usedBitset = 0;

    public void put(final Term key, final Term value) {
        map.put(key, value);
        setBitsetByKey(key);
    }

    public boolean containsKey(final Term key) {
        // fail fast test
        // it can't contain the key if the bitset is false for this key
        if (!checkBitsetByKey(key)) {
            return false;
        }

        // slow path
        return map.containsKey(key);
    }

    public Term get(final Term key) {
        return map.get(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
        usedBitset = 0;
    }

    public void merge(final FastTermTermMap source) {
        // TODO< optimize bitset >

        for(final Term c : source.map.keySet()) {
            put(c, source.get(c));
        }
    }

    protected void setBitsetByKey(final Term key) {
        usedBitset |= (1 << (key.hashCode() % 64));
    }

    protected boolean checkBitsetByKey(final Term key) {
        return (usedBitset & (1 << (key.hashCode() % 64))) != 0;
    }
}

package objenome.util.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Map with a fixed size (no resize possible) so this implementation is threadsafe.
 * 
 * @author Peter Fichtner
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class FixedMap<K, V> implements Serializable {

    private static final long serialVersionUID = 1147573724856141127L;

    private final Map<K, V> map;

    public FixedMap(final Collection<K> keys) {
        this.map = new HashMap<>(keys.size(), 1.0f);
        for (final K key : keys) {
            // init all keys using null value so all keys exists (and beside the map guaranteed has
            // the correct size)
            this.map.put(key, null);
        }
    }

    public FixedMap(final FixedMap<K, V> fixedMap) {
        this.map = new HashMap<>(fixedMap.map);
    }

    public void put(final K key, final V value) {
        // to prevent set of keys that where not defined (will lead to HashMap resize!) in the
        // constructor we'll check here
        checkValidKey(key);
        this.map.put(key, value);
    }

    public Object get(final K key) {
        // we COULD check here (like in put) if the key is valid but we don't
        // checkValidKey(key);
        return this.map.get(key);
    }

    private void checkValidKey(final K key) {
        if (!this.map.containsKey(key)) {
            throw new IllegalStateException("Key " + key //$NON-NLS-1$
                    + " was not defined to be added during constructor call"); //$NON-NLS-1$
        }
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        return this.map.equals(((FixedMap<?, ?>) obj).map);
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

}

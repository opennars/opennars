//package nars.util;
//
//import java.lang.ref.Reference;
//import java.lang.ref.ReferenceQueue;
//import java.lang.ref.SoftReference;
//import java.util.AbstractMap;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Function;
//
//MAYBE BROKE
///* /***************************************
// *                                     *
// *  JBoss: The OpenSource J2EE WebOS   *
// *                                     *
// *  Distributable under LGPL license.  *
// *  See terms of license at gnu.org.   *
// *                                     *
// ***************************************/
//
//
///**
// * This Map will remove entries when the value in the map has been
// * cleaned from garbage collection
// *
// * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
// * @version <tt>$Revision: 1.4 $</tt>
// */
//public class WeakValueHashMap extends AbstractMap {
//
//    private static class WeakValueRef extends SoftReference {
//        public final Object key;
//
//        private WeakValueRef(Object key, Object val, ReferenceQueue q) {
//            super(val, q);
//            this.key = key;
//        }
//
//        private static WeakValueRef create(Object key, Object val, ReferenceQueue q) {
//            if (val == null) return null;
//            else return new WeakValueRef(key, val, q);
//        }
//
//    }
//
//    @Override
//    public Set entrySet() {
//        processQueue();
//        return hash.entrySet();
//    }
//
//    /* Hash table mapping WeakKeys to values */
//    private final HashMap hash;
//
//    /* Reference queue for cleared WeakKeys */
//    private final ReferenceQueue queue = new ReferenceQueue();
//
//    /* Remove all invalidated entries from the map, that is, remove all entries
//       whose values have been discarded.
//     */
//    private void processQueue() {
//        WeakValueRef ref;
//        ReferenceQueue q = this.queue;
//        HashMap h = this.hash;
//
//        while ((ref = (WeakValueRef) q.poll()) != null) {
//            final Object rref = ref;
//            h.computeIfPresent(ref.key, (kk, existingRef) -> {
//                // only remove if it is the *exact* same WeakValueRef
//                if (rref == existingRef)
//                    return null; //remove entry
//                return existingRef; //keep
//            });
//        }
//    }
//
//
//   /* -- Constructors -- */
//
//    /**
//     * Constructs a new, empty <code>WeakHashMap</code> with the given
//     * initial capacity and the given load factor.
//     *
//     * @param initialCapacity The initial capacity of the
//     *                        <code>WeakHashMap</code>
//     * @param loadFactor      The load factor of the <code>WeakHashMap</code>
//     * @throws IllegalArgumentException If the initial capacity is less than
//     *                                  zero, or if the load factor is
//     *                                  nonpositive
//     */
//    public WeakValueHashMap(int initialCapacity, float loadFactor) {
//        hash = new HashMap(initialCapacity, loadFactor);
//    }
//
//    /**
//     * Constructs a new, empty <code>WeakHashMap</code> with the given
//     * initial capacity and the default load factor, which is
//     * <code>0.75</code>.
//     *
//     * @param initialCapacity The initial capacity of the
//     *                        <code>WeakHashMap</code>
//     * @throws IllegalArgumentException If the initial capacity is less than
//     *                                  zero
//     */
//    public WeakValueHashMap(int initialCapacity) {
//        hash = new HashMap(initialCapacity);
//    }
//
//    /**
//     * Constructs a new, empty <code>WeakHashMap</code> with the default
//     * initial capacity and the default load factor, which is
//     * <code>0.75</code>.
//     */
//    public WeakValueHashMap() {
//        hash = new HashMap();
//    }
//
//    /**
//     * Constructs a new <code>WeakHashMap</code> with the same mappings as the
//     * specified <tt>Map</tt>.  The <code>WeakHashMap</code> is created with an
//     * initial capacity of twice the number of mappings in the specified map
//     * or 11 (whichever is greater), and a default load factor, which is
//     * <tt>0.75</tt>.
//     *
//     * @param t the map whose mappings are to be placed in this map.
//     * @since 1.3
//     */
//    public WeakValueHashMap(Map t) {
//        this(Math.max(2 * t.size(), 11), 0.75f);
//        putAll(t);
//    }
//
//   /* -- Simple queries -- */
//
//    /**
//     * Returns the number of key-value mappings in this map.
//     * <strong>Note:</strong> <em>In contrast with most implementations of the
//     * <code>Map</code> interface, the time required by this operation is
//     * linear in the size of the map.</em>
//     */
//    @Override
//    public int size() {
//        processQueue();
//        return hash.size();
//    }
//
//    /**
//     * Returns <code>true</code> if this map contains no key-value mappings.
//     */
//    @Override
//    public boolean isEmpty() {
//        processQueue();
//        return hash.isEmpty();
//    }
//
//    /**
//     * Returns <code>true</code> if this map contains a mapping for the
//     * specified key.
//     *
//     * @param key The key whose presence in this map is to be tested
//     */
//    @Override
//    public boolean containsKey(Object key) {
//        processQueue();
//        return hash.containsKey(key);
//    }
//
//   /* -- Lookup and modification operations -- */
//
//    /**
//     * Returns the value to which this map maps the specified <code>key</code>.
//     * If this map does not contain a value for this key, then return
//     * <code>null</code>.
//     *
//     * @param key The key whose associated value, if any, is to be returned
//     */
//    @Override
//    public Object get(Object key) {
//        processQueue();
//        Reference ref = (Reference) hash.get(key);
//        if (ref != null) return ref.get();
//        return null;
//    }
//
//    @Override
//    public Object computeIfAbsent(Object key, Function mappingFunction) {
//        processQueue();
//
//        Object v, vv;
//        if ((v = hash.get(key)) != null) {
//            vv = ((Reference) v).get();
//            if (vv != null)
//                return vv;
//        }
//
//        Object newValue;
//        if ((newValue = mappingFunction.apply(key)) != null) {
//            hash.put(WeakValueRef.create(newValue, newValue, queue), newValue);
//            return newValue;
//        }
//
//        return null;
//    }
//
//    /**
//     * Updates this map so that the given <code>key</code> maps to the given
//     * <code>value</code>.  If the map previously contained a mapping for
//     * <code>key</code> then that mapping is replaced and the previous value is
//     * returned.
//     *
//     * @param key   The key that is to be mapped to the given
//     *              <code>value</code>
//     * @param value The value to which the given <code>key</code> is to be
//     *              mapped
//     * @return The previous value to which this key was mapped, or
//     * <code>null</code> if if there was no mapping for the key
//     */
//    @Override
//    public Object put(Object key, Object value) {
//        processQueue();
//        Object rtn = _put(key, value);
//        if (rtn != null) rtn = ((Reference) rtn).get();
//        return rtn;
//    }
//
//    private Object _put(Object key, Object value) {
//        return hash.computeIfAbsent(key, (k) -> {
//                return WeakValueRef.create(k, value, queue);
//            });
//    }
//
//    /**
//     * Removes the mapping for the given <code>key</code> from this map, if
//     * present.
//     *
//     * @param key The key whose mapping is to be removed
//     * @return The value to which this key was mapped, or <code>null</code> if
//     * there was no mapping for the key
//     */
//    @Override
//    public Object remove(Object key) {
//        processQueue();
//        return hash.remove(key);
//    }
//
//    /**
//     * Removes all mappings from this map.
//     */
//    @Override
//    public void clear() {
//        processQueue();
//        hash.clear();
//    }
// }

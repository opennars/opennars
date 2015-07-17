package nars.util;

import nars.Global;
import nars.bag.impl.CurveBag;
import nars.budget.Item;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** adapter to a Map for coordinating changes in a Map with another Collection */
public abstract class CollectorMap<K, E extends Item<K>> {


    public final Map<K, E> map;



    public CollectorMap(Map<K, E> map) {
        this.map = map;
    }

    /** implementation for adding the value to another collecton (called internally)  */
    abstract protected E addItem(final E e);

    /** implementation for removing the value to another collecton (called internally) */
    abstract protected E removeItem(final E e);


    public E put(final E value) {


        E removed, removed2;

        /*synchronized (nameTable)*/
        {

            final K key = value.name();
            removed = putKey(key, value);
            if (removed != null) {

                E remd = removeItem(removed);

                if (remd == null) {
                    throw new RuntimeException("unable to remove item corresponding to key " + key);
                }
            }


            removed2 = addItem(value);

            if (removed != null && removed2 != null) {
                throw new RuntimeException("Only one item should have been removed on this insert; both removed: " + removed + ", " + removed2);
            }
            if ((removed2 != null) && (!removed2.name().equals(key))) {
                removeKey(removed2.name());
                removed = removed2;
                //return removed2;
            }
        }

        if (Global.DEBUG && Global.DEBUG_BAG)  size();

        return removed;
    }

    public E remove(final K key) {

        E e = removeKey(key);
        if (e != null) {
            E removed = removeItem(e);
            if (removed == null)
                throw new RuntimeException(key + " removed from index but not from items list");
            if (removed!=e)
                throw new RuntimeException(key + " removed " + e + " but item removed was " + removed);
        }

        return e;
    }


    public E removeKey(final K key) {
        E e = map.remove(key);
        return e;
    }


    public int size() {
        return map.size();
    }

    public boolean containsValue(E it) {
        return map.containsValue(it);
    }

    public void clear() {
        map.clear();
    }

    public E get(K key) {
        return map.get(key);
    }

    public boolean containsKey(K name) {
        return map.containsKey(name);
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<E> values() {
        return map.values();
    }


    /**
     * put key in index, do not add value
     */
    protected E putKey(final K key, final E value) {
        return map.put(key, value);
    }


}

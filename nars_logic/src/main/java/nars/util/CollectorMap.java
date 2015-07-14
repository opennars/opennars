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

    /**
     * current sum of occupied level
     */
    protected float mass;


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
                mass -= removed.getPriority();
            }


            removed2 = addItem(value);
            mass += value.getPriority();

            if (removed != null && removed2 != null) {
                throw new RuntimeException("Only one item should have been removed on this insert; both removed: " + removed + ", " + removed2);
            }
            if ((removed2 != null) && (!removed2.name().equals(key))) {
                removeKey(removed2.name());
                return removed2;
            }
        }

        return removed;
    }

    public E remove(final K key) {

        E e = removeKey(key);
        if (e != null) {
            E removed = removeItem(e);
            if (removed == null)
                throw new RuntimeException(key + " removed from index but not from items list");
            mass -= e.getPriority();
        }

        return e;
    }

    /**
     * remove the key only, not from items.
     */
    protected E removeKey(final K key) {
        return removeKey(key, 0);
    }

    /** remove the key and subtract some mass, in case this needs to be applied
     * after an external removal of the collection. only use if you are sure
     * the item has already been removed. this exists only for efficiency purposes
     * and the main remove() method should be used in general.
     */
    public E removeKey(final K key, final float massToRemove) {
        E e = map.remove(key);
        if (e!=null && massToRemove > 0)
            mass -= massToRemove;
        
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
        mass = 0;
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

    public float mass() {
        return mass;
    }

    /**
     * put key in index, do not add value
     */
    protected E putKey(final K key, final E value) {
        return map.put(key, value);
    }


}

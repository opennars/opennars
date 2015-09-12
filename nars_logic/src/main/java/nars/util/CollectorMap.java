package nars.util;

import nars.Global;
import nars.budget.Itemized;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** adapter to a Map for coordinating changes in a Map with another Collection */
public abstract class CollectorMap<K, V extends Itemized<K>> implements Serializable {


    public final Map<K, V> map;



    public CollectorMap(Map<K, V> map) {
        this.map = map;
    }

    /** implementation for adding the value to another collecton (called internally)  */
    abstract protected V addItem(final V e);

    /** implementation for removing the value to another collecton (called internally) */
    abstract protected V removeItem(final V e);



    //not used anymore
    @Deprecated public void merge(final V value) {



        final K key = value.name();
        final V valPrev = putKey(key, value);

        if (!value.getBudget().mergeIfChanges(valPrev.getBudget(), Global.BUDGET_EPSILON))
            return;

        //TODO check before and after removal index and if the same just replace
        {
            final V valPrev2 = removeItem(valPrev);
            if (valPrev != valPrev2)
                throw new RuntimeException("unable to remove item corresponding to key " + key);


            final V removed2 = addItem(value);
            if (removed2 != null)
                throw new RuntimeException("Only one item should have been valPrev on this insert; both valPrev: " + valPrev + ", " + removed2);
        }


    }


    public V put(final V value) {


        V removed, removed2;

        /*synchronized (nameTable)*/
        {

            final K key = value.name();
            removed = putKey(key, value);
            if (removed != null) {

                V remd = removeItem(removed);

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

        return removed;
    }

    public V remove(final K key) {

        final V e = removeKey(key);
        if (e != null) {
            V removed = removeItem(e);
            if (removed == null) {
                /*if (Global.DEBUG)
                    throw new RuntimeException(key + " removed from index but not from items list");*/
                return null;
            }
            if (removed!=e)
                throw new RuntimeException(key + " removed " + e + " but item removed was " + removed);
            return removed;
        }

        return e;
    }


    public V removeKey(final K key) {
        V e = map.remove(key);
        return e;
    }


    public int size() {
        return map.size();
    }

    public boolean containsValue(V it) {
        return map.containsValue(it);
    }

    public void clear() {
        map.clear();
    }

    final public V get(final K key) {
        return map.get(key);
    }

    public boolean containsKey(K name) {
        return map.containsKey(name);
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<V> values() {
        return map.values();
    }


    /**
     * put key in index, do not add value
     */
    public V putKey(final K key, final V value) {
        return map.put(key, value);
    }


}

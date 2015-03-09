package nars.util.bag.impl.experimental;

import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.Item;
import nars.util.bag.Bag;
import nars.util.data.CuckooMap;
import nars.util.data.linkedlist.DD;
import nars.util.data.linkedlist.DDList;
import nars.util.data.linkedlist.DDNodePool;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * ChainBag repeatedly cycles through a linked list containing
 * the set of items, stored in an arbitrary order.
 *
 * Probabalistic selection is decided according to a random function
 * of an item's priority, with options for normalizing against
 * the a priority range encountered in a sliding window.
 *
 * This allows it to maximize the dynamic range across the bag's contents
 * regardless of their absolute priority distribution (percentile vs.
 * percentage).
 *
 * Probability can be further weighted by a curve function to
 * fine-tune behavior.
 *
 */
public class ChainBag<V extends Item<K>, K> extends Bag<K, V> {



    private int capacity;
    private float mass;
    DD<V> current = null;

    private final DDNodePool<V> nodePool = new DDNodePool(16);

    V nextRemoval = null;

    /**
     * mapping from key to item
     */
    public final Map<K, DD<V>> index;

    /**
     * array of lists of items, for items on different level
     */
    public final DDList<V> chain;


    public ChainBag(int capacity) {
        super();

        this.capacity = capacity;
        this.mass = 0;
        this.index = new CuckooMap(capacity * 2);
        this.chain = new DDList(0, nodePool);

    }


    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public float mass() {
        return mass;
    }

    @Override
    public V remove() {
        DD<V> d = next();
        if (d==null) return null;
        return remove(d.item.name());
    }

    @Override
    public V peekNext() {
        DD<V> d = next();
        if (d!=null) return d.item;
        return null;
    }

    @Override
    public V put(V newItem) {

        if (nextRemoval!=null && nextRemoval.getPriority() > newItem.getPriority())
            return newItem; //too low priority to add to this

        DD<V> d = chain.add(newItem);
        DD<V> previous = index.put(newItem.name(), d);
        if (previous!=null) {
            //displaced an item with the same key
            return null;
        }
        else {
            boolean atCapacity = (size() >= capacity());

            if (atCapacity && nextRemoval!=null) {
                V overflow = remove(nextRemoval.name());
                nextRemoval = null;
                return overflow;
            }
            else {
                //bag will remain over-budget until a removal candidate is decided
            }
        }
        return null;
    }

    protected DD<V> next() {
        final int s = size();
        if (s == 0) return null;
        boolean atCapacity = s >= capacity();

        if (current == null) current = chain.getFirstNode();

        DD<V> next = current;
        do {

            if (selects(next.item))
                break;
            else if (atCapacity) {
                considerRemoving(next);
            }

        } while (true);

        DD<V> selected = next;
        if (size() > 1) next = after(next); //cycle

        return selected;
    }

    protected boolean considerRemoving(final DD<V> d) {
        //TODO improve this based on adaptive statistics measurement
        final float p = d.item.getPriority();

        if (nextRemoval==null) {
            if (p < 0.25f) {
                nextRemoval = d.item;
                return true;
            }
        }
        else {
            if (p < nextRemoval.getPriority()) {
                nextRemoval = d.item;
                return true;
            }
        }

        return false;
    }

    protected boolean selects(V v) {
        return Memory.randomNumber.nextFloat() < v.getPriority();
    }

    protected DD<V> after(DD<V> d) {
        DD<V> n = d.next;
        if (n == null)
            n = chain.getFirstNode();
        return n;
    }

    @Override
    public int size() {
        int s1 = index.size();
        if (Parameters.DEBUG) {
            int s2 = chain.size();
            if (s1 != s2)
                throw new RuntimeException(this + " bag fault; inconsistent index");
        }
        return s1;
    }





    @Override
    public Iterator<V> iterator() {
        return chain.iterator();
    }

    @Override
    public void clear() {

        chain.clear();
        index.clear();
        mass = 0;
        current = null;
    }



    @Override
    public V remove(K key) {
        DD<V> d = index.remove(key);
        if (d!=null) {
            chain.remove(d);

            if (Parameters.DEBUG) size();

            return d.item;
        }

        return null;
    }



    @Override
    public V get(K key) {
        DD<V> d = index.get(key);
        if (d!=null) return d.item;
        return null;
    }

    @Override
    public Set<K> keySet() {
        return index.keySet();
    }

}

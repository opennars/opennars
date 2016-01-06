//package nars.bag.impl.experimental;
//
//import nars.Global;
//import nars.bag.Bag;
//import nars.bag.BagBudget;
//import nars.budget.Item;
//import nars.util.data.linkedlist.DD;
//import nars.util.data.linkedlist.DDList;
//import nars.util.data.linkedlist.DDNodePool;
//import nars.util.math.Distributor;
//import org.apache.commons.math3.stat.Frequency;
//import org.apache.commons.math3.stat.descriptive.moment.Mean;
//
//import java.io.Externalizable;
//import java.io.IOException;
//import java.io.ObjectInput;
//import java.io.ObjectOutput;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//import java.util.function.Consumer;
//
///**
// * ChainBag repeatedly cycles through a linked list containing
// * the set of items, stored in an arbitrary order.
// *
// * Probabalistic selection is decided according to a random function
// * of an item's priority, with options for normalizing against
// * the a priority range encountered in a sliding window.
// *
// * This allows it to maximize the dynamic range across the bag's contents
// * regardless of their absolute priority distribution (percentile vs.
// * percentage).
// *
// * Probability can be further weighted by a curve function to
// * fine-tune behavior.
// *
// */
//public class ChainBag<V extends Item<K>, K> extends Bag<K, V> implements Externalizable {
//
//
//    private final transient Mean mean; //priority mean, continuously calculated
//    private Random rng;
//
//    private boolean ownsNodePool = false;
//
//    private int capacity;
//
//    transient DD<V> current = null;
//
//    public transient Frequency removal = new Frequency();
//
//    float minMaxMomentum = 0.98f;
//
//    private final transient DDNodePool<? extends Item> nodePool;
//
//    V nextRemoval = null;
//
//    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        int n = size();
//        out.writeInt(n);
//        out.writeObject(rng);
//        forEach(c -> {
//            try {
//                out.writeObject(c);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//        int num = in.readInt();
//        if (num > capacity())
//            throw new RuntimeException(this + " invalid capacity for readExternal results");
//
//        //TODO this might cause a problem if RNG were shared before externalizing, after internalizing they will be unique
//        rng = (Random)in.readObject();
//
//        for (int i = 0; i < num; i++) {
//            V c = (V)in.readObject();
//            put(c);
//        }
//    }
//
//    /**
//     * mapping from key to item
//     */
//    public final Map<K, DD<V>> index;
//
//    /**
//     * array of lists of items, for items on different level
//     */
//    public final DDList<V> chain;
//
//    private static final float PERCENTILE_THRESHOLD_FOR_EMERGENCY_REMOVAL = 0.5f; //slightly below half
//    private float estimatedMax = 0.5f;
//    private float estimatedMin = 0.5f;
//    private float estimatedMean = 0.5f;
//
//    final short[] d;
//    int dp = 0;
//
//    public ChainBag() {
//        this(null, 0);
//    }
//
//    public ChainBag(Random rng, DDNodePool<V> nodePool, int capacity) {
//
//        d = Distributor.get((int)(Math.sqrt(capacity))).order;
//
//        this.rng = rng;
//        this.capacity = capacity;
//        //this.index = new CuckooMap(rng, (capacity/2));
//        index = Global.newHashMap(capacity);
//
//
//        this.nodePool = nodePool;
//
//        chain = new DDList(0, nodePool);
//        mean = new Mean();
//    }
//
//
//    public ChainBag(Random rng, int capacity) {
//        this(rng, new DDNodePool(4), capacity);
//        ownsNodePool = true;
//    }
//
//
//    @Override
//    public int capacity() {
//        return capacity;
//    }
//
//    @Override
//    public V pop() {
//        if (size() == 0) return null;
//        DD<V> d = next(true);
//        if (d==null) return null;
//
//        removal.addValue(d.item.getPriority());
//        V v = remove(d.item.name());
//
//        if (Global.DEBUG) validate();
//
//        return v;
//    }
//
//    @Override
//    public V peekNext() {
//        DD<V> d = next(true);
//        if (d!=null) return d.item;
//        return null;
//    }
//
//    //TODO handle deleted items like Bag.update(..)
////    @Override
////    public V update(BagTransaction<K, V> selector) {
////
////        final K key = selector.name();
////        final DD<V> bx;
////        if (key != null) {
////            bx = index.get(key);
////        }
////        else {
////            bx = next(true);
////        }
////
////        if ((bx == null) || (bx.item == null)) {
////            //allow selector to provide a new instance
////            V n = selector.newItem();
////            if (n!=null) {
////                return putReplacing(n, selector);
////            }
////            //no instance provided, nothing to do
////            return null;
////        }
////
////        final V b = bx.item;
////
////        //allow selector to modify it, then if it returns non-null, reinsert
////
////        if (!b.getBudget().isDeleted())
////            temp.budget( b.getBudget() );
////        else
////            temp.zero();
////
////        final Budget c = selector.updateItem(b, temp);
////        if ((c!=null) && (!c.equalsByPrecision(b.getBudget()))) {
////            b.getBudget().budget(c);
////            updatePercentile(b.getPriority());
////        }
////
////        return b;
////
////    }
//
//    @Override
//    public V put(V newItem) {
//
//        if (nextRemoval!=null && nextRemoval.getPriority() > newItem.getPriority())
//            return newItem; //too low priority to add to this
//
//        DD<V> d = chain.add(newItem);
//        DD<V> previous = index.put(newItem.name(), d);
//        if (previous!=null) {
//
//            //displaced an item with the same key
//            merge(newItem, previous.item);
//
//            if (previous!=d)
//                chain.remove(previous);
//
//            updatePercentile(newItem.getPriority());
//
//            if (Global.DEBUG) size();
//
//            return null;
//        }
//
//        boolean atCapacity = (size() > capacity());
//
//        V overflow = null;
//        if (atCapacity) {
//            if (nextRemoval == null) {
//                //find something to remove
//                getNextRemoval();
//            }
//            overflow = remove(nextRemoval.name());
//            if (overflow == null) {
//                //TODO not sure why this happens
//                if (Global.DEBUG) validate();
//                //throw new RuntimeException(this + " error removing nextRemoval=" + nextRemoval);
//            }
//            nextRemoval = null;
//        }
//
//
//        updatePercentile(newItem.getPriority());
//
//        if (Global.DEBUG) validate();
//
//        return overflow;
//    }
//
//    protected void getNextRemoval() {
//        int size = size();
//        if (size == 0) return;
//
//        int loops = 0;
//
//        DD<V> c = current; //save current position
//
//        while (loops++ <= size && nextRemoval == null)
//            next(false);
//
//        if (nextRemoval == null) {
//            //throw new RuntimeException(this + " considered nothing removeable");
//            nextRemoval = current.item;
//        }
//
//        current = c;  //restore current position if it wasn't what was removed
//    }
//
//    /**
//     *
//     * @param byPriority - whether to select according to priority, or just the next item in chain order
//     * @return
//     */
//    protected DD<V> next(boolean byPriority) {
//        int s = size();
//        if (s == 0) return null;
//        //final boolean atCapacity = s >= capacity();
//
//        DD<V> next = after(current);
//
//        if (s == 1)
//            return next;
//
//        do {
//
//
//            /*if (next == null) {
//                throw new RuntimeException("size = " + size() + " yet there is no first node in chain");
//            }*/
//
//            V ni = next.item;
//
//            /*if (ni == null) {
//                throw new RuntimeException("size = " + size() + " yet iterated cell with null item");
//            }*/
//
//            double percentileEstimate = getPercentile(ni.getPriority());
//
//
//            if (!byPriority) {
//                if (nextRemoval == null)
//                    considerRemoving(next, percentileEstimate);
//                break;
//            }
//            if (selectPercentile(percentileEstimate))
//                break;
//
//            considerRemoving(next, percentileEstimate);
//
//            next = after(next);
//
//        } while (true);
//
//        return current = next;
//    }
//
//    @Override
//    public void setCapacity(int c) {
//        capacity = c;
//    }
//
//
//
//    /** updates the adaptive percentile measurement; should be called on put and when budgets update  */
//    private void updatePercentile(float priority) {
//        //DescriptiveStatistics percentile is extremely slow
//        //contentStats.getPercentile(ni.getPriority())
//        //approximate percentile using max/mean/min
//
//        mean.increment(priority);
//        float  mean = (float)this.mean.getResult();
//
//        float momentum = minMaxMomentum;
//
//
//        estimatedMax = (estimatedMax < priority) ? priority : (1.0f - momentum) * mean + (momentum) * estimatedMax;
//        estimatedMin = (estimatedMin > priority) ? priority : (1.0f - momentum) * mean + (momentum) * estimatedMin;
//        estimatedMean = mean;
//    }
//
//    /** uses the adaptive percentile data to estimate a percentile of a given priority */
//    private double getPercentile(float priority) {
//
//        float mean = estimatedMean;
//
//        float upper, lower;
//        if (priority < mean) {
//            lower = estimatedMin;
//            upper = mean;
//        }
//        else if (priority == mean) {
//            return 0.5f;
//        }
//        else {
//            upper = estimatedMax;
//            lower = mean;
//        }
//
//        float perc = (priority - lower) / (upper-lower);
//
//        float minPerc = 1.0f / size();
//
//        if (perc < minPerc) return minPerc;
//
//        return perc;
//    }
//
//    protected boolean considerRemoving(DD<V> d, double percentileEstimate) {
//        //TODO improve this based on adaptive statistics measurement
//        V item = d.item;
//        float p = item.getPriority();
//        V nr = nextRemoval;
//        if (nr==null) {
//            if (percentileEstimate <= PERCENTILE_THRESHOLD_FOR_EMERGENCY_REMOVAL) {
//                nextRemoval = item;
//                return true;
//            }
//        }
//        else if (nr != item) {
//            if (p < nr.getPriority()) {
//                nextRemoval = item;
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    protected boolean selectPercentile(double percentileEstimate) {
//        //return selectPercentileRandom(percentileEstimate);
//        return selectPercentileDistributor(percentileEstimate);
//    }
//
//    protected boolean selectPercentileDistributor(double percentileEstimate) {
//        int dLen = d.length;
//        return d[ (dp++)%dLen ]/((double)dLen) < (percentileEstimate);
//    }
//
//    protected boolean selectPercentileRandom(double percentileEstimate) {
//        return rng.nextFloat() < percentileEstimate;
//    }
//
//    protected boolean selectPercentage(V v) {
//        return rng.nextFloat() < v.getPriority();
//    }
//
//    protected DD<V> after(DD<V> d) {
//        DD<V> n = d!=null ? d.next : null;
//        if ((n == null) || (n.item == null))
//            return chain.getFirstNode();
//        return n;
//    }
//
//    @Override
//    public int size() {
//        return index.size();
//    }
//
//    public void validate() {
//        int s1 = index.size();
//        if (Global.DEBUG) {
//            int s2 = chain.size();
//            if (s1 != s2)
//                throw new RuntimeException(this + " bag fault; inconsistent index (" + s1 + " index != " + s2 + " chain)");
//            if (s1 > capacity()+2)
//                throw new RuntimeException(this + " has exceeded capacity: " + s1 + " > " + capacity());
//        }
//    }
//
//
//    @Override
//    public Iterator<V> iterator() {
//        return chain.iterator();
//    }
//
//    @Override
//    public void clear() {
//
//        index.clear();
//        chain.clear();
//
//        current = null;
//        estimatedMin = estimatedMax = estimatedMean = 0.5f;
//
//    }
//
//
//    @Override
//    public void delete() {
//
//        if (ownsNodePool)
//            nodePool.delete();
//
//        index.clear();
//        chain.delete();
//
//    }
//
//    @Override
//    public BagBudget<K> remove(K key) {
//        DD<V> d = index.remove(key);
//        if (d!=null) {
//            V v = d.item; //save it here because chain.remove will nullify .item field
//            chain.remove(d);
//
//            if (Global.DEBUG) validate();
//
//            if (current!=null && v == current.item)
//                current = after(current);
//
//            return v;
//        }
//
//        return null;
//    }
//
//
//
//    @Override
//    public V get(K key) {
//        DD<V> d = index.get(key);
//        return (d!=null) ? d.item : null;
//    }
//
//    @Override
//    public Set<K> keySet() {
//        return index.keySet();
//    }
//
//    @Override
//    public void forEach(Consumer<? super V> value) {
//        chain.forEach(value);
//    }
// }

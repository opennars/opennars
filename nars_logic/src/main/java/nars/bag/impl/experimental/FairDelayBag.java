///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.bag.impl.experimental;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import nars.Memory;
//import nars.budget.Item;
//import nars.util.math.Distributor;
//
///**
// * TODO add a strict probability mode which excludes low priority items from exceeding
// * their proportion in a resulting reload() pending queue
// */
//public class FairDelayBag<E extends Item<K>, K> extends DelayBag<K, E> {
//
//    /** # of levels should be "fairly" low, reducing the complete cycle length of the distributor */
//    static final int levels = 10;
//
//    static final short[] distributor = Distributor.get(levels).order;
//
//
//    public FairDelayBag(Memory m, AtomicDouble forgetRate, int capacity) {
//        super(m, forgetRate, capacity);
//    }
//
//
//    public FairDelayBag(Memory memory, AtomicDouble forgetRate, int capacity, int targetPendingBufferSize) {
//        super(memory, forgetRate, capacity, targetPendingBufferSize);
//
//    }
//
//
//
//    @Override
//    protected boolean fireable(E c) {
//        /** since distributor has a min value of 1,
//            subtract one so that items with low priority can be selected */
//        int currentLevel = distributor[reloadIteration % levels] - 1;
//        return c.getPriority() * levels >= currentLevel;
//    }
//
//    @Override
//    protected void adjustActivationThreshold() {
//        //do nothing about this, fireable in this impl doesn't depend on activation threshold
//    }
//
//
////    @Override
////    public E UPDATE(BagSelector<K, E> selector) {
////        //TODO provide a full implementation
////        super.putInFast(selector);
////        //this needs to return the selected or created item, not the result of PUT which is overflow
////        return null;
////    }
//
// }

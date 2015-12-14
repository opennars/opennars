package nars.bag.impl;

import nars.Memory;

/** nearly a Map */
public interface CacheBag<K,V>  {

    void clear();

    V get(K key);

    Object remove(K key);

    /** same semantics as Map.put; output value is an existing value or null if none */
    V put(K k, V v);


    int size();

    /** called when memory is ready to begin using this,
     *  allows letting the cache be aware of the memory
     */
    void start(Memory n);

//    void setOnRemoval(Consumer<V> onRemoval);
//    Consumer<V> getOnRemoval();

    default void delete() {

    }





//
//    /** performs an exhaustive element comparison of two bags */
//    public static <K,V extends Itemized<K>> boolean equalsInSequence(CacheBag<K,V> a, CacheBag<K,V> b) {
//        if (a == b) return true;
//        if (a.getClass()!=b.getClass())
//            return false;
//
//        Iterator<V> iterator1 = a.iterator();
//        Iterator<V> iterator2 = b.iterator();
//
//        while(true) {
//            if(iterator1.hasNext()) {
//                if(!iterator2.hasNext()) {
//                    return false;
//                }
//
//                V o1 = iterator1.next();
//                V o2 = iterator2.next();
//                if (Objects.equal(o1, o2) && o1.getBudget().equalsBudget(o2.getBudget())) {
//                    continue;
//                }
//
//                return false;
//            }
//
//            return !iterator2.hasNext();
//        }
//
//        //return Iterables.elementsEqual(a, b);
//    }
//




//    static TermIndex memory() {
//        return memory(1);
//    }
//
//    static TermIndex memory(int capacity) {
//        return //new MapCacheBag(
//            //new WeakValueHashMap<>(capacity) );
//                GuavaCacheBag.make(capacity * 256);
//    }
}

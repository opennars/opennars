package nars.bag.impl;

/** nearly a Map */
public interface CacheBag<K,V>  {

    void clear();

    V get(Object key);

    Object remove(K key);

    /** same semantics as Map.put; output value is an existing value or null if none */
    V put(K k, V v);


    int size();


//    void setOnRemoval(Consumer<V> onRemoval);
//    Consumer<V> getOnRemoval();

    default void delete() {

    }
}

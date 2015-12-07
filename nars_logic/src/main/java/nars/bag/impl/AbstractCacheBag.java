package nars.bag.impl;

import nars.Memory;

/**
 * Created by me on 9/1/15.
 */
abstract public class AbstractCacheBag<K, V> implements CacheBag<K,V> {

    protected Memory memory = null;


    //private Consumer<V> onRemoval;

    /*public void setOnRemoval(Consumer<V> onRemoval) {
        this.onRemoval = onRemoval;
    }

    public Consumer<V> getOnRemoval() {
        return onRemoval;
    }*/

    @Override public void start(Memory n) {
        this.memory = n;
    }



//    @Override
//    public int hashCode() {
//        throw new RuntimeException("not impl yet");
//    }

}

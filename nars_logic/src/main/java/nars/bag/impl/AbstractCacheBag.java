package nars.bag.impl;

import nars.Memory;

/**
 * Created by me on 9/1/15.
 */
public abstract class AbstractCacheBag<V> implements CacheBag<V> {

    protected Memory memory = null;


    //private Consumer<V> onRemoval;

    /*public void setOnRemoval(Consumer<V> onRemoval) {
        this.onRemoval = onRemoval;
    }

    public Consumer<V> getOnRemoval() {
        return onRemoval;
    }*/

    @Override public void start(Memory n) {
        memory = n;
    }



//    @Override
//    public int hashCode() {
//        throw new RuntimeException("not impl yet");
//    }

}

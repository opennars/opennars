package nars.bag.impl;

import nars.budget.Itemized;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by me on 9/1/15.
 */
abstract public class AbstractCacheBag<K, V extends Itemized<K>> implements CacheBag<K,V>, Externalizable {


    //private Consumer<V> onRemoval;

    /*public void setOnRemoval(Consumer<V> onRemoval) {
        this.onRemoval = onRemoval;
    }

    public Consumer<V> getOnRemoval() {
        return onRemoval;
    }*/

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CacheBag)
            return CacheBag.equals(this, ((CacheBag)obj));
        return false;
    }

//    @Override
//    public int hashCode() {
//        throw new RuntimeException("not impl yet");
//    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int num = in.readInt();
        for (int i = 0; i < num; i++) {
            put( (V) in.readObject());
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size());

        //TODO use forEach if it can do the right order

        forEach(v -> {
            try {
                out.writeObject(v);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

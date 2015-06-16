package nars.bag.impl;

import nars.nal.Itemized;

import java.util.Map;
import java.util.function.Consumer;

/**
 * CacheBag backed by Infinispan, supporting distributed memory sharing
 */
public class InfiniCacheBag<K, V extends Itemized<K>> extends MapCacheBag<K, V> {

    private final String userID;
    private final String channel;

    public InfiniCacheBag(String userID, String channel) {
        super();
        this.userID = userID;
        this.channel = channel;
    }

    @Override
    public Map<K, V> newMap() {
        //TODO
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + ":" + userID + "x" + data.size();
    }

}

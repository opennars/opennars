package nars.bag.impl;

import nars.nal.Itemized;
import org.infinispan.Cache;
import spangraph.InfiniPeer;

import java.util.Map;

/**
 * CacheBag backed by Infinispan, supporting distributed memory sharing
 */
public class InfiniCacheBag<K, V extends Itemized<K>> extends MapCacheBag<K, V> {


    private final Cache<K,V> map;


//    public static <K, V extends Itemized<K>> InfiniCacheBag<K,V> make(InfiniPeer p) {
//
//    }

    public static <K, V extends Itemized<K>> InfiniCacheBag<K,V> local(String userID, String channel) {
        return new InfiniCacheBag(InfiniPeer.local(userID).the(channel));
    }
    public static <K, V extends Itemized<K>> InfiniCacheBag<K,V> file(String channel, String diskPath, int maxEntries) {
        return new InfiniCacheBag(InfiniPeer.local("file", diskPath, maxEntries).the(channel));
    }

    public InfiniCacheBag(Cache<K,V> c) {
        super(c);
        this.map = c;
    }

    @Override
    public void clear() {
        //throw new RuntimeException("unable to clear() shared concept bag");
    }


}

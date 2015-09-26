package nars.bag.impl;

import nars.budget.Itemized;
import nars.util.db.InfiniPeer;
import org.infinispan.Cache;

/**
 * CacheBag backed by Infinispan, supporting distributed memory sharing
 */
public class InfiniCacheBag<K, V extends Itemized<K>> extends MapCacheBag<K, V> {



//    public static <K, V extends Itemized<K>> InfiniCacheBag<K,V> make(InfiniPeer p) {
//
//    }

    public static <K, V extends Itemized<K>> InfiniCacheBag<K,V> local(String userID, String channel) {
        return new InfiniCacheBag(InfiniPeer.clusterLocal(userID).the(channel));
    }
    public static <K, V extends Itemized<K>> InfiniCacheBag<K,V> file(String channel, String diskPath, int maxEntries) {
        return new InfiniCacheBag(InfiniPeer.file(diskPath, maxEntries).the(channel));
    }


    public InfiniCacheBag(Cache<K,V> c) {
        super(c);
    }

    @Override
    public void clear() {
        //throw new RuntimeException("unable to clear() shared concept bag");
    }


}

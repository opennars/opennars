package nars.bag.impl;

import nars.budget.Itemized;
import nars.util.db.InfiniPeer;
import org.infinispan.Cache;
import org.infinispan.persistence.spi.PersistenceException;

/**
 * CacheBag backed by Infinispan, supporting distributed memory sharing
 */
public class InfiniCacheBag<K, V extends Itemized<K>> extends MapCacheBag<K, V, Cache<K,V>> {
    private boolean asyncPut = false;


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

        System.out.println(this + " " +
                c.size() + " " + c.getAdvancedCache().getDataContainer() + " " + c.getAdvancedCache().getDistributionManager());
                //c.getAdvancedCache().getStats().getTotalNumberOfEntries() + " entries");
    }


    @Override
    public V put(V v) {
        if (asyncPut) {
            data.putAsync(v.name(), v);

            //TODO compute(..) with item merging

            return null;
        }
        else {
            try {
                return data.put(v.name(), v);
            }
            catch (PersistenceException e) {
                System.err.println(e + " while trying to write " + v);
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + data.toString();
    }

    @Override
    public void clear() {
        //throw new RuntimeException("unable to clear() shared concept bag");
    }


}

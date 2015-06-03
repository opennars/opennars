package nars.db;

import com.hazelcast.config.Config;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;
import nars.bag.impl.CacheBag;
import nars.nal.Itemized;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by me on 6/2/15.
 */
public class HazelCacheBag<K, V extends Itemized<K>> extends CacheBag<K,V>  {

    private final Map<K, V> concepts;
    private final String userID;

    public HazelCacheBag(String userID, String channel) {
        Config cfg = new Config();

        //cfg.setProperty( "hazelcast.logging.type", "none" );
        cfg.setProperty("hazelcast.memcache.enabled", "false");
        cfg.setProperty("hazelcast.rest.enabled", "false");
        cfg.setProperty("hazelcast.system.log.enabled", "false");

        cfg.setProperty("hazelcast.elastic.memory.enabled", "true");
        cfg.setProperty("hazelcast.elastic.memory.unsafe.enabled", "true");

        cfg.setInstanceName(this.userID = userID);


        cfg.setNativeMemoryConfig(new NativeMemoryConfig()
                .setAllocatorType(NativeMemoryConfig.MemoryAllocatorType.POOLED)
                .setSize(new MemorySize(256, MemoryUnit.MEGABYTES))
                .setEnabled(true)
                .setMinBlockSize(16)
                .setPageSize(1 << 20));

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);

        concepts = instance.getMap(channel);

    }
    @Override
    public void clear() {
        //throw new RuntimeException("unable to clear() shared concept bag " + concepts);
    }

    @Override
    public V get(K key) {
        return concepts.get(key);
    }

    @Override
    public V take(K key) {
        return concepts.remove(key);
    }

    @Override
    public void put(V v) {

        concepts.put(v.name(), v);
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Iterator<V> iterator() {
        return concepts.values().iterator();
    }

    @Override
    public String toString() {
        return super.toString() + ":" + userID + "x" + concepts.size();
    }
}

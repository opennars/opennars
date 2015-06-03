package nars.db;

import com.hazelcast.config.Config;
import com.hazelcast.config.NativeMemoryConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;
import nars.bag.impl.CacheBag;
import nars.nal.Itemized;

import java.util.Map;

/**
 * Created by me on 6/2/15.
 */
public class HazelCacheBag<K, V extends Itemized<K>> extends CacheBag<K,V> {

    public HazelCacheBag(String userID, String channel) {
        Config cfg = new Config();

        //cfg.setProperty( "hazelcast.logging.type", "none" );
        cfg.setProperty("hazelcast.memcache.enabled", "false");
        cfg.setProperty("hazelcast.rest.enabled", "false");
        cfg.setProperty("hazelcast.system.log.enabled", "false");

        cfg.setProperty("hazelcast.elastic.memory.enabled", "true");
        cfg.setProperty("hazelcast.elastic.memory.unsafe.enabled", "true");



        cfg.setNativeMemoryConfig(new NativeMemoryConfig()
                .setAllocatorType(NativeMemoryConfig.MemoryAllocatorType.POOLED)
                .setSize(new MemorySize(256, MemoryUnit.MEGABYTES))
                .setEnabled(true)
                .setMinBlockSize(16)
                .setPageSize(1 << 20));

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);

        Map<Integer, String> mapCustomers = instance.getMap("customers");

    }
    @Override
    public void clear() {

    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public V take(K key) {
        return null;
    }

    @Override
    public void put(V v) {

    }

    @Override
    public long size() {
        return 0;
    }
}

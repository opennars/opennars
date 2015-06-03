package nars.util.db;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIteratorSet;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntriesEvicted;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.Event;
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;

import java.io.PrintStream;
import java.util.Map;
import java.util.UUID;

/**
 * Peer-to-peer node manager : wraps CacheManager functionality
 */
@Listener(sync = true)
public class InfiniPeer extends DefaultCacheManager  {

    private final String userID;

    public InfiniPeer(String userID, GlobalConfiguration globalConfig, Configuration config) {
        super(globalConfig, config);

        this.userID = userID;

        addListener(this);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stop();
            }
        });


    }

    static public void test(String userID) {

        /*GlobalConfiguration globalConfig = new GlobalConfigurationBuilder()
                .serialization()
                .addAdvancedExternalizer(998, new PersonExternalizer())
                .addAdvancedExternalizer(999, new AddressExternalizer())
                .build();*/

        /*Configuration config = new ConfigurationBuilder()
                .persistence().passivation(false)
                .addSingleFileStore().location("/tmp").async().enable()
                .preload(false).shared(false).threadPoolSize(20).build();*/



        InfiniPeer cacheMan = InfiniPeer.start(userID);

        final Cache<Object, Object> cache = cacheMan.the("abc", true);


        // Add a entry
        //cache.put("key", "value");

        //cache.putForExternalRead(UUID.randomUUID().toString(), cacheMan.getAddress());
        cache.put(UUID.randomUUID().toString(), cacheMan.getAddress());

// Validate the entry is now in the cache
        //assertEqual(1, cache.size());
        //assertTrue(cache.containsKey("key"));
// Remove the entry from the cache
        //Object v = cache.remove("key");
// Validate the entry is no longer in the cache
        //assertEqual("value", v);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //synchronize for display purposes
        synchronized(cache) {
            cacheMan.print(cache, System.out);
        }

    }

    public void print(Cache cache, PrintStream out) {

        System.out.println(userID +  " -----");
        System.out.println(cache + " " + cache.size());
        System.out.println(cache.entrySet());
        System.out.println("---\n");

    }

    public <K,V> Cache<K, V> the(String cacheID) {
        return the(cacheID, true);
    }

    public <K,V> Cache<K, V> the(String cacheID, boolean createIfMissing) {
        Cache<K, V> cache = getCache(cacheID, createIfMissing);
        cache.addListener(this);
        return cache;
    }


    @ViewChanged
    @Merged
    public void viewChangeEvent(ViewChangedEvent e) {

        System.out.println("viewChanged: " + e.getNewMembers() + " <- " + e.getOldMembers());
        //updateClusterTable(e.getNewMembers());
    }

    @CacheEntryModified
    @CacheEntryRemoved
    @CacheEntriesEvicted
    public void entryChanged(Event<?, ?> e) {
        if (!e.isPre())
            System.out.println("change: " + e);
    }

    public static InfiniPeer start(String userID) {


        GlobalConfiguration globalConfig = new GlobalConfigurationBuilder()

                .transport()
                .nodeName(userID)
                .defaultTransport()

                        //https://github.com/infinispan/infinispan/tree/master/core/src/main/resources/default-configs
                        //https://github.com/belaban/JGroups/blob/master/conf/udp-largecluster.xml
                        //https://github.com/infinispan/infinispan/blob/master/core/src/test/java/org/infinispan/test/fwk/JGroupsConfigBuilder.java
                .addProperty("configurationFile", "udp-largecluster.xml")


                .globalJmxStatistics()
                .allowDuplicateDomains(true)
                .build();

        Configuration config = new ConfigurationBuilder()

                .unsafe()
                .clustering()
                        //.cacheMode(CacheMode.DIST_SYNC)
                .cacheMode(CacheMode.DIST_SYNC)
                .sync()
                .l1().lifespan(25000L)
                .hash().numOwners(3)
                .build();

        return new InfiniPeer(
                userID,
                globalConfig,
                config
        );
    }

    public static void main(String args[]) throws Exception {


        InfiniPeer.start(UUID.randomUUID().toString());


//        EmbeddedCacheManager manager = new DefaultCacheManager();
//        manager.defineConfiguration("custom-cache", new ConfigurationBuilder()
//                .eviction().strategy(EvictionStrategy.LIRS).maxEntries(10)
//                .build());
//        Cache<Object, Object> c = manager.getCache("custom-cache");


        /*
           ConfigurationBuilder builder = new ConfigurationBuilder();
   builder.persistence()
         .passivation(false)
         .addSingleFileStore()
            .preload(true)
            .shared(false)
            .fetchPersistentState(true)
            .ignoreModifications(false)
            .purgeOnStartup(false)
            .location(System.getProperty("java.io.tmpdir"))
            .async()
               .enabled(true)
               .threadPoolSize(5)
            .singleton()
               .enabled(true)
               .pushStateWhenCoordinator(true)
               .pushStateTimeout(20000);
         */


        /*

        ConfigurationBuilder b = new ConfigurationBuilder();
b.persistence()
    .addStore(SoftIndexFileStoreConfigurationBuilder.class)
        .indexLocation("/tmp/sifs/testCache/index");
        .dataLocation("/tmp/sifs/testCache/data")
         */
    }


}

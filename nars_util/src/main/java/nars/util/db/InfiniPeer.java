package nars.util.db;


import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntriesEvicted;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.Event;
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.jgroups.protocols.UDP;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Peer-to-peer node manager : wraps CacheManager functionality
 *
 * @see //github.com/belaban/JGroups/tree/master/conf
 */
@Listener(sync = true)
public class InfiniPeer extends DefaultCacheManager {

    static {
        //Logger.getLogger(JGroupsTransport.class.getName()).setLevel(Level.WARNING);
        Logger.getLogger(UDP.class.getName()).setLevel(Level.SEVERE);
    }

    public final String userID;

    public InfiniPeer(GlobalConfiguration globalConfig, Configuration config) {
        super(globalConfig, config);

        if (globalConfig!=null && globalConfig.transport() != null)
            this.userID = globalConfig.transport().nodeName();
        else
            this.userID = null;

        addListener(this);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            InfiniPeer.this.stop();
        }));


    }

    public InfiniPeer(ConfigurationBuilder config) {
        this(null, config.build());
    }


    public void print(Cache cache, PrintStream out) {

        System.out.println(userID + " -----");
        System.out.println(cache + " " + cache.size());
        System.out.println(cache.entrySet());
        System.out.println("---\n");

    }

    public <K, V> Cache<K, V> the(String cacheID) {
        return the(cacheID, true);
    }

    public <K, V> Cache<K, V> the(String cacheID, boolean createIfMissing) {
        Cache<K, V> cache = getCache(cacheID, createIfMissing);
        cache.addListener(this);
        return cache;
    }


    @ViewChanged
    @Merged
    public void viewChangeEvent(ViewChangedEvent e) {

        //System.out.println("viewChanged: " + e.getNewMembers() + " <- " + e.getOldMembers());
        //updateClusterTable(e.getNewMembers());
    }

    @CacheEntryCreated
    @CacheEntryModified
    @CacheEntryRemoved
    @CacheEntriesEvicted
    public void entryChanged(Event<?, ?> e) {
        /*if (!e.isPre())
            System.out.println("change: " + e);*/
    }

//    /** creates a local infinipeer */
//    public static InfiniPeer local(String userID) {
//
//        GlobalConfiguration globalConfig = new GlobalConfigurationBuilder()
//                .build();
//
//        Configuration config = new ConfigurationBuilder()
//                .unsafe()
//                .build();
//
//        return new InfiniPeer(
//                userID,
//                globalConfig,
//                config
//        );
//
//    }


    /**
     * for local only mode on the same host
     */
    @Deprecated public static InfiniPeer clusterLocal(String userID) {
        return cluster(userID, t ->
                        t.nodeName(userID)
                                .defaultTransport()
                                .addProperty("configurationFile", "fast.xml")
        );

    }

    public static InfiniPeer the(GlobalConfigurationBuilder gconf, Configuration conf) {
        return new InfiniPeer(
                gconf.build(),
                conf
        );
    }

    //SIMPLE form
//    public static InfiniPeer file2(String diskPath, int maxEntries) {
//        ConfigurationBuilder builder = new ConfigurationBuilder().simpleCache(true);
//        builder.eviction().
//    }

    /** creates a web-enabled infinipeer */
    public static InfiniPeer cluster(String userID,
                                     Consumer<TransportConfigurationBuilder> transportConfigger) {
        return cluster(userID, transportConfigger, n -> { });
    }

    /** creates a web-enabled infinipeer */
    public static InfiniPeer cluster(String userID,
                                     Consumer<TransportConfigurationBuilder> transportConfigger,
                                     Consumer<ConfigurationBuilder> c) {


        GlobalConfigurationBuilder globalConfigBuilder = new GlobalConfigurationBuilder();
        globalConfigBuilder
                .globalJmxStatistics()
                //.enable()
                .cacheManagerName(userID);
                //.security().authorization().disable();


        transportConfigger.accept(
                globalConfigBuilder.transport()
                //.machineId(userID).siteId(userID)
                .nodeName(userID) );

        ConfigurationBuilder config = baseConfig();

        config.persistence()
                .addClusterLoader()
                .clustering()
                .cacheMode(CacheMode.DIST_SYNC).sync()
                //.cacheMode(CacheMode.DIST_ASYNC).async()
                //.remoteCallTimeout(500)
                .l1().lifespan(25000L)
                ;
                //.hash().numOwners(?);

        //TODO expand on these options

        c.accept(config);

        return new InfiniPeer(
                globalConfigBuilder.build(),
                config.build()
        );
    }

    private static ConfigurationBuilder baseConfig() {
        ConfigurationBuilder c = new ConfigurationBuilder();
        //c.invocationBatching().enable();
        //c.versioning().disable();
        //zc.storeAsBinary().storeKeysAsBinary(true);
        return c;
    }


    /** for local only mode on the same host, saved to disk */
    public static InfiniPeer file(String diskPath, int maxEntries) {

        ConfigurationBuilder config = baseConfig();

        return new InfiniPeer(
                useFileStore(diskPath, maxEntries, config)
        );


        /* Eviction is disabled by default. If enabled (using an empty <eviction /> element), certain default values are used:
            strategy: EvictionStrategy.NONE is assumed, if a strategy is not specified..
            wakeupInterval: 5000 is used if not specified.
            If you wish to disable the eviction thread, set wakeupInterval to -1.
            maxEntries: -1 is used if not specified, which means unlimited entries.
            0 means no entries, and the eviction thread will strive to keep the cache empty. */

        /*

        GlobalConfigurationBuilder globalConfigBuilder = new GlobalConfigurationBuilder()
                .globalJmxStatistics().enabled(false);

        globalConfigBuilder.transport().nodeName(userID)
                .defaultTransport();

                //.addProperty("configurationFile", "fast.xml");



        globalConfigBuilder.globalJmxStatistics().allowDuplicateDomains(true)
                .build();

        Configuration config = new ConfigurationBuilder()
                .persistence()
                .addSingleFileStore()
                .location(diskPath)
                .maxEntries(maxEntries)
                .fetchPersistentState(true)
                .ignoreModifications(false)

                        //.purgeOnStartup(true)

                //.transaction().autoCommit(true).transactionMode(TransactionMode.NON_TRANSACTIONAL)

                .unsafe()

                        .clustering().sync()
                //.clustering()
                        //.cacheMode(CacheMode.DIST_SYNC)
                //.cacheMode(CacheMode.DIST_SYNC)
                //.sync()
                //.l1().lifespan(25000L)
                //.hash().numOwners(3)
                .build();

        */



    }

    public static ConfigurationBuilder useFileStore(String diskPath, int maxEntries, ConfigurationBuilder config) {
        config
            .persistence()
        //single file store:
                .addSingleFileStore()
                .shared(true)
                .location(diskPath)
                .fetchPersistentState(true)
                .maxEntries(maxEntries)

                //.async().enable()
        /*.eviction().maxEntries(
                (long)(maxEntries * 0.99f))
            .expiration().wakeUpInterval(5000l).lifespan(-1).maxIdle(500l).build();*/
        ;

        return config;
    }


    /* JDBC
    *
    * ConfigurationBuilder builder = new ConfigurationBuilder();
builder.persistence().addStore(JdbcStringBasedStoreConfigurationBuilder.class)
      .fetchPersistentState(false)
      .ignoreModifications(false)
      .purgeOnStartup(false)
      .table()
         .dropOnExit(true)
         .createOnStart(true)
         .tableNamePrefix("ISPN_STRING_TABLE")
         .idColumnName("ID_COLUMN").idColumnType("VARCHAR(255)")
         .dataColumnName("DATA_COLUMN").dataColumnType("BINARY")
         .timestampColumnName("TIMESTAMP_COLUMN").timestampColumnType("BIGINT")
      .connectionPool()
         .connectionUrl("jdbc:h2:mem:infinispan_binary_based;DB_CLOSE_DELAY=-1")
         .username("sa")
         .driverClass("org.h2.Driver");
    * */

    /* JMX
    ConfigurationBuilder b = new ConfigurationBuilder();
b.persistence()
    .addStore(CLInterfaceLoaderConfigurationBuilder.class)
    .connectionString("jmx://1.2.3.4:4444/MyCacheManager/myCache");
     */
    /*
    Configuration cacheConfig = new ConfigurationBuilder().persistence()
                                .addStore(LevelDBStoreConfigurationBuilder.class)
                                .location("/tmp/leveldb/data")
                                .expiredLocation("/tmp/leveldb/expired")
                                .build();
     */

    /*
    Configuration c = new ConfigurationBuilder()
   .clustering()
      .cacheMode(CacheMode.DIST_SYNC)
      .hash()
         .numOwners(2)
         .numSegments(100)
         .capacityFactor(2)
   .build();
     */

    /** creates a cluster infinipeer */
    public static InfiniPeer cluster(String userID) {
        return cluster(userID, t ->
                        t.nodeName(userID)
                                .defaultTransport()
                                //.addProperty("configurationFile", "udp-largecluster.xml")
                                //.addProperty("configurationFile", "udp.xml")
                                .addProperty("configurationFile", "fast.xml")
        );
    }
    public static InfiniPeer clusterAndFile(String userID, String diskPath, int maxEntries) {
        return cluster(userID, t -> {
            t.nodeName(userID)
                .defaultTransport()
                //.addProperty("configurationFile", "udp-largecluster.xml")
                //.addProperty("configurationFile", "udp.xml")
                .addProperty("configurationFile", "fast.xml").build();
            },
            c -> {
                useFileStore(diskPath, maxEntries, c);
            }
        );
    }
    //


    /** creates a web-scale (> 100 nodes) infinipeer */
    public static InfiniPeer webLarge(String userID) {
        return cluster(userID, t ->
                        t.nodeName(userID)
                                .defaultTransport()
                                .addProperty("configurationFile", "udp-largecluster.xml")
        );

        //https://github.com/infinispan/infinispan/tree/master/core/src/main/resources/default-configs
        //https://github.com/belaban/JGroups/blob/master/conf/udp-largecluster.xml
        //https://github.com/infinispan/infinispan/blob/master/core/src/test/java/org/infinispan/test/fwk/JGroupsConfigBuilder.java
    }


    public static InfiniPeer clusterLocal() {
        return clusterLocal("");
    }


    public static InfiniPeer tmp() {
        return file(tmpDefaultPath(), 128*1024);
    }

    public static String tmpDefaultPath() {
        return getTempDir() + "/" + "opennars";
    }

    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

//    public static void main(String args[]) throws Exception {
//
//
//        InfiniPeer.web(UUID.randomUUID().toString());
//
//
////        EmbeddedCacheManager manager = new DefaultCacheManager();
////        manager.defineConfiguration("custom-cache", new ConfigurationBuilder()
////                .eviction().strategy(EvictionStrategy.LIRS).maxEntries(10)
////                .build());
////        Cache<Object, Object> c = manager.getCache("custom-cache");
//
//
//        /*
//           ConfigurationBuilder builder = new ConfigurationBuilder();
//   builder.persistence()
//         .passivation(false)
//         .addSingleFileStore()
//            .preload(true)
//            .shared(false)
//            .fetchPersistentState(true)
//            .ignoreModifications(false)
//            .purgeOnStartup(false)
//            .location(System.getProperty("java.io.tmpdir"))
//            .async()
//               .enabled(true)
//               .threadPoolSize(5)
//            .singleton()
//               .enabled(true)
//               .pushStateWhenCoordinator(true)
//               .pushStateTimeout(20000);
//         */
//
//
//        /*
//
//        ConfigurationBuilder b = new ConfigurationBuilder();
//b.persistence()
//    .addStore(SoftIndexFileStoreConfigurationBuilder.class)
//        .indexLocation("/tmp/sifs/testCache/index");
//        .dataLocation("/tmp/sifs/testCache/data")
//         */
//    }
//

}

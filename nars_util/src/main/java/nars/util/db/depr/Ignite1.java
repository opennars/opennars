//package nars.db;
//
//import org.apache.ignite.Ignite;
//import org.apache.ignite.IgniteCache;
//import org.apache.ignite.Ignition;
//import org.apache.ignite.configuration.IgniteConfiguration;
//
///**
// * Created by me on 6/2/15.
// */
//public class Ignite1 {
//
//
//    public static void main(String[] args) {
//        //Ignite ignite = Ignition.start();
//        IgniteConfiguration cfg = new IgniteConfiguration();
//        cfg.setWorkDirectory("/tmp/x");
//        cfg.setNetworkTimeout(1);
//        cfg.setDiscoveryStartupDelay(1);
//        cfg.set
//
//        Ignite ignite = Ignition.start(cfg);
//
//
//        // Get an instance of named cache.
//        final IgniteCache<Integer, String> cache = ignite.getOrCreateCache("cacheName");
//
//
//        // Store keys in cache.
//        for (
//                int i = 0;
//                i < 10; i++)
//            cache.put(i, Integer.toString(i));
//
//        // Retrieve values from cache.
//        for (
//                int i = 0;
//                i < 10; i++)
//            System.out.println("Got [key=" + i + ", val=" + cache.get(i) + ']');
//
//        // Remove objects from cache.
//        for (
//                int i = 0;
//                i < 10; i++)
//            cache.remove(i);
//
//        // Atomic put-if-absent.
//        cache.putIfAbsent(1, "1");
//
//// Atomic replace.
//        cache.replace(1, "1", "2");
//    }
//}

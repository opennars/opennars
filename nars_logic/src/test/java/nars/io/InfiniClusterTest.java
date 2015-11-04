package nars.io;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import nars.Memory;
import nars.NAR;
import nars.bag.impl.InfiniCacheBag;
import nars.nar.Default;
import nars.task.Task;
import nars.term.Term;
import nars.time.RealtimeMSClock;
import nars.util.data.Util;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.db.InfiniPeer;
import org.infinispan.Cache;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 10/10/15.
 */
public class InfiniClusterTest {

    public synchronized static NAR newClusterPeer(Supplier<Cache<Term,byte[]>> s, int port) throws IOException {
        return new Default(
                new Memory(
                        new RealtimeMSClock(),
                        new XorShift1024StarRandom(1),
                        new InfiniCacheBag(
                                s.get()
                        )
                ),
                1024,
                1, 2, 3
        );
    }

    NAR a, b;

    //public void testInfinispanCluster() throws IOException {
    @Ignore //this test works it just has a delay for initializing network which we dont need
    @Test
    public void testCluster() throws InterruptedException {


        Thread ta = new Thread(() -> {

            try {
                a = newClusterPeer(() ->
                                InfiniPeer.clusterAndFile("a",
                                        InfiniPeer.tmpDefaultPath(), 2048
                                ).the("testCluster"),
                        8080
                );
                System.out.println(a + " ready");
                a.input("a:b. b:c. c:d.");
                a.frame(50);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        Thread tb = new Thread(()-> {
            try {
                b = newClusterPeer(() ->
                        InfiniPeer.cluster("b").the("testCluster"),
                        8081
                );
                System.out.println(b + " ready");

                System.out.println("A=" + a!=null ? a.memory.concepts.size() : null + " concepts");
                System.out.println("B=" + b.memory.concepts.size() + " concepts");



                //int numA = a.nar.memory.concepts.size();
                //(b.nar.memory.concepts.size() >= numA );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        ta.start();
        Util.pause(1000);
        tb.start();
        Util.pause(1000);
        ta.join();
        tb.join();

        assertTrue(b.memory.concepts.size() > 3);

    }

    @Test public void testInfiniSpanPersistence() {
        System.out.println("starting cache");
        Cache<Term, byte[]> s = InfiniPeer.file(
                InfiniPeer.tmpDefaultPath(), 2048
        ).the("testInfiniSpanPersistence");
        s.clear();
        Default d = new Default(
                new Memory(
                        new RealtimeMSClock(),
                        new XorShift1024StarRandom(1),
                        new InfiniCacheBag(
                                s
                        )
                ),
                1024,
                1, 2, 3
        );
        d.input("a:b. b:c. c:d.");
        d.frame(4);

        Set<Task> before = getAllTasks(d);

        System.out.println("stopping cache manager");
        s.getCacheManager().stop();

        //Util.pause(100);

        System.out.println("restarting cache");
        Default e = new Default(
                new Memory(
                        new RealtimeMSClock(),
                        new XorShift1024StarRandom(1),
                        new InfiniCacheBag(
                            //load again
                            InfiniPeer.file(
                                    InfiniPeer.tmpDefaultPath(), 2048
                            ).the("testInfiniSpanPersistence")
                        )
                ),
                1024,
                1, 2, 3
        );

        Set<Task> after = getAllTasks(e);

        System.out.println(before.size() + " vs. " + after.size());
        System.out.println(" before: " + before);
        System.out.println("  after: " + after);

        System.out.println("unsaved: " + Sets.difference(before, after));

        Assert.assertEquals(before, after);

//        for (Concept c : e.memory.concepts) {
//            System.out.println(c + " " +c.getBeliefs());
//        }

    }



    private static Set<Task> getAllTasks(NAR n) {
        HashSet<Task> s = new HashSet<Task>();
        n.memory.concepts.forEach(c -> {
            if (c != null)//temporar
                Iterators.addAll(s, c.iterateTasks(true, true, true, true));
        });
        return s;
    }

}

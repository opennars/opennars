package nars.io;

import nars.Memory;
import nars.NAR;
import nars.bag.impl.InfiniCacheBag;
import nars.clock.RealtimeMSClock;
import nars.concept.Concept;
import nars.nar.Default;
import nars.term.Term;
import nars.util.data.Util;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.db.InfiniPeer;
import org.infinispan.Cache;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 10/10/15.
 */
public class InfiniClusterTest {

    public synchronized static NAR newClusterPeer(Supplier<Cache<Term,Concept>> s, int port) throws IOException {
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
    @Test
    public void testCluser() throws InterruptedException {


        Thread ta = new Thread(() -> {

            try {
                a = newClusterPeer(() ->
                                InfiniPeer.clusterAndFile("a",
                                        InfiniPeer.tmpDefaultPath(), 2048
                                ).the("common"),
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
                                InfiniPeer.cluster("b").the("common"),
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

        assertTrue(b.memory.concepts.size() > 4);

    }


}

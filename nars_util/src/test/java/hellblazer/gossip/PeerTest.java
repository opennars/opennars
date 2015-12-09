package hellblazer.gossip;

import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by me on 5/22/15.
 */
public class PeerTest {

    @Test
    public void testPeers() throws IOException, InterruptedException {
        AtomicBoolean received = new AtomicBoolean(false);

        GossipPeer a = new GossipPeer(10001) {


        };

        GossipPeer b = new GossipPeer(10002) {

            @Override

            public void onUpdate(UUID id, Object j) {

                System.out.println(j + " " + j.getClass());
                assertEquals(TestBean.class, j.getClass());
                assertEquals("test", ((TestBean)j).value);
                //assertEquals("{\"value\":\"test\"}", j.toString());
                received.set(true);
                stop();
                a.stop();
            }
        };
        b.connect("localhost", 10001);

        //a.put((Serializable)new byte[] { 0, 1, 2, 3, 4, 5 });
        //a.put((Serializable)new byte[] { 0, 1, 2, 3, 4, 5 });
        //a.put("test");
        a.put(new TestBean("test"));

        a.gossip.waitFor(2000);

        System.out.println("finished");

        assertTrue(received.get());
    }

    public static class TestBean implements Serializable {
        private final String value;

        public TestBean(String v) {
            value = v;
        }

        public String getValue() {
            return value;
        }
    }
}

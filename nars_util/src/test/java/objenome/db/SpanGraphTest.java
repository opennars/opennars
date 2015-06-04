package objenome.db;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;
import nars.util.db.InfiniPeer;
import nars.util.db.MapGraph;
import nars.util.db.SpanGraph;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 6/3/15.
 */
public class SpanGraphTest {

    final static String graphID = "h";

    final static Function<String, SpanGraph> graph = (String x) -> {
        return new SpanGraph(graphID, InfiniPeer.start(x));
    };

    @Test
    public void testVertexPropagation() throws InterruptedException {


        //final List<Vertex> receivedByB = new ArrayList(1);

        final AtomicReference<SpanGraph> b = new AtomicReference(null);


        SpanGraph a = graph.apply("PeerA");

        Vertex v = a.addVertex("x");
        assertEquals("correct vertex id", v.getId(), "x");
        assertEquals("non-string vertex id", ((MapGraph.MVertex)a.addVertex(17)).getId(), 17);

        Thread x = new Thread(() -> {

            try {
                int preDelayMS = 10;
                int afterConnectedDelayMS = 100;

                sleep(preDelayMS);

                SpanGraph g = graph.apply("PeerB");
                b.set(g);


                sleep(afterConnectedDelayMS);
            }
            catch (Throwable e) {
                assertTrue(e.toString(), false);
            }

        });

        x.start();
        x.join();

        assertEquals(a, b.get());


    }


    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

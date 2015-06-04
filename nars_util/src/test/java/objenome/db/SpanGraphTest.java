package objenome.db;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import nars.util.db.InfiniPeer;
import nars.util.db.MapGraph;
import nars.util.db.SpanGraph;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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

        final AtomicReference<SpanGraph> bRef = new AtomicReference(null);


        SpanGraph a = graph.apply("PeerA");

        Vertex vx = a.addVertex("x");
        Vertex vy = a.addVertex("y");
        assertEquals("correct vertex id", vx.getId(), "x");
        assertEquals("correct vertex id", vy.getId(), "y");
        assertEquals("non-string vertex id", ((MapGraph.MVertex)a.addVertex(17)).getId(), 17);
        assertEquals(3, a.vertexCount());

        Thread x = new Thread(() -> {

            try {
                int preDelayMS = 10;
                int afterConnectedDelayMS = 100;

                sleep(preDelayMS);

                SpanGraph b = graph.apply("PeerB");
                bRef.set(b);

                b.addEdge("xy", "x", "y");
                assertEquals(1, b.edgeCount());

                sleep(afterConnectedDelayMS);
            }
            catch (Throwable e) {
                e.printStackTrace();
                assertTrue(e.toString(), false);
            }

        });

        x.start();
        x.join();

        SpanGraph b = bRef.get();

        assertEquals(1, a.edgeCount());
        assertEquals(0, a.differentEdges(b).size());
        assertEquals(0, b.differentEdges(a).size());
        assertEquals("Graphs:\n" + a.toString() + "\n" + b.toString(), a, b);


    }


    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

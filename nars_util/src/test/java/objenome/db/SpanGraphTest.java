package objenome.db;

import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Vertex;
import nars.util.db.InfiniPeer;
import nars.util.db.SpanGraph;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 6/3/15.
 */
public class SpanGraphTest {

    final static String  graphID = "h";

    final static Function<String,SpanGraph> graph = (String x) -> {
        return new SpanGraph(graphID, InfiniPeer.start(x));
    };

    @Test
    public void testVertexPropagation() throws InterruptedException {


        final List<Vertex> receivedByB = new ArrayList(1);



        final Runnable b = () -> {

            int preDelayMS = 10;
            int afterConnectedDelayMS = 100;

            sleep(preDelayMS);

            SpanGraph g = graph.apply("PeerB");

            sleep(afterConnectedDelayMS);

            Iterables.addAll(receivedByB, g.getVertices());
        };


        SpanGraph g = graph.apply("PeerA");

        Vertex v = g.addVertex("x");

        Thread x = new Thread(b);
        x.start();
        x.join();


        assertEquals(1, receivedByB.size());
        assertEquals("[v[x]]", receivedByB.toString());

    }



    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

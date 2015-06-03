package objenome.db;

import com.tinkerpop.blueprints.Vertex;
import nars.util.db.SpanGraph;
import nars.util.db.InfiniPeer;

/**
 * Created by me on 6/3/15.
 */
public class SpanGraphTest {


    public static final String graphID = "h";
    static final Runnable b = () -> {


        sleep(1000);

        InfiniPeer peer = InfiniPeer.start("PeerB");
        SpanGraph g = new SpanGraph(graphID, peer);

        sleep(1000);

        System.out.println( "b received: " + g.getVertices() );
    };

    static final Runnable a = () -> {

        InfiniPeer peer = InfiniPeer.start("PeerA");

        SpanGraph g = new SpanGraph("h", peer);
        Vertex v = g.addVertex("x");

        new Thread(b).start();

    };



    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)  {
        new Thread(a).start();

    }
}

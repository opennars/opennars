package automenta.vivisect.dimensionalize;

import nars.util.graph.NARGraph;
import org.jgrapht.graph.DefaultEdge;

import java.awt.*;

/**
* Created by me on 3/12/15.
*/
public class UIEdge<V> extends DefaultEdge {
    public final V s, t;
    public final Object e;
    public Shape shape;

    public UIEdge(V s, V t, Object e) {
        super();
        this.s = s;
        this.t = t;

        if (e instanceof NARGraph.NAREdge) {
            this.e = ((NARGraph.NAREdge)e).getObject();
        }
        else {
            this.e = e;
        }
    }

    @Override
    public V getSource() {
        return s;
    }

    @Override
    public V getTarget() {
        return t;
    }

}

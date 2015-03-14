package automenta.vivisect.dimensionalize;

import nars.logic.entity.Named;
import nars.util.graph.NARGraph;
import org.jgrapht.graph.DefaultEdge;

import java.awt.*;

/**
* Created by me on 3/12/15.
*/
public class UIEdge<V extends Named> extends DefaultEdge {
    final V s, t;
    public final Object e;
    public final String name;
    public Shape shape;

    public UIEdge(V s, V t, Object e) {
        super();
        this.s = s;
        this.t = t;
        this.name = e.toString() + this.s.name() + ':' + this.t.name() + ':';

        if (e instanceof NARGraph.NAREdge) {
            this.e = ((NARGraph.NAREdge)e).getObject();
        }
        else {
            this.e = e;
        }
    }


    @Override
    public boolean equals(Object obj) {
        return name.equals(((UIEdge)obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
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

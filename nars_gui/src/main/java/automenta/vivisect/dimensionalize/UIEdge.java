package automenta.vivisect.dimensionalize;

import javolution.util.FastSet;
import nars.logic.entity.Named;
import org.jgrapht.graph.DefaultEdge;

import java.awt.*;
import java.util.Set;

/**
* Created by me on 3/12/15.
*/
public class UIEdge<V extends Named> extends DefaultEdge {

    final V s, t;

    /** items contained in this edge */
    public final Set<Named> e = new FastSet<Named>().atomic();

    public final String name;
    public Shape shape;

    public UIEdge(V s, V t) {
        super();
        this.s = s;
        this.t = t;


        this.name = this.s.name().toString() + ':' + this.t.name();

    }

    public UIEdge<V> add(Named item) {
        e.add(item);
        return this;
    }
    public UIEdge<V> remove(Named item) {
        e.remove(item);
        return this;
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

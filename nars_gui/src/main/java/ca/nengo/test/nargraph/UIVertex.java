package ca.nengo.test.nargraph;

import automenta.vivisect.dimensionalize.UIEdge;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import com.google.common.collect.Iterables;
import javolution.util.FastSet;
import nars.logic.entity.Named;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.piccolo2d.util.PAffineTransform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

abstract public class UIVertex<V extends Named> extends AbstractWidget implements Named {

    public final V vertex;
    private final ArrayRealVector coords;
    protected long layoutPeriod = -1;
    boolean destroyed = false;

    final Set<UIEdge> incoming = new FastSet<UIEdge>().atomic();
    final Set<UIEdge> outgoing = new FastSet<UIEdge>().atomic();

    public UIVertex(V vertex) {
        super(vertex.name().toString());
        this.vertex = vertex;
        this.coords = new ArrayRealVector(2);

        //initial random position, to seed layout
        double x, y;
        move(x = 1000 * (Math.random() - 0.5), y = 1000*(Math.random() - 0.5));
        coords.setEntry(0, x); coords.setEntry(1, y);
    }

    @Override
    public Object name() {
        return vertex;
    }

    abstract public float getPriority();

    @Override
    protected void destroy() {
        //System.out.println("before destroy " + this);
        destroyed = true;
        setBounds(0,0,0,0);
    }

    @Override
    public String toString() {
        return "NARGraphVertex[" + getName() + ']';
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UIVertex) {
            return getName().equals(((UIVertex) obj).getName());
        }
        return false;
    }

    @Override
    protected void paint(PaintContext paintContext, double width, double height) {

    }

    public V vertex() {
        return this.vertex;
    }


    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        return vertex.toString();
    }

    public ArrayRealVector getCoordinates() {
        //if (destroyed) return null;
        return coords;
    }

    /** loads the actual geometric coordinates to the coords array prior to next layout iteration */
    public void getActualCoordinates(long layoutPeriod /* in realtime msec */) {
        double x = getX();
        double y = getY();
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            this.layoutPeriod = -1;
            return;
        }
        coords.setEntry(0, x);
        coords.setEntry(1, y);
        this.layoutPeriod = layoutPeriod;
    }

    public double getX() {
        PAffineTransform tr = ui.getPNode().getTransformReference(false);
        if (tr!=null) return tr.getTranslateX();
        return Double.NaN;
    }
    public double getY() {
        PAffineTransform tr = ui.getPNode().getTransformReference(false);
        if (tr!=null) return tr.getTranslateY();
        return Double.NaN;
    }


    private Set<UIEdge> getEdgeSet(final boolean in) {
        return in ? incoming : outgoing;
    }

    public void link(UIEdge v, boolean in) {
        if (destroyed) return;
        getEdgeSet(in).add(v);
    }

    public void unlink(UIEdge v, boolean in) {
        if (destroyed) return;
        getEdgeSet(in).remove(v);
    }

    abstract public boolean isDependent();

    public Iterable<UIEdge> getEdges(boolean in, boolean out) {
        if (destroyed) return Collections.emptyList();

        boolean i = !incoming.isEmpty();
        boolean o = !outgoing.isEmpty();
        if (i && o)
            return Iterables.concat(incoming, outgoing);
        else if (i)
            return incoming;
        else if (o)
            return outgoing;
        else
            return Collections.emptyList();
    }

    public int degree() {
        return incoming.size() + outgoing.size();
    }
}

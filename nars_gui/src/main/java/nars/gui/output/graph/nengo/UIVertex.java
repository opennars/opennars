package nars.gui.output.graph.nengo;

import ca.nengo.model.SimulationException;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import nars.util.data.id.Named;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.piccolo2d.PNode;
import org.piccolo2d.util.PAffineTransform;

import javax.swing.*;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

abstract public class UIVertex<V extends Named> extends AbstractWidget {

    public final V vertex;
    private final ArrayRealVector coords;
    private final PNode links;
    protected long layoutPeriod = -1;
    boolean destroyed = false;

    final Set<UIEdge<UIVertex<V>>> incoming = new CopyOnWriteArraySet<>();//.atomic();
    final Set<UIEdge<UIVertex<V>>> outgoing = new CopyOnWriteArraySet<>();//.atomic();

    public UIVertex(V vertex) {
        super(vertex.toString());
        this.vertex = vertex;
        this.coords = new ArrayRealVector(2);

        //initial random position, to seed layout
        double x, y;
        move(x = 1000 * (Math.random() - 0.5), y = 1000*(Math.random() - 0.5));
        coords.setEntry(0, x); coords.setEntry(1, y);

        links = new PNode();
        ui.getPNode().addChild(links);
        links.lowerToBottom();

    }


    abstract public float getPriority();

    @Override
    protected void destroy() {
        //System.out.println("before destroy " + this);
        destroyed = true;
        //setBounds(0,0,0,0);
    }

    @Override
    public String toString() {
        return "NARGraphVertex[" + name() + ']';
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UIVertex) {
            return name().equals(((UIVertex) obj).name());
        }
        return false;
    }

    public void update() {
        for (UIEdge e : getEdgeSet(false)) {
            e.update();
        }

    }

    @Override
    protected void paint(PaintContext paintContext, double width, double height) {
        for (UIEdge e : getEdgeSet(false)) {
            e.render();
        }

    }

    public V vertex() {
        return this.vertex;
    }

    @Override
    public void run(float startTime, float endTime) throws SimulationException {
        //NEVER CALLED
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


    private Set<UIEdge<UIVertex<V>>> getEdgeSet(final boolean in) {
        return in ? incoming : outgoing;
    }

    public boolean link(UIEdge v, boolean in) {
        if (destroyed) return false;
        if (getEdgeSet(in).add(v)) {
            if (!in) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        links.addChild(v.getPNode());
                    }
                });
            }
            return true;
        }
        return false;
    }

    public boolean unlink(UIEdge v, boolean in) {
        if (destroyed) return false;
        if (getEdgeSet(in).remove(v)) {
            if (!in) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        links.removeChild(v.getPNode());
                    }
                });
            }
            return true;
        }
        return false;
    }

    abstract public boolean isDependent();

    public Set<UIEdge<UIVertex<V>>> getEdgesIn() {
        return incoming;
    }
    public Set<UIEdge<UIVertex<V>>> getEdgesOut() {
        return outgoing;
    }

//    /** Iterables.concat makes a copy of the list, unnecessary */
//    @Deprecated public Iterable<UIEdge<UIVertex<V>>> getEdges(boolean in, boolean out) {
//        if (destroyed) return Collections.emptyList();
//
//        boolean i = !incoming.isEmpty();
//        boolean o = !outgoing.isEmpty();
//        if (i && o)
//            return Iterables.concat(incoming, outgoing);
//        else if (i)
//            return incoming;
//        else if (o)
//            return outgoing;
//        else
//            return Collections.emptyList();
//    }

    public int degree() {
        return incoming.size() + outgoing.size();
    }

    /** if unadded, return null to prevent creation, or return this to allow this node to be created. otherwise just return this */
    abstract public UIVertex add(Named v);

    /** return null to prevent deltetion, or return this to allow this node to be deleted */
    public abstract UIVertex remove(Named v);

    public abstract double getRadius();
}

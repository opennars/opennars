package ca.nengo.test.nargraph;

import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import nars.logic.entity.Named;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.piccolo2d.util.PAffineTransform;

import java.util.HashMap;

/**
* Created by me on 3/12/15.
*/
abstract public class UIVertex<V extends Named> extends AbstractWidget {

    public final V vertex;
    private final ArrayRealVector coords;
    protected long layoutPeriod = -1;


    public UIVertex(V vertex) {
        super(vertex.name().toString());
        this.vertex = vertex;
        this.coords = new ArrayRealVector(2);

        //initial random position, to seed layout
        double x, y;
        move(x = 1000 * (Math.random() - 0.5), y = 1000*(Math.random() - 0.5));
        coords.setEntry(0, x); coords.setEntry(1, y);
    }

    abstract public float getPriority();

    @Override
    protected void beforeDestroy() {
        System.out.println("before destroy " + this);
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
        if (obj instanceof UIVertex)
            return getName().equals(((UIVertex) obj).getName());
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


}

package ca.nengo.test.nargraph;

import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.util.ScriptGenException;
import nars.logic.entity.Named;
import org.apache.commons.math3.linear.ArrayRealVector;

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
        ui.dragTo(Math.random() - 0.5, Math.random() - 0.5);
        getActualCoordinates(0);
    }

    abstract public float getPriority();

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
        return ui.getPNode().getTransform().getTranslateX();
    }
    public double getY() {
        return ui.getPNode().getTransform().getTranslateY();
    }
}

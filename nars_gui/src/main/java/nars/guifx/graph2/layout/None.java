package nars.guifx.graph2.layout;

import automenta.vivisect.dimensionalize.IterativeLayout;
import nars.guifx.graph2.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;


public class None implements IterativeLayout {

    public final ArrayRealVector zero = new ArrayRealVector(2);

    @Override
    public ArrayRealVector getPosition(Object vertex) {
        return zero;
    }

    @Override
    public void run(NARGraph graph, int iterations) {

    }

    @Override
    public void resetLearning() {

    }

    @Override
    public double getRadius(Object vertex) {
        return 0;
    }

    @Override
    public void init(Object n) {

    }
}

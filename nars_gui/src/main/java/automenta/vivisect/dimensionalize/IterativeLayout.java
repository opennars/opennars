package automenta.vivisect.dimensionalize;


import nars.guifx.graph2.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.Collection;

public interface IterativeLayout<V,E> {

    ArrayRealVector getPosition(V vertex);

    public void run(NARGraph graph, int iterations);

    void resetLearning();

    double getRadius(V vertex);

    default public double getEdgeWeight(E e) {
        return 1.0;
    }

    default public void pre(Collection<V> vertices) {

    }

    void init(V n);

}

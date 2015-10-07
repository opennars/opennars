package automenta.vivisect.dimensionalize;


import nars.guifx.graph2.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;

public interface IterativeLayout<V,E> {

    ArrayRealVector getPosition(V vertex);

    void run(NARGraph graph, int iterations);

    void resetLearning();

    double getRadius(V vertex);

//    default public double getEdgeWeight(E e) {
//        return 1.0;
//    }


    default void pre(V[] vertices) {

    }

    /** setup starting conditions for a new node */
    void init(V n);

}

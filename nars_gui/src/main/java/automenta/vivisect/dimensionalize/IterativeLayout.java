package automenta.vivisect.dimensionalize;


import nars.guifx.graph2.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;

public interface IterativeLayout<V,E> {

    @Deprecated default ArrayRealVector getPosition(V vertex) {
        return null;
    }

    default void pre(V[] vertices) {

    }

    void run(NARGraph graph, int iterations);


    @Deprecated default double getRadius(V vertex) {
        return 1.0;
    }

//    default public double getEdgeWeight(E e) {
//        return 1.0;
//    }




    /** setup starting conditions for a new node */
    void init(V n);

}

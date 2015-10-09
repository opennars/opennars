package nars.guifx.graph2.layout;


import nars.guifx.graph2.SpaceGrapher;
import nars.guifx.graph2.TermNode;

public interface IterativeLayout<V extends TermNode> {


    /** setup starting conditions for a new node */
    default void init(V n) {

    }

    @Deprecated default void pre(V[] vertices) {

    }

    void run(SpaceGrapher graph, int iterations);


    @Deprecated default double getRadius(V vertex) {
        return 1.0;
    }

//    default public double getEdgeWeight(E e) {
//        return 1.0;
//    }







}

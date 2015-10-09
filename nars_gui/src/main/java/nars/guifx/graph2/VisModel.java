package nars.guifx.graph2;

import nars.guifx.graph2.source.SpaceGrapher;

import java.util.function.Consumer;

/** graph node visualization */
public interface VisModel<K extends Comparable, T extends TermNode<K>> extends Consumer<T> {

    T newNode(K t);

    default void start(SpaceGrapher<K,T> g) {

    }

    default void stop(SpaceGrapher<K,T> gg) {

    }


//    void apply(TermNode t);

//    Color getEdgeColor(double termPrio, double taskMean);
//
//    Paint getVertexColor(double priority, float conf);
//
//    double getVertexScale(Concept c);
}

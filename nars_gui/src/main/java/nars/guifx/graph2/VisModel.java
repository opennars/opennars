package nars.guifx.graph2;

import nars.term.Termed;

import java.util.function.Consumer;

/** graph node visualization */
public interface VisModel<K extends Termed, T extends TermNode> extends Consumer<T> {

    T newNode(K t);

    default void start(SpaceGrapher g) {

    }

    default void stop(SpaceGrapher gg) {

    }


//    void apply(TermNode t);

//    Color getEdgeColor(double termPrio, double taskMean);
//
//    Paint getVertexColor(double priority, float conf);
//
//    double getVertexScale(Concept c);
}

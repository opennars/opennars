package nars.guifx.graph2;

import nars.guifx.graph2.source.SpaceGrapher;
import nars.term.Termed;

import java.util.function.Consumer;

/** graph node visualization */
public interface NodeVis extends Consumer<TermNode> {

    TermNode newNode(Termed t);

    default void start(SpaceGrapher g) {

    }

    default void stop(SpaceGrapher gg) {

    }

    default void updateNode(TermNode prev) {

    }


//    void apply(TermNode t);

//    Color getEdgeColor(double termPrio, double taskMean);
//
//    Paint getVertexColor(double priority, float conf);
//
//    double getVertexScale(Concept c);
}

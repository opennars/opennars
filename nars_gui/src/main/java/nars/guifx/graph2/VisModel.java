package nars.guifx.graph2;

import nars.term.Term;

import java.util.function.Consumer;

/** graph node visualization */
public interface VisModel<T extends TermNode> extends Consumer<T> {

    T newNode(Term t);

    default void start(NARGraph g) {

    }

    default void stop(NARGraph gg) {

    }


//    void apply(TermNode t);

//    Color getEdgeColor(double termPrio, double taskMean);
//
//    Paint getVertexColor(double priority, float conf);
//
//    double getVertexScale(Concept c);
}

package nars.guifx.graph2;

import nars.term.Term;

import java.util.function.Consumer;

/** graph node visualization */
public interface VisModel extends Consumer<TermNode> {

    TermNode newNode(Term t);


//    void apply(TermNode t);

//    Color getEdgeColor(double termPrio, double taskMean);
//
//    Paint getVertexColor(double priority, float conf);
//
//    double getVertexScale(Concept c);
}

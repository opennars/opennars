package nars.guifx.graph2;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import nars.concept.Concept;

/**
 * Created by me on 9/5/15.
 */
public interface VisModel {

    Color getEdgeColor(double termPrio, double taskMean);

    Paint getVertexColor(double priority, float conf);

    double getVertexScale(Concept c);
}

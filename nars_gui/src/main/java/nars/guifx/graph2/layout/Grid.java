package nars.guifx.graph2.layout;

import javafx.beans.property.SimpleDoubleProperty;
import nars.guifx.graph2.TermNode;

/**
 * Created by me on 9/6/15.
 */
public class Grid extends Linear {

    /** desired aspect ratio of the arrangement of rows/cols */
    public final SimpleDoubleProperty aspectRatio = new SimpleDoubleProperty(100);

    public final SimpleDoubleProperty scale = new SimpleDoubleProperty(100);
    public final SimpleDoubleProperty margin = new SimpleDoubleProperty(100);


    @Override
    public void setPosition(TermNode v, int i, int max) {
        double size = 50; //temproary
        double spacing = 50;
        int cols = (int) Math.ceil(Math.sqrt(max));
        double x = i / ((double)cols), y = i % cols;
        v.move(x * (size + spacing/2), y* (size + spacing/2), 0.1, 0.05);
    }


}

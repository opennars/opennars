package nars.guifx;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;

/**
 * Created by me on 9/13/15.
 */
public class ResizableCanvas extends Canvas {




    public ResizableCanvas() {

        parentProperty().addListener((z,p,n) -> {
            if (n==null) return;

            //n.boundsInParentProperty().addListener((c,a,b) -> {
                //if (n instanceof Control) {
                    Region x = (Region)n;
                        //System.out.println(x + " ");
                    widthProperty().bind(x.widthProperty());
                    heightProperty().bind(x.heightProperty());
                //}
            //});
        });

        init();
    }

    public ResizableCanvas(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height) {

        // Bind canvas size to stack pane size.
        widthProperty().bind(width);
        heightProperty().bind(height);

        init();
    }

    private void init() {

        boolean bindRedraw = true; //TODO parameter to make this optional to avoid unnecessary event being attached
        if (bindRedraw) {
            // Redraw canvas when size changes.
            widthProperty().addListener(evt -> draw());
            heightProperty().addListener(evt -> draw());
        }

    }

    protected void draw() {
        /*double width = getWidth();
        double height = getHeight();

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        gc.setStroke(Color.RED);
        gc.strokeLine(0, 0, width, height);
        gc.strokeLine(0, height, width, 0);*/
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}

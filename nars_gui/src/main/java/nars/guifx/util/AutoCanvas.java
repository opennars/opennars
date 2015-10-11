package nars.guifx.util;


import javafx.scene.canvas.Canvas;

/**
 * autosizing and other functions
 */
abstract public class AutoCanvas extends Canvas {


    public AutoCanvas() {
        super();
        // Redraw canvas when size changes.
        widthProperty().addListener(evt -> render());
        heightProperty().addListener(evt -> render());
    }

    abstract protected void render();


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

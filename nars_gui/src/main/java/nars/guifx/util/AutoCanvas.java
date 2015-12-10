package nars.guifx.util;


import javafx.scene.canvas.Canvas;

/**
 * autosizing and other functions
 */
@SuppressWarnings("AbstractClassNeverImplemented")
public abstract class AutoCanvas extends Canvas {


    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public AutoCanvas() {
        // Redraw canvas when size changes.
        widthProperty().addListener(evt -> render());
        heightProperty().addListener(evt -> render());
    }

    protected abstract void render();


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

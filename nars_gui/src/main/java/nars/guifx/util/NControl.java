package nars.guifx.util;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;

/**
 * Created by me on 10/11/15.
 */
public abstract class NControl extends StackPane {

    public final ChangeListener<Number> redrawOnDoubleChange = (observable, oldValue, newValue) -> {
        //TODO debounce these with a AtomicBoolean or something
        redraw();
    };

    protected final Canvas canvas;

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public NControl(double w, double h) {

        Canvas canvas = this.canvas = new Canvas(w, h);

        ReadOnlyObjectProperty<Bounds> bp = boundsInParentProperty();

        //maxWidth(Double.MAX_VALUE);
        //maxHeight(Double.MAX_VALUE);

        if (w <= 0 && h <= 0) {

            canvas.boundsInParentProperty().addListener((b) -> {

                setWidth(bp.get().getHeight());
                setHeight(bp.get().getHeight());
                redraw();
            });

        } else if (h <= 0) {
            canvas.maxHeight(Double.MAX_VALUE);
            canvas.boundsInParentProperty().addListener((b) -> {
                setHeight(bp.get().getHeight());
                redraw();
            });
        } else {
            setHeight(h);
        }
        if (w <= 0) {
            //maxWidth(Double.MAX_VALUE);
            boundsInParentProperty().addListener((b) -> {
                canvas.setWidth(bp.get().getWidth());
                setPrefWidth(bp.get().getWidth());
                setWidth(bp.get().getWidth());
                redraw();
            });
            //((DoubleProperty)widthProperty()).bind( canvas.widthProperty() );
            //setWidth(bp.get().getWidth());
        } else {
            setWidth(w);
        }


        canvas.widthProperty().addListener(redrawOnDoubleChange);
        canvas.heightProperty().addListener(redrawOnDoubleChange);

        getChildren().setAll(canvas);



    }

    public final GraphicsContext graphics() {
        return canvas.getGraphicsContext2D();
    }

    protected abstract void redraw();
}

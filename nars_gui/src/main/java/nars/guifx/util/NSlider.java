package nars.guifx.util;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * versatile light-weight slider component for javafx
 */
public class NSlider extends StackPane {


    public final DoubleProperty value = new SimpleDoubleProperty(0.5);
    public final DoubleProperty min = new SimpleDoubleProperty(0);
    public final DoubleProperty max = new SimpleDoubleProperty(1);

    private transient final GraphicsContext g;

    public final ChangeListener<Number> redrawOnDoubleChange = (observable, oldValue, newValue) -> {
        //TODO debounce these with a AtomicBoolean or something
        redrawLater();
    };
    private final Canvas canvas;

    public NSlider(double w, double h) {
        this(null, w, h);
    }

    final StringProperty label;

    public NSlider(String label, double w, double h) {
        super();

        this.canvas = new Canvas(w, h);

        Label overlay = new Label();
        {
            this.label = overlay.textProperty();
            overlay.setMouseTransparent(true);
            overlay.setBlendMode(BlendMode.DIFFERENCE);
        }
        this.label.set(label);

        if (h <= 0) {
            canvas.maxHeight(Double.MAX_VALUE);
            canvas.boundsInParentProperty().addListener((b) -> {
                setHeight( boundsInParentProperty().get().getHeight() );
                redraw();
            });
        }
        else {
            setHeight(h);
        }
        if (w <= 0) {
            maxWidth(Double.MAX_VALUE);
            boundsInParentProperty().addListener((b) -> {
                setWidth( boundsInParentProperty().get().getWidth() );
                redraw();
            });
        }
        else {
            setWidth(w);
        }





        //setManaged(false);
        //setPickOnBounds(false);
        //setMouseTransparent(true);


        //widthProperty().bind(widthProperty());
        //heightProperty().bind(heightProperty());

        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                canvas.setCursor(Cursor.MOVE);
                dragChange(mouseEvent);
            }
        });

        canvas.setCursor(Cursor.CROSSHAIR);

        canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                canvas.setCursor(Cursor.CROSSHAIR);
            }
        });
        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                canvas.setCursor(Cursor.MOVE);
                dragChange(mouseEvent);
            }
        });

        value.addListener(redrawOnDoubleChange);
        min.addListener(redrawOnDoubleChange);
        max.addListener(redrawOnDoubleChange);
        canvas.widthProperty().addListener(redrawOnDoubleChange);
        canvas.heightProperty().addListener(redrawOnDoubleChange);

        g = canvas.getGraphicsContext2D();
        redraw();



        getChildren().setAll(canvas, overlay);


    }

    protected void dragChange(MouseEvent mouseEvent) {
        double dx = mouseEvent.getX();
        double dy = mouseEvent.getY();

        double min = this.min.get();
        double max = this.max.get();
        double v = p(dx, dy) * (max-min) + min ;
        if (v < min) v = min;
        if (v > max) v = max;
        value.set( v );
        //System.out.println(dx + " " + dy + " " + value.get());
    }

    private double p(double dx, double dy) {
        return dx / canvas.getWidth();
    }

    protected void redrawLater() {
        Platform.runLater(()->redraw());
    }

    /** value to proportion of width */
    public double p() {
        final double v = this.value.get();
        final double min = this.min.get();
        final double max = this.max.get();
        return (v - min) / (max - min);
    }

    protected void redraw() {
        double W = canvas.getWidth();
        double H = canvas.getHeight();

        double p = p();
        double barSize = W * p;

        double margin = 4;
        double mh = margin/2.0;


        g.setFill(Color.BLACK);
        //g.fillRect(W-barSize, 0, W-barSize, H);
        g.fillRect(0, 0, W, H);

        g.setLineWidth(mh*2);
        g.setStroke(Color.GRAY);
        g.strokeRect(0, 0, W, H);

        g.setLineWidth(0);
        double hp = 0.5 + 0.5 * p;
        g.setFill(Color.ORANGE.deriveColor(70 * (p - 0.5), hp, hp, 1f));
        g.fillRect(mh, mh, barSize - mh*2, H - mh*2);
    }

    public NSlider set(double v, double min, double max) {
        this.value.set(v);
        this.min.set(min);
        this.max.set(max);
        return this;
    }

    public NSlider bind(DoubleProperty p) {
        p.bindBidirectional(value);
        return this;
    }



//    public static void makeDraggable(final Stage stage, final Node byNode) {
//        final Delta dragDelta = new Delta();
//        byNode.setOnMouseEntered(new EventHandler<MouseEvent>() {
//            @Override public void handle(MouseEvent mouseEvent) {
//                if (!mouseEvent.isPrimaryButtonDown()) {
//                    byNode.setCursor(Cursor.HAND);
//                }
//            }
//        });
//        byNode.setOnMouseExited(new EventHandler<MouseEvent>() {
//            @Override public void handle(MouseEvent mouseEvent) {
//                if (!mouseEvent.isPrimaryButtonDown()) {
//                    byNode.setCursor(Cursor.DEFAULT);
//                }
//            }
//        });
//    }
}

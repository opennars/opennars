package nars.guifx.util;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;
import nars.guifx.NARfx;

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
        this(label, w, h, BarSlider);
    }

    public NSlider(String label, double w, double h, NSliderVis vis) {
        super();

        this.vis = vis;

        this.canvas = new Canvas(w, h);

        Text overlay = new Text();
        {
            this.label = overlay.textProperty();
            overlay.setSmooth(false);
            overlay.setFill(Color.WHITE);
            overlay.setMouseTransparent(true);
            //overlay.setBlendMode(BlendMode.EXCLUSION);
            //overlay.setManaged(false);
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

    public interface NSliderVis {

        /**
         *
         * @param p proportional position, between 0 and 1.0
         * @param canvas target canvas
         * @param w parent width
         * @param h parent height
         * @param g re-usable graphics context
         */
        void redraw(double p, Canvas canvas, double w, double h, GraphicsContext g);

    }

    public NSliderVis vis;

    public static final NSliderVis BarSlider = (p, canvas1, W, H, g1) -> {

        double barSize = W * p;

        double margin = 4;
        double mh = margin/2.0;


        g1.setFill(Color.BLACK);
        g1.fillRect(0, 0, W, H);

        g1.setLineWidth(mh*2);
        g1.setStroke(Color.GRAY);
        g1.strokeRect(0, 0, W, H);

        g1.setLineWidth(0);
        double hp = 0.5 + 0.5 * p;
        g1.setFill(Color.ORANGE.deriveColor(70 * (p - 0.5), hp, 0.65f, 1f));
        g1.fillRect(mh, mh, barSize - mh*2, H - mh*2);
    };

    public static final NSliderVis NotchSlider = (p, canvas, W, H, g) -> {

        double barSize = W * p;

        double margin = 4;
        double mh = margin/2.0;

        g.setFill(Color.BLACK);
        g.fillRect(0, 0, W, H);

        double notchRadius = W * 0.1;

        g.setLineWidth(0);
        double hp = 0.5 + 0.5 * p;
        g.setFill(Color.ORANGE.deriveColor(70 * (p - 0.5), hp, 0.65f, 1f));
        g.fillRect(mh + barSize - notchRadius, mh, notchRadius, H - mh*2);
    };

    public static final NSliderVis CircleKnob = (p, canvas, W, H, g) -> {

        g.setFill(Color.BLACK);
        g.fillRect(0, 0, W, H);

        double circumferenceActive = 0.95; //how much of the circumference of the interior circle is active as a dial track

        double theta = p * circumferenceActive * (2 * Math.PI);

        double margin = W/10; //margin proportional to viewing area

//        double x = W/2 + (W/2-margin) * Math.cos(theta);
//        double y = H/2 + (H/2-margin) * Math.sin(theta);
//        double t = 4;
        /*g.setFill(Color.WHITE);
        g.fillOval(x-t, y-t, t*2,t*2);*/

        double ew = W - margin * 2;
        double eh = H - margin * 2;

        g.setFill(Color.GRAY);
        g.fillOval(margin*2, margin*2, ew-margin*2, eh-margin*2);

        double hp = 0.5 + 0.5 * p;
        g.setFill(Color.ORANGE.deriveColor(70 * (p - 0.5), hp, 0.65f, 1f));


        final double atheta = theta * 180.0/Math.PI; //radian to degree
        final double knobArc = 30;
        g.fillArc( margin,margin, ew, eh,
                    atheta - knobArc/2, knobArc, ArcType.ROUND);



    };

    protected void redraw() {
        double W = canvas.getWidth();
        double H = canvas.getHeight();
        vis.redraw(p(), canvas, W, H, g);
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


    public static void main(String[] args) {
        NARfx.run((a, b)-> {

            FlowPane p = new FlowPane(16,16);

            p.getChildren().setAll(
                    new NSlider("Bar", 128, 45, NSlider.BarSlider),
                    new NSlider("Notch", 128, 45, NSlider.NotchSlider),
                    new NSlider("Notch--", 64, 25, NSlider.NotchSlider),
                    new NSlider("Circle", 128, 128, NSlider.CircleKnob)
            );

            b.setScene(new Scene(p, 800, 800));
            b.show();
        });
    }
}

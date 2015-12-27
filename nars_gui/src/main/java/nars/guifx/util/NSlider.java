package nars.guifx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import nars.guifx.NARfx;
import nars.util.Texts;

import java.util.Arrays;
import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * versatile light-weight slider component for javafx
 */
public class NSlider extends NControl {


    public final SimpleDoubleProperty[] value;
    public final SimpleDoubleProperty min = new SimpleDoubleProperty(0);
    public final SimpleDoubleProperty max = new SimpleDoubleProperty(1);

    private final int dimensions;

    public NSlider(double w, double h, double... initialVector) {
        this(w, h, BarSlider, initialVector);
    }

    public NSlider(String label, double w, double h, double... initialVector) {
        this(label, w, h, BarSlider, initialVector);
    }

    public NSlider(String label, double w, double h, Vis vis, double... initialVector) {
        this(new SimpleStringProperty(label), w, h, vis, initialVector);
    }

    public NSlider(StringProperty label, double w, double h, Vis vis, double... initialVector) {
        this(w, h, vis, initialVector);
        addLabel(label);
    }

    /** adds a label which is automatically displays an updated string representation of the value */
    public void addValueLabel() {
        StringProperty s = new SimpleStringProperty();
        SimpleDoubleProperty v = value[0];
        InvalidationListener valuer = (c) -> {
            s.setValue(Texts.n4(v.floatValue()));
        };
        v.addListener(valuer);
        valuer.invalidated(null);
        addLabel(s);
    }

    public void addLabel(StringProperty t) {
        //final StringProperty label;

        Text text = new Text(t.getValue());


        text.textProperty().bind(t);
        addLabel(text);


    }

    public void addLabel(Text text) {
        //label = text.textProperty();
        //final SimpleDoubleProperty fontScale = new SimpleDoubleProperty(0.05);
        DoubleBinding wp = widthProperty().multiply(0.01);
        text.scaleXProperty().bind(wp);
        text.scaleYProperty().bind(wp);

        text.wrappingWidthProperty().bind(widthProperty());

        //text.setWrappingWidth(w * 0.9);
        //text.setLayoutX(w / 2 - text.getLayoutBounds().getWidth() / 2);
        //text.setLayoutY(h / 2);

        text.setSmooth(false);
        text.setFill(Color.WHITE);
        text.setMouseTransparent(true);
        text.setBlendMode(BlendMode.DIFFERENCE);

        text.setBoundsType(TextBoundsType.VISUAL);
        //text.setCache(true);

        getChildren().add(text);
    }

    @Deprecated
    public NSlider(double w, double h, Vis vis, double... vector) {
        this(w, h, new LeftRightDrag(), vis, vector);
    }

    /**
     * control & vis must all support the same dimensionality as the specified initialVector. otherwise an error will appear
     */
    public NSlider(double w, double h, Control control, Vis vis, double... vector) {
        super(w, h);

        this.vis = vis;
        this.control = control;

        if ((dimensions = vector.length) == 0)
            throw new RuntimeException("zero-length vector");

        value = new SimpleDoubleProperty[dimensions];
        for (int i = 0; i < dimensions; i++) {
            (value[i] = newValueEntry(i))
                    .addListener(redrawOnDoubleChange);
        }

        min.addListener(redrawOnDoubleChange);
        max.addListener(redrawOnDoubleChange);
        control.start(this);

        value(vector); //causes initial render
    }

    public SimpleDoubleProperty newValueEntry(int i) {
        return new SimpleDoubleProperty();
    }

    /**
     * sets the value
     */
    public NSlider value(double... v) {
        ensureDimension(v);
        for (int i = 0; i < v.length; i++) {
            value(i, v[i]);
        }
        redraw();
        return this;
    }

    public NSlider value(int dimension, double v) {
        value[dimension].set(v);
        return this;
    }

    public NSlider valueLater(double... v) {
        runLater(() -> value(v));
        return this;
    }

    public final void ensureDimension(double[] v) {
        ensureDimension(v.length);
    }

    public final void ensureDimension(double v) {
        if (v != dimensions)
            throw new RuntimeException("invalid dimensions");
    }

    public double v(int index) {
        return value[index].doubleValue();
    }

    /* convenience methods: v_ */

    public double v()  {  return v(0);     }
    public double v0() {  return v(0);    }
    public double v1() {  return v(1);    }
    public double v2() {  return v(2);    }

    public double vx() {  return v(0);    }
    public double vy() {  return v(1);    }
    public double vz() {  return v(2);    }

    public interface Control {
        void start(NSlider n);

        void stop();
    }

    final Control control;


    /**
     * cache of the normalized value vector
     */
    private double[] normalized;


    public void denormalized(double... n) {
        double mn = min.get();
        double mx = max.get();

        for (int i = 0; i < n.length; i++) {
            double nn = n[i];
            double v = (nn) * (mx - mn) + mn;
            if (v < mn) v = mn; //clip to bounds
            if (v > mx) v = mx;
            value(i, v);
        }

    }

    public double[] normalized() {
        double[] n = normalized;
        if (n == null) {
            n = normalized = new double[dimensions];
        }

        //TODO only compute this if invalidated
        for (int i = 0; i < dimensions; i++) {
            n[i] = p(value[i].get());
        }

        return n;
    }

    /**
     * normalizesa a value to the specified numeric bounds
     */
    public final double p(double v) {
        double min = this.min.get();
        double max = this.max.get();
        return (v - min) / (max - min);
    }

    public interface Vis {

        /**
         * @param vector vector of the normalized proportional positions, between 0 and 1.0
         * @param canvas target canvas
         * @param w      parent width
         * @param h      parent height
         * @param g      re-usable graphics context
         */
        void redraw(double[] vector, Canvas canvas, double w, double h, GraphicsContext g) throws RuntimeException;

        static double getFirstAndOnlyDimension(double[] vector) {
            if (vector.length != 1) throw new RuntimeException("invalid dimension");
            return vector[0];
        }
    }

    public Vis vis;

    public static final Vis BarSlider = (vector, canvas1, W, H, g1) -> {


        double p = Vis.getFirstAndOnlyDimension(vector);
        double barSize = W * p;

        double margin = 4;
        double mh = margin / 2.0;


        g1.setLineWidth(mh * 2);
        g1.setStroke(Color.GRAY);
        g1.strokeRect(0, 0, W, H);

        g1.setLineWidth(0);
        double hp = 0.5 + 0.5 * p;
        g1.setFill(Color.ORANGE.deriveColor(70 * (p - 0.5), hp, 0.65f, 1.0f));
        g1.fillRect(mh, mh, barSize - mh * 2, H - mh * 2);
    };

    public static final Vis NotchSlider = (v, canvas, W, H, g) -> {

        double p = Vis.getFirstAndOnlyDimension(v);
        double barSize = W * p;

        double margin = 4;
        double mh = margin / 2.0;


        double notchRadius = W * 0.1;

        g.setLineWidth(0);

        //TODO use a x,w calculation that keeps the notch within bounds that none if it goes invisible at the extremes

        double hp = 0.5 + 0.5 * p;
        g.setFill(Color.ORANGE.deriveColor(70 * (p - 0.5), hp, 0.65f, 1.0f));
        g.fillRect(mh + barSize - notchRadius, mh, notchRadius * 2, H - mh * 2);
    };

    public static final Vis CircleKnob = (v, canvas, W, H, g) -> {
        double p = Vis.getFirstAndOnlyDimension(v);

        g.clearRect(0, 0, W, H);
        //g.setFill(Color.BLACK);
        //g.fillRect(0, 0, W, H);

        double angleStart = 0;
        double circumferenceActive = 0.5; //how much of the circumference of the interior circle is active as a dial track

        double theta = angleStart + (1-p) * circumferenceActive * (2 * Math.PI);


//        double x = W/2 + (W/2-margin) * Math.cos(theta);
//        double y = H/2 + (H/2-margin) * Math.sin(theta);
//        double t = 4;
        /*g.setFill(Color.WHITE);
        g.fillOval(x-t, y-t, t*2,t*2);*/

        double ew = W;
        double eh = H;

        double np = 0.75 + (p * 0.25);

        //double ews = ew * np, ehs = eh * np; //scale by prop

        double ul = (W - ew) / 2.0;
        double ut = (H - eh) / 2.0;

        g.setFill(Color.DARKGRAY);
        g.fillOval(ul, ut, ew, eh);

        double hp = 0.5 + 0.5 * p;
        g.setFill(Color.ORANGE.deriveColor(70 * (p - 0.5), hp, 0.65f, 1.0f));


        double atheta = theta * 180.0 / Math.PI; //radian to degree
        double knobArc = 60;
        g.fillArc(ul, ut, ew, eh,
                atheta - knobArc / 2, knobArc, ArcType.ROUND);


    };

    @Override
    protected void redraw() {
        //HACK - called before initialized
        if (vis == null) return;

        double W = canvas.getWidth();
        double H = canvas.getHeight();

        //background
        GraphicsContext g = graphics();

        g.setFill(Color.BLACK);
        g.fillRect(0, 0, W, H);

        // render the slider
        vis.redraw(normalized(), canvas, W, H, g);
    }

//    public NSlider set(double v, double min, double max) {
//        this.value.set(v);
//        this.min.set(min);
//        this.max.set(max);
//        return this;
//    }

    public NSlider bind(DoubleProperty... p) {
        ensureDimension(p.length);

        for (int i = 0; i < p.length; i++)
            p[i].bindBidirectional(value[i]);

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
        NARfx.run((a, b) -> {

            FlowPane p = new FlowPane(16, 16);

            p.getChildren().setAll(
                new NSlider("Bar", 256, 96, NSlider.BarSlider, 0.5),
                new NSlider("Notch", 128, 45, NSlider.NotchSlider, 0.25),
                new NSlider("Notch--", 64, 25, NSlider.NotchSlider, 0.75),
                new NSlider("Knob", 256, 256, NSlider.CircleKnob, 0.5),
                new NSlider("Ranged", 256, 256, NSlider.BarSlider, 75)
                    .range(0, 100).on(0, c -> System.out.println(Arrays.toString(c.normalized())))
            );


            b.setScene(new Scene(p, 800, 800));
            b.show();
        });
    }

    private NSlider range(double min, double max) {
        this.min.set(min);
        this.max.set(max);
        return this;
    }
    private NSlider on(int dimension, Consumer<NSlider> callback) {

        //TODO save listener so it can be de-registered
        value[0].addListener(c -> {
           callback.accept(NSlider.this);
        });

        return this;
    }

    public static class LeftRightDrag implements Control, EventHandler<MouseEvent> {

        private Canvas canvas;
        private NSlider n;

        @Override
        public void start(NSlider n) {
            canvas = n.canvas;
            this.n = n;

            canvas.setOnMouseDragged(this);
            canvas.setOnMousePressed(this); //could also work as released
            canvas.setOnMouseReleased(this);

            canvas.setCursor(Cursor.CROSSHAIR);

        }

        @Override
        public void stop() {
            throw new RuntimeException("unimpl");
        }

        @Override
        public void handle(MouseEvent e) {

            canvas.setCursor(
                (e.getEventType()==MouseEvent.MOUSE_RELEASED) ? Cursor.CROSSHAIR : Cursor.MOVE
            );

            n.denormalized(e.getX() / canvas.getWidth());

            //System.out.println(dx + " " + dy + " " + value.get());

            e.consume();
        }

        double p(double dx) {
            return dx / canvas.getWidth();
        }

    }
}

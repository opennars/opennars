package nars.guifx;

import com.gs.collections.api.block.function.primitive.DoubleToDoubleFunction;
import com.gs.collections.impl.list.mutable.primitive.FloatArrayList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import nars.Global;
import nars.guifx.util.ColorMatrix;
import nars.guifx.util.NControl;

import java.util.Collection;
import java.util.List;
import java.util.function.DoubleSupplier;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/10/15.
 */
public class Plot2D extends NControl/*Canvas */ implements Runnable {

    //public static final ColorArray BlueRed = new ColorArray(128, Color.BLUE, Color.RED);

    final static ColorMatrix ca = new ColorMatrix(17,1,(x,y)-> {
        return Color.hsb(x*360.0, 0.6f, y * 0.5 + 0.5);
    });

    final class Series {
        final FloatArrayList history = new FloatArrayList(); //TODO make Float
        final DoubleSupplier valueFunc;
        final String name;
        final Color color; //main color


        transient private double minValue;
        transient private double maxValue;



        public Series(String name, DoubleSupplier valueFunc) {
            this.valueFunc = valueFunc;
            this.name = name;
            this.color = NARfx.hashColor(name, ca);
        }

        public void update() {
            while (history.size() > maxHistory)
                history.removeAtIndex(0);


            double d = valueFunc.getAsDouble();
            if (Double.isFinite(d))
                history.add( (float)d );

            minValue = Float.POSITIVE_INFINITY;
            maxValue = Float.NEGATIVE_INFINITY;

            history.forEach(v -> {
                if (Double.isFinite(v)) {
                    if (v < minValue) minValue = v;
                    if (v > maxValue) maxValue = v;
                }
                //mean += v;
            });
        }
    }

    transient private double minValue;
    transient private double maxValue;

    public final List<Series> series = Global.newArrayList();

    private final int maxHistory;


    private SimpleObjectProperty<PlotVis> plotVis = new SimpleObjectProperty<>();


    public Plot2D(PlotVis p, int history, double w, double h) {
        super(w, h);

        maxWidth(Double.MAX_VALUE);
        maxHeight(Double.MAX_VALUE);

//        parentProperty().addListener(p-> {
//            if (p!=null) {
//                //parent.widthProperty().addListener(LinePlot.this);
//                widthProperty().bind(parent.widthProperty());
//                heightProperty().bind(parent.heightProperty());
//            }
//        });
//        layoutBoundsProperty().addListener((b) -> {
//            Bounds ng = getLayoutBounds();
//            setWidth(ng.getWidth());
//            setHeight(ng.getHeight());
//            System.out.println(ng);
//        });


        this.maxHistory = history;

        plotVis.addListener((n) -> update());


        plotVis.set(p);
    }


    public Plot2D add(String name, DoubleSupplier func) {
        series.add(new Series(name, func));
        return this;
    }

    public void run() {

        //HACK (not initialized yet but run() called
        if (series == null || series.isEmpty()) return;

        GraphicsContext g = graphics();

        final double W = g.getCanvas().getWidth();
        final double H = g.getCanvas().getHeight();


        g.clearRect(0, 0, W, H);


        //super.paint(paintContext);

        series.forEach(Series::update);

        this.minValue  = Float.POSITIVE_INFINITY;
        this.maxValue  = Float.NEGATIVE_INFINITY;
        series.forEach(s -> {
            this.minValue = Math.min(this.minValue, s.minValue);
            this.maxValue = Math.max(this.maxValue, s.maxValue);
        });

//        if (count == 0) return;
//
//        if (count > 0) {
//            mean /= count;
//            label = Texts.n4(mean) + " +- " + Texts.n4(maxValue - minValue);
//        } else {
//            label = "empty";
//        }


        PlotVis pv = plotVis.get();
        if (pv!=null) {
            pv.draw(series, g, minValue, maxValue);
        }

//        //g.setFont(NengoStyle.FONT_BOLD);
//        g.setFill(Color.WHITE);
//        g.fillText(name, 10, 10);
//        //g.setFont(NengoStyle.FONT_SMALL);
//        g.fillText(label, 10, 25);
    }

    @FunctionalInterface
    public interface PlotVis {
        void draw(Collection<Series> series, GraphicsContext g, double minValue, double maxValue);
    }

    public final static PlotVis BarWave = (Collection<Series> history, GraphicsContext g, double minValue, double maxValue) -> {
        if (minValue != maxValue) {

            final double w = g.getCanvas().getWidth();
            final double h = g.getCanvas().getHeight();

            double prevX = -1;
            final int histSize = history.size();

            final double dx = (w / histSize);

//            double x = 0;
//            for (int i = 0; i < histSize; i++) {
//                final double v = history.get(i);
//
//                double py = (v - minValue) / (maxValue - minValue);
//                if (py < 0) py = 0;
//                if (py > 1.0) py = 1.0;
//
//                double y = py * h;
//
//                g.setFill(BlueRed.get(py));
//
//                g.fillRect(prevX + 1, (h / 2f - y / 2), FastMath.ceil(x - prevX), y);
//
//                prevX = x;
//                x += dx;
//            }
        }
    };
    public static final PlotVis Line = (Collection<Series> series, GraphicsContext g, double minValue, double maxValue) -> {
        if (minValue != maxValue) {

            double m = 10; //margin

            final double w = g.getCanvas().getWidth();
            double H = g.getCanvas().getHeight();
            final double h = H - m*2;

            g.setGlobalBlendMode(BlendMode.DIFFERENCE);
            g.setFill(Color.BLACK);
            g.setStroke(Color.GRAY);
            g.fillText(String.valueOf(maxValue), 0, m+g.getFont().getSize());
            g.strokeLine(0, m, w, m);
            g.strokeLine(0, H-m, w, H-m);
            g.fillText(String.valueOf(minValue), 0, H-m-2);
            g.setGlobalBlendMode(BlendMode.SRC_OVER /* default */);

            series.forEach(s -> {



                DoubleToDoubleFunction ypos = (v) -> {
                    double py = (v - minValue) / (maxValue - minValue);
                    if (py < 0) py = 0;
                    if (py > 1.0) py = 1.0;
                    return m + (1.0 - py) * h;
                };

                double mid = ypos.valueOf(0.5 * (s.minValue + s.maxValue));

                g.setFill(s.color);
                g.fillText(s.name, m, mid);

                g.setLineWidth(2);
                g.setStroke(s.color);
                g.beginPath();

                FloatArrayList sh = s.history;

                final int histSize = sh.size();

                final double dx = (w / histSize);

                double x = 0;
                for (int i = 0; i < sh.size(); i++) { //TODO why does the array change
                    final double v = sh.get(i);

                    double y = ypos.valueOf(v);


                    //System.out.println(x + " " + y);
                    g.lineTo(x, y);

                    x += dx;
                }
                g.stroke();
            });
        }
    };

    public void update() {
        series.forEach(Series::update);
        draw();
    }

    public void draw() {
        runLater( this );
    }


    @Override
    protected void redraw() {
        run();
    }
}


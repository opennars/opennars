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
import org.apache.commons.math3.util.FastMath;

import java.util.Collection;
import java.util.List;
import java.util.function.DoubleSupplier;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/10/15.
 */
public class Plot2D extends NControl/*Canvas */ implements Runnable {

    //public static final ColorArray BlueRed = new ColorArray(128, Color.BLUE, Color.RED);

    public static final ColorMatrix ca = new ColorMatrix(17, 1, (x, y) -> Color.hsb(x * 360.0, 0.6f, y * 0.5 + 0.5));

    public abstract static class Series {
        public final FloatArrayList history = new FloatArrayList(); //TODO make Float
        final String name;
        final Color color; //main color

        protected transient double minValue;
        protected transient double maxValue;

        @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
        public Series(String name) {
            this.name = name;
            color = NARfx.hashColor(name, ca);
        }

        public abstract void update(int maxHistory);

    }

    private transient double minValue;
    private transient double maxValue;

    public final List<Series> series = Global.newArrayList();

    private final int maxHistory;


    private final SimpleObjectProperty<PlotVis> plotVis = new SimpleObjectProperty<>();


    public Plot2D(PlotVis p, int history, double h) {
        this(p, history, 0, h);
    }

    public Plot2D(PlotVis p, int history, double w, double h) {
        super(w, h);

        maxHistory = history;

        plotVis.addListener((n) -> update());


        plotVis.set(p);
    }

    public Plot2D add(Series s) {
        series.add(s);
        return this;
    }

    public Plot2D add(String name, DoubleSupplier valueFunc) {
        add(new Series(name) {

            @Override
            public void update(int maxHistory) {
                while (history.size() > maxHistory)
                    history.removeAtIndex(0);


                double d = valueFunc.getAsDouble();
                if (Double.isFinite(d))
                    history.add((float) d);

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

        });
        return this;
    }

    @Override
    public void run() {

        List<Series> series = this.series;

        //HACK (not initialized yet but run() called
        if (series == null || series.isEmpty()) return;

        GraphicsContext g = graphics();

        double W = g.getCanvas().getWidth();
        double H = g.getCanvas().getHeight();

        g.clearRect(0, 0, W, H);


        PlotVis pv = plotVis.get();
        if (pv != null) {
            pv.draw(series, g, minValue, maxValue);
        }

    }

    @FunctionalInterface
    public interface PlotVis {
        void draw(Collection<Series> series, GraphicsContext g, double minValue, double maxValue);
    }

    public static final PlotVis BarWave = (Collection<Series> series, GraphicsContext g, double minValue, double maxValue) -> {
        if (minValue != maxValue) {

            double w = g.getCanvas().getWidth();
            double h = g.getCanvas().getHeight();


            series.forEach(s -> {
                int histSize = s.history.size();

                double dx = (w / histSize);

                double x = 0;
                double prevX = -1;

                for (int i = 0; i < histSize; i++) {
                    double v = s.history.get(i);

                    double py = (v - minValue) / (maxValue - minValue);
                    if (py < 0) py = 0;
                    if (py > 1.0) py = 1.0;

                    double y = py * h;

                    g.setFill(s.color);

                    g.fillRect(prevX + 1, (h / 2.0f - y / 2), FastMath.ceil(x - prevX), y);

                    prevX = x;
                    x += dx;
                }

            });
        }
    };
    public static final PlotVis Line = (Collection<Series> series, GraphicsContext g, double minValue, double maxValue) -> {
        if (g == null)
            return;

        if (minValue != maxValue) {

            double m = 10; //margin

            double w = g.getCanvas().getWidth();
            double H = g.getCanvas().getHeight();
            double h = H - m * 2;

            g.setGlobalBlendMode(BlendMode.DIFFERENCE);
            g.setFill(Color.BLACK);
            g.setStroke(Color.GRAY);
            g.fillText(String.valueOf(maxValue), 0, m + g.getFont().getSize());
            g.strokeLine(0, m, w, m);
            g.strokeLine(0, H - m, w, H - m);
            g.fillText(String.valueOf(minValue), 0, H - m - 2);
            g.setGlobalBlendMode(BlendMode.SRC_OVER /* default */);

            series.forEach(s -> {


                DoubleToDoubleFunction ypos = (v) -> {
                    double py = (v - minValue) / (maxValue - minValue);
                    if (py < 0) py = 0;
                    else if (py > 1.0) py = 1.0;
                    return m + (1.0 - py) * h;
                };

                double mid = ypos.valueOf(0.5 * (s.minValue + s.maxValue));

                g.setFill(s.color);
                g.fillText(s.name, m, mid);

                g.setLineWidth(2);
                g.setStroke(s.color);
                g.beginPath();

                FloatArrayList sh = s.history;

                int ss = sh.size();
                int histSize = ss;

                double dx = (w / histSize);

                double x = 0;
                for (int i = 0; i < ss; i++) { //TODO why does the array change
                    //System.out.println(x + " " + y);
                    g.lineTo(x, ypos.valueOf(sh.get(i)));

                    x += dx;
                }
                g.stroke();
            });
        }
    };

    protected void updateSeries() {
        int mh = maxHistory;
        series.forEach(s -> s.update(mh));

        minValue = Float.POSITIVE_INFINITY;
        maxValue = Float.NEGATIVE_INFINITY;
        series.forEach(s -> {
            minValue = Math.min(minValue, s.minValue);
            maxValue = Math.max(maxValue, s.maxValue);
        });
    }

    public void update() {
        updateSeries();
        draw();
    }

    public void draw() {
        runLater(this);
    }


    @Override
    protected void redraw() {
        run();
    }
}


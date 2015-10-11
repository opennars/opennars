package nars.guifx;

import com.gs.collections.impl.list.mutable.primitive.FloatArrayList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nars.guifx.util.ColorArray;
import nars.guifx.util.NControl;
import nars.io.Texts;
import org.apache.commons.math3.util.FastMath;

import java.util.function.DoubleSupplier;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/10/15.
 */
public class Plot2D extends NControl/*Canvas */ implements Runnable {

    public static final ColorArray BlueRed = new ColorArray(128, Color.BLUE, Color.RED);

    private final FloatArrayList history = new FloatArrayList(); //TODO make Float
    private final DoubleSupplier valueFunc;
    private final String name;
    private final int maxHistory;
    private double minValue;
    private double maxValue;
    private String label;
    private double mean;
    int count;
    private SimpleObjectProperty<PlotVis> plotVis = new SimpleObjectProperty<>();


    @Deprecated public Plot2D(String name, DoubleSupplier valueFunc, int history, double w, double h) {
        this(name, BarWave, valueFunc, history, w, h );
    }

    public Plot2D(String name, PlotVis p, DoubleSupplier valueFunc, int history, double w, double h) {
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


        this.name = name;
        this.maxHistory = history;
        this.valueFunc = valueFunc;

        plotVis.addListener((n) -> update());

        plotVis.set(p);
    }



    public void run() {

        //HACK (not initialized yet but run() called
        if (history == null) return;

        GraphicsContext g = graphics();

        final double W = g.getCanvas().getWidth();
        final double H = g.getCanvas().getHeight();


        g.clearRect(0, 0, W, H);


        //super.paint(paintContext);


        minValue = Float.POSITIVE_INFINITY;
        maxValue = Float.NEGATIVE_INFINITY;
        mean = 0;
        count = 0;
        history.forEach(v -> {
            if (v < minValue) minValue = v;
            if (v > maxValue) maxValue = v;
            mean += v;
            count++;
        });

        if (count == 0) return;

        if (count > 0) {
            mean /= count;
            label = Texts.n4(mean) + " +- " + Texts.n4(maxValue - minValue);
        } else {
            label = "empty";
        }


        PlotVis pv = plotVis.get();
        if (pv!=null) {
            pv.draw(history, g, minValue, maxValue);
        }

        //g.setFont(NengoStyle.FONT_BOLD);
        g.setFill(Color.WHITE);
        g.fillText(name, 10, 10);
        //g.setFont(NengoStyle.FONT_SMALL);
        g.fillText(label, 10, 25);
    }

    @FunctionalInterface
    public interface PlotVis {
        void draw(FloatArrayList history, GraphicsContext g, double minValue, double maxValue);
    }

    public final static PlotVis BarWave = (FloatArrayList history, GraphicsContext g, double minValue, double maxValue) -> {
        if (minValue != maxValue) {

            final double w = g.getCanvas().getWidth();
            final double h = g.getCanvas().getHeight();

            double prevX = -1;
            final int histSize = history.size();

            final double dx = (w / histSize);

            double x = 0;
            for (int i = 0; i < histSize; i++) {
                final double v = history.get(i);

                double py = (v - minValue) / (maxValue - minValue);
                if (py < 0) py = 0;
                if (py > 1.0) py = 1.0;

                double y = py * h;

                g.setFill(BlueRed.get(py));

                g.fillRect(prevX + 1, (h / 2f - y / 2), FastMath.ceil(x - prevX), y);

                prevX = x;
                x += dx;
            }
        }
    };
    public final static PlotVis Line = (FloatArrayList history, GraphicsContext g, double minValue, double maxValue) -> {
        if (minValue != maxValue) {

            final double w = g.getCanvas().getWidth();
            final double h = g.getCanvas().getHeight();

            double prevX = -1;
            final int histSize = history.size();

            final double dx = (w / histSize);

            double x = 0;

            g.setLineWidth(3);
            g.setStroke(Color.BLUE);

            g.beginPath();
            for (int i = 0; i < histSize; i++) {
                final double v = history.get(i);

                double py = (v - minValue) / (maxValue - minValue);
                if (py < 0) py = 0;
                if (py > 1.0) py = 1.0;

                double y = py * h;


                //System.out.println(x + " " + y);
                g.lineTo(x, y);

                prevX = x;
                x += dx;
            }
            g.stroke();
        }
    };

    public void update() {
        while (history.size() > maxHistory)
            history.removeAtIndex(0);
        history.add( (float) valueFunc.getAsDouble() );
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


package nars.guifx;

import com.gs.collections.impl.list.mutable.primitive.DoubleArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import nars.io.Texts;
import org.apache.commons.math3.util.FastMath;

import java.util.function.DoubleSupplier;

/**
 * Created by me on 8/10/15.
 */
public class LinePlot extends Canvas {

    public static final ColorArray BlueRed = new ColorArray(128, Color.BLUE, Color.RED);

    private final DoubleArrayList history;
    private final DoubleSupplier valueFunc;
    private final String name;
    private final int maxHistory;
    private double minValue;
    private double maxValue;
    private String label;
    private double mean;
    int count;

    public LinePlot(String name, DoubleSupplier valueFunc, int history) {
        super(300,300);


//        ChangeListener sizechanged = new ChangeListener() {
//            @Override
//            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//                update();
//            }
//        };
//        widthProperty().addListener(sizechanged);
//        heightProperty().addListener(sizechanged);


        this.name = name;
        this.history = new DoubleArrayList(history);
        this.maxHistory = history;
        this.valueFunc = valueFunc;

        update();
    }



    public synchronized void update() {

        GraphicsContext g = getGraphicsContext2D();

        final double W = g.getCanvas().getWidth();
        final double H = g.getCanvas().getHeight();


        g.clearRect(0,0, W, H);

        while (history.size() > maxHistory)
            history.remove(0);
        history.add(  valueFunc.getAsDouble() );


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
            label = Texts.n4(mean) + " +- " + Texts.n4(maxValue-minValue);
        }
        else {
            label = "empty";
        }




        int nh = history.size();
        double x = 0;
        double dx = (W/ nh);
        final float bh = (float)H;
        final double mv = minValue;
        final double Mv = maxValue;
        //final int ih = (int)bh;

        g.setFill(Color.WHITE);

        if (mv != Mv) {
            int prevX = -1;
            final int histSize = history.size();


            for (int i = 0; i < histSize; i++) {
                final double v = history.get(i);

                double py = (v - mv) / (Mv - mv); if (py < 0) py = 0; if (py > 1.0) py = 1.0;

                double y = py * bh;

                final int iy = (int) y;

                g.setFill(BlueRed.get(py));

                g.fillRect(prevX+1, (int)(bh / 2f - y / 2), (int) FastMath.ceil(x - prevX), iy);

                prevX = (int)x;
                x += dx;
            }
        }

        //g.setFont(NengoStyle.FONT_BOLD);
        g.setFill(Color.WHITE);
        g.fillText(name, 10, 10);
        //g.setFont(NengoStyle.FONT_SMALL);
        g.fillText(label, 10, 25);

    }

}


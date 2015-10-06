package nars.guifx;

import com.gs.collections.impl.list.mutable.primitive.DoubleArrayList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nars.guifx.util.ColorArray;
import nars.io.Texts;
import org.apache.commons.math3.util.FastMath;

import java.util.function.DoubleSupplier;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/10/15.
 */
public class LinePlot extends Canvas /*implements ChangeListener*/ {

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
        super(100, 100);


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
        this.history = new DoubleArrayList(history);
        this.maxHistory = history;
        this.valueFunc = valueFunc;

        draw();

    }



    public void draw() {
        while (history.size() > maxHistory)
            history.removeAtIndex(0);
        history.add(  valueFunc.getAsDouble() );

        runLater( () -> {
            GraphicsContext g = getGraphicsContext2D();

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


            int nh = history.size();
            double x = 0;
            double dx = (W / nh);
            final float bh = (float) H;
            final double mv = minValue;
            final double Mv = maxValue;
            //final int ih = (int)bh;

            g.setFill(Color.WHITE);

            if (mv != Mv) {
                int prevX = -1;
                final int histSize = history.size();


                for (int i = 0; i < histSize; i++) {
                    final double v = history.get(i);

                    double py = (v - mv) / (Mv - mv);
                    if (py < 0) py = 0;
                    if (py > 1.0) py = 1.0;

                    double y = py * bh;

                    final int iy = (int) y;

                    g.setFill(BlueRed.get(py));

                    g.fillRect(prevX + 1, (int) (bh / 2f - y / 2), (int) FastMath.ceil(x - prevX), iy);

                    prevX = (int) x;
                    x += dx;
                }
            }

            //g.setFont(NengoStyle.FONT_BOLD);
            g.setFill(Color.WHITE);
            g.fillText(name, 10, 10);
            //g.setFont(NengoStyle.FONT_SMALL);
            g.fillText(label, 10, 25);
        });

    }


}


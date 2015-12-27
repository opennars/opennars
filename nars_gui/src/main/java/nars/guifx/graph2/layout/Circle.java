package nars.guifx.graph2.layout;

import javafx.beans.property.SimpleDoubleProperty;
import nars.data.Range;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.source.SpaceGrapher;

import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;

/**
 * TODO rewrite as subclass of Linear
 */
public class Circle implements IterativeLayout {

    @Range(min=0.1, max=10)
    public final SimpleDoubleProperty radiusMin = new SimpleDoubleProperty(4);

    @Range(min=0.2, max=50)
    public final SimpleDoubleProperty radiusMax = new SimpleDoubleProperty(8);



    public static void run(TermNode[] verts,
                           //PreallocatedResultFunction<N,double[]> getPosition,
                           ToDoubleFunction<TermNode> radiusFraction,
                           ToDoubleFunction<TermNode> angle,
                           BiConsumer<TermNode, double[]> setPosition) {


        double[] d = new double[2];

        for (TermNode v : verts) {
            if (v == null) continue; //break?

            double r = radiusFraction.applyAsDouble(v);
            double a = angle.applyAsDouble(v);
            d[0] = Math.cos(a) * r;
            d[1] = Math.sin(a) * r;
            setPosition.accept(v, d);
        }

    }


    @Override
    public void run(SpaceGrapher graph, int iterations) {
        TermNode[] termList = graph.displayed;

        //double[] i = new double[1];
        //double numFraction = Math.PI * 2.0 * 1.0 / termList.length;

        int num = graph.getVertices().size(); //termList.length;
        double radiusMin = num * this.radiusMin.get();
        double radiusMax = radiusMin + num * this.radiusMax.get();

        run(termList,
                (v) -> {
                    double vpri = v.priNorm; // 1f;// v.c.getPriority();
                    double r = (v.c != null ? vpri : 0);
                    return r * (radiusMax - radiusMin) + radiusMin;
                },
                (v) -> {
                    return Math.PI * 2 * (v.term.hashCode() % 8192) / 8192.0;

                    /*i[0] += numFraction;
                    return i[0];*/
                },
                (v, d) -> {
                    v.move(d[0], d[1]);//, 0.5f, 1f);
                });

    }


}

package nars.guifx.graph2.layout;

import javafx.beans.property.SimpleDoubleProperty;
import nars.guifx.graph2.SpaceGrapher;
import nars.guifx.graph2.TermNode;

import java.util.function.BiConsumer;
import java.util.function.ToDoubleFunction;

/**
 * TODO rewrite as subclass of Linear
 */
public class Circle implements IterativeLayout {

    public final SimpleDoubleProperty radiusMin = new SimpleDoubleProperty(1);
    public final SimpleDoubleProperty radiusMax = new SimpleDoubleProperty(4);



    public void run(TermNode[] verts,
                    //PreallocatedResultFunction<N,double[]> getPosition,
                    ToDoubleFunction<TermNode> radiusFraction,
                    ToDoubleFunction<TermNode> angle,
                    BiConsumer<TermNode, double[]> setPosition) {


        double d[] = new double[2];

        for (TermNode v : verts) {
            if (v == null) continue; //break?

            final double r = radiusFraction.applyAsDouble(v);
            final double a = angle.applyAsDouble(v);
            d[0] = Math.cos(a) * r;
            d[1] = Math.sin(a) * r;
            setPosition.accept(v, d);
        }

    }


    @Override
    public void run(SpaceGrapher graph, int iterations) {
        final TermNode[] termList = graph.displayed;

        double[] i = new double[1];
        double numFraction = Math.PI * 2.0 * 1.0 / termList.length;

        final int num = graph.maxNodes.get(); //termList.length;
        double radiusMin = num * this.radiusMin.get();
        double radiusMax = radiusMin + num * this.radiusMax.get();

        run(termList,
                (v) -> {
                    double r = (v.c != null ? v.c.getPriority() : 0);
                    double min = radiusMin;
                    double max = radiusMax;
                    return r * (max - min) + min;
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

    @Override
    public void init(TermNode n) {

    }


}

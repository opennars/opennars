package nars.guifx.graph2;

import automenta.vivisect.dimensionalize.IterativeLayout;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Created by me on 9/6/15.
 */
public class CircleLayout<N extends TermNode, E extends TermEdge> implements IterativeLayout<N,E> {


    public void run(Collection<N> verts,
                    //PreallocatedResultFunction<N,double[]> getPosition,
                    ToDoubleFunction<N> radiusFraction,
                    ToDoubleFunction<N> angle,
                    NARGraph1.PairConsumer<N, double[]> setPosition) {


        double d[] = new double[2];

        verts.forEach(v -> {
            final double r = radiusFraction.applyAsDouble(v);
            final double a = angle.applyAsDouble(v);
            d[0] = Math.cos(a) * r;
            d[1] = Math.sin(a) * r;
            setPosition.accept(v, d);
        });

    }

    @Override
    public ArrayRealVector getPosition(N vertex) {
        return null;
    }

    @Override
    public void run(NARGraph1 graph, int iterations) {
        final List termList = graph.termList;

        double[] i = new double[1];
        double numFraction = Math.PI * 2.0 * 1.0 / termList.size();
        double radiusMin = (termList.size() + 1) * 10;
        double radiusMax = 3f * radiusMin;

        run(termList,
                (v) -> {
                    double r = 1f - (v.c != null ? v.c.getPriority() : 0);
                    double min = radiusMin;
                    double max = radiusMax;
                    return r * (max - min) + min;
                },
                (v) -> {
                    //return Math.PI * 2 * (v.term.hashCode() % 8192) / 8192.0;

                    i[0] += numFraction;
                    return i[0];
                },
                (v, d) -> {
                    v.move(d[0], d[1]);//, 0.5f, 1f);
                });

    }

    @Override
    public void resetLearning() {

    }

    @Override
    public double getRadius(N vertex) {
        return 0;
    }

}

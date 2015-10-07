package nars.guifx.graph2.layout;

import nars.guifx.graph2.TermNode;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Created by me on 10/7/15.
 */
public class Spiral extends Linear {


    @Override
    public void setPosition(TermNode v, int i, int max) {
        double size = 50; //temproary
        double minR = 200;
        final double dr = 15;
        final double dt = 0.2;
        //double p = (i / ((double)max));
        double r = minR + i * dr;
        double theta = i * dt;
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        v.move(x, y, 0.1, 1);
    }


    @Override
    public ArrayRealVector getPosition(Object vertex) {
        return null;
    }

    @Override
    public double getRadius(Object vertex) {
        return 0;
    }

    @Override
    public void init(Object n) {

    }
}

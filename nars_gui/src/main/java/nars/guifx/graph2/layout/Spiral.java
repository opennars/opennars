package nars.guifx.graph2.layout;

import nars.guifx.graph2.TermNode;

/**
 * Created by me on 10/7/15.
 */
public class Spiral extends Linear {


    @Override
    public void setPosition(TermNode v, int i, int max) {
        double minR = 200;
        double dr = 15;
        double dt = 0.2;
        //double p = (i / ((double)max));
        double r = minR + i * dr;
        double theta = i * dt;
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        v.move(x, y, 0.1, 1);
    }



}

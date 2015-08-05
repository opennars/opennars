/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import java.awt.*;

/**
 *
 * @author thorsten
 */
public class RenderArrayFunction1D implements RenderFunction1D {


    private final Color color;
    private final double[] ys;

    public RenderArrayFunction1D(Color color, double[] ys) {
        this.color = color;
        this.ys = ys;
    }

    @Override
    public double compute(double x) {
        int i = (int) Math.round(x * (ys.length - 1));
        if(i >= ys.length){
            return 0;
        }
        return ys[i];
    }

    @Override
    public Color getColor() {
        return color;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import java.awt.Color;

/**
 *
 * @author thorsten
 */
public class RenderArrayFunction1D implements RenderFunction1D {

    private final double width;
    private final Color color;
    private final double[] ys;

    public RenderArrayFunction1D(double width, Color color, double[] ys) {
        this.width = width;
        this.color = color;
        this.ys = ys;
    }

    @Override
    public double compute(double x) {
        int i = (int) Math.round(x / width * (ys.length - 1));
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

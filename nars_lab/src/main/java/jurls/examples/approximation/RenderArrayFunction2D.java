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
public class RenderArrayFunction2D implements RenderFunction2D {

    private final double width;
    private final double height;
    private final Color color;
    private final double[][] zs;

    public RenderArrayFunction2D(double width, double height, Color color, double[][] zs) {
        this.width = width;
        this.height = height;
        this.color = color;
        this.zs = zs;
    }

    @Override
    public double compute(double x, double y) {
        int i = (int) Math.round(x / width * (zs.length - 1));
        if (i >= zs.length) {
            return 0;
        }
        int j = (int) Math.round(y / height * (zs[i].length - 1));
        if (j >= zs[i].length) {
            return 0;
        }
        return zs[i][j];
    }

    @Override
    public Color getColor() {
        return color;
    }

}

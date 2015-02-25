/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import nars.util.data.XORShiftRandom;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.awt.Color;

/**
 *
 * @author thorsten
 */
public class RenderArrayFunction implements RenderFunction {

    private final double width;
    private final Color color;
    private final double[] ys;

    public RenderArrayFunction(double width, Color color, double[] ys) {
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
    public double value(double... xs) {
        return compute(xs[0]);
    }

    @Override
    public ArrayRealVector parameterGradient(ArrayRealVector result, double... xs) {
        return null;
    }

    @Override
    public void addToParameters(ArrayRealVector deltas) {

    }

    @Override
    public void learn(double[] xs, double y) {

    }

    @Override
    public int numberOfParameters() {
        return 0;
    }

    @Override
    public int numberOfInputs() {
        return 0;
    }

    @Override
    public double minOutputDebug() {
        return 0;
    }

    @Override
    public double maxOutputDebug() {
        return 0;
    }

    @Override
    public double getParameter(int i) {
        return 0;
    }

    @Override
    public void setParameter(int i, double v) {

    }

    @Override
    public Color getColor() {
        return color;
    }

    final static XORShiftRandom mutateRandom = new XORShiftRandom();

    public void mutate(final double v, float rerandomProbability) {
        for (int i = 0; i < ys.length; i++) {
            double y;
            if (mutateRandom.nextDouble() < rerandomProbability) {
                y = (Math.random()-0.5f) * 2f;
            }
            else {
                y = ys[i] + (mutateRandom.nextDouble()-0.5) * v;
                if (y < -1) y = -1;
                if (y > 1) y = 1;
            }
            ys[i] = y;


        }
    }
}

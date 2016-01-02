/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import jurls.core.approximation.ParameterizedFunction;

import java.awt.*;

/**
 *
 * @author thorsten
 */
public class RenderParameterizedFunction2D implements RenderFunction2D {

    private ParameterizedFunction parameterizedFunction = null;
    private final Color color;
    private final double[] xs = new double[2];

    public RenderParameterizedFunction2D(Color color) {
        this.color = color;
    }

    public void setParameterizedFunction(ParameterizedFunction parameterizedFunction) {
        this.parameterizedFunction = parameterizedFunction;
    }

    public ParameterizedFunction getParameterizedFunction() {
        return parameterizedFunction;
    }

    @Override
    public double compute(double x, double y) {
        if (parameterizedFunction == null) {
            return 0;
        }
        xs[0] = x;
        xs[1] = y;
        return parameterizedFunction.value(xs);
    }

    @Override
    public Color getColor() {
        return color;
    }

    public void oneStepTowards(double x, double y, double z) {
        xs[0] = x;
        xs[1] = y;
        parameterizedFunction.learn(xs, z);
    }
}

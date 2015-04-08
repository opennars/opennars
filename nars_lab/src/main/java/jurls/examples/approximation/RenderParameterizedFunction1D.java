/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import java.awt.Color;
import jurls.core.approximation.ParameterizedFunction;

/**
 *
 * @author thorsten
 */
public class RenderParameterizedFunction1D implements RenderFunction1D {

    private ParameterizedFunction parameterizedFunction = null;
    private final Color color;
    private double[] xs = new double[1];

    public RenderParameterizedFunction1D(Color color) {
        this.color = color;
    }

    public void setParameterizedFunction(ParameterizedFunction parameterizedFunction) {
        this.parameterizedFunction = parameterizedFunction;
    }

    public ParameterizedFunction getParameterizedFunction() {
        return parameterizedFunction;
    }

    @Override
    public double compute(double x) {
        xs[0] = x;
        return parameterizedFunction.value(xs);
    }

    @Override
    public Color getColor() {
        return color;
    }

    public void oneStepTowards(double x, double y) {
        xs[0] = x;
        parameterizedFunction.learn(xs, y);
    }
}

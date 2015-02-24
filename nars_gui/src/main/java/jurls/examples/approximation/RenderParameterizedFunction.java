/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.examples.approximation;

import jurls.core.approximation.ParameterizedFunction;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.awt.*;

/**
 *
 * @author thorsten
 */
public class RenderParameterizedFunction implements RenderFunction {

    private ParameterizedFunction parameterizedFunction = null;
    private final Color color;
    private double[] xs = new double[1];

    public RenderParameterizedFunction(Color color) {
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
    public double value(double... xs) {
        return 0;
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
        return 1;
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

    public void oneStepTowards(double x, double y) {
        xs[0] = x;
        parameterizedFunction.learn(xs, y);
    }
}

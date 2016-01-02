/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.Arrays;

/**
 *
 * @author thorsten
 */
public class InputNormalizer implements ParameterizedFunction {

    private final ParameterizedFunction parameterizedFunction;
    private final double[] maxInputs;
    private final double[] minInputs;

    public InputNormalizer(ParameterizedFunction parameterizedFunction) {
        this.parameterizedFunction = parameterizedFunction;
        maxInputs = new double[parameterizedFunction.numberOfInputs()];
        minInputs = new double[parameterizedFunction.numberOfInputs()];
        Arrays.fill(minInputs, Double.POSITIVE_INFINITY);
        Arrays.fill(maxInputs, Double.NEGATIVE_INFINITY);
    }

    private double[] normalizeInput(double[] xs) {
        double[] xs2 = new double[xs.length];

        for (int i = 0; i < xs.length; ++i) {
            double x = xs[i];
            double max = maxInputs[i];
            double min = minInputs[i];
            if (x > max) {
                max = maxInputs[i] = x;
            }
            if (x < min) {
                min = minInputs[i] = x;
            }
            if (min == max) {
                max = maxInputs[i] = min + 0.0001;
            }
            xs2[i] = (x - min) / (max - min);
        }

        return xs2;
    }

    @Override
    public double value(double[] xs) {
        return parameterizedFunction.value(normalizeInput(xs));
    }

    @Override
    public void learn(double[] xs, double y) {
        parameterizedFunction.learn(normalizeInput(xs), y);
    }

    @Override
    public int numberOfParameters() {
        return parameterizedFunction.numberOfParameters();
    }

    @Override
    public int numberOfInputs() {
        return parameterizedFunction.numberOfInputs();
    }

    @Override
    public void parameterGradient(double[] output, double... xs) {
        parameterizedFunction.parameterGradient(output, normalizeInput(xs));
    }

    @Override
    public void addToParameters(double[] deltas) {
        parameterizedFunction.addToParameters(deltas);
    }

    @Override
    public double minOutputDebug() {
        return parameterizedFunction.minOutputDebug();
    }

    @Override
    public double maxOutputDebug() {
        return parameterizedFunction.maxOutputDebug();
    }

    @Override
    public double getParameter(int i) {
        return parameterizedFunction.getParameter(i);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import jurls.core.utils.Utils;

/**
 *
 * @author thorsten
 */
public class OutputNormalizer implements ParameterizedFunction {

    private final ParameterizedFunction parameterizedFunction;
    private double minOutput = 0;
    private double maxOutput = 1;

    public OutputNormalizer(ParameterizedFunction parameterizedFunction) {
        this.parameterizedFunction = parameterizedFunction;
    }

    private double normalizeOutput(double y) {
        if (y > maxOutput) {
            maxOutput = y;
        }
        if (y < minOutput) {
            minOutput = y;
        }

        return (y - minOutput) / (maxOutput - minOutput);
    }

    private double denormalizeOutput(double y) {
        return y * (maxOutput - minOutput) + minOutput;
    }

    @Override
    public double value(double[] xs) {
        return denormalizeOutput(parameterizedFunction.value(xs));
    }

    @Override
    public void learn(double[] xs, double y) {
        parameterizedFunction.learn(xs, normalizeOutput(y));
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
        parameterizedFunction.parameterGradient(output, xs);
        Utils.multiplySelf(output, maxOutput - minOutput);
    }

    @Override
    public void addToParameters(double[] deltas) {
        parameterizedFunction.addToParameters(deltas);
    }

    @Override
    public double minOutputDebug() {
        return denormalizeOutput(parameterizedFunction.minOutputDebug());
    }

    @Override
    public double maxOutputDebug() {
        return denormalizeOutput(parameterizedFunction.maxOutputDebug());
    }

    @Override
    public double getParameter(int i) {
        return parameterizedFunction.getParameter(i);
    }
}

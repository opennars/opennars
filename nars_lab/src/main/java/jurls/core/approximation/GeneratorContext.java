/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thorsten2
 */
public class GeneratorContext {

    private int parameterIndex = 0;
    private final List<Scalar> parameterScalars = new ArrayList<>();
    private final List<Double> parameterData = new ArrayList<>();
    private DiffableFunctionSource diffableFunctionSource;
    private final Scalar[] inputScalars;

    public GeneratorContext(int numInputs) {
        inputScalars = new Scalar[numInputs];
        for (int i = 0; i < numInputs; ++i) {
            inputScalars[i] = new Scalar(i);
        }
    }

    public Scalar[] getInputScalars() {
        return inputScalars;
    }

    public DiffableFunctionSource getDiffableFunctionSource() {
        return diffableFunctionSource;
    }

    public void setDiffableFunctionSource(DiffableFunctionSource diffableFunctionSource) {
        this.diffableFunctionSource = diffableFunctionSource;
    }

    public Scalar newParameter(double x) {
        return newParameter(x, x);
    }

    public Scalar newParameter(double lower, double upper) {
        Scalar s = new Scalar(inputScalars.length + parameterIndex);
        parameterData.add(Math.random() * (upper - lower) + lower);
        parameterScalars.add(s);
        parameterIndex++;
        return s;
    }

    public Scalar newBoundedParameter(double lower, double upper) {
        Scalar s = newParameter(lower, upper);
        s.setLowerBound(lower);
        s.setUpperBound(upper);
        return s;
    }

    public List<Scalar> getParameterScalars() {
        return parameterScalars;
    }

    public List<Double> getParameterData() {
        return parameterData;
    }
}

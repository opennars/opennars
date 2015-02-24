/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import jurls.core.utils.Utils;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author thorsten
 */
public class GradientFitter implements ParameterizedFunction, Functional {

    private final ParameterizedFunction parameterizedFunction;
    private final double[] previousDeltas;
    private final ApproxParameters approxParameters;
    private ArrayRealVector gradient;

    public GradientFitter(ApproxParameters approxParameters,
            ParameterizedFunction parameterizedFunction) {
        this.approxParameters = approxParameters;
        this.parameterizedFunction = parameterizedFunction;
        previousDeltas = new double[parameterizedFunction.numberOfParameters()];
    }

    @Override
    public void learn(double[] xs, double y) {
        double q = parameterizedFunction.value(xs);
        double e = y - q;

        gradient = parameterGradient(gradient, xs);
        gradient.mapMultiplyToSelf(e);

        double l = Utils.length(gradient.getDataRef());
        if (l == 0) {
            l = 1;
        }

        double[] gr = gradient.getDataRef();
        final double a = approxParameters.getAlpha();
        final double m = approxParameters.getMomentum();
        for (int i = 0; i < gr.length; ++i) {
            previousDeltas[i] = gr[i] = a * gr[i] / l + m * previousDeltas[i];            
        }

        parameterizedFunction.addToParameters(gradient);
    }

    @Override
    public double value(double[] xs) {
        return parameterizedFunction.value(xs);
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
    public ArrayRealVector parameterGradient(ArrayRealVector output, double[] xs) {
        return parameterizedFunction.parameterGradient(output, xs);
    }

    @Override
    public void addToParameters(ArrayRealVector deltas) {
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
    public Object[] getDependencies() {        
        return new Object[] { parameterizedFunction };        
    }

    @Override
    public double getParameter(int i) {
        return parameterizedFunction.getParameter(i);
    }

    @Override
    public void setParameter(int i, double v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
        
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author me
 */
abstract class AbstractParameterizedFunction implements ParameterizedFunction {
    
    
    private final int numInputs;

    public AbstractParameterizedFunction(int numInputs) {
        
        this.numInputs = numInputs;
 
    }
    
    @Override
    public int numberOfInputs() {
        return numInputs;
    }

    @Override
    public double minOutputDebug() {
        return 0;
    }

    @Override
    public double maxOutputDebug() {
        return 1;
    }    
    
    @Override
    public ArrayRealVector parameterGradient(ArrayRealVector output, double[] xs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addToParameters(ArrayRealVector deltas) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

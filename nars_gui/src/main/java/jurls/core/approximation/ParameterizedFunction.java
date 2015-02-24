/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author thorsten
 */
public interface ParameterizedFunction {

    public double value(double[] xs);
    
    /** result is a vector for the method to store a result in, allowing re-use
     *  and avoiding allocation of arrays. if null or of incorrect size, 
     *  the method will return a new instance of the array.
     */
    public ArrayRealVector parameterGradient(ArrayRealVector result, double... xs);

    public static ArrayRealVector array(ArrayRealVector candidate, int expectedSize) {
        if ((candidate == null) || (candidate.getDimension()!=expectedSize))
                return new ArrayRealVector(expectedSize);
                
        return candidate;        
    }
    
    public void addToParameters(ArrayRealVector deltas);

    public void learn(double[] xs, double y);

    public int numberOfParameters();

    public int numberOfInputs();
    
    public double minOutputDebug();

    public double maxOutputDebug();
    
    public double getParameter(int i);
    
    default public Scalar getScalarParameter(int i) {
        return null;
    }
    
    public void setParameter(int i, double v);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 *
 * @author me
 */
public class LinearInterpolationFunction implements ParameterizedFunction, Function<Double,Double> {
    
    protected int numPoints = 64;
    protected TreeMap<Double,Double> evidence = new TreeMap();

    public LinearInterpolationFunction(int numInputs, int numPoints) {
        this.numPoints = numPoints;
    }

    /** function approximation from collected evidence */
    @Override public Double apply(Double x) {
        
        return valueEvidence(x);
    }
    
    public double valueEvidence(double x) {
        //linear interpolate between two closest values
        Map.Entry<Double, Double> eLow = evidence.lowerEntry(x);
        Map.Entry<Double, Double> eHigh = evidence.higherEntry(x);
        
        if (eLow == null && eHigh == null)
            return 0;
        if ((eLow!=null) && (eHigh!=null)) {
            double lk = eLow.getKey();
            double hk = eHigh.getKey();
            double l = eLow.getValue();
            double h = eHigh.getValue();
            if (l == h) return l;
            if (x == lk) return l;
            if (x == hk) return h;

            double ld = Math.abs(lk - x);
            double lh = Math.abs(hk - x);
            double pl = ld / (ld + lh);

            return l * (1.0 - pl) + h * (pl);
        }
        if (eLow==null) {
            return eHigh.getValue();
        }
        if (eHigh==null) {
            return eLow.getValue();
        }
        return 0.0d;
        
    }

    @Override public double value(double[] xs) {
        return valueEvidence(xs[0]);
    }

    @Override
    public void parameterGradient(double[] output, double... xs) {

    }

    @Override
    public void addToParameters(double[] deltas) {

    }

    @Override
    public void learn(double[] X, double y) {
        if (X.length > 1) 
            throw new RuntimeException("Only one input variable supported currently");
        double x = X[0];
       
        while (evidence.size() > numPoints) {
            //remove a random point; this can be improved by merging points which are near
            double low = evidence.firstKey();
            double high = evidence.lastKey();
            double r = Math.random() * ( high - low ) + low;
            Double removed =  evidence.lowerKey(r);
            if (removed == null)
                removed = evidence.higherKey(r);
            if (removed!=null)
                evidence.remove( removed );
        }
            
        evidence.put(x, y);
        
        
            
    }
    
    @Override
    public int numberOfParameters() {
        //TODO 
        return numPoints;
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

//    @Override
//    public double getParameter(int i) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void setParameter(int i, double v) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

}



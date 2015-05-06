/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.obj;

import java.util.function.Function;
import objenome.goal.Between;
import objenome.solver.IncompleteSolutionException;
import objenome.goal.numeric.FindZeros;
import objenome.goal.numeric.OptimizeMultivariate;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author me
 */
public class NumericAnalysisTest {
    
    public static class ExampleScalarFunction  {
        
        private final double constParameter;

        public ExampleScalarFunction(@Between(min=-4.0, max=4.0) double constParameter) {
            
            this.constParameter = constParameter;
        }
        
        
        public double output(double x) {
            return Math.sin(constParameter * x) * (x-constParameter)*(x-constParameter);
        }

    }
    
    public static class ExampleMultivariateFunction  {
        
        private final double a;
        private final boolean b;

        public ExampleMultivariateFunction(@Between(min=-4.0, max=4.0) double a, boolean b) {
            
            this.a = a;
            this.b = b;
        }
        
        
        public double output(double x) {
            if (b)
                return Math.sin(a * x) * (x-a)*(x-a);
            else
                return Math.tanh(a * -x) * (x-a)*(x-a);
        }

        @Override
        public String toString() {
            return a + "," + b;
        }
        
        

    }    

    @Test public void testFindZeros() throws IncompleteSolutionException {

        Objenome o = Objenome.solve(new FindZeros(ExampleScalarFunction.class, 
                
                new Function<ExampleScalarFunction, Double>() {            
                    public Double apply(ExampleScalarFunction s) {                
                        return s.output(0.0) + s.output(0.5) + s.output(1.0);
                    }
                    
        }), ExampleScalarFunction.class);
        
        double bestParam = ((Number)o.getSolutions().get(0)).doubleValue();
        assertEquals(-3.97454, bestParam, 0.001);
    }

    @Test public void testMultivariate() throws IncompleteSolutionException {

        Objenome o = Objenome.solve(new OptimizeMultivariate(ExampleMultivariateFunction.class, new Function<ExampleMultivariateFunction, Double>() {

            public Double apply(ExampleMultivariateFunction s) {      
                double v = s.output(0.0) + s.output(0.5) + s.output(1.0);
                return v;
            }
            
        }) {
            @Override protected RandomGenerator getRandomGenerator() {
                JDKRandomGenerator j = new JDKRandomGenerator();  j.setSeed(0); return j;
            }        
        } .minimize(), ExampleMultivariateFunction.class);
        
        double bestParam = ((Number)o.getSolutions().get(1)).doubleValue();
        assertEquals(-2.5919, bestParam, 0.001);
    }
    
}

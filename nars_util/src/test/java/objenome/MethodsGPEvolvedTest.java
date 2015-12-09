/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome;

import objenome.solution.SetDoubleValue;
import objenome.solution.SetMethodsGPEvolved;
import objenome.solver.Solution;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author me
 */
public class MethodsGPEvolvedTest {

    public abstract static class ExampleUnknownMethod {
        
        public abstract double unknown(double x);

    }

    public abstract static class ExampleUnknownMethodWithConstructor {
        
        private final double constantParam;

        public ExampleUnknownMethodWithConstructor(double constantParam) {
            this.constantParam = constantParam;
        }

        public abstract double unknown(double x);
        
        /*public double output(double x) {
            return unknown(x) + constantParam;
        }*/

    }
    
    @Test
    public void testAbstractClass1() throws Exception {

        Multitainer a = new Multitainer();
        
        Objenome genome = a.random(ExampleUnknownMethod.class);
        
        List<Solution> genes = genome.getSolutions();
        
        System.out.println(genes);
        
        assertEquals(1, genes.size());
        //assertEquals("first gene is for the double constant parameter", SetDoubleValue.class, genes.get(0).getClass());
        assertEquals("gene is for the abstract method", SetMethodsGPEvolved.class, genes.get(0).getClass());
        
        Container c = genome.container();
        Object result = c.get(ExampleUnknownMethod.class);
        
        assertNotNull(result);
        assertTrue(!result.getClass().getName().equals(ExampleUnknownMethod.class.getName()));
        assertEquals(ExampleUnknownMethod.class, result.getClass().getSuperclass());

        
        assertEquals(c, result.getClass().getField("pheno").get(result));
    }

    @Test
    public void testAbstractClass2() {
        Multitainer a = new Multitainer();
        
        Objenome genome = a.random(ExampleUnknownMethodWithConstructor.class);
        
        List<Solution> genes = genome.getSolutions();                
        
        assertEquals(2, genes.size());
        assertEquals("first gene is for the double constant parameter", SetDoubleValue.class, genes.get(0).getClass());
        assertEquals("second gene is for the abstract method", SetMethodsGPEvolved.class, genes.get(1).getClass());
        
        
        Object result = genome.get(ExampleUnknownMethodWithConstructor.class);
        assertNotNull(result);
        
    }
}

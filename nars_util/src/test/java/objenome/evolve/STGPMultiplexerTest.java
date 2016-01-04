package objenome.evolve;

import junit.framework.TestCase;
import objenome.problem.STGPBoolean;
import objenome.solver.evolve.Population;
import objenome.solver.evolve.TypedOrganism;
import objenome.util.BenchmarkSolutions;
import org.junit.Test;

public class STGPMultiplexerTest extends TestCase {

    public static STGPBoolean.BooleanCases multiplexerProblem(int bits) {
        int noAddressBits = BenchmarkSolutions.multiplexerAddressBits(bits);
        Boolean[][] inputValues = BenchmarkSolutions.generateBoolSequences(bits);
        Boolean[] expectedOutputs = new Boolean[inputValues.length];
        for (int i = 0; i < inputValues.length; i++) {
            expectedOutputs[i] = BenchmarkSolutions.multiplexer(inputValues[i], noAddressBits);
        }
        return new STGPBoolean.BooleanCases(inputValues, expectedOutputs);
    }

    @Test public void testSTGPMultiplexer() {

        int popSize = 100;
        int generations = 50;
            STGPBoolean e = new STGPBoolean(multiplexerProblem(6), popSize, generations);
            
            Population<TypedOrganism> p = e.cycle();
            
            TypedOrganism best = p.best();
            
            System.out.println(p.best());
            System.out.println(p.size());            
            System.out.println(p);            
            System.out.println(best.evaluate());
            
            assertTrue(best.depth() > 1);
            assertEquals(popSize, p.size());
            assertNotNull(p.best());
            
            
            
		
		//final int noSuccess = getNoSuccesses(model, false, false);
	}
}
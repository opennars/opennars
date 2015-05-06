package nars.obj.evolve;

import objenome.solver.evolve.STGPIndividual;
import objenome.goal.ProblemSTGP;
import objenome.solver.evolve.Population;
import junit.framework.TestCase;
import objenome.goal.STGPRegression;
import objenome.util.BenchmarkSolutions;
import org.junit.Test;

public class STGPRegressionTest extends TestCase {

	@Test public void testRegression() {            
            
            ProblemSTGP e = new STGPRegression(20, BenchmarkSolutions.XpXXpXXX);
            
            Population<STGPIndividual> p = e.run();
            
            STGPIndividual best = p.fittest();
            
            //assertTrue(best.depth() > 1);            
            assertEquals(100, p.size());
            assertNotNull(p.fittest());

            
            System.out.println(p.fittest());
            System.out.println(p.size());            
            System.out.println(p);            
            System.out.println(best.evaluate());
            
            
            
	}
}
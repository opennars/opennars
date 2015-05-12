package objenome.evolve;

import objenome.solver.evolve.TypedOrganism;
import objenome.problem.ProblemSTGP;
import objenome.solver.evolve.Population;
import junit.framework.TestCase;
import objenome.problem.STGPRegression;
import objenome.util.BenchmarkSolutions;
import org.junit.Test;

public class STGPRegressionTest extends TestCase {

	@Test public void testRegression() {            
            
            ProblemSTGP e = new STGPRegression(20, BenchmarkSolutions.XpXXpXXX);
            
            Population<TypedOrganism> p = e.cycle();
            
            TypedOrganism best = p.best();
            
            //assertTrue(best.depth() > 1);            
            assertEquals(100, p.size());
            assertNotNull(p.best());

            
            System.out.println(p.best());
            System.out.println(p.size());            
            System.out.println(p);            
            System.out.println(best.evaluate());
            
            
            
	}
}
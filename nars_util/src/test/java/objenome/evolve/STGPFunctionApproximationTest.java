package objenome.evolve;

import com.google.common.collect.Lists;

import java.util.List;
import objenome.solver.evolve.STGPIndividual;
import objenome.solver.evolve.Population;
import junit.framework.TestCase;
import objenome.goal.Observation;
import objenome.goal.STGPFunctionApproximation;
import objenome.solver.evolve.Individual;
import org.junit.Test;

public class STGPFunctionApproximationTest extends TestCase {

	@Test public void testRegression() {            
            
            int individuals = 100;
            double cullRate = 0.95;
            
            STGPFunctionApproximation e = new STGPFunctionApproximation(individuals, 5, true, true, false, true) {
                @Override
                public Population<STGPIndividual> run() {
                    Population<STGPIndividual> p = super.run();

                    System.out.println(getBestError() + " = " + getBest());

                    return p;
                }
            };
            
            //setup function
            int j =0;
            for (double x = 0; x < 4.0; x+=0.1) {
                e.samples.add(new Observation<Double[], Double>( 
                        new Double[] { x },
                        /*(j ^ (j+10)) * */ ( Math.sin(x) * Math.tan(x * 0.5))
                ));
                j++;
            }
            
            Population<STGPIndividual> p = e.run();
            
            STGPIndividual best = p.fittest();
            
            //assertTrue(best.depth() > 1);            
            assertEquals(individuals, p.size());
            assertNotNull(p.fittest());

            List<Individual>  firstBest = Lists.newArrayList(p.elites(0.5f));
            
            System.out.println(p.fittest());
            System.out.println(p.size());            
            System.out.println(p);            
            System.out.println(best.evaluate());


            int sizeBefore = p.size();
            p.cullThis(cullRate);
            int sizeAfter = p.size();

            assertTrue(sizeAfter < sizeBefore);
            
            p = e.run();
            
            assertEquals(individuals, p.size());

            int loops = 150;
            for (int i = 0; i < loops; i++) {
                p.cullThis(cullRate);
                p = e.run();
            }
            
            List<Individual> nextBest = Lists.newArrayList(p.elites(0.5f));
            
            System.out.println(nextBest);
            
            //show some evolution in change of elites
            assertTrue(!firstBest.equals(nextBest));
            
	}
}
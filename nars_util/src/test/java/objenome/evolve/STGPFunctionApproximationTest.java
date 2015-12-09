package objenome.evolve;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import objenome.goal.Observation;
import objenome.problem.STGPFunctionApproximation;
import objenome.solver.evolve.Organism;
import objenome.solver.evolve.Population;
import objenome.solver.evolve.TypedOrganism;
import org.junit.Test;

import java.util.List;

public class STGPFunctionApproximationTest extends TestCase {

	@Test public void testRegression() {            
            
            int individuals = 25;
            double cullRate = 0.95;
            
            STGPFunctionApproximation e = new STGPFunctionApproximation(individuals, 5, true, true, false, true) {
                @Override
                public Population<TypedOrganism> cycle() {
                    Population<TypedOrganism> p = super.cycle();

                    //System.out.println(getBestError() + " = " + getBest());

                    return p;
                }
            };
            
            //setup function
            int j =0;
            for (double x = 0; x < 4.0; x+=0.1) {
                e.samples.add(new Observation<>(
                        new Double[]{x},
                        /*(j ^ (j+10)) * */ (Math.sin(x) * Math.tan(x * 0.5))
                ));
                j++;
            }
            
            Population<TypedOrganism> p = e.cycle();
            
            TypedOrganism best = p.best();
            
            //assertTrue(best.depth() > 1);            
            assertEquals(individuals, p.size());
            assertNotNull(p.best());

            List<Organism>  firstBest = Lists.newArrayList(p.elites(0.5f));
            
            System.out.println(p.best());
            System.out.println(p.size());            
            System.out.println(p);            
            System.out.println(best.evaluate());


            int sizeBefore = p.size();
            p.cullThis(cullRate);
            int sizeAfter = p.size();

            assertTrue(sizeAfter < sizeBefore);
            
            p = e.cycle();
            
            assertEquals(individuals, p.size());

            int loops = 25;
            for (int i = 0; i < loops; i++) {
                p.cullThis(cullRate);
                p = e.cycle();
            }
            
            List<Organism> nextBest = Lists.newArrayList(p.elites(0.5f));
            
            System.out.println(nextBest);
            
            //show some evolution in change of elites
            assertTrue(!firstBest.equals(nextBest));
            
	}
}
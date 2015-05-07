package objenome.evolve;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import objenome.goal.DefaultProblemSTGP;
import objenome.op.Variable;
import objenome.solver.evolve.*;
import objenome.solver.evolve.fitness.CachedFitnessFunction;
import objenome.solver.evolve.fitness.DoubleFitness;
import objenome.solver.evolve.fitness.STGPFitnessFunction;
import org.junit.Test;

import java.util.List;

public class STGPMultivarTest extends TestCase {

    int individuals = 150;
    int generations = 2024;

    @Test
    public void testRegression() {


        DefaultProblemSTGP e = new DefaultProblemSTGP(individuals, 5, true, true, false, true) {

            Variable va, vb;

            @Override
            protected FitnessFunction initFitness() {
                return new CachedFitnessFunction(new STGPFitnessFunction() {
                    @Override
                    public Fitness evaluate(Population population, STGPIndividual individual) {

                        double score = 0;

                        for (double a = -1; a<=1; a+=0.1) {

                            va.setValue(a);

                            for (double b = -1; b<=1; b+=0.1) {

                                vb.setValue(b);

                                double w = (Double)individual.evaluate();

                                double z = Math.sin(a) * Math.tan(b * Math.abs(a - b));

                                score += 1.0 / (1.0 + Math.abs(w-z));
                            }
                        }

                        evaluated(individual, score);

                        return new DoubleFitness.Maximise(score);
                    }

                });


            }


            @Override
            protected Iterable<Variable> initVariables() {
                return Lists.newArrayList(
                        va = doubleVariable("a"), vb = doubleVariable("b")
                );
            }

            @Override
            public Population<STGPIndividual> run() {
                Population<STGPIndividual> p = super.run();

                //System.out.println(getBestError() + " = " + getBest());

                return p;
            }
        };



        Population p = null;

        for ( ; 0 <= generations; generations--) {
            p = e.run();
            System.out.println("generation: " + generations);
            System.out.println(p.size());
        }

        List<Individual> nextBest = Lists.newArrayList(p.elites(0.5f));

        System.out.println(nextBest);

        //show some evolution in change of elites
        //assertTrue(!firstBest.equals(nextBest));

    }


    double bestVal = Double.MIN_VALUE;
    STGPIndividual best = null;

    protected void evaluated(STGPIndividual i, double score) {
        if (score > bestVal) {
            bestVal = score;
            best = i;
            System.out.println(score + ": " + i);
        }
    }

}


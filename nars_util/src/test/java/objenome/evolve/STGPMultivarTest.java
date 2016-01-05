package objenome.evolve;

import com.google.common.collect.Lists;
import jdk.nashorn.internal.ir.annotations.Ignore;
import objenome.goal.DefaultProblemSTGP;
import objenome.goal.DoubleFitness;
import objenome.goal.TypedFitnessFunction;
import objenome.op.Variable;
import objenome.solver.evolve.*;

import java.util.List;

@Ignore
public enum STGPMultivarTest {
    ;

    static int individuals = 4096;
    static int generations = 202400;


    public static void main(String[] a) {


        DefaultProblemSTGP e = new DefaultProblemSTGP(individuals, 6, true, true, true, true) {

            Variable va, vb;

            @Override
            protected FitnessFunction initFitness() {
                return
                //new CachedFitnessFunction(
                        new TypedFitnessFunction() {
                    @Override
                    public Fitness evaluate(Population population, TypedOrganism individual) {

                        double cost = 0;
                        double range = 1;

                        int samples = 0;

                        for (double a = -range; a<=range; a+=0.05) {

                            va.set(a);

                            for (double b = -range; b<=range; b+=0.05) {

                                vb.set(b);

                                double w = individual.eval();

                                double z = ((a*10)%2 - 0.5) + (b*7)%3 - 1;
                                //double z = a * (a + b);

                                cost += Math.abs(w-z);
                                samples++;
                            }
                        }

                        cost/=samples;

                        double score = 1.0 / (1.0 + cost);

                        evaluated(individual, score);


                        return new DoubleFitness.Maximise(score);
                    }

                };




            }


            @Override
            protected Iterable<Variable> initVariables() {
                return Lists.newArrayList(
                        va = doubleVariable("a"), vb = doubleVariable("b")
                );
            }


        };



        Population p = null;

        for ( ; 0 <= generations; generations--) {
            p = e.cycle();
            //System.out.println(Arrays.toString(p.elites(1.0)));
        }

        List<Organism> nextBest = Lists.newArrayList(p.elites(0.1f));

        System.out.println(nextBest);

        //show some evolution in change of elites
        //assertTrue(!firstBest.equals(nextBest));

    }


    static double bestVal = Double.MIN_VALUE;
    static TypedOrganism best = null;

    protected static void evaluated(TypedOrganism i, double score) {
        if (score > bestVal) {
            bestVal = score;
            best = i;
            System.out.println(score + ": " + i);
        }
    }

}


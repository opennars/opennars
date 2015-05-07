package nars.analyze.experimental;

import nars.Global;
import nars.NAR;
import nars.Symbols;
import nars.event.CycleReaction;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.filter.ConstantDerivationLeak;
import nars.testing.TestNAR;
import objenome.goal.DefaultProblemSTGP;
import objenome.op.DoubleVariable;
import objenome.op.Variable;
import objenome.solver.evolve.FitnessFunction;
import objenome.solver.evolve.STGPIndividual;
import objenome.solver.evolve.fitness.DoubleFitness;
import objenome.solver.evolve.fitness.STGPFitnessFunction;

import java.util.Arrays;
import java.util.function.Consumer;

import static objenome.goal.DefaultProblemSTGP.doubleVariable;

/**
 * https://github.com/apache/commons-math/blob/master/src/test/java/org/apache/commons/math4/genetics/GeneticAlgorithmTestBinary.java
 */
public class OptimizeLeakGenetic2 extends Controls {


    final static int cycles = 25;
    final int individuals = 16;

    static int g = 1;


    public final DoubleVariable derPri = doubleVariable("derPri");
    public final DoubleVariable derQua = doubleVariable("derQua");
    public final DoubleVariable derQuest = doubleVariable("derQuest");
    public final DoubleVariable derJudge = doubleVariable("derJudge");
    public final DoubleVariable derGoal = doubleVariable("derGoal");
    public final DoubleVariable derComplex = doubleVariable("derComplex");
    public final DoubleVariable c0 = doubleVariable("c0");
    public final DoubleVariable c1 = doubleVariable("c1");
    public final DoubleVariable c2 = doubleVariable("c2");
    public final DoubleVariable c3 = doubleVariable("c3");


    double[] conPri = new double[4];

    protected void onCycle(NAR n) {
        n.memory.concepts.conceptPriorityHistogram(conPri);
        c0.set(conPri[0]);
        c1.set(conPri[1]);
        c2.set(conPri[2]);
        c3.set(conPri[3]);
    }

    protected void setDerived(Task d) {

        derPri.set(d.getPriority() );
        derPri.set(d.getQuality() );

        char p = d.getPunctuation();

        derJudge.set( p == Symbols.JUDGMENT ? 1 : 0);
        derGoal.set( p == Symbols.GOAL? 1 : 0);
        derQuest.set( ((p == Symbols.QUEST) || (p == Symbols.QUESTION)) ? 1 : 0);
        derComplex.set( d.getTerm().getComplexity() );

    }


    public double cost(final STGPIndividual leakProgram) {
        //float jLeakPri, float jLeakDur, float gLeakPri,float gLeakDur, float qLeakPri, float qLeakDur
//        jLeakPri.getFloat(),
//                jLeakDur.getFloat(),
//                gLeakPri.getFloat(),
//                gLeakDur.getFloat(),
//                qLeakPri.getFloat(),
//                qLeakDur.getFloat()
//        );

        Default b = new Default() {
            @Override
            protected void initDerivationFilters() {

                getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(0, 0) {
                    @Override
                    protected void leak(Task derived) {

                        setDerived(derived);

                        float mult = (float)leakProgram.eval();

                        if (!Double.isFinite(mult))
                            mult = 0;

                        derived.mulPriority(mult);
                    }
                });
            }
        };
        b.setInternalExperience(null);

        Consumer<TestNAR> eachNAR = new Consumer<TestNAR>() {

            @Override
            public void accept(TestNAR nar) {


                new CycleReaction(nar) {

                    @Override
                    public void onCycle() {
                        OptimizeLeakGenetic2.this.onCycle(nar);
                    }

                };
                //testNAR.memory.logic.on();
            }
        };
        ExampleScores e = new ExampleScores(b, cycles, eachNAR, "test1");

        //e.memory.logic.setActive(isActive());

//        System.out.println(
//                n4(jLeakPri) + ", " + n4(jLeakDur) + ",   " +
//                        n4(gLeakPri) + ", " + n4(gLeakDur) + ",   " +
//                        n4(qLeakPri) + ", " + n4(qLeakDur) + ",   " +
//                        "   " + e.totalCost);

        return e.totalCost;
    }


    public static void main(String[] args) {
        Global.DEBUG = false;

        new OptimizeLeakGenetic2();
    }





        public OptimizeLeakGenetic2() {

            super();

            reflect(this);

            DefaultProblemSTGP e = new DefaultProblemSTGP(individuals, 4, true, false, false, true) {

                Variable va, vb;

                @Override
                protected FitnessFunction initFitness() {
                    return
                            //new CachedFitnessFunction(
                            new STGPFitnessFunction() {
                                @Override
                                public objenome.solver.evolve.Fitness evaluate(objenome.solver.evolve.Population population, STGPIndividual individual) {


                                    double range = 1;

                                    double cost = cost(individual);

                                    double score = 1.0 / (1.0 + cost);

                                    evaluated(individual, score);


                                    return new DoubleFitness.Maximise(score);
                                }

                            };




                }


                @Override
                protected Iterable<Variable> initVariables() {
                    return OptimizeLeakGenetic2.this.getVariables();
                }


            };



            objenome.solver.evolve.Population p = null;

            while (true) {

                long start = System.currentTimeMillis();
                p = e.cycle();
                long time = System.currentTimeMillis()-start;

                if (g%1 == 0)
                    System.out.println(g + " (" + time + " ms) " + Arrays.toString(p.elites(0.25)));

                g++;

                System.gc();
            }

            //List<Individual> nextBest = Lists.newArrayList(p.elites(0.1f));

            //System.out.println(nextBest);

            //show some evolution in change of elites
            //assertTrue(!firstBest.equals(nextBest));

        }


        static double bestVal = Double.MIN_VALUE;
        static STGPIndividual best = null;

        static protected void evaluated(STGPIndividual i, double score) {
            if (score > bestVal) {
                bestVal = score;
                best = i;

                System.out.println();
                System.out.println(score + ": " + i);
                System.out.println();
            }
        }



}

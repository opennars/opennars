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
import objenome.solver.evolve.TypedOrganism;
import objenome.goal.DoubleFitness;
import objenome.goal.TypedFitnessFunction;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.List;
import java.util.function.Consumer;

import static objenome.goal.DefaultProblemSTGP.doubleVariable;

/**
 * https://github.com/apache/commons-math/blob/master/src/test/java/org/apache/commons/math4/genetics/GeneticAlgorithmTestBinary.java
 */
public class OptimizeLeakGenetic2 {


    final static int cycles = 750;
    final int individuals = 48;

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
    private final List<Variable> variables;


    double[] conPri = new double[4];

    protected void onCycle(NAR n) {
        n.memory.concepts.conceptPriorityHistogram(conPri);

        //these represent the points of the boundaries between histogram bins
        c0.set(conPri[0]);
        c1.set(conPri[0] + conPri[1]);
        c2.set(conPri[0] + conPri[1] + conPri[2]);

    }

    protected void setDerived(Task d) {

        derPri.set(d.getPriority());
        derQua.set(d.getQuality());

        char p = d.getPunctuation();

        derJudge.set(p == Symbols.JUDGMENT ? 1 : 0);
        derGoal.set(p == Symbols.GOAL ? 1 : 0);
        derQuest.set(((p == Symbols.QUEST) || (p == Symbols.QUESTION)) ? 1 : 0);

        derComplex.set(d.getTerm().getComplexity());

    }


    public double cost(final TypedOrganism leakProgram) {

        Default b = new Default() {
            @Override
            protected void initDerivationFilters() {

                getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(0, 0) {
                    @Override
                    protected boolean leak(Task derived) {


                        System.out.println(derived.getExplanation());

                        setDerived(derived);

                        float mult = (float) leakProgram.eval();

                        if (!Double.isFinite(mult))
                            mult = 0;

                        derived.mulPriority(mult);

                        if (derived.getPriority() < Global.BUDGET_THRESHOLD)
                            return false;


                        System.out.println(" " + derived.getPriority() + " " + mult + " ");
                        return true;
                    }
                });
            }
        };
        b.level(6);
        b.setConceptBagSize(512);
        b.setInternalExperience(null);

        Consumer<TestNAR> eachNAR = new Consumer<TestNAR>() {

            @Override
            public void accept(TestNAR nar) {


//                new AbstractReaction(nar.memory.event, Events.TaskRemove.class) {
//
//                    @Override
//                    public void event(Class event, Object... args) {
//                        taskRemoved++;
//                    }
//                };

                new CycleReaction(nar) {

                    @Override
                    public void onCycle() {
                        OptimizeLeakGenetic2.this.onCycle(nar);
                    }

                };
                //testNAR.memory.logic.on();
            }
        };

        ExampleScores e = new ExampleScores(b, cycles, eachNAR, "test1", "test2", "test3", "test4", "test5", "test6");

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

        variables = Controls.reflect(OptimizeLeakGenetic2.class, this);

        DefaultProblemSTGP e = new DefaultProblemSTGP(individuals, 7, true, true, false, true) {


            @Override
            protected FitnessFunction initFitness() {
                return
                        //new CachedFitnessFunction(
                        new TypedFitnessFunction() {
                            @Override
                            public objenome.solver.evolve.Fitness evaluate(objenome.solver.evolve.Population population, TypedOrganism individual) {

                                return new DoubleFitness.Minimize(
                                        cost(individual)
                                );
                            }

                        };


            }


            @Override
            protected Iterable<Variable> initVariables() {
                return variables;
            }


        };


        objenome.solver.evolve.Population p = null;

        while (true) {

            long start = System.currentTimeMillis();
            p = e.cycle();
            long time = System.currentTimeMillis() - start;

            if (g % 1 == 0) {
                //System.out.println(g + " (" + time + " ms) " + Arrays.toString(p.elites(0.25)));

                SummaryStatistics stats = p.getStatistics();
                System.out.println(g + " min=" + stats.getMin() + "..mean=" + stats.getMean() + "..max=" + stats.getMax() + "  (" + time + " ms) ");

                System.out.println(p.best().toString());
            }

            g++;

            System.gc();
        }

        //List<Individual> nextBest = Lists.newArrayList(p.elites(0.1f));

        //System.out.println(nextBest);

        //show some evolution in change of elites
        //assertTrue(!firstBest.equals(nextBest));

    }


//    static double bestVal = Double.MAX_VALUE;
//    static STGPIndividual best = null;
//
//    static protected void evaluated(STGPIndividual i, double cost) {
//        if (cost < bestVal) {
//            bestVal = cost;
//            best = i;
//
//            System.out.println();
//            System.out.println(cost + ": " + i);
//            System.out.println();
//        }
//    }


}

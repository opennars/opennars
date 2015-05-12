package nars.analyze.experimental;

import nars.Global;
import nars.NAR;
import nars.Symbols;
import nars.event.CycleReaction;
import nars.io.LibraryInput;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.filter.ConstantDerivationLeak;
import nars.testing.TestNAR;
import nars.testing.condition.OutputCondition;
import objenome.op.DoubleVariable;
import objenome.op.Node;
import objenome.op.Variable;
import objenome.op.math.*;
import objenome.solver.Civilization;
import objenome.solver.EGoal;
import objenome.solver.evolve.OrganismBuilder;
import objenome.solver.evolve.RandomSequence;
import objenome.solver.evolve.TypedOrganism;
import objenome.solver.evolve.init.Full;

import java.util.ArrayList;
import java.util.List;

import static objenome.goal.DefaultProblemSTGP.doubleVariable;

/**
 * https://github.com/apache/commons-math/blob/master/src/test/java/org/apache/commons/math4/genetics/GeneticAlgorithmTestBinary.java
 */
public class OptimizeLeakGenetic3 extends Civilization<TypedOrganism> {


    public static void main(String[] args) {
        Global.DEBUG = false;

        final int threads = 3;
        final int individuals = 8;
        final int cycles = 750;

        Civilization c = new OptimizeLeakGenetic3(threads, individuals, cycles);
        c.run();
    }

    @Override
    public List<Node> getOperators(RandomSequence random) {
        List<Node> l = new ArrayList();

        l.add( new DoubleERC(random, -1.0, 2.0, 2));

        l.add(new Add());
        l.add(new Subtract());
        l.add(new Multiply());
        l.add(new DivisionProtected());
        return l;
    }

    public OptimizeLeakGenetic3(int threads, int populationSize, final int maxCycles) {
        super(threads, populationSize);




        for (String path : LibraryInput.getPaths("test1", "test2", "test3", "test4", "test5", "test6")) {
            add(new LibraryGoal(path, maxCycles));
        }

        System.out.println(this + ": " + goals.size() + " goals");



        long g;

//        while (true) {
//
//            long start = System.currentTimeMillis();
//            //p = e.cycle();
//            long time = System.currentTimeMillis() - start;
//
//            if (g % 1 == 0) {
//                //System.out.println(g + " (" + time + " ms) " + Arrays.toString(p.elites(0.25)));
//
//                SummaryStatistics stats = getPopulation().getStatistics();
//                System.out.println(g + " min=" + stats.getMin() + "..mean=" + stats.getMean() + "..max=" + stats.getMax() + "  (" + time + " ms) ");
//
//                System.out.println(getPopulation().best().toString());
//            }
//
//            g++;
//
//            System.gc();
//        }



    }

    public static class LibraryGoal extends EGoal<TypedOrganism> {

        public final DoubleVariable derPri;
        public final DoubleVariable derQua;
        public final DoubleVariable derQuest;
        public final DoubleVariable derJudge;
        public final DoubleVariable derGoal;

        public final DoubleVariable derComplex;

        public final DoubleVariable c0;
        public final DoubleVariable c1;
        public final DoubleVariable c2;

        private final String path;
        private final int maxCycles;

        public final List<Variable> var = Controls.reflect(LibraryGoal.class, this);
        private final String script;

        double[] conPri = new double[4];

        public LibraryGoal(String path, int maxCycles) {
            super(path);

            this.path = path;
            this.maxCycles = maxCycles;
            this.script = LibraryInput.getExample(path);


            derPri = doubleVariable("derPri");
            derQua = doubleVariable("derQua");
            derQuest = doubleVariable("derQuest");
            derJudge = doubleVariable("derJudge");
            derGoal = doubleVariable("derGoal");
            derComplex = doubleVariable("derComplex");
            c0 = doubleVariable("c0");
            c1 = doubleVariable("c1");
            c2 = doubleVariable("c2");
        }

        protected void cycle(NAR n) {
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


        @Override
        public double cost(TypedOrganism leakProgram) {

            Default b = new Default() {
                @Override
                protected void initDerivationFilters() {

                    getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(0, 0) {
                        @Override
                        protected boolean leak(Task derived) {


                            //System.out.println(derived.getExplanation());

                            setDerived(derived);

                            float mult = (float) leakProgram.eval();

                            if (!Double.isFinite(mult))
                                mult = 0;

                            derived.mulPriority(mult);

                            if (derived.getPriority() < Global.BUDGET_THRESHOLD)
                                return false;


                            //System.out.println(" " + derived.getPriority() + " " + mult + " ");
                            return true;
                        }
                    });
                }
            };
            b.level(6);
            b.setConceptBagSize(512);
            b.setInternalExperience(null);

            TestNAR nar = new TestNAR(b);

            new CycleReaction(nar) {
                @Override
                public void onCycle() {
                    cycle(nar);
                }
            };

            nar.requires.addAll(OutputCondition.getConditions(nar, script, 0));

            nar.input(script);

            nar.run(maxCycles);

            double cost = OutputCondition.cost(nar);
            if (Double.isInfinite(cost))
                cost = maxCycles;
            return cost;
        }

    }

}

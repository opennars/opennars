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
import objenome.op.VariableNode;
import objenome.op.math.*;
import objenome.op.trig.HyperbolicTangent;
import objenome.solver.Civilization;
import objenome.solver.EGoal;
import objenome.solver.evolve.RandomSequence;
import objenome.solver.evolve.TypedOrganism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static objenome.goal.DefaultProblemSTGP.doubleVariable;

/**
 * https://github.com/apache/commons-math/blob/master/src/test/java/org/apache/commons/math4/genetics/GeneticAlgorithmTestBinary.java
 */
public class OptimizeLeakGenetic3 extends Civilization<TypedOrganism> {




    public static void main(String[] args) {
        Global.DEBUG = false;

        final int threads = 2;
        final int individuals = 64;
        final int cycles = 750;

        Civilization c = new OptimizeLeakGenetic3(threads, individuals, cycles);
        c.run(10000);
    }

    @Override
    public List<Node> getOperators(RandomSequence random) {
        List<Node> l = new ArrayList();

        l.add( new DoubleERC(random, -1.0, 2.0, 2));

        l.add(new Add());
        l.add(new Subtract());
        l.add(new Multiply());
        l.add(new DivisionProtected());


        l.add(new Min2());
        l.add(new Max2());
        l.add(new Absolute());

        l.add(new HyperbolicTangent());

        LibraryGoal prototype = new LibraryGoal();
        for (Variable v : prototype.var)
            l.add(new VariableNode(v));

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

        public List<Variable> var;
        public Variable<Double> derPri;
        public Variable<Double> derQua;
        public Variable<Double> derQuest;
        public Variable<Double> derJudge;
        public Variable<Double> derGoal;

        public Variable<Double> derComplex;

        public Variable<Double> c0;
        public Variable<Double> c1;
        public Variable<Double> c2;

        private String path;
        private int maxCycles;

        private String script;

        double[] conPri = new double[4];

        public LibraryGoal() {
            super(null);

            initVar();

        }

        protected void initVar() {
            derPri = doubleVariable("derPri");
            derQua = doubleVariable("derQua");
            derQuest = doubleVariable("derQuest");
            derJudge = doubleVariable("derJudge");
            derGoal = doubleVariable("derGoal");
            derComplex = doubleVariable("derComplex");
            c0 = doubleVariable("c0");
            c1 = doubleVariable("c1");
            c2 = doubleVariable("c2");

            var = Controls.reflect(LibraryGoal.class, this);
        }

        public LibraryGoal(String path, int maxCycles) {
            super(path);

            this.path = path;
            this.maxCycles = maxCycles;
            this.script = LibraryInput.getExample(path);

            initVar();

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


            //bind all program variables to this instance
            derPri = leakProgram.var("derPri");
            derQua = leakProgram.var("derQua");
            derQuest = leakProgram.var("derQuest");
            derJudge = leakProgram.var("derJudge");
            derGoal = leakProgram.var("derGoal");
            derComplex = leakProgram.var("derComplex");
            c0 = leakProgram.var("c0");
            c1 = leakProgram.var("c1");
            c2 = leakProgram.var("c2");

            Default b = new Default() {
                @Override
                protected void initDerivationFilters() {

                    getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(0, 0) {
                        @Override
                        protected boolean leak(Task derived) {


                            //System.out.println(derived.getExplanation());

                            setDerived(derived);

                            float newPriority = (float) leakProgram.eval();

                            if (!Double.isFinite(newPriority))
                                newPriority = 0;

                            derived.setPriority(newPriority);

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
                cost = 1.0;

            return cost / maxCycles;
        }

    }

}

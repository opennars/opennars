//package nars.meter.experiment;
//
//import nars.Global;
//import nars.Symbols;
//import nars.event.CycleReaction;
//import LibraryInput;
//import nars.meter.TestNAR;
//import nars.nar.Default;
//import nars.task.TaskSeed;
//import objenome.op.Variable;
//import objenome.solver.Civilization;
//import objenome.solver.DefaultCivilization;
//import objenome.solver.evolve.TypedOrganism;
//
//import static objenome.goal.DefaultProblemSTGP.doubleVariable;
//
///**
// * https://github.com/apache/commons-math/blob/master/src/test/java/org/apache/commons/math4/genetics/GeneticAlgorithmTestBinary.java
// */
//public class OptimizeLeakGenetic3  {
//
//
//
//
//    public static void main(String[] args) {
//        Global.DEBUG = true;
//
//        final int threads = 3;
//        final int individuals = 64;
//        final int maxDepth = 6;
//
//        Civilization c = new DefaultCivilization(threads, individuals, maxDepth);
//
//        final int cycles = 2050;
//        for (String path : LibraryInput.getPaths("test1", "test2", "test3", "test4", "test5", "test6")) {
//            c.add(new BudgetFilteringExperiment(path, cycles));
//        }
//
//        //System.out.println(this + ": " + goals.size() + " goals");
//
//
//
//        c.start();
//    }
//
//
//
//
//    public static class BudgetFilteringExperiment extends TestCompletionSpeed {
//
//
//
//        public Variable<Double> derPri = derPri = doubleVariable("derPri");
//        public Variable<Double> derQua = doubleVariable("derQua");
//        public Variable<Double> derQuest = doubleVariable("derQuest");
//        public Variable<Double> derJudge = doubleVariable("derJudge");
//        public Variable<Double> derGoal = doubleVariable("derGoal");
//
//        public Variable<Double> derComplex;
//
//        public Variable<Double> c0;
//        public Variable<Double> c1;
//        public Variable<Double> c2;
//
//
//        double[] conPri = new double[4];
//
//        public BudgetFilteringExperiment(String path, int maxCycles) {
//            super(path, maxCycles);
//        }
//
//
//        protected void setDerived(TaskSeed d) {
//
//            derPri.set(d.getPriority());
//            derQua.set(d.getQuality());
//
//            char p = d.getPunctuation();
//
//            derJudge.set(p == Symbols.JUDGMENT ? 1 : 0);
//            derGoal.set(p == Symbols.GOAL ? 1 : 0);
//            derQuest.set(((p == Symbols.QUEST) || (p == Symbols.QUESTION)) ? 1 : 0);
//
//            derComplex.set(d.getTerm().complexity());
//
//        }
//
//
//        @Override
//        public TestNAR newNAR(TypedOrganism leakProgram) {
//
//            //bind all program variables to this instance
//            derPri = leakProgram.var("derPri");
//            derQua = leakProgram.var("derQua");
//            derQuest = leakProgram.var("derQuest");
//            derJudge = leakProgram.var("derJudge");
//            derGoal = leakProgram.var("derGoal");
//            derComplex = leakProgram.var("derComplex");
//            c0 = leakProgram.var("c0");
//            c1 = leakProgram.var("c1");
//            c2 = leakProgram.var("c2");
//
//
//            Default b = new Default() {
////                @Override
////                protected void initDerivationFilters() {
////
////                    getLogicPolicy().derivationFilters.add(new MultiplyDerivedBudget(0, 0) {
////                        @Override
////                        protected boolean leak(TaskSeed derived) {
////
////
////                            //System.out.println(derived.getExplanation());
////
////                            setDerived(derived);
////
////                            float newPriority = (float) leakProgram.eval();
////
////                            if (!Double.isFinite(newPriority))
////                                newPriority = 0;
////
////                            derived.setPriority(newPriority);
////
////                            if (derived.getPriority() < 0)
////                                return false;
////
////
////                            //System.out.println(" " + derived.getPriority() + " " + mult + " ");
////                            return true;
////                        }
////                    });
////                }
//            };
//            b.level(6);
//            b.setActiveConcepts(512);
//            b.setInternalExperience(null);
//
//
//
//            TestNAR nar = new TestNAR(b);
//
//            new CycleReaction(nar) {
//                @Override
//                public void onCycle() {
//
//                    nar.memory.getCycleProcess().conceptPriorityHistogram(conPri);
//
//                    //these represent the points of the boundaries between histogram bins
//                    c0.set(conPri[0]);
//                    c1.set(conPri[0] + conPri[1]);
//                    c2.set(conPri[0] + conPri[1] + conPri[2]);
//                }
//            };
//
//            return nar;
//        }
//
//
//    }
//
//}

///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.evolve;
//
//import nars.Global;
//import nars.NAR;
//import nars.nar.Default;
//import nars.util.data.MultiOutputStream;
//import org.apache.commons.math3.genetics.*;
//import org.encog.ml.CalculateScore;
//import org.encog.ml.MLMethod;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.util.*;
//
///**
// * Refactoring to use Apache Commons Genetics packages which should be more
// * flexible than Encog's because they use generic types
// */
//public class GeneticSearchApache {
//
//    int maxCycles = 32;
//    int populationSize = 16;
//    int generationsPerPopulation = 16;
//    final int winners = 4;
//    float elitismRate = 0.25f;
//
//    public static class IntegerParameter {
//
//        private double min, max;
//        private final String name;
//        private boolean integer;
//
//        public IntegerParameter(String name, double min, double max) {
//
//            this.name = name;
//            this.min = min;
//            this.max = max;
//            integer = true;
//        }
//
//        public IntegerParameter(String name, double min, double max, boolean i) {
//            this(name, min, max);
//            this.integer = i;
//        }
//
//        public double getMax() {
//            return max;
//        }
//
//        public double getMin() {
//            return min;
//        }
//
//        public double getRandom() {
//            return acceptable(r(min, max));
//        }
//
//        public boolean isInteger() {
//            return integer;
//        }
//
//        public double acceptable(double i) {
//            if (i < min) {
//                i = min;
//            }
//            if (i > max) {
//                i = max;
//            }
//            if (integer) {
//                return Math.round(i);
//            }
//            return i;
//        }
//
//        @Override
//        public String toString() {
//            return name + "[" + min + ".." + max + "]";
//        }
//
//    }
//
//    static final List<IntegerParameter> param = new ArrayList();
//    static final Map<String, Integer> paramIndex = new HashMap();
//
//    static {
//
//        param.add(new IntegerParameter("builderType", 1, 1)); //result will be % number types
//        param.add(new IntegerParameter("conceptMax", 200, 1100));
//        param.add(new IntegerParameter("subConceptMax", 0, 200));
//        param.add(new IntegerParameter("conceptTaskLinks", 5, 50));
//        param.add(new IntegerParameter("conceptTermLinks", 20, 150));
//
//        param.add(new IntegerParameter("conceptForgetDurations", 0.5, 5.0, false));
//        param.add(new IntegerParameter("termLinkForgetDurations", 2, 8, false));
//        param.add(new IntegerParameter("taskLinkForgetDurations", 5, 15, false));
//
//        param.add(new IntegerParameter("conceptsFiredPerCycle", 3, 8));
//        param.add(new IntegerParameter("cyclesPerDuration", 3, 7));
//
//        param.add(new IntegerParameter("conceptBeliefs", 3, 20));
//        param.add(new IntegerParameter("conceptGoals", 3, 14));
//        param.add(new IntegerParameter("conceptQuestions", 3, 10));
//
//        param.add(new IntegerParameter("contrapositionPriority", 20, 40));
//
//        param.add(new IntegerParameter("bagLevels", 100, 100));
//
//        //param.add(new IntegerParameter("prologEnable", 0, 1));
//        int j = 0;
//        for (IntegerParameter i : param) {
//            paramIndex.put(i.name, j++);
//        }
//    }
//
//    public static class NARGenome extends AbstractListChromosome<Object>  {
//
//        /** copies existing */
//        public NARGenome(List<Object> representation) {
//            super(representation);
//        }
//
//        public static NARGenome random() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        protected void checkValidity(List<Object> list) throws InvalidRepresentationException {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public AbstractListChromosome<Object> newFixedLengthChromosome(List<Object> list) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public double fitness() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public NAR build() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//    }
//
//    public static class CalculateNALTestScore implements CalculateScore {
//
//        private final int maxCycles;
//
//        final float parallelizationPenalizationRate = 0f; //scale of score divisor for parallel
//
//        public CalculateNALTestScore(int maxCycles) {
//            this.maxCycles = maxCycles;
//        }
//
//        public static synchronized double score(int maxCycles, NAR n) {
//            Global.DEBUG = false;
//            //return NALTestScore.score(n, maxCycles);
//            return 0;
//        }
//
//        @Override
//        public double calculateScore(MLMethod phenotype) {
//
//            try {
//                NARGenome genome = (NARGenome) phenotype;
//
//                System.out.print("    " + genome.toString());
//
//                final NAR n = genome.build();
//
//                double s = score(maxCycles, n);
//
//                //divide score based on degree of parallelism
//                //s /= (1.0 + ((genome.i("conceptsFiredPerCycle") - 1.0) * parallelizationPenalizationRate));
//
//                System.out.println(" score: " + s);
//
//                return s;
//            } catch (Throwable e) {
//                if (Global.DEBUG) {
//                    e.printStackTrace();
//                }
//                return 0;
//            }
//
//        }
//
//        @Override
//        public boolean shouldMinimize() {
//            return true;
//        }
//
//        @Override
//        public boolean requireSingleThreaded() {
//            return true;
//        }
//
//    }
//
//
//    public GeneticSearchApache() throws Exception {
//
//        File file = new File("/home/me/Downloads/default_nar_param_genetic." + new Date().toString() + ".txt");
//
//        FileOutputStream fos = new FileOutputStream(file);
//
//        System.setOut(new PrintStream(new MultiOutputStream(System.out, fos)));
//
//        System.out.println(param);
//
//        System.out.println("Default Score: " + CalculateNALTestScore.score(maxCycles, new NAR(new Default())));
//
//        // initialize a new genetic algorithm
//        GeneticAlgorithm ga = new GeneticAlgorithm(
//            new OnePointCrossover<Integer>(),
//            1,
//            new RandomKeyMutation(),
//            0.10,
//            new TournamentSelection(winners)
//        );
//
//        // initial population
//        Population initial = new ElitisticListPopulation(populationSize, elitismRate);
//        initial.addChromosome(NARGenome.random());
//
//        // stopping musts
//        StoppingCondition stopCond = new FixedGenerationCount(generationsPerPopulation);
//
//        // test the algorithm
//        Population finalPopulation = ga.evolve(initial, stopCond);
//
//        // best chromosome from the final population
//        Chromosome bestFinal = finalPopulation.getFittestChromosome();
//        System.out.println(bestFinal);
//
////        while (true) {
////
////            MLMethodGeneticAlgorithm ml = new MLMethodGeneticAlgorithm(new MethodFactory() {
////
////                @Override
////                public MLMethod factor() {
////                    return NARGenome.newRandom();
////                }
////
////            }, new CalculateNALTestScore(maxCycles), populationSize);
////
////            ml.setThreadCount(1);
////
////            genetic = ml.getGenetic();
////
////
////            genetic.addOperation(0.2, new MutateShuffle());
////
////            genetic.addOperation(0.9, new MutatePerturb(0.05f));
////            genetic.addOperation(0.5, new MutatePerturb(0.2f));
////            genetic.addOperation(0.3, new MutatePerturb(0.3f));
////
////            for (int i = 0; i < generationsPerPopulation; i++) {
////                ml.iteration();
////
////                MLMethodGenome g = (MLMethodGenome) genetic.getBestGenome();
////
////                System.out.println("  BEST: " + g.getScore() + " " + Arrays.toString(g.getData()));
////            }
////        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        new GeneticSearchApache();
//    }
//
//    public static double r(double min, double max) {
//        return (Math.random() * (1 + max - min) + min);
//    }
//
//    public static double minmax(double x, double min, double max) {
//        return Math.min(Math.max(x, min), max);
//    }
//
// }

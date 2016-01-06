///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.evolve;
//
//import nars.Global;
//import nars.NAR;
//import nars.NARSeed;
//import nars.nar.Default;
//import nars.nar.Neuromorphic;
//import nars.nar.Solid;
//import nars.util.data.MultiOutputStream;
//import org.encog.ml.CalculateScore;
//import org.encog.ml.MLEncodable;
//import org.encog.ml.MLMethod;
//import org.encog.ml.MethodFactory;
//import org.encog.ml.ea.train.basic.TrainEA;
//import org.encog.ml.genetic.MLMethodGeneticAlgorithm;
//import org.encog.ml.genetic.MLMethodGenome;
//import org.encog.ml.genetic.mutate.MutatePerturb;
//import org.encog.ml.genetic.mutate.MutateShuffle;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.PrintStream;
//import java.util.*;
//
///**
// *
// * @see
// * https://github.com/encog/encog-java-examples/blob/master/src/main/java/org/encog/examples/ml/tsp/genetic/SolveTSP.java
// * @author me
// */
//public class GeneticSearchEncog {
//    //623.0 [1, 1585, 302, 46, 33, 4, 20, 10, 4, 11]
//    //607.0 [1, 656, 528, 74, 124, 5, 3, 7, 3, 13]
//    //577.0 [1, 259.0, 156.0, 2.0, 101.0, 4.0, 16.0, 2.0, 3.0, 1.0]
//
//    //0: Default Score: 2023.0
//    //1473.0 [0.0, 650.0, 415.0, 5.0, 57.0, 4.806817046483262, 15.098489110914961, 9.683106815451737, 1.0, 6.0]
//    //1367.0 [0.0, 760.0, 16.0, 77.0, 126.0, 1.9526462004203577, 1.1296896689902738, 1.5503616143067935, 1.0, 16.0]
//    int maxCycles = 200;
//    int populationSize = 8;
//    int generationsPerPopulation = 16;
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
//        param.add(new IntegerParameter("builderType", 0, 0)); //result will be % number types
//        param.add(new IntegerParameter("conceptMax", 200, 1100));
//        param.add(new IntegerParameter("subConceptMax", 0, 200));
//        param.add(new IntegerParameter("conceptTaskLinks", 4, 50));
//        param.add(new IntegerParameter("conceptTermLinks", 10, 150));
//
//        param.add(new IntegerParameter("conceptForgetDurations", 0.5, 5.0, false));
//        param.add(new IntegerParameter("termLinkForgetDurations", 2, 8, false));
//        param.add(new IntegerParameter("taskLinkForgetDurations", 5, 15, false));
//
//        param.add(new IntegerParameter("conceptsFiredPerCycle", 1, 1));
//        param.add(new IntegerParameter("cyclesPerDuration", 5, 5));
//
//        param.add(new IntegerParameter("conceptBeliefs", 3, 20));
//        param.add(new IntegerParameter("conceptGoals", 3, 14));
//        param.add(new IntegerParameter("conceptQuestions", 3, 10));
//
//        param.add(new IntegerParameter("contrapositionPriority", 5, 60));
//
//        param.add(new IntegerParameter("bagLevels", 60, 100));
//
//        //param.add(new IntegerParameter("prologEnable", 0, 1));
//        int j = 0;
//        for (IntegerParameter i : param) {
//            paramIndex.put(i.name, j++);
//        }
//    }
//
//    public static class NARGenome extends MLMethodGenome implements MLEncodable {
//
//        public NARGenome() {
//            this(new MLEncodable() {
//
//                @Override
//                public int encodedArrayLength() {
//                    return param.size();
//                }
//
//                @Override
//                public void encodeToArray(double[] doubles) {
//                    //nothing
//                }
//
//                @Override
//                public void decodeFromArray(double[] doubles) {
//                    //nothing
//                }
//
//            });
//
//        }
//
//
//
//        public NARGenome(MLEncodable phenotype) {
//            super(phenotype);
//
//
//            //normalize to acceptable values
//            for (int i = 0; i < param.size(); i++) {
//                getData()[i] = param.get(i).acceptable(getData()[i]);
//            }
//        }
//
//        public static NARGenome newRandom() {
//            NARGenome g = new NARGenome();
//            double[] d = g.getData();
//
//            for (int i = 0; i < param.size(); i++) {
//                IntegerParameter p = param.get(i);
//                d[i] = r(p.getMin(), p.getMax());
//            }
//
//            return g;
//        }
//
//        public int i(String name) {
//            int idx = paramIndex.get(name);
//            return (int) (getData()[idx] = param.get(idx).acceptable(getData()[idx]));
//        }
//
//        public int i(String name, int defaultValue) {
//            Integer idx = paramIndex.get(name);
//            if (idx == null) {
//                return defaultValue;
//            }
//            return (int) (getData()[idx] = param.get(idx).acceptable(getData()[idx]));
//        }
//
//        public double d(String name) {
//            int idx = paramIndex.get(name);
//            return getData()[idx] = param.get(idx).acceptable(getData()[idx]);
//        }
//
//        public void set(String name, double value) {
//            int idx = paramIndex.get(name);
//            getData()[idx] = param.get(idx).acceptable(value);
//        }
//
//        public void random(int index) {
//            IntegerParameter p = param.get(index);
//            getData()[index] = p.getRandom();
//        }
//
//        public NAR newNAR() {
//
//            int builderType = i("builderType") % 3;
//            int numConcepts = i("conceptMax");
//            int numTaskLinks = i("conceptTaskLinks");
//            int numTermLinks = i("conceptTermLinks");
//            float conceptForget = (float) d("conceptForgetDurations");
//            float beliefForget = (float) d("termLinkForgetDurations");
//            float taskForget = (float) d("taskLinkForgetDurations");
//            int conceptsFired = i("conceptsFiredPerCycle");
//            int duration = i("cyclesPerDuration");
//            int bagLevels = i("bagLevels");
//
//            //int prolog = get("prologEnable");
//            Default b;
//
//            if (builderType == 0) {
//                b = new Default();
//            } else if (builderType == 1) {
//                b = new Neuromorphic(conceptsFired /*ants */);
//            } else if (builderType == 2) {
//                b = new Solid(1024, 4, 4);
//            } else {
//                throw new RuntimeException("Invalid Builder type " + builderType);
//            }
//
//
//            b.setActiveConcepts(numConcepts);
//            b.setTaskLinkBagSize(numTaskLinks);
//            b.setTermLinkBagSize(numTermLinks);
//            b.conceptsFiredPerCycle.set(conceptsFired);
//
//            NAR n = new NAR(b);
//
//            (n.param).duration.set(duration);
//            (n.param).conceptForgetDurations.set(conceptForget);
//            (n.param).taskLinkForgetDurations.set(taskForget);
//            (n.param).termLinkForgetDurations.set(beliefForget);
//
//            (n.param).conceptBeliefsMax.set(i("conceptBeliefs"));
//            (n.param).conceptGoalsMax.set(i("conceptGoals"));
//            (n.param).conceptQuestionsMax.set(i("conceptQuestions"));
//
//            if (builderType != 1) {
//                //analogous to # of ants but in defaultbag
//            }
//
//            /*if (prolog == 1) {
//             new NARPrologMirror(n, 0.75f, true);
//             }*/
//            return n;
//        }
//
//        @Override
//        public String toString() {
//            return "NARGenome[" + Arrays.toString(getData()) + "]";
//        }
//
//        @Override
//        public int encodedArrayLength() {
//            return getData().length;
//        }
//
//        @Override
//        public void encodeToArray(double[] doubles) {
//            System.arraycopy(getData(), 0, doubles, 0, encodedArrayLength());
//        }
//
//        @Override
//        public void decodeFromArray(double[] doubles) {
//            System.arraycopy(doubles, 0, getData(), 0, doubles.length);
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
//                final NAR n = genome.newNAR();
//
//                double s = score(maxCycles, n);
//
//                //divide score based on degree of parallelism
//                s /= (1.0 + ((genome.i("conceptsFiredPerCycle") - 1.0) * parallelizationPenalizationRate));
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
//    private TrainEA genetic;
//
//    public GeneticSearchEncog() throws Exception {
//
//        Global.DEBUG = false;
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
//        while (true) {
//
//            MLMethodGeneticAlgorithm ml = new MLMethodGeneticAlgorithm(new MethodFactory() {
//
//                @Override
//                public MLMethod factor() {
//                    return NARGenome.newRandom();
//                }
//
//            }, new CalculateNALTestScore(maxCycles), populationSize);
//
//            ml.setThreadCount(1);
//
//            genetic = ml.getGenetic();
//
//
//            genetic.addOperation(0.2, new MutateShuffle());
//
//            genetic.addOperation(0.9, new MutatePerturb(0.05f));
//            genetic.addOperation(0.5, new MutatePerturb(0.2f));
//            genetic.addOperation(0.3, new MutatePerturb(0.3f));
//
//            for (int i = 0; i < generationsPerPopulation; i++) {
//                ml.iteration();
//
//                MLMethodGenome g = (MLMethodGenome) genetic.getBestGenome();
//
//                System.out.println("  BEST: " + g.getScore() + " " + Arrays.toString(g.getData()));
//            }
//        }
//    }
//
//    public static void main(String[] args) throws Exception {
//        new GeneticSearchEncog();
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

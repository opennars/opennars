//package nars.evolve;
//
//import nars.NAR;
//import nars.io.in.LibraryInput;
//import nars.meter.condition.OutputCondition;
//import nars.nar.Default;
//import nars.util.data.random.XORShiftRandom;
//import org.encog.Encog;
//import org.encog.engine.network.activation.ActivationSigmoid;
//import org.encog.mathutil.randomize.factory.RandomFactory;
//import org.encog.ml.CalculateScore;
//import org.encog.ml.data.MLData;
//import org.encog.ml.data.MLDataPair;
//import org.encog.ml.data.basic.BasicMLData;
//import org.encog.ml.data.basic.BasicMLDataPair;
//import org.encog.ml.data.basic.BasicMLDataSet;
//import org.encog.ml.ea.species.Species;
//import org.encog.ml.ea.train.EvolutionaryAlgorithm;
//import org.encog.neural.neat.NEATNetwork;
//import org.encog.neural.neat.NEATPopulation;
//import org.encog.neural.neat.NEATUtil;
//import org.encog.neural.networks.BasicNetwork;
//import org.encog.neural.networks.layers.BasicLayer;
//import org.encog.neural.networks.training.TrainingSetScore;
//import org.encog.neural.networks.training.propagation.manhattan.ManhattanPropagation;
//import org.encog.util.arrayutil.NormalizeArray;
//import org.encog.util.simple.EncogUtility;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.text.DecimalFormat;
//import java.util.*;
//
///**
// * Measures how well a NAR completes unit tests under various parameters
// */
//public class NALTestPerformance {
//
//    static int exampleNum = 0;
//    static int absTime = 0;
//
//    public static void reset() { exampleNum = absTime = 0; }
//
//    static class Telemetry implements Runnable {
//
//        public final String example;
//
//        public Integer successAt = null;      //cycle it succeeded the test
//        public Integer failAt = null;         //cycle a fatal error occurred
//        public double success = 0;                         //how many tests succeeded
//
//        public final List<String> fields = new ArrayList();
//        public final LinkedList<double[]> logicState = new LinkedList();
//        private final NAR nar;
//        private final int maxCycles;
//        private int additionalCycles;
//
//        public Telemetry(NAR nar, String examplePath, int additionalCycles, int maxCycles) {
//            this.nar = nar;
//            this.example = examplePath;
//            this.maxCycles = maxCycles;
//
//            this.additionalCycles = additionalCycles;
//        }
//
//        public void run() {
//
//            final String input = LibraryInput.getExample(example);
//
//            nar.memory.clear(1);
//
//            final List<OutputCondition> expects = new ArrayList();
//
//            List<OutputCondition> extractedExpects = OutputCondition.getConditions(nar, input, 5);
//            for (OutputCondition e1 : extractedExpects) {
//                expects.add(e1);
//            }
//
//            nar.input(input);
//
//            boolean error = false;
//            int successes = 0;
//
//            /*CompoundMeter m = new CompoundMeter(nar.memory.logic, nar.memory.resource);
//            m.update(nar.memory);
//
//            fields.addAll(m.keySet());*/
//            fields.add("id");
//            fields.add("time");
//            fields.add("absTime");
//            fields.add("successes");
//            fields.add("error");
//
//            additionalCycles += 1; //increased by one to guarantee completion
//
//            do {
//                double successRate = successes / ((double)expects.size());
//                /*m.update(nar.memory);
//                logicState.add(m.toArray(
//                        exampleNum,
//                        nar.memory.getCycleTime(),
//                        absTime++,
//                        successRate,
//                        error ? 1 : 0
//                ));*/
//
//                if (error)
//                    break;
//                if (successes == expects.size()) {
//                    if (additionalCycles == 0)
//                        break;
//                    additionalCycles--;
//                }
//
//                try {
//                    nar.frame(1);
//                } catch (Exception e) {
//                    error = true;
//                }
//
//                successes = 0;
//                for (OutputCondition e : expects) {
//                    if (e.isTrue()) {
//                        successes++;
//                    }
//                }
//
//            } while (nar.time() < maxCycles);
//
//            exampleNum++;
//
//            if (error) {
//                failAt = (int) nar.time();
//            }
//            if (successes == expects.size()) {
//                successAt = (int) nar.time();
//            }
//            if (!expects.isEmpty()) {
//                success = successes / expects.size();
//            }
//        }
//
//        @Override
//        public String toString() {
//            return example + " successAt=" + successAt + ", failAt=" + failAt + ", points=" + logicState.size();
//        }
//
//        public void printCSVHeader(PrintStream out) {
//            printCSVLine(out, fields);
//            out.println();
//        }
//
//        public void printCSV(PrintStream out) {
//            out.println("#" + toString());
//            for (double[] l : logicState) {
//                printCSVLine(out, l);
//            }
//            out.println();
//        }
//
//        public double[] getIndex(int index) {
//            double[] x = new double[logicState.size()];
//            int i = 0;
//            for (double[] l : logicState) {
//                x[i++] = l[i];
//            }
//            return x;
//        }
//
//        public void getDataPairs(int[] in, int[] out, List<MLDataPair> data, int historySize) {
//            int ins = in.length * (1 + historySize);
//            int outs = out.length;
//
//            int c = 0;
//            for (int il = (1+historySize); il < logicState.size(); il++) {
//                double[] next = logicState.get(il);
//                double[] current = logicState.get(il-1);
//
//                double[] i = new double[ins];
//                double[] o = new double[outs];
//                int ia = 0, oa = 0;
//                for (int x : in) {
//                    i[ia++] = current[x];
//                }
//
//                double currentTest = current[current.length-5];
//
//                for (int h = 0; h < historySize; h++) {
//                    double[] prev = logicState.get(il-2-h);
//
//                    for (int x : in) {
//                        i[ia++] = prev[x];
//                    }
//                }
//
//                for (int x : out) {
//                    o[oa++] = next[x];
//                }
//
//                data.add(new BasicMLDataPair(new BasicMLData(i), new BasicMLData(o)));
//            }
//        }
//
//    }
//    protected final static DecimalFormat df = new DecimalFormat("#.###");
//
//    public static void printCSVLine(PrintStream out, List<String> o) {
//        StringJoiner line = new StringJoiner(",", "", "");
//        int n = 0;
//        for (String x : o) {
//            line.add(x + "_" + (n++));
//        }
//        out.println(line.toString());
//    }
//
//    public static void printCSVLine(PrintStream out, double[] o) {
//        StringJoiner line = new StringJoiner(",", "", "");
//        for (double x : o) {
//            line.add(df.format(x));
//        }
//        out.println(line.toString());
//    }
//
//    /*
//     @Override
//     public Performance printMeaning() {
//     super.printMeaning();
//     System.out.printMeaning(", " + df.format(getCycleTimeMS() / totalCycles * 1000.0) + " ns/cycle, " + (((float)totalCycles)/(warmups+repeats)) + " cycles/test");
//     return this;
//
//     }
//     @Override
//     public Performance printCSV(boolean finalComma) {
//     super.printCSV(true);
//     System.out.printMeaning(df.format(getCycleTimeMS() / totalCycles * 1000.0) + ", " + (((float)totalCycles)/(warmups+repeats)));
//     if (finalComma)
//     System.out.printMeaning(", ");
//     return this;
//
//     }
//
//     */
//
//    public static class NALControlMLDataSet extends BasicMLDataSet {
//
//
//       public final Map<Integer, NormalizeArray> normalizations = new HashMap();
//       public final List<String> fields;
//        final List<String> allFields;
//
//       public NALControlMLDataSet(List<String> allFields, int[] ins, int[] outs, List<MLDataPair> r) {
//            super(r);
//
//            int n = 0;
//            this.allFields = new ArrayList();
//            for (String s : allFields)
//                this.allFields.add(s + (n++));
//
//            fields = new ArrayList();
//            for (int i : ins) {
//                fields.add("IN_" + allFields.get(i));
//            }
//            for (int i : outs) {
//                fields.add("OUT_" + allFields.get(i));
//            }
//       }
//
//       public void normalize() {
//           for (int i = 0; i < getInputSize(); i++) {
//               double[] x = getIndex(true, i);
//               NormalizeArray na = new NormalizeArray();
//               na.setNormalizedLow(0);
//               x = na.process( x  );
//               if (na.getStats().getActualLow() == na.getStats().getActualHigh())
//                   Arrays.fill(x, 0);
//               normalizations.put(i, na );
//               setIndex(true, i, x);
//           }
//           for (int i = 0; i < getIdealSize(); i++) {
//               double[] x = getIndex(false, i);
//               NormalizeArray na = new NormalizeArray();
//               na.setNormalizedLow(0);
//               x = na.process( x  );
//               if (na.getStats().getActualLow() == na.getStats().getActualHigh())
//                   Arrays.fill(x, 0);
//               normalizations.put(-i, na );
//               setIndex(false, i, x);
//           }
//       }
//
//        public double[] getIndex(boolean input, int index) {
//            double[] x = new double[(int)getRecordCount()];
//            int i = 0;
//            for (MLDataPair p : this) {
//                if (input)
//                    x[i++] = p.getInput().getData(index);
//                else
//                    x[i++] = p.getIdeal().getData(index);
//            }
//            return x;
//        }
//
//        public void setIndex(boolean input, int index, double[] x) {
//            int i = 0;
//            for (MLDataPair p : this) {
//                if (input)
//                    p.getInput().setData(index, x[i++]);
//                else
//                    p.getIdeal().setData(index, x[i++]);
//            }
//        }
//
//        public static void printCSVLine(PrintStream out, List<String> o) {
//            StringJoiner line = new StringJoiner(",", "", "");
//            int n = 0;
//            line.add("absCycle");
//            for (String x : o) {
//                line.add(x + "_" + (n++));
//            }
//            out.println(line.toString());
//        }
//
//        public static void printCSVLine(PrintStream out, int n, double[] a, double[] b) {
//            StringJoiner line = new StringJoiner(",", "", "");
//            line.add(Integer.toString(n));
//            for (double x : a)
//                line.add(df.format(x));
//            for (double x : b)
//                line.add(df.format(x));
//            out.println(line.toString());
//        }
//
//        public void toCSV(String filename) throws IOException {
//            PrintStream ps = new PrintStream(new File(filename));
//            printCSVLine(ps, fields);
//            int n = 0;
//            for (MLDataPair r : this)
//                printCSVLine(ps, n++, r.getInputArray(), r.getIdealArray());
//
//        }
//    }
//    public static List<String> getExamplePaths() {
//        return null;
////        Collection c = NALTest.params();
////        List<String> l = new ArrayList();
////        for (Object o : c) {
////            String e = ((Object[])o)[0].toString();
////            e = e.substring(e.indexOf("nal/")+4, e.length());
////            l.add(e);
////        }
////        return l;
//    }
//
//    public static NALControlMLDataSet test(NAR n, int tests, int maxCycles, int extraCycles, int[] ins, int[] outs, int historySize) throws Exception {
//
//
//        Collection c = null; //NALTest.params();
//
//
//
//        List<MLDataPair> results = new ArrayList(maxCycles * tests);
//
//
//        int testNum = 0;
//        Telemetry t = null;
//        for (Object o : c) {
//            String examplePath = (String) ((Object[]) o)[0];
//
//            n.reset();
//            t = new Telemetry(n, examplePath, extraCycles, maxCycles);
//            t.run();
//
//
//            /*if (testNum++ == 0)
//                t.printCSVHeader(ps);*/
//            /*t.printCSV(ps);
//                    */
//
//            //all fields
//            if (ins == null) {
//                ins = new int[ t.fields.size() ];
//                for (int i = 0 ;i < ins.length; i++)
//                    ins[i] = i;
//                outs = new int[0];
//            }
//
//            t.getDataPairs(ins, outs, results, historySize);
//
//
//            if (testNum++ == tests)
//                break;
//
//        }
//
//        NALControlMLDataSet nc = new NALControlMLDataSet(t.fields, ins, outs, results);
//        return nc;
//    }
//
//    public void testNEAT() throws Exception {
//        int populationSize = 2000;
//        int tests = 128;
//        double initialConnectionDensity = 0.75;
//        int additionalCycles = 30;
//        int maxIterations = 300;
//        int historySize = 6;
//
///*[emotion.happy0, task.derived1, task.executed.priority.mean2, rule.contrapositions.complexity.mean3, cycle.cpu_time.mean4, emotion.busy5, cycle.frequency.hz6, concepts.count7, goal.process8, rule.contrapositions9, judgment.process10, task.add_new.priority.mean11, io.to_memory.ratio12, rule.fire.tasklink.priority.mean13, concepts.beliefs.sum14, task.add_new15, rule.tasktermlinks16, concepts.priority.mean17, concept.new18, concept.new.complexity.mean19, concepts.questions.sum20, task.derived.priority.mean21, question.process22, cycle.frequency_potential.mean.hz23, task.link_to24, task.executed25, rule.fire.tasklinks.delta26, rule.tasktermlink.priority.mean27, cycle.ram_use.delta_Kb.sampled28, memory.noveltasks.total29, id30, time31, absTime32, successes33, error34]
//*/
//        //int[] ins = new int[] { 1, 8, 9, 22, 10, 15 };
//        int[] ins = new int[] { 1, 17, 11, 13 };
//        int[] outs = new int[] { 5 };
//        //int[] ins = null, outs = null;
//
//        NALControlMLDataSet trainingSet = test(new NAR(new Default()), tests, 2000, additionalCycles, ins, outs, historySize);
//        trainingSet.normalize();
//
//        System.out.println(trainingSet.allFields);
//        System.out.println(trainingSet.fields);
//        if (ins!=null) {
//            System.out.println("Training samples: " + trainingSet.getRecordCount());
//            for (int i : ins)
//                System.out.println(" in: "  + trainingSet.allFields.get(i));
//            for (int i : outs)
//                System.out.println("out: "  + trainingSet.allFields.get(i));
//        }
//        /*for (int i = 0; i < trainingSet.size(); i++)
//            System.out.println(trainingSet.get(i));*/
//        trainingSet.toCSV("/tmp/nal.csv");
//        if (ins == null)
//            return;
//
//
//        NEATPopulation pop = new NEATPopulation(trainingSet.getInputSize(), trainingSet.getIdealSize(), populationSize);
//        pop.setActivationCycles(4);
//        pop.setSurvivalRate(0.2);
//        pop.setInitialConnectionDensity(initialConnectionDensity);// not required, but speeds training
//        pop.setRandomNumberFactory(new RandomFactory() {
//            //for speed
//
//            final Random x = new XORShiftRandom();
//
//            @Override public Random factor() {
//                return x;
//            }
//            @Override public RandomFactory factorFactory() {
//                return this;
//            }
//        });
//        pop.reset();
//
//
//        CalculateScore score = new TrainingSetScore(trainingSet);
//
//        // train the neural network
//
//        final EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(pop,score);
//
//        do {
//                train.iteration();
//
//                double averageLinks = 0, averageNeurons = 0;
//                int count = pop.getSpecies().size();
//                for (Species s  : pop.getSpecies()) {
//                    NEATNetwork n = (NEATNetwork)train.getCODEC().decode(s.getLeader());
//                    averageLinks += n.getLinks().length;
//                    averageNeurons += n.getActivationFunctions().length;
//
//                }
//                averageLinks /= count;
//                averageNeurons /= count;
//
//                System.out.println("Epoch #" + train.getIteration() + " Error:"
//                        + (train.getError()) + ", Species:" + count + ", avgLinks=" + averageLinks + ", avgNeurons=" + averageNeurons);
//        } while ((train.getError() > 0.00001) && (train.getIteration() < maxIterations));
//
//        NEATNetwork network = (NEATNetwork)train.getCODEC().decode(train.getBestGenome());
//
//        // test the neural network
//
//        //System.out.println("Neural Network Results:");
//        EncogUtility.evaluate(network, trainingSet);
//
//        System.out.println("NEATLinks: " + network.getLinks().length);
//
//        Encog.getInstance().shutdown();
//
//    }
//
//    /** Multilayer perceptron with backprop */
//    public static void testMLP() throws Exception {
//        int tests = 64;
//        int additionalCycles = 20;
//        int maxIterations = 5250;
//        int historySize = 6;
//
///*[emotion.happy0, task.derived1, task.executed.priority.mean2, rule.contrapositions.complexity.mean3, cycle.cpu_time.mean4, emotion.busy5, cycle.frequency.hz6, concepts.count7, goal.process8, rule.contrapositions9, judgment.process10, task.add_new.priority.mean11, io.to_memory.ratio12, rule.fire.tasklink.priority.mean13, concepts.beliefs.sum14, task.add_new15, rule.tasktermlinks16, concepts.priority.mean17, concept.new18, rule.fire.tasklinks19, concept.new.complexity.mean20, concepts.questions.sum21, task.derived.priority.mean22, question.process23, cycle.frequency_potential.mean.hz24, task.link_to25, task.executed26, rule.tasktermlink.priority.mean27, cycle.ram_use.delta_Kb.sampled28, memory.noveltasks.total29, id30, time31, absTime32, successes33, error34]
//*/
//        //int[] ins = new int[] { 1, 8, 9, 22, 10, 15 };
//        int[] ins = new int[] { 23, 10, 19 };
//        int[] outs = new int[] {  10 };
//        //int[] ins = null, outs = null;
//
//        NALControlMLDataSet trainingSet = test(new NAR(new Default()), tests, 2000, additionalCycles, ins, outs, historySize);
//        trainingSet.normalize();
//
//        System.out.println(trainingSet.allFields);
//        System.out.println(trainingSet.fields);
//        if (ins!=null) {
//            System.out.println("Training samples: " + trainingSet.getRecordCount());
//            for (int i : ins)
//                System.out.println(" in: "  + trainingSet.allFields.get(i));
//            for (int i : outs)
//                System.out.println("out: "  + trainingSet.allFields.get(i));
//        }
//        /*for (int i = 0; i < trainingSet.size(); i++)
//            System.out.println(trainingSet.get(i));*/
//        //trainingSet.toCSV("/tmp/nal.csv");
//        if (ins == null)
//            return;
//
//
//
//        // create a neural network, without using a factory
//        BasicNetwork network = new BasicNetwork();
//        int inputSize = trainingSet.getInputSize();
//        int outputSize = trainingSet.getIdealSize();
//        network.addLayer(new BasicLayer(null,true,inputSize));
//        network.addLayer(new BasicLayer(new ActivationSigmoid(),true, inputSize/2));
//        network.addLayer(new BasicLayer(new ActivationSigmoid(),true,outputSize));
//        network.getStructure().finalizeStructure();
//        network.reset();
//
//        System.out.println("Network Structure: " + Arrays.toString(network.getFlat().getLayerCounts()));
//
//
//
//        //final ResilientPropagation train = new ResilientPropagation(network, trainingSet);
//        //train.setBatchSize(5);
//
//        final ManhattanPropagation train = new ManhattanPropagation(network, trainingSet, 0.01);
//
//        //final Backpropagation train = new Backpropagation(network, trainingSet);
//
//        //final NeuralPSO train = new NeuralPSO(network, trainingSet);
//
//        int epoch = 1;
//
//        do {
//
//                train.iteration();
//                System.out.println("Epoch #" + epoch + " Error:" + train.getError());
//                epoch++;
//        } while ((train.getError() > 0.00001) && (epoch < maxIterations));
//        train.finishTraining();
//
//        // test the neural network
//
//        System.out.println("Neural Network Results:");
//        for(MLDataPair pair: trainingSet ) {
//                final MLData output = network.compute(pair.getInput());
//                //System.out.println(pair.getInput());
//                double myError = 0;
//                double[] i = pair.getIdealArray();
//                int j = 0;
//                for (double o : output.getData()) {
//                    myError += Math.abs(o - i[j++]);
//                }
//                System.out.println(myError + "  actual=" + Arrays.toString(output.getData()) + ",ideal=" + pair.getIdeal());
//        }
//
//
//        Encog.getInstance().shutdown();
//    }
//
//    public static void main(String[] args) throws Exception {
//        testMLP();
//    }
// }

package nars.perf;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.NARBuilder;
import nars.core.build.DefaultNARBuilder;
import nars.io.TextInput;
import nars.test.core.NALTest;
import static nars.test.core.NALTest.getExpectations;
import org.encog.Encog;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.util.arrayutil.NormalizeArray;
import org.encog.util.simple.EncogUtility;

/**
 * Measures how well a NAR completes unit tests under various parameters
 */
public class NALTestPerformance {

    static int exampleNum = 0;
    
    static class Telemetry implements Runnable {

        public final String example;

        public Integer successAt = null;      //cycle it succeeded the test
        public Integer failAt = null;         //cycle a fatal error occurred
        public double success = 0;                         //how many tests succeeded

        public final List<String> fields = new ArrayList();
        public final LinkedList<double[]> logicState = new LinkedList();
        private final NAR nar;
        private final int maxCycles;
        private int additionalCycles;

        public Telemetry(NAR nar, String examplePath, int additionalCycles, int maxCycles) {
            this.nar = nar;
            this.example = examplePath;
            this.maxCycles = maxCycles;

            this.additionalCycles = additionalCycles;
        }

        public void run() {

            final String input = NALTest.getExample(example);

            Memory.resetStatic();

            final List<NALTest.Expect> expects = new ArrayList();

            List<NALTest.Expect> extractedExpects = getExpectations(nar, input, false);
            for (NALTest.Expect e1 : extractedExpects) {
                expects.add((NALTest.Expect) nar.addOutput(e1));
            }

            nar.addInput(new TextInput(input));

            boolean error = false;
            int successes = 0;

            fields.addAll(nar.memory.logic.keySet());
            fields.add("id");
            fields.add("time");
            fields.add("successes");
            fields.add("error");
            
            do {
                double successRate = successes / ((double)expects.size());
                logicState.add(nar.memory.updateLogicState().toArray(
                        exampleNum++,
                        nar.getTime(),
                        successRate, 
                        error ? 1 : 0
                ));

                if (error)
                    break;
                if (successes == expects.size()) {
                    if (additionalCycles == 0)
                        break;
                    additionalCycles--;
                }
                
                try {
                    nar.step(1);
                } catch (Exception e) {
                    error = true;
                }
                
                successes = 0;
                for (NALTest.Expect e : expects) {
                    if (e.realized) {
                        successes++;
                    }
                }                

            } while (nar.getTime() < maxCycles);
                        

            if (error) {
                failAt = (int) nar.getTime();
            }
            if (successes == expects.size()) {
                successAt = (int) nar.getTime();
            }
            if (expects.size() != 0) {
                success = successes / expects.size();
            }
        }

        @Override
        public String toString() {
            return example + " successAt=" + successAt + ", failAt=" + failAt + ", points=" + logicState.size();
        }

        public void printCSVHeader(PrintStream out) {            
            printCSVLine(out, fields);
            out.println();
        }

        public void printCSV(PrintStream out) {
            out.println("#" + toString());
            for (double[] l : logicState) {
                printCSVLine(out, l);
            }
            out.println();
        }
        
        public double[] getIndex(int index) {
            double[] x = new double[logicState.size()];
            int i = 0;
            for (double[] l : logicState) {
                x[i++] = l[i];                
            }
            return x;            
        }
                
        public void getDataPairs(int[] in, int[] out, List<MLDataPair> data) {
            int ins = in.length;
            int outs = out.length;
            
            
            for (double[] l : logicState) {
                double[] i = new double[ins];
                double[] o = new double[outs];
                int ia = 0, oa = 0;
                for (int x : in) {
                    i[ia++] = l[x];
                }
                for (int x : out) {
                    o[oa++] = l[x];
                }
                    
                data.add(new BasicMLDataPair(new BasicMLData(i), new BasicMLData(o)));
            }
        }

    }
    protected final static DecimalFormat df = new DecimalFormat("#.###");

    public static void printCSVLine(PrintStream out, List<String> o) {
        StringJoiner line = new StringJoiner(",", "", "");
        int n = 0;
        for (String x : o) {
            line.add(x + "_" + (n++));
        }
        out.println(line.toString());
    }

    public static void printCSVLine(PrintStream out, double[] o) {
        StringJoiner line = new StringJoiner(",", "", "");       
        for (double x : o) {
            line.add(df.format(x));
        }
        out.println(line.toString());
    }

    /*
     @Override
     public Performance print() {                
     super.print();
     System.out.print(", " + df.format(getCycleTimeMS() / totalCycles * 1000.0) + " ns/cycle, " + (((float)totalCycles)/(warmups+repeats)) + " cycles/run");
     return this;
                
     }
     @Override
     public Performance printCSV(boolean finalComma) {
     super.printCSV(true);
     System.out.print(df.format(getCycleTimeMS() / totalCycles * 1000.0) + ", " + (((float)totalCycles)/(warmups+repeats)));
     if (finalComma)
     System.out.print(", ");
     return this;
                
     }
        
     */
    
    public static class NALControlMLDataSet extends BasicMLDataSet {
        
       
       public final Map<Integer, NormalizeArray> normalizations = new HashMap();
       public final List<String> allFields;
       public final List<String> fields;
        
       public NALControlMLDataSet(List<String> allFields, int[] ins, int[] outs, List<MLDataPair> r) {
            super(r);
            
            this.allFields = allFields;
            
            fields = new ArrayList();
            for (int i : ins) {
                fields.add("IN_" + allFields.get(i));
            }
            for (int i : outs) {
                fields.add("OUT_" + allFields.get(i));
            }
       }
 
       public void normalize() {
           for (int i = 0; i < getInputSize(); i++) {
               double[] x = getIndex(true, i);
               NormalizeArray na = new NormalizeArray();
               na.setNormalizedLow(0);
               x = na.process( x  );
               normalizations.put(i, na );
               setIndex(true, i, x);
           }
           for (int i = 0; i < getIdealSize(); i++) {
               double[] x = getIndex(false, i);
               NormalizeArray na = new NormalizeArray();
               na.setNormalizedLow(0);
               x = na.process( x  );
               normalizations.put(-i, na );
               setIndex(false, i, x);
           }
       }
        
        public double[] getIndex(boolean input, int index) {
            double[] x = new double[(int)getRecordCount()];
            int i = 0;
            for (MLDataPair p : this) {
                if (input)
                    x[i++] = p.getInput().getData(index);
                else
                    x[i++] = p.getIdeal().getData(index);
            }
            return x;            
        }

        public void setIndex(boolean input, int index, double[] x) {
            int i = 0;
            for (MLDataPair p : this) {
                if (input)
                    p.getInput().setData(index, x[i++]);
                else
                    p.getIdeal().setData(index, x[i++]);
            }
        }

        public static void printCSVLine(PrintStream out, List<String> o) {
            StringJoiner line = new StringJoiner(",", "", "");
            int n = 0;
            line.add("cycle");            
            for (String x : o) {
                line.add(x + "_" + (n++));
            }
            out.println(line.toString());
        }

        public static void printCSVLine(PrintStream out, int n, double[] a, double[] b) {
            StringJoiner line = new StringJoiner(",", "", "");
            line.add(Integer.toString(n));
            for (double x : a)
                line.add(df.format(x));
            for (double x : b)
                line.add(df.format(x));
            out.println(line.toString());
        }
        
        public void toCSV(String filename) throws IOException {
            PrintStream ps = new PrintStream(new File(filename));
            printCSVLine(ps, fields);
            int n = 0;
            for (MLDataPair r : this)
                printCSVLine(ps, n++, r.getInputArray(), r.getIdealArray());
            
        }
    }
            
    public static NALControlMLDataSet test(NARBuilder n, int tests, int extraCycles, int[] ins, int[] outs) throws Exception {

        Collection c = NALTest.params();
     
        

        List<MLDataPair> results = new ArrayList(10000);
        
        
        int testNum = 0;
        Telemetry t = null;
        for (Object o : c) {
            String examplePath = (String) ((Object[]) o)[0];
            t = new Telemetry(n.build(), examplePath, extraCycles, 2000);
            t.run();
          
            /*if (testNum++ == 0)
                t.printCSVHeader(ps);*/
            /*t.printCSV(ps);
                    */
            
            t.getDataPairs(
                    ins,
                    outs, results);
            
            if (testNum++ == tests)
                break;
        }
                        
        NALControlMLDataSet nc = new NALControlMLDataSet(t.fields, ins, outs, results);
        return nc;
    }

    public static void main(String[] args) throws Exception {
        int populationSize = 1000;
        int tests = 3;

        int[] ins = new int[] { 0, 2, 13, 15, 16, 18 };
        int[] outs = new int[] { 3, 10, 14 };
        
        NALControlMLDataSet trainingSet = test(new DefaultNARBuilder(), tests, 150, ins, outs);
        trainingSet.normalize();

        System.out.println(trainingSet.fields);
        System.out.println("Training samples: " + trainingSet.getRecordCount());
        for (int i : ins) 
            System.out.println(" in: "  + trainingSet.allFields.get(i));
        for (int i : outs) 
            System.out.println("out: "  + trainingSet.allFields.get(i));
        for (int i = 0; i < trainingSet.size(); i++)
            System.out.println(trainingSet.get(i));
        trainingSet.toCSV("/tmp/nal.csv");

        
        
        NEATPopulation pop = new NEATPopulation(trainingSet.getInputSize(), trainingSet.getIdealSize(), populationSize);
        pop.setInitialConnectionDensity(1.0);// not required, but speeds training
        pop.reset();
        

        CalculateScore score = new TrainingSetScore(trainingSet);
        // train the neural network

        final EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(pop,score);

        do {
                train.iteration();
                System.out.println("Epoch #" + train.getIteration() + " Error:" + train.getError()+ ", Species:" + pop.getSpecies().size());
        } while(train.getError() > 0.02);

        NEATNetwork network = (NEATNetwork)train.getCODEC().decode(train.getBestGenome());

        // test the neural network
        System.out.println("Neural Network Results:");
        EncogUtility.evaluate(network, trainingSet);

        Encog.getInstance().shutdown();
                
    }

}

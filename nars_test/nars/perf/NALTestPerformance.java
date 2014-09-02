package nars.perf;

import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.NARBuilder;
import nars.core.build.DefaultNARBuilder;
import nars.io.TextInput;
import nars.test.core.NALTest;
import static nars.test.core.NALTest.getExpectations;

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

        public Telemetry(NAR nar, String examplePath, int maxCycles) {
            this.nar = nar;
            this.example = examplePath;
            this.maxCycles = maxCycles;

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
                if (successes == expects.size())
                    break;
                
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

    }
    protected final static DecimalFormat df = new DecimalFormat("#.###");

    public static void printCSVLine(PrintStream out, List<String> o) {
        StringJoiner line = new StringJoiner(",", "", "");
        for (String x : o) {
            line.add(x);
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
    public static void test(NARBuilder n) throws Exception {

        Collection c = NALTest.params();
        int i = 0;
        
        PrintStream ps = new PrintStream(new File("/tmp/nal.csv"));

        for (Object o : c) {
            String examplePath = (String) ((Object[]) o)[0];
            Telemetry t = new Telemetry(n.build(), examplePath, 2000);
            t.run();
                        
            if (i++ == 0)
                t.printCSVHeader(ps);
            t.printCSV(ps);
        }
    }

    public static void main(String[] args) throws Exception {
        test(new DefaultNARBuilder());

    }

}

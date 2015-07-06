package nars.nal;

import junit.framework.TestCase;
import nars.Global;
import nars.NAR;
import nars.NARSeed;
import nars.io.in.LibraryInput;
import nars.nar.Default;
import org.junit.Ignore;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;


/**
 * the original script test system (being replaced)
 * DEPRECATED Use TestNAR instead
 */
@Ignore
@RunWith(Parameterized.class)
@Deprecated public class NALTest extends TestCase {

    static {
        Global.DEBUG = true;
        Global.THREADS = 1;
    }

    static final NARSeed[] builds = new NARSeed[] {
            new Default()
            //new Neuromorphic(4).setMaxInputsPerCycle(1)
    };

    private final NARSeed build;

    int minCycles = 1250; //TODO reduce this to one or zero to avoid wasting any extra time during tests
    static public long randomSeed = 1;
    static public boolean showInput = false;
    static public boolean showOutput = false;
    static public boolean saveSimilar = true;
    static public boolean showSuccess = false;
    static public boolean showFail = true;
    static public boolean showTrace = false;
    static public boolean showReport = true;
    static public boolean requireSuccess = true;
    private static boolean waitForEnterKeyOnStart = false; //useful for running profiler or some other instrumentation
      

    public static Map<String, Boolean> tests = new HashMap();
    public static Map<String, Double> scores = new HashMap();




    @Parameterized.Parameters(name="{1} {0}")
    public static List params() {
        Map<String, String> et = LibraryInput.getUnitTests();
        Collection<String> t = et.values();
        for (String x : et.keySet()) addTest(x);

        List<Object[]> params = new ArrayList(t.size() * builds.length);
        for (String script : t) {
            for (NARSeed b : builds) {
                params.add(new Object[] { b, script });
            }
        }
        return params;
    }


    
    public NAR newNAR() {
        if (build == null) throw new RuntimeException("Unknown prototype");
        return new NAR(build);

        //return new NAR(new Default());
        //return new NAR(new Default());
        //return NAR.build(Default.fromJSON("nal/build/pei1.fast.nar"));        
        //return new ContinuousBagNARBuilder().build();
        //return new DiscretinuousBagNARBuilder().build();
    }
    
    

    
    public static void addTest(String name) {
        name = name.substring(3, name.indexOf(".nal"));
        tests.put(name, true);
    }

    public static double runTests(Class c) {

        tests.clear();
        scores.clear();
        
        if (waitForEnterKeyOnStart) {
            System.out.println("When ready, press enter");
            try {
                System.in.read();
            } catch (IOException ex) { }
        }
        
        //Result result = org.junit.runner.JUnitCore.runClasses(NALTest.class);
        Result result = JUnitCore.runClasses(new ParallelComputer(false, false),c);
        //Result result = JUnitCore.runClasses(Computer.serial(), c);

        if (result.getFailures()!=null) {
            for (Failure f : result.getFailures()) {
                System.out.println(f);
                System.out.println("  " + f.getException());
                f.getException().printStackTrace();

                String test = f.getMessage().substring(f.getMessage().indexOf("test/nal") + 8, f.getMessage().indexOf(".nal"));

                tests.put(test, false);
            }
        }
        
        int levelSuccess[] = new int[10];
        int levelTotals[] = new int[10];
        
        
        for (Map.Entry<String, Boolean> e : tests.entrySet()) {
            String name = e.getKey();
            int level = 0;
            try {
                level = Integer.parseInt(name.split("\\.")[0]);
            }
            catch (NumberFormatException ne) {
                //throw new RuntimeException(ne);
            }
            levelTotals[level]++;
            if (e.getValue()) {
                levelSuccess[level]++;
            }
        }

        double totalScore = 0;
        for (Double d : scores.values())
            totalScore += d;
        
        if (showReport) {
            int totalSucceeded = 0, total = 0;
            for (int i = 0; i < 9; i++) {
                float rate = (levelTotals[i] > 0) ? ((float)levelSuccess[i]) / levelTotals[i] : 0;
                String prefix = (i > 0) ? ("NAL" + i) : "Other";

                System.out.println(prefix + ": " + (rate*100.0) + "%  (" + levelSuccess[i] + "/" + levelTotals[i] + ")" );
                totalSucceeded += levelSuccess[i];
                total += levelTotals[i];
            }
            System.out.println(totalSucceeded + " / " + total);

            System.out.println("Score: " + totalScore);
        }
        
        return totalScore;
        
    }
    
    public static void main(String[] args) {
        
        runTests(NALTest.class);

    }
    
    transient final String scriptPath;

    
    public NALTest(NARSeed b, String scriptPath) {
        this.scriptPath = scriptPath;
        this.build = b;
    }

//    public Sentence parseOutput(String o) {
//        //getTruthString doesnt work yet because it gets confused when Stamp is at the end of the string. either remove that first or make getTruthString aware of that
//        return TextPerception.parseOutput(o);
//    }
    
    
//    public double score() {
//        return testNAL(scriptPath);
//    }
//
//
//    protected double testNAL(final String path) {
//        System.err.flush();
//        System.out.flush();
//
//
//
//        String input;
//        NAR nar;
//
//        NALPerformance test = new NALPerformance(nar = newNAR(), input = LibraryInput.getExample(path) );
//
//
//        if (showOutput)
//            new TextOutput(nar, System.out);
//        if (showTrace) {
//            new TraceWriter(nar, System.out);
//        }
//
//
//        if (showOutput) {
//            System.out.println(input);
//            System.out.println();
//        }
//
//        nar.memory.randomSeed(randomSeed);
//        test.run(minCycles);
//
//        System.err.flush();
//        System.out.flush();
//
//        double score = test.getScore();
//        boolean success = test.getSuccess();
//
//        scores.put(path, score);
//
//        //System.out.println(lastSuccess + " ,  " + path + "   \t   excess cycles=" + (n.time() - lastSuccess) + "   end=" + n.time());
//
//        if ((!success & showFail) || (success && showSuccess)) {
//            System.out.print(path + ' ');
//
//            if (showInput)
//                System.out.println(input);
//            System.out.flush();
//
//            test.printResults(System.out);
//            System.out.flush();
//
//            System.out.println("\n\n");
//
//        }
//
//        //System.err.println("Status: " + success + " total=" + expects.size() + " " + expects);
//
//        /*if (requireSuccess)
//            assertTrue(path, success);*/
//
//
//
//        return score;
//
//    }



//    @Test
//    public void test() {
//        double s = testNAL(scriptPath);
//        if (requireSuccess && !Double.isFinite(s))
//            assertTrue(scriptPath, false);
//    }

    
    
    
    
    
}

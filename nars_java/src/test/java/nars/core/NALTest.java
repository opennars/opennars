package nars.core;

import nars.core.build.Default;
import nars.io.ExampleFileInput;
import nars.io.InferenceLogger;
import nars.io.TextOutput;
import nars.util.NALPerformance;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;


@RunWith(Parameterized.class)
public class NALTest  {
        

    static {
        Parameters.DEBUG = true;
    }

    int minCycles = 1550; //TODO reduce this to one or zero to avoid wasting any extra time during tests
    static public long randomSeed = 1;
    static public boolean showInput = true;
    static public boolean showOutput = false;
    static public boolean saveSimilar = true;
    static public boolean showSuccess = false;
    static public boolean showFail = true;
    static public boolean showTrace = false;
    static public boolean showReport = true;
    static public boolean requireSuccess = true;
    private static boolean waitForEnterKeyOnStart = false; //useful for running profiler or some other instrumentation
      

    protected static Map<String, String> examples = new HashMap(); //path -> script data
    public static Map<String, Boolean> tests = new HashMap();
    public static Map<String, Double> scores = new HashMap();
    
    


    public static String getExample(String path) {
        try {
            String existing = examples.get(path);
            if (existing!=null)
                return existing;
            
            existing = ExampleFileInput.load(path);
            
            examples.put(path, existing);
            return existing;
        } catch (Exception ex) {
            assertTrue(path + ": " + ex.toString()  + ": ", false);
        }
        return "";
    }
    
    public NAR newNAR() {
        //return new NAR(new Default());
        return new NAR(new Default());
        //return NAR.build(Default.fromJSON("nal/build/pei1.fast.nar"));        
        //return new ContinuousBagNARBuilder().build();
        //return new DiscretinuousBagNARBuilder().build();
    }
    
    
    @Parameterized.Parameters
    public static Collection params() {
        Map<String, Object> et = ExampleFileInput.getUnitTests();
        Collection t = et.values();
        for (String x : et.keySet()) addTest(x);
        return t;
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
        Result result = JUnitCore.runClasses(new ParallelComputer(true, true),c);
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

    public NALTest(String scriptPath) {        
        this.scriptPath = scriptPath;
        
    }

//    public Sentence parseOutput(String o) {
//        //getTruthString doesnt work yet because it gets confused when Stamp is at the end of the string. either remove that first or make getTruthString aware of that
//        return TextPerception.parseOutput(o);
//    }
    
    
    public double run() {               
        return testNAL(scriptPath);
    }
    
    
    protected double testNAL(final String path) {
        System.err.flush();
        System.out.flush();

        Memory.resetStatic(randomSeed);
        
        String input;
        NAR nar;


        NALPerformance test = new NALPerformance(nar = newNAR(), input = getExample(path) );


        if (showOutput)
            new TextOutput(nar, System.out);
        if (showTrace) {
            new InferenceLogger(nar, System.out);
        }
        
        
        if (showOutput) {
            System.out.println(input);
            System.out.println();
        }

        test.run(minCycles);

        System.err.flush();
        System.out.flush();
        
        double score = test.getScore();                
        boolean success = test.getSuccess();
        
        scores.put(path, score);
        
        //System.out.println(lastSuccess + " ,  " + path + "   \t   excess cycles=" + (n.time() - lastSuccess) + "   end=" + n.time());

        if ((!success & showFail) || (success && showSuccess)) {
            System.out.print(path + ' ');

            if (showInput)
                System.out.println(input);
            System.out.flush();

            test.printResults(System.out);
            System.out.flush();

            System.out.println("\n\n");

        }
        
        //System.err.println("Status: " + success + " total=" + expects.size() + " " + expects);
        
        if (requireSuccess)
            assertTrue(path, success);


        
        return score;
        
    }

    
    
    
    
    @Test
    public void test() {
        testNAL(scriptPath);
    }

    
    
    
    
    
}

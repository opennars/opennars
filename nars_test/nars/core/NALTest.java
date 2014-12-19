package nars.core;

import nars.io.condition.OutputEmptyCondition;
import nars.io.condition.OutputNotContainsCondition;
import nars.io.condition.OutputContainsCondition;
import nars.io.condition.OutputCondition;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.gui.InferenceLogger;
import nars.io.Output;
import nars.io.Output.ERR;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.io.Texts;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class NALTest  {
        

    static {
        Memory.randomNumber.setSeed(1);
        Parameters.DEBUG = true;
    }

    int minCycles = 1550; //TODO reduce this to one or zero
    static public boolean showOutput = false;
    static public boolean saveSimilar = true;
    static public boolean showSuccess = false;
    static public boolean showFail = true;
    static public boolean showTrace = false;
    static public boolean showReport = true;
    static public boolean requireSuccess = true;
    static public int similarsToSave = 5;       
    private static boolean waitForEnterKeyOnStart = false; //useful for running profiler or some other instrumentation
      

    protected static Map<String, String> examples = new HashMap(); //path -> script data
    public static Map<String, Boolean> tests = new HashMap();
    public static Map<String, Double> scores = new HashMap();
    final String scriptPath;
    
    /** reads an example file line-by-line, before being processed, to extract expectations */
    public static List<OutputCondition> getConditions(NAR n, String example, boolean saveSimilar)  {
        List<OutputCondition> conditions = new ArrayList();
        String[] lines = example.split("\n");
        
        for (String s : lines) {
            s = s.trim();
            
            
            final String expectOutContains2 = "''outputMustContain('";

            if (s.indexOf(expectOutContains2)==0) {

                //remove ') suffix:
                String e = s.substring(expectOutContains2.length(), s.length()-2); 
                
                /*try {                    
                    Task t = narsese.parseTask(e);                    
                    expects.add(new ExpectContainsSentence(n, t.sentence));
                } catch (Narsese.InvalidInputException ex) {
                    expects.add(new ExpectContains(n, e, saveSimilar));
                } */
                
                conditions.add(new OutputContainsCondition(n, e, similarsToSave));

            }     
            
            final String expectOutNotContains2 = "''outputMustNotContain('";

            if (s.indexOf(expectOutNotContains2)==0) {

                //remove ') suffix:
                String e = s.substring(expectOutNotContains2.length(), s.length()-2);                 
                conditions.add(new OutputNotContainsCondition(n, e));

            }   
            
            final String expectOutEmpty = "''expect.outEmpty";
            if (s.indexOf(expectOutEmpty)==0) {                                
                conditions.add(new OutputEmptyCondition(n));
            }                
            

        }
        
        return conditions;
    }


    public static String getExample(String path) {
        try {
            String existing = examples.get(path);
            if (existing!=null)
                return existing;
            
            StringBuilder  sb  = new StringBuilder();
            String line;
            File fp = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(fp));
            while ((line = br.readLine())!=null) {
                sb.append(line).append("\n");
            }
            existing = sb.toString();
            examples.put(path, existing);
            return existing;
        } catch (Exception ex) {
            assertTrue(path + ": " + ex.toString()  + ": ", false);
        }
        return "";
    }
    
    public NAR newNAR() {
        return new NAR(new Default());
        //return NAR.build(Default.fromJSON("nal/build/pei1.fast.nar"));        
        //return new ContinuousBagNARBuilder().build();
        //return new DiscretinuousBagNARBuilder().build();
    }
    
    public static Map<String,Object> getUnitTests() {
        Map<String,Object> l = new TreeMap();
        
        final String[] directories = new String[] { "nal/test", "nal/DecisionMaking", "nal/ClassicalConditioning" };
        
        for (String dir : directories ) {

            File folder = new File(dir);
        
            for (final File file : folder.listFiles()) {
                if (file.getName().equals("README.txt") || file.getName().contains(".png"))
                    continue;
                if(!("extra".equals(file.getName()))) {
                    addTest(file.getName());
                    l.put(file.getName(), new Object[] { file.getAbsolutePath() } );
                }
            }
            
        }
        return l;
    }
    
    @Parameterized.Parameters
    public static Collection params() {
        return getUnitTests().values();
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
        
        Result result = JUnitCore.runClasses(new ParallelComputer(true, true), c);
              
        
        for (Failure f : result.getFailures()) {
            String test = f.getMessage().substring(f.getMessage().indexOf("test/nal") + 8, f.getMessage().indexOf(".nal"));
            
            tests.put(test, false);
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
        Memory.resetStatic();
        
        final List<OutputCondition> expects = new ArrayList();
        
        NAR n = null;
        boolean error = false;
        try {
            n = newNAR();


            


            String example = getExample(path);
            List<OutputCondition> extractedExpects = getConditions(n, example, saveSimilar);
            for (OutputCondition e1 : extractedExpects)
                expects.add(e1);

            if (showOutput)
                new TextOutput(n, System.out);
            if (showTrace) {
                new InferenceLogger(n, System.out);
            }


            n.addInput(new TextInput(example));
            n.run(minCycles);
        }
        catch(Throwable e){     
            System.err.println(e);
            if (Parameters.DEBUG) {                
                e.printStackTrace();
            }
            
            error = true;
        }
      
        
        
        System.err.flush();
        System.out.flush();
        
        boolean success = expects.size() > 0 && (!error);
        for (OutputCondition e: expects) {
            if (!e.succeeded) success = false;
        }

        double score = Double.POSITIVE_INFINITY;
        if (success) {
            long lastSuccess = -1;
            for (OutputCondition e: expects) {
                if (e.getTrueTime()!=-1) {
                    if (lastSuccess < e.getTrueTime())
                        lastSuccess = e.getTrueTime();
                }                
            }
            if (lastSuccess!=-1) {
                //score = 1.0 + 1.0 / (1+lastSuccess);
                score = lastSuccess;
                scores.put(path, score);
            }
        }
        else {
            scores.put(path, Double.POSITIVE_INFINITY);
        }
        
        //System.out.println(lastSuccess + " ,  " + path + "   \t   excess cycles=" + (n.time() - lastSuccess) + "   end=" + n.time());

        if ((!success & showFail) || (success && showSuccess)) {
            System.err.println('\n' + path + " @" + n.memory.getCycleTime());
            for (OutputCondition e: expects) {
                System.err.println("  " + e);
            }
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

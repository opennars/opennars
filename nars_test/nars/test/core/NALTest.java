package nars.test.core;

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
    //static public Narsese narsese;
    
    final int similarityThreshold = 4;
    
    private static boolean waitForEnterKeyOnStart = false; //useful for running profiler or some other instrumentation
      

    protected static Map<String, String> exCache = new HashMap(); //path -> script data
    public static Map<String, Boolean> tests = new HashMap();
    public static Map<String, Double> scores = new HashMap();
    final String scriptPath;
    
    /** reads an example file line-by-line, before being processed, to extract expectations */
    public static List<Expect> getExpectations(NAR n, String example, boolean saveSimilar)  {
        List<Expect> expects = new ArrayList();
        String[] lines = example.split("\n");
        
        for (String s : lines) {
            s = s.trim();

            final String expectOutContains = "''expect.outContains('";

            if (s.indexOf(expectOutContains)==0) {

                //without ') suffix:
                String e = s.substring(expectOutContains.length(), s.length()-2);                           
                
                expects.add(new ExpectContains(n, e, saveSimilar));

                
            }
            
            
            //TEMPORARY, process old script format
            final String expectOutContains2 = "''outputMustContain('";

            if (s.indexOf(expectOutContains2)==0) {

                //without ') suffix:
                String e = s.substring(expectOutContains2.length(), s.length()-2); 
                e = e.replace("\\\\","\\");
                
                /*try {                    
                    Task t = narsese.parseTask(e);                    
                    expects.add(new ExpectContainsSentence(n, t.sentence));
                } catch (Narsese.InvalidInputException ex) {
                    expects.add(new ExpectContains(n, e, saveSimilar));
                } */
                
                expects.add(new ExpectContains(n, e, saveSimilar));

            }     
            
            //TEMPORARY, process old script format
            final String expectOutNotContains2 = "''outputMustNotContain('";

            if (s.indexOf(expectOutNotContains2)==0) {

                //without ') suffix:
                String e = s.substring(expectOutNotContains2.length(), s.length()-2); 
                e = e.replace("\\\\","\\");
                
                expects.add(new ExpectNotContains(n, e));

            }   
            
            final String expectOutEmpty = "''expect.outEmpty";
            if (s.indexOf(expectOutEmpty)==0) {                                
                expects.add(new ExpectOutputEmpty(n));
            }                
            

        }
        
        return expects;
    }


    public static String getExample(String path) {
        try {
            String existing = exCache.get(path);
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
            exCache.put(path, existing);
            return existing;
        } catch (Exception ex) {
            assertTrue(path + ": " + ex.toString()  + ": ", false);
        }
        return "";
    }
    
    public NAR newNAR() {
        return NAR.build(new Default());
        //return NAR.build(Default.fromJSON("nal/build/pei1.fast.nar"));        
        //return new ContinuousBagNARBuilder().build();
        //return new DiscretinuousBagNARBuilder().build();
    }
    
    @Parameterized.Parameters
    public static Collection params() {
        
        Map<String,Object> l = new TreeMap();
        
        final String[] directories = new String[] { "nal/test", "nal/DecisionMaking" };
        
        for (String dir : directories ) {

            File folder = new File(dir);
        
            for (final File file : folder.listFiles()) {
                if (file.getName().equals("README.txt"))
                    continue;
                if(!("extra".equals(file.getName()))) {
                    addTest(file.getName());
                    l.put(file.getName(), new Object[] { file.getAbsolutePath() } );
                }
            }
            
        }
                
        return l.values();
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
        
        int levelSuccess[] = new int[9];
        int levelTotals[] = new int[9];
        
        
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
        
        final List<Expect> expects = new ArrayList();
        
        NAR n = null;
        boolean error = false;
        try {
            n = newNAR();


            


            String example = getExample(path);
            List<Expect> extractedExpects = getExpectations(n, example, saveSimilar);
            for (Expect e1 : extractedExpects)
                expects.add(e1);

            if (showOutput)
                new TextOutput(n, System.out);
            if (showTrace) {
                new InferenceLogger(n, System.out);
            }


            n.addInput(new TextInput(example));
            n.finish(minCycles);
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
        for (Expect e: expects) {
            if (!e.succeeded) success = false;
        }

        double score = Double.POSITIVE_INFINITY;
        if (success) {
            long lastSuccess = -1;
            for (Expect e: expects) {
                if (e.successAt!=-1) {
                    if (lastSuccess < e.successAt)
                        lastSuccess = e.successAt;
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
            for (Expect e: expects) {
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

    public static abstract class Expect extends Output {

        public boolean succeeded = false;
        public List<CharSequence> exact = new ArrayList();
        public final NAR nar;
        long successAt = -1;

        public Expect(NAR nar) {
            super(nar);
            this.nar = nar;
        }
        
        public boolean isInverse() { return false; }
        
        @Override
        public void event(Class channel, Object... args) {
            if ((succeeded) && (!isInverse()))
                return;
            if ((channel == OUT.class) || (channel == EXE.class)) {
                Object signal = args[0];                
                if (condition(channel, signal)) {
                    if (saveSimilar)
                        exact.add(TextOutput.getOutputString(channel, signal, true, true, nar));
                    setSucceeded();
                }
            }
        }
        
        protected void setSucceeded() { 
            if (successAt == -1)
                successAt = nar.time();
            succeeded = true;            
        }
        
        public boolean success() { return succeeded; }
        
        /** returns true if condition was satisfied */
        abstract public boolean condition(Class channel, Object signal);

        @Override
        public String toString() {            
            return  getClass().getSimpleName() + " " + (succeeded ? "OK: " + exact : getFailureReason());
        }

        abstract public String getFailureReason();
    }
    
    public static class ExpectOutputEmpty extends Expect {

        List<String> output = new LinkedList();
        
        public ExpectOutputEmpty(NAR nar) {
            super(nar);
            succeeded = true;
        }

        public String getFailureReason() {
            return "FAIL: output exists but should not: " + output;
        }
         
        @Override
        public boolean condition(Class channel, Object signal) {
            //any OUT or ERR output is a failure
            if ((channel == OUT.class) || (channel == ERR.class)) {
                output.add(channel.getSimpleName().toString() + ": " + signal.toString());
                succeeded = false;
                return false;
            }
            return false;
        }
    }
    
    public static class ExpectContains extends Expect {

        final String containing;
        public Map<String, Integer> almost = new HashMap();
        final boolean saveSimilar;

        public ExpectContains(NAR nar, String containing, boolean saveSimilar) {
            super(nar);            
            this.containing = containing;
            this.saveSimilar = saveSimilar;
        }

        @Override
        public String getFailureReason() {
            String s = "FAIL: No substring match: " + containing;
            if (!almost.isEmpty()) {
                for (String cs : getCandidates(5)) {
                    s += "\n\t" + cs;
                }
            }
            return s;
        }
        
        public List<String> getCandidates(int max) {
            List<String> c = new ArrayList(almost.keySet());
            
            Collections.sort(c, new Comparator<String>() {

                @Override
                public int compare(String a, String b) {
                    return Integer.compare(almost.get(a), almost.get(b));
                }
                
            });
            
            if (c.size() < max) 
                return c;
            else
                return c.subList(0, max);            
        }
        
        public boolean cond(Class channel, Object signal) {
            
            if ((channel == OUT.class) || (channel == EXE.class)) {
                
                String o;
                if (signal instanceof Task) {
                    //only compare for Sentence string, faster than TextOutput.getOutputString
                    //which also does unescaping, etc..
                    Sentence s = ((Task)signal).sentence;
                    o = s.toString(nar, false).toString();
                    if (o.contains(containing)) return true;                    
                }
                else {
                    o = TextOutput.getOutputString(channel, signal, false, false, nar).toString();
                    if (o.contains(containing)) return true;
                }

                if (saveSimilar) {
                    int dist = Texts.levenshteinDistance(o, containing);            
                    //if (dist < similarityThreshold + Math.abs(o.length() - containing.length()))
                    almost.put("similar(" + dist + "): " + TextOutput.getOutputString(channel, signal, false, false, nar), dist);
                }            
            }
            if (channel == ERR.class) {
                assertTrue(signal.toString(), false);
            }
            return false;
        }
        
        @Override
        public boolean condition(Class channel, Object signal) {
            if (succeeded)
                return true;
            return cond(channel,signal);
        }
    }
    
    
    public static class ExpectNotContains extends ExpectContains {

        
        public ExpectNotContains(NAR nar, String containing) {
            super(nar, containing, false);
            succeeded = true;
        }

        @Override
        public String getFailureReason() {
            return "incorrect output: " + containing;
        }


        
        @Override
        public boolean condition(Class channel, Object signal) {
            if (!succeeded)
                return false;
            if (cond(channel,signal)) {
                onFailure(channel, signal);
                succeeded = false;
                return false;
            }
            return true;
        }
        
        public boolean isInverse() { return true; }

        protected void onFailure(Class channel, Object signal) {
        }
    }
    
}

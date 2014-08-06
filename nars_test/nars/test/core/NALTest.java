package nars.test.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import nars.core.DefaultNARBuilder;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.io.Output;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.io.TextPerception;
import nars.util.StringUtil;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class NALTest  {


    

      

    protected static Map<String, String> exCache = new HashMap(); //path -> script data
    public static Map<String, Boolean> tests = new HashMap();
    
    boolean saveSimilar = true;

    private final String scriptPath;
    
    /** reads an example file line-by-line, before being processed, to extract expectations */
    public static List<Expect> getExpectations(NAR n, String example, boolean saveSimilar)  {
        List<Expect> expects = new ArrayList();
        String[] lines = example.split("\n");
        
        for (String s : lines) {
            s = s.trim();

            final String expectOutContains = "*expect.outContains('";

            if (s.indexOf(expectOutContains)==0) {

                //without ') suffix:
                String e = s.substring(expectOutContains.length(), s.length()-3);
                
                Expect ex = new ExpectContains(n, e, saveSimilar);

                expects.add((Expect)n.addOutput(ex));
            }
            
            
            //TEMPORARY, process old script format
            final String expectOutContains2 = "''outputMustContain('";

            if (s.indexOf(expectOutContains2)==0) {

                //without ') suffix:
                String e = s.substring(expectOutContains2.length(), s.length()-3); 
                e = e.replace("\\\\","\\");
                
                Expect ex = new ExpectContains(n, e, saveSimilar);

                expects.add((Expect)n.addOutput(ex));
            }                
            
            final String expectOutEmpty = "*expect.outEmpty";
            if (s.indexOf(expectOutEmpty)==0) {                
                Expect ex = new ExpectOutputEmpty(n);
                expects.add((Expect)n.addOutput(ex));
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
    
    public static NAR newNAR() {
        return new DefaultNARBuilder().build();
    }
    
    @Parameterized.Parameters
    public static Collection params() {
        List l = new LinkedList();
        
        File folder = new File("nal/test");
        
        for (final File file : folder.listFiles()) {
            if (file.getName().equals("README.txt"))
                continue;
            addTest(file.getName());
            l.add(new Object[] { file.getAbsolutePath() } );
        }
        
        return l;
    }
    
    
    public static void addTest(String name) {
        name = name.substring(3, name.indexOf(".nal"));
        tests.put(name, true);
    }

    public static void main(String[] args) {
        Result result = org.junit.runner.JUnitCore.runClasses(NALTest.class);
        
        System.out.println("\n\n");
        
        for (Failure f : result.getFailures()) {
            String test = f.getMessage().substring(f.getMessage().indexOf("test/nal") + 8, f.getMessage().indexOf(".nal"));
            
            tests.put(test, false);
        }
        
        int levelSuccess[] = new int[9];
        int levelTotals[] = new int[9];
        
        
        for (Map.Entry<String, Boolean> e : tests.entrySet()) {
            String name = e.getKey();
            int level = Integer.parseInt(name.split("\\.")[0]);
            levelTotals[level]++;
            if (e.getValue())
                levelSuccess[level]++;
        }
        for (int i = 1; i < 9; i++) {
            float rate = (levelTotals[i] > 0) ? ((float)levelSuccess[i]) / levelTotals[i] : 0;
            System.out.println("NAL" + i + ": " + (rate*100.0) + "%  (" + levelSuccess[i] + "/" + levelTotals[i] + ")" );
        }
    }

    public NALTest(String scriptPath) {        
        this.scriptPath = scriptPath;
    }

    public Sentence parseOutput(String o) {
        //getTruthString doesnt work yet because it gets confused when Stamp is at the end of the string. either remove that first or make getTruthString aware of that
        return TextPerception.parseOutput(o);
    }
    
    
    protected void testNAL(final String path) {
        @Deprecated int minCycles = 10; //TODO reduce this to one or zero
        
        NAR.resetStatics();
        
        final NAR n = newNAR();
        
        final List<Expect> expects = new ArrayList();
        
                
        String example = getExample(path);
        List<Expect> extractedExpects = getExpectations(n, example, saveSimilar);
        for (Expect e1 : extractedExpects)
            expects.add((Expect)n.addOutput(e1));
        
        n.addInput(new TextInput(example));
        
        n.finish(minCycles);
        
        System.out.println('\n' + path + " @" + n.getTime());
        
        if (expects.size() == 0) {
            //no tests
            System.out.println("  no tests");
            assertTrue(path + " has no test", false);
        }
        boolean success = true;
        for (Expect e: expects) {
            System.out.println("  " + e);
            if (!e.realized) success = false;
        }
        assertTrue(path, success);
        
    }

    
    
    
    
    @Test
    public void test() {
        testNAL(scriptPath);
    }

    public static abstract class Expect implements Output {

        public boolean realized = false;
        public List<String> exact = new ArrayList();
        protected final NAR nar;

        public Expect(NAR nar) {
            super();
            this.nar = nar;
            
            nar.addOutput(this);
        }
        
        @Override
        public void output(Class channel, Object signal) {
            if (channel == OUT.class) {
                if (condition(channel, signal)) {
                    exact.add(TextOutput.getOutputString(channel, signal, true, true, nar));
                    realized = true;
                }
            }
        }
        
        /** returns true if condition was satisfied */
        abstract public boolean condition(Class channel, Object signal);

        public String toString() {            
            return realized ? "OK: " + exact : getFailureReason();
        }

        abstract public String getFailureReason();
    }
    
    public static class ExpectOutputEmpty extends Expect {

        public ExpectOutputEmpty(NAR nar) {
            super(nar);
            realized = true;
        }

        public String getFailureReason() {
            return "FAIL: output exists";
        }
         
        @Override
        public boolean condition(Class channel, Object signal) {
            //any OUT or ERR output is a failure
            if ((channel == OUT.class) || (channel == ERR.class)) {
                realized = false;
                return false;
            }
            return false;
        }
    }

    public static class ExpectContains extends Expect {

        private final String containing;
        final int similarityThreshold = 4;
        public Map<String, Integer> almost = new HashMap();
        private final boolean saveSimilar;

        public ExpectContains(NAR nar, String containing, boolean saveSimilar) {
            super(nar);            
            this.containing = containing;
            this.saveSimilar = saveSimilar;
        }

        public String getFailureReason() {
            String s = "FAIL: No substring match: " + containing;
            if (!almost.isEmpty()) {
                for (String cs : getCandidates(5)) {
                    s += "\n\tsimilar: " + cs;
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
        
        @Override
        public boolean condition(Class channel, Object signal) {
            if (channel == OUT.class) {
                String o = TextOutput.getOutputString(channel, signal, false, false, nar);
                if (o.contains(containing)) return true;

                if (saveSimilar) {
                    int dist = StringUtil.levenshteinDistance(o, containing);            
                    //if (dist < similarityThreshold + Math.abs(o.length() - containing.length()))
                    almost.put(TextOutput.getOutputString(channel, signal, false, true, nar), dist);
                }            
            }
            if (channel == ERR.class) {
                assertTrue(signal.toString(), false);
            }
            return false;
        }
    }

}

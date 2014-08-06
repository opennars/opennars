package nars.test.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import nars.core.DefaultNARBuilder;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.io.Output;
import nars.io.TextOutput;
import nars.io.TextPerception;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class NALTest  {

    static final boolean testPerformance = false;    
    private final int performanceIterations = 4;
    boolean showSuccess = false;
    int maxSummaryOutputLines = 300;
    
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine js = factory.getEngineByName("JavaScript");
      

    protected static Map<String, String> exCache = new HashMap(); //path -> script data
    

    
    public static String getExample(String path)  {
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
        
    public boolean match(String x, String y) {
        return x.equals(y);
    }
    
    
    final List<String> out = new ArrayList();
    int currentOutputLine = 0;
    public boolean outputContains(String s) {        
        currentOutputLine = 0;
        for (String o : out) {
            if (o.contains(s))
                return true;
            currentOutputLine++;
        }
        return false;
    }
    

    public Sentence parseOutput(String o) {
        //getTruthString doesnt work yet because it gets confused when Stamp is at the end of the string. either remove that first or make getTruthString aware of that
        return TextPerception.parseOutput(o);
    }
    
    public static NAR newNAR() {
        return new DefaultNARBuilder().build();
    }
    
    protected void testNAL(final String path) {
        @Deprecated int minCycles = 1; //TODO reduce this to one or zero
        
        NAR.resetStatics();
        
        final NAR n = newNAR();
        
        final List<String> expressions = new ArrayList(2);
        out.clear();

        
        //new TextOutput(n, new PrintWriter(System.out));
        new TextOutput(n/*, System.out*/) {
            @Override
            public void output(Class channel, Object signal) {                
                
                //super.output(channel, signal);
                
                if (channel == Output.ERR.class) {                       
                    if (signal instanceof Exception) {
                        ((Throwable)signal).printStackTrace();;
                    }

                    assertTrue(path + " ERR: " + signal, false);
                }
                
                if (channel == Output.ECHO.class) {
                    String e = signal.toString();
                    
                    //extract embedded Javascript expressions
                    if (e.startsWith("\"\'")) {      
                        //remove begining "' and trailing "
                        String expression = e.substring(2, e.length()-1);
                        expressions.add(expression);
                    }           
                }
                else if (channel == OUT.class) {
                    String s = getOutputString(channel, signal, true, true, n);                    
                    out.add(s);
                }

            }
        };         
        n.addInput(getExample(path));
        n.finish(minCycles);


        //JS context setup --------
        js.put("test", this);
        js.put("out", out);
        try {
            js.eval("function outputMustContain(x) { return test.outputContains(x); };");
        } catch (ScriptException ex) {
            System.err.println(ex.toString());
        }
        //-------------------------

        if (expressions.isEmpty()) {
            assertTrue(path + " contains no expressions to evaluate",  false);
        }
        
        for (String e : expressions) {
            try {
                Object result = js.eval(e);
                if (result instanceof Boolean) {
                    boolean r = (Boolean)result;
                    
                    
                    if (!r) {
                        String failMsg = path + ": " + e + " FAIL @ " + + n.getTime() + ", output lines=" + out.size();
                        System.out.println(failMsg);
                        printOutputSummary();
                        System.out.println();
                        
                        assertTrue(failMsg, r);
                    }
                    else {
                        if (showSuccess) {
                            System.out.println(path + ": " + e + " OK @ " + + n.getTime() + ", output lines=" + out.size());
                            System.out.println(out.get(currentOutputLine));
                            
                        }
                    }
                    
                }
            }
            catch (Exception x) {
                assertTrue(path + ": " + x.toString()  + ": ", false);                
            }            
        }
        
        System.out.println(path + " OK");
        
        if (testPerformance) {
            perfNAL(path, 0, performanceIterations, 1);            
        }
    }
    
    protected void printOutputSummary() {
        int b = out.size();
        int a = Math.max(0, b - maxSummaryOutputLines);
        List<String> l = out.subList(a, b);
        for (String s : l)
            System.out.println((a++) + ": " + s);
    }
    
    public static void perfNAL(final String path, final int extraCycles, int repeats, int warmups) {
        perfNAL(path, extraCycles, repeats, warmups, true);
    }
    
    public static void perfNAL(final String path, final int extraCycles, int repeats, int warmups, boolean gc) {
        
        
        new Performance(path, repeats, warmups, gc) {
            private NAR n;

            @Override
            public void init() {
                NAR.resetStatics();
                
                n = newNAR();
            }

            @Override
            public void run(boolean warmup) {
                n.reset();
                n.addInput(getExample(path));
                n.finish(extraCycles);
            }


        }.print();
        
           
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

    private final String scriptPath;

    public NALTest(String scriptPath) {        
        this.scriptPath = scriptPath;
    }

    @Test
    public void test() {
        testNAL(scriptPath);
    }
    
    public static Map<String, Boolean> tests = new HashMap();
    
    public static void addTest(String name) {
        name = name.substring(3, name.indexOf(".nal"));
        tests.put(name, true);
    }

    public static void main(String[] args) {
        
        Result result = org.junit.runner.JUnitCore.runClasses(NALTest.class);
        
        
     
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

}

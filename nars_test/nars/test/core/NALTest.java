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
import nars.core.NAR;
import nars.entity.Sentence;
import nars.io.Output;
import nars.io.TextOutput;
import nars.io.TextPerception;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class NALTest  {

    static final boolean testPerformance = false;    
    private final int performanceIterations = 4;

    int maxSummaryOutputLines = 10;
    
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine js = factory.getEngineByName("JavaScript");
      

    protected Map<String, String> exCache = new HashMap(); //path -> script data
    

    
    public String getExample(String path)  {
        try {
            String existing = exCache.get(path);
            if (existing!=null)
                return existing;
            
            StringBuffer  sb  = new StringBuffer();
            String line;
            File fp = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(fp));
            while ((line = br.readLine())!=null) {
                sb.append(line + "\n");
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
        if (x.equals(y))
            return true;
        return false;
    }
    
    
    final List<String> out = new ArrayList();

    public boolean outputContains(String s) {        
        for (String o : out) {
            if (o.indexOf(s)!=-1)
                return true;
        }
        return false;
    }
    

    public Sentence parseOutput(String o) {
        //getTruthString doesnt work yet because it gets confused when Stamp is at the end of the string. either remove that first or make getTruthString aware of that
        return TextPerception.parseOutput(o);
    }
    
    protected void testNAL(final String path) {
        final NAR n = new NAR();        
        
        
        final LinkedList<String> expressions = new LinkedList();
        out.clear();

        
        //new TextOutput(n, new PrintWriter(System.out));
        new TextOutput(n) {
            @Override
            public void output(Class c, Object line) {                
                if (c == Output.ERR.class) {   
                    assertTrue(path + " ERR: " + line, false);
                }
                
                String s = line.toString();
                s = s.trim();

                if (c == Output.ECHO.class) {
                    if (s.startsWith("\"\'")) {      
                        //remove begining "' and trailing "
                        String expression = s.substring(2, s.length()-1);
                        expressions.add(expression);
                        return;
                    }                        
                }

                if (c == Output.OUT.class) {
                    out.add(s);
                }
            }            
        };         
        
        n.addInput(getExample(path));

        n.finish(1);


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
                    
                }
            }
            catch (Exception x) {
                assertTrue(path + ": " + x.toString()  + ": ", false);                
            }            
        }
        
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
    
    protected void perfNAL(final String path, final int extraCycles, int repeats, int warmups) {
        
        
        new Performance(path, repeats, warmups) {
            private NAR n;

            @Override
            public void init() {
                n = new NAR();
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

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(NALTest.class);
    }

}

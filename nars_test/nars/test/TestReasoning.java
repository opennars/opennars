package nars.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import nars.core.NAR;
import nars.io.ExperienceReader;
import nars.io.ExperienceWriter;
import nars.io.Symbols;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit Test Reasoning, using input and output files from nal/Examples ;
 * <pre>
 * To create a new test input, add the NARS input as XX-in.txt in nal/Examples ,
 *  run the test suite, and move resulting file in temporary directory
 * /tmp/nars_test/XX-out.txt
 * into nal/Example
 * </pre>
 *
 */
public class TestReasoning  {

    private ScriptEngineManager engineManager = new ScriptEngineManager();
    private ScriptEngine js = engineManager.getEngineByName("nashorn");

    private Map<String, String> exCache = new HashMap(); //path -> script data
    
    @Test
    public void testExample1_0() { 
        testNAL("nal1.0.nal");    
        perfNAL("nal1.0.nal", 0, 25, 1);
    }
        
    @Test
    public void testExample1_1() { 
        testNAL("nal1.1.nal");    
        perfNAL("nal1.1.nal", 0, 25, 1);
    }
    
    public String getExample(String path)  {
        try {
            String existing = exCache.get(path);
            if (existing!=null)
                return existing;
            
            StringBuffer  sb  = new StringBuffer();
            String line;
            File fp = new File(getClass().getResource("nal/" + path).toURI());
            BufferedReader br = new BufferedReader(new FileReader(fp));
            while ((line = br.readLine())!=null) {
                sb.append(line + "\n");
            }
            existing = sb.toString();
            exCache.put(path, existing);
            return existing;
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
        return "";
    }
        
    public boolean match(String x, String y) {
        if (x.equals(y))
            return true;
        return false;
    }
        
    
    protected void testNAL(String path) {
        int extraCycles = 0;
        final NAR n = new NAR();        
        
        final LinkedList<String> out = new LinkedList();
        final LinkedList<String> expressions = new LinkedList();

        ////new ExperienceWriter(n, new PrintWriter(System.out));
        new ExperienceWriter(n) {
            @Override
            public void nextOutput(ArrayList<String> lines) {
                for (String s : lines) {
                    s = s.trim();
                    String OUT_PREFIX = Symbols.OUTPUT_LINE + ": ";

                    if (s.startsWith(OUT_PREFIX)) {
                        out.add(s.substring(OUT_PREFIX.length()));
                    }
                    else if (s.startsWith("\"\'")) {      
                        //remove begining "' and trailing "
                        String expression = s.substring(2, s.length()-1);
                        expressions.add(expression);
                    }
                }                
            }            
        };            

        n.run(extraCycles, false);

        js.put("test", this);
        js.put("out", out);

        //System.err.println("'" + path + "' output=" + Arrays.asList(out));

        for (String e : expressions) {
            try {
                Object result = js.eval(e);
                if (result instanceof Boolean) {
                    assertTrue(path + ": " + e, (Boolean)result);
                }
            }
            catch (Exception x) {
                assertTrue(path + ": " + e + " --> " + x.toString() , false);
                System.err.println(e);
                System.err.println(x);
                x.printStackTrace();
            }            
        }
        
    }
    
    protected void perfNAL(String path, int extraCycles, int repeats, int warmups) {
        
        final NAR n = new NAR();        
        
        long totalTime = 0;
        long totalMemory = 0;

        for (int r = 0; r < repeats; r++) {
            long start = System.nanoTime();

            System.gc();

            long freeMemStart = Runtime.getRuntime().freeMemory();

            n.reset();
            new ExperienceReader(n, getExample(path));
            n.run(extraCycles, false);

            if (warmups == 0) {
                totalTime += System.nanoTime() - start;
                totalMemory += freeMemStart - Runtime.getRuntime().freeMemory();
            }
            else
                warmups--;
        }
        System.err.print(path + ": " + ((double)totalTime)/((double)repeats)/1000000.0 + "ms per iteration, ");
        System.err.println(((double)totalMemory)/((double)repeats)/1024.0 + " kb per iteration");
        
    }


}

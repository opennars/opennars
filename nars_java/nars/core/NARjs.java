package nars.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Javascript NAR Runner
 * @author me
 */
public class NARjs {
    final static ScriptEngineManager factory = new ScriptEngineManager();
    
    final ScriptEngine js = factory.getEngineByName("JavaScript");

    public NARjs() throws Exception {
        super();
        js.eval("load('nashorn:mozilla_compat.js')");
        
        js.eval("importPackage('java.lang')");
        js.eval("importPackage('java.util')");
        js.eval("importPackage('java.io')");

        js.eval("importPackage('nars.core')");
        js.eval("importPackage('nars.core.build')");
        js.eval("importPackage('nars.io')");
        js.eval("importPackage('nars.gui')");
        
        js.eval("function newDefaultNAR() { var x = new DefaultNARBuilder().build(); new TextOutput(x, System.out); return x; }");
    }

    public Object eval(String s) throws ScriptException {
        return js.eval(s);
    }
    
    public static void printHelp() {
        System.out.println("Help coming soon.");
    }
    
    public static void main(String[] args) throws Exception {
        NARjs j = new NARjs();
        
        System.out.println(NAR.VERSION +  " Javascript Console - :h for help, :q to exit");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("> ");

        String s;
        while ((s = br.readLine())!=null) {
            
            
            try {
                if (s.equals(":q"))
                    break;
                else if (s.startsWith(":h")) {
                    printHelp();
                    continue;
                }
                    
                Object ret = j.eval(s);
                
                if (ret != null) {
                    System.out.println(ret);
                }
            } catch (Exception e) {
                System.out.println(e.getClass().getName() + " in parsing: " + e.getMessage());
            } finally {

                
                System.out.print("> ");
                
            }
        }
    
        br.close();
        System.exit(0);
    }
}

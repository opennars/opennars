package nars.op.software;

import nars.nal.nal8.TermFunction;
import nars.nal.term.Term;
import nars.op.mental.Mental;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.HashMap;

/**
 * Executes a Javascript expression
 */
public class Javascript extends TermFunction implements Mental {

    ScriptEngine js = null;

    final HashMap global = new HashMap();

    public Javascript() {
        super("^js");
    }
    
    @Override public Object function(Term[] args) {
        if (args.length < 1) {
            return null;
        }
        
        if (js == null) {
            ScriptEngineManager factory = new ScriptEngineManager();
            js = factory.getEngineByName("JavaScript");
        }
        
        // copy over all arguments
        Term[] scriptArguments;
        scriptArguments = new Term[args.length-1];
        System.arraycopy(args, 1, scriptArguments, 0, args.length-1);
        
        Bindings bindings = new SimpleBindings();
        bindings.put("global", global);
        bindings.put("arg", scriptArguments);
        bindings.put("memory", getMemory());
        bindings.put("nar", nar);

        
        String input = args[0].toString();
        if (input.charAt(0) == '"') {
            input = input.substring(1, input.length() - 1);
        }
        Object result;
        try {
            result = js.eval(input, bindings);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

}

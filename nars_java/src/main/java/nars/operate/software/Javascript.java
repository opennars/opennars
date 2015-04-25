package nars.operate.software;

import nars.nal.nal8.TermFunction;
import nars.nal.term.Term;
import nars.operate.mental.Mental;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

/**
 * Executes a Javascript expression
 */
public class Javascript extends TermFunction implements Mental {
    
    ScriptEngine js = null;      

    public Javascript() {
        super("^js");
    }
    
    @Override public Term function(Term[] args) {
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
        bindings.put("scriptArguments", scriptArguments);
        bindings.put("memory", getMemory());
        
        String input = (args[0].name()).toString();
        if (input.charAt(0) == '"') {
            input = input.substring(1, input.length() - 1);
        }
        Object result;
        try {
            result = js.eval(input, bindings);
        } catch (Throwable ex) {
            result = ex.toString();
        }
        return quoted(result.toString());
    }

}

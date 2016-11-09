package nars.lab.operator.software;

import java.util.HashMap;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import nars.core.Memory;
import nars.io.Output.ERR;
import nars.io.Texts;
import nars.language.Term;
import nars.operator.SynchronousFunctionOperator;
import nars.operator.mental.Mental;

/**
 * Executes a Javascript expression
 */
public class Javascript extends SynchronousFunctionOperator implements Mental {
    
    ScriptEngine js = null;   
    Bindings bindings = null;
    final HashMap global = new HashMap();

    public Javascript() {
        super("^js");
    }
    
    @Override protected Term function(Memory memory, Term[] args) {
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
        
        if(bindings == null) {
            bindings = new SimpleBindings();
        }
        bindings.put("arg", scriptArguments);
        bindings.put("memory", memory);
        bindings.put("nar", nar); 
        bindings.put("global", global);
        
        String input = Texts.unescape(args[0].name()).toString();
        if (input.charAt(0) == '"') {
            input = input.substring(1, input.length() - 1);
        }
        Object result;
        try {
            result = js.eval(input, bindings);
        } catch (Throwable ex) {
            //result=ex.toString();
            nar.emit(ERR.class, ex.toString());
            return null;
        }
        if(result==null) {
            return null;
        }
        return Term.text(result.toString());
    }

    @Override public Term getRange() {
        return Term.get("js_evaluation");
    }
}

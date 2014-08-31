package nars.operator.software;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import nars.io.Texts;
import nars.language.Term;
import nars.operator.SynchronousFunctionOperator;
import nars.storage.Memory;

/**
 * Executes a Javascript expression
 */
public class Javascript extends SynchronousFunctionOperator {

    final ScriptEngineManager factory = new ScriptEngineManager();
    final ScriptEngine js = factory.getEngineByName("JavaScript");      

    public Javascript() {
        super("^js");
    }
    
    @Override protected Term function(Memory memory, Term[] x) {
        if (x.length!=1)
            return null;
        
        js.put("memory", memory);
        
        String input = Texts.unescape(x[0].name()).toString();
        if (input.charAt(0) == '"') {
            input = input.substring(1, input.length() - 1);
        }
        Object result;
        try {
            result = js.eval(input);
        } catch (Throwable ex) {
            result = ex.toString();
        }
        return new Term(Texts.escape('"' + result.toString() + '"').toString());
    }

    @Override public Term getRange() {
        return new Term("js_evaluation");
    }
    

}

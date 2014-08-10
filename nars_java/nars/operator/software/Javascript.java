package nars.operator.software;

import java.util.ArrayList;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import nars.core.Parameters;
import nars.entity.Task;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.storage.Memory;

/**
 * Executes a Javascript expression
 */
public class Javascript extends Operator {

    final ScriptEngineManager factory = new ScriptEngineManager();
    final ScriptEngine js = factory.getEngineByName("JavaScript");      

    public Javascript() {
        super("^js");
    }

    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        
        js.put("memory", memory);
        //TODO make memory access optional by constructor argument
        //TODO allow access to NAR instance?

        if (args.length < 1)
            return null;
        
        Object result = null;
        
        String input = Texts.unescape(args[0].name()).toString();
        if (input.charAt(0) == '"')
            input = input.substring(1, input.length()-1);                
        try {
            result = js.eval(input);
        } catch (Throwable ex) {            
            result = ex.toString();
        }
        
        memory.output(Javascript.class, input + " | " + result);
        
        if (result instanceof Number) {
            result = "n_" + result;
        }
        
        Term r = new Term(Texts.escape('"' + result.toString() + '"').toString());
        Inheritance t = Inheritance.make(
                Product.make(new Term[] { operation, r }, memory),
                new Term("js_evaluation"), memory);
        
        memory.output(Task.class, t);
        
        ArrayList<Task> results = new ArrayList<>(1);
        results.add(memory.newTask(t, Symbols.JUDGMENT_MARK, 1f, 0.99f, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY));
        return results;
        

    }
    
}

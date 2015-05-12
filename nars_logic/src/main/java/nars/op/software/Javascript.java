package nars.op.software;

import nars.NAR;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.TermFunction;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.op.io.Echo;
import nars.op.mental.Mental;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.ArrayList;
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

    class DynamicFunction extends TermFunction {

        private final String function;

        public DynamicFunction(String name, String function) {
            super("^" + name);
            this.function = function;
        }

        @Override public Object function(Term[] args) {

            Bindings bindings = newBindings(args);
            String input = function + ".apply(this," + bindings.get("args").toString() + ")";

            Object result;
            try {
                result = js.eval(input, bindings);
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
            return result;
        }


    }

    public class DynamicJavascriptFunctionOperator extends TermFunction {

        protected DynamicJavascriptFunctionOperator() {
            super("^jsOp");
        }

        @Override
        protected ArrayList<Task> execute(Operation operation, Term[] args) {
            ArrayList<Task> result = super.execute(operation, args);

            //HACK prevent this from being re-executed
            operation.getTask().delete();
            //nar.concept(operation.getTerm()).goals.clear();
            nar.concept(operation.getTerm()).delete();

            return result;
        }

        @Override
        public Object function(Term... x) {
            String funcName = Atom.unquote(x[0]);
            String functionCode = Atom.unquote(x[1]);
            nar.input(new Echo(Javascript.class, "JS Operator Bind: ^" + funcName + " = " + functionCode));
            DynamicFunction d = new DynamicFunction(funcName, functionCode.toString());
            nar.on(d);
            return null;
        }
    }

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        boolean x = super.setEnabled(n, enabled);
        if (enabled) {
            n.on(new DynamicJavascriptFunctionOperator());
        }
        return x;
    }

    //    public DynamicFunction newDynamic(String funcName, ScriptObject functionCode) {
//        DynamicFunction d = new DynamicFunction(funcName, functionCode.toString());
//        nar.on(d);
//        return d;
//    }

    public Bindings newBindings(Term[] operationArguments) {
        // copy over all arguments
        Term[] scriptArguments;
        scriptArguments = new Term[operationArguments.length-1];
        System.arraycopy(operationArguments, 1, scriptArguments, 0, operationArguments.length-1);


        Bindings bindings = new SimpleBindings();
        bindings.put("global", global);
        bindings.put("js", this);
        bindings.put("arg", scriptArguments);
        bindings.put("memory", getMemory());
        bindings.put("nar", nar);

        return bindings;
    }

    @Override public Object function(Term[] args) {
        if (args.length < 1) {
            return null;
        }
        
        if (js == null) {
            ScriptEngineManager factory = new ScriptEngineManager();
            js = factory.getEngineByName("JavaScript");
        }
        

        Bindings bindings = newBindings(args);


        
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

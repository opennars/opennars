package nars.op.software;

import nars.NAR;
import nars.nal.nal8.Execution;
import nars.nal.nal8.Operator;
import nars.nal.nal8.operator.NullOperator;
import nars.nal.nal8.operator.TermFunction;
import nars.op.mental.Mental;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compile.TermBuilder;
import nars.term.compound.Compound;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.HashMap;

/**
 * Executes a Javascript expression
 */
public class js extends TermFunction implements Mental {

    private static final ThreadLocal<ScriptEngine> js = new ThreadLocal<ScriptEngine>() {
        @Override
        protected ScriptEngine initialValue() {
            ScriptEngineManager factory = new ScriptEngineManager();
            js.set(factory.getEngineByName("JavaScript"));
            return js.get();
        }
    };

    final HashMap global = new HashMap();

    class DynamicFunction extends TermFunction {

        private final String function;
        private Object fnCompiled;

        public DynamicFunction(String name, String function) {
            super(name);
            this.function = function;

            try {
                fnCompiled = js.get().eval(function);
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        @Override public Object function(Compound o, TermBuilder i) {
            Term[] args = Operator.opArgsArray(o);
            Bindings bindings = newBindings(null, args);
            bindings.put("_o", fnCompiled);

            Object result;
            try {
                String input = "_o.apply(this,arg)";
                result = js.get().eval(input, bindings);
            } catch (Throwable ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            return result;
        }


    }

    /** create dynamic javascript functions */
    //TODO make this an ImmediateOperator that will not conceptualize its subterms
    public class jsop extends NullOperator {

        @Override
        public void execute(Execution e) {
            Task op = e.task;
            Term[] x = Operator.opArgsArray(op.term());
            String funcName = Atom.unquote(x[0]);
            String functionCode = Atom.unquote(x[1]);
            //nar.input( echo.newTask("JS Operator Bind: " + funcName + " = " + functionCode));
            DynamicFunction d = new DynamicFunction(funcName, functionCode);
            e.nar.onExec(d);

            //op.stop();
        }



    }

//    public class JSBelievedConceptBuilder extends ConstantConceptBuilder {
//
//        private Object fnCompiled;
//
//        public JSBelievedConceptBuilder(String fnsource) {
//
//            ensureJSLoaded();
//
//            try {
//                this.fnCompiled = js.eval(fnsource);
//            }
//            catch (Throwable ex) {
//                ex.printStackTrace();
//            }
//        }
//
//
//        @Override
//        protected Truth truth(Term t, Memory m) {
//
//            Bindings bindings = new SimpleBindings();
//            bindings.put("t", t);
//            bindings.put("_o", fnCompiled);
//            String input = "_o.apply(this,[t])";
//
//            Object result;
//            try {
//                result = js.eval(input, bindings);
//            } catch (Throwable ex) {
//                ex.printStackTrace();
//                throw new RuntimeException(ex);
//            }
//
//            if (result instanceof Number) {
//                return new DefaultTruth(((Number)result).floatValue(), 0.99f);
//            }
//            if (result instanceof Object[]) {
//                if (((Object[])result).length > 1) {
//                    Object a = ((Object[])result)[0];
//                    Object b = ((Object[])result)[1];
//                    if ((a instanceof Number) && (b instanceof Number)) {
//                        return new DefaultTruth(((Number) a).floatValue(), ((Number) b).floatValue());
//                    }
//                }
//            }
//
//            return null;
//        }
//    }


//    /** create dynamic javascript functions */
//    public class jsbelief extends NullOperator {
//
//
//        @Override
//        public List<Task> apply(Operation op) {
//            Term[] x = op.args();
//
//            String functionCode = Atom.unquote(x[0]);
//
//            nar.on(new JSBelievedConceptBuilder(functionCode));
//
//            op.stop();
//
//            return null;
//        }
//
//    }

//
//    @Override
//    public boolean setEnabled(NAR n, boolean enabled) {
//        //this is a plugin which attches additional plugins. kind of messy, this will change
//        boolean x = super.setEnabled(n, enabled);
//        if (enabled) {
//            n.onExec(new jsop());
//            //n.on(new jsbelief());
//        }
//        return x;
//    }


    public Bindings newBindings(NAR nar, Term[] args) {

        Bindings bindings = new SimpleBindings();
        bindings.put("global", global);
        bindings.put("js", this);
        bindings.put("arg", args);
        bindings.put("memory", nar);
        bindings.put("nar", nar);

        return bindings;
    }


    @Override public Object function(Compound o, TermBuilder i) {
        Term[] args = Operator.opArgsArray(o);
        if (args.length < 1) {
            return null;
        }

        // copy over all arguments
        Term[] scriptArguments = new Term[args.length - 1];
        System.arraycopy(args, 1, scriptArguments, 0, args.length-1);

        Bindings bindings = newBindings(null /*TODO */, scriptArguments);


        
        String input = args[0].toString();
        if (input.charAt(0) == '"') {
            input = input.substring(1, input.length() - 1);
        }
        Object result;
        try {

            result = js.get().eval(input, bindings);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

}

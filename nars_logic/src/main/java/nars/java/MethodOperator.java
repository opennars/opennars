package nars.java;

import com.github.drapostolos.typeparser.TypeParser;
import nars.$;
import nars.Op;
import nars.nal.nal8.Execution;
import nars.nal.nal8.operator.TermFunction;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compile.TermBuilder;
import nars.term.compound.Compound;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by me on 8/19/15.
 */
public class MethodOperator extends TermFunction {


    static final TypeParser parser = TypeParser.newBuilder().build();

    private final Method method;
    private final Parameter[] params;

    private static final Object[] empty = new Object[0];
    private final AtomicBoolean enable;
    private final NALObjects context;
    boolean feedback = true;

    public static final Atom ERROR = Atom.the("ERR");
    private volatile Task currentTask = null;

    public MethodOperator(AtomicBoolean enable, Method m, NALObjects context) {
        super(getParentMethodName(m));

        this.context = context;
        method = m;
        params = method.getParameters();
        this.enable = enable;
    }

    private static Term getParentMethodName(Method m) {
        Class<?> sc = m.getDeclaringClass();

        String superClass = sc.getSimpleName();
        String methodName = m.getName();
        return $.the(superClass + '_' + methodName);
    }

    @Override
    public void execute(Execution e) {
        currentTask = e.task; //HACK

        super.execute(e);

        currentTask = null;
    }


    @Override
    public Object function(Compound o, TermBuilder ti) {
        Term[] x = o.terms();

        //System.out.println("method: " + method + " w/ " + x);

        if (!enable.get())
            return null;

        int pc = method.getParameterCount();
        int requires, paramOffset;
        if (Modifier.isStatic(method.getModifiers())) {
            requires = 1;
            paramOffset = 0;
        }
        else {
            requires = 1 + 1;
            paramOffset = 1;
        }

        if (x.length < requires)
            throw new RuntimeException("invalid argument count: needs " + requires + " but has " + Arrays.toString(x));

        Object instance = paramOffset == 0 ? null : context.object(x[0]);

        Object[] args;
        if (pc == 0) {
            args = empty;
        }
        else {
            args = new Object[pc];


            Term xv = x[paramOffset];
            if (!xv.op(Op.PRODUCT)) {
                //throw new RuntimeException("method parameters must be a product but is " + xv);
                return null;
            }

            Compound pxv = (Compound)xv;
            if (pxv.size()!=pc) {
                //throw new RuntimeException("invalid # method parameters; requires " + pc + " but " + pxv.size() + " given");
                return null;
            }

            for (int i = 0; i < pc; i++) {
                Object a = context.object(pxv.term(i));
                Class<?> pt = params[i].getType();
                if (!pt.isAssignableFrom(a.getClass())) {
                    a = parser.parseType(a.toString(), pt);
                }

                args[i] = a;
            }
        }

        try {

            //Object result = Invoker.invoke(instance, method.getName(), args); /** from Boon library */


            Object ll = currentTask.getLogLast();
            Object result = ll instanceof NALObjects.InvocationResult ? ((NALObjects.InvocationResult) ll).value : context.invokeVolition(currentTask, method, instance, args);

            if (feedback)
                return context.term(result);

        } catch (IllegalArgumentException e) {

//            System.err.println(e + ": " + Arrays.toString(args) + " for " + method);
//            //e.printStackTrace();
//
//            //create a task to never desire this
//            nar.goal(o, Tense.Present, 0.0f, 0.9f);
//
//            //return ERROR atom as feedback
//            return ERROR;
            e.printStackTrace();

        } catch (Exception e) {
            //System.err.println(method + " <- " + instance + " (" + instance.getClass() + " =?= " + method.getDeclaringClass() + "\n\t<<< " + Arrays.toString(args));
            //nar.memory.eventError.emit(e);
            return context.term(e);
        }

        return null;
    }

}

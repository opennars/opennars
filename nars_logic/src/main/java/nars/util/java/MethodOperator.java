package nars.util.java;

import com.github.drapostolos.typeparser.TypeParser;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.TermFunction;
import nars.term.Atom;
import nars.term.Term;

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

    private final Termizer termizer;
    private final static Object[] empty = new Object[0];
    private final AtomicBoolean enable;
    boolean feedback = true;

    public static final Atom ERROR = Atom.the("ERR");

    public MethodOperator(AtomicBoolean enable, Termizer termizer, Method m) {
        super(m.getDeclaringClass().getSimpleName() + "_" + m.getName());


        this.method = m;
        this.params = method.getParameters();

        this.termizer = termizer;
        this.enable = enable;
    }


    @Override
    public Object function(Operation o) {
        Term[] x = o.args();

        //System.out.println("method: " + method + " w/ " + x);

        if (!enable.get())
            return null;

        int pc = method.getParameterCount();
        final int requires, paramOffset;
        if (Modifier.isStatic(method.getModifiers())) {
            requires = pc;
            paramOffset = 0;
        }
        else {
            requires = pc + 1;
            paramOffset = 1;
        }

        if (x.length < requires)
            throw new RuntimeException("invalid argument count: needs " + requires);

        final Object instance;
        if (paramOffset == 0)
            instance = null;
        else {
            instance = termizer.object(x[0]);
        }

        final Object[] args;
        if (pc == 0) {
            args = empty;
        }
        else {
            args = new Object[pc];



            for (int i = 0; i < args.length; i++) {
                Object a = termizer.object(x[i + paramOffset]);
                Class<?> pt = params[i].getType();
                if (!pt.isAssignableFrom(a.getClass())) {
                    a = parser.parseType(a.toString(), pt);
                }

                args[i] = a;
            }
        }

        try {

            //Object result = Invoker.invoke(instance, method.getName(), args); /** from Boon library */

            Object result = method.invoke(instance, args);
            if (feedback)
                return termizer.term(result);
        } catch (IllegalArgumentException e) {

            System.err.println(e + ": " + Arrays.toString(args) + " for " + method);

            //create a task to never desire this
            nar.goal(o, 0.0f, 0.9f);

            //return ERROR atom as feedback
            return ERROR;

        } catch (Exception e) {
            System.err.println(method + " <- " + instance + " (" + instance.getClass() + " =?= " + method.getDeclaringClass() + "\n\t<<< " + Arrays.toString(args));
            nar.memory.eventError.emit(e);
            return termizer.term(e);
        }

        return null;
    }

}

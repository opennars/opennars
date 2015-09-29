package nars.util.java;

import nars.nal.nal8.operator.TermFunction;
import nars.term.Term;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by me on 8/19/15.
 */
public class MethodOperator extends TermFunction {

    private final Method method;
    private final Termizer termizer;
    private final static Object[] empty = new Object[0];
    private final AtomicBoolean enable;
    boolean feedback = false;

    public MethodOperator(AtomicBoolean enable, Termizer termizer, Method m) {
        super(m.getDeclaringClass().getSimpleName() + "_" + m.getName());
        this.method = m;
        this.termizer = termizer;
        this.enable = enable;
    }


    @Override
    public Object function(Term... x) {
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
                args[i] = termizer.object(x[i + paramOffset]);
            }
        }

        try {

            //Object result = Invoker.invoke(instance, method.getName(), args); /** from Boon library */

            Object result = method.invoke(instance, args);
            if (feedback)
                return termizer.term(result);
        } catch (Exception e) {
            System.err.println(method + " <- " + instance + " (" + instance.getClass() + " =?= " + method.getDeclaringClass() + "\n\t<<< " + Arrays.toString(args));
            nar.memory.eventError.emit(e);
            e.printStackTrace();
            return termizer.term(e);
        }

        return null;
    }

}

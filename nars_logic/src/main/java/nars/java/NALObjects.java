package nars.java;

import com.gs.collections.api.map.MutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import nars.$;
import nars.Global;
import nars.NAR;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operator;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;


/**
 * Dynamic proxy for any POJO that intercepts specific
 * methods and generates reasoner events which can be
 * stored, input to one or more reasoners, etc..
 * <p>
 *
 * TODO option to include stack traces in conjunction with invocation
 *
 */
public class NALObjects extends DefaultTermizer implements Termizer, MethodHandler {

    private final NAR nar;
    final MutableMap<Class, ProxyFactory> proxyCache = new UnifiedMap().asSynchronized();

    //    final Map<Object, Term> instances = new com.google.common.collect.MapMaker()
//            .concurrencyLevel(4).weakKeys().makeMap();
    //final HashBiMap<Object,Term> instances = new HashBiMap();

    final Map<Method,MethodOperator> methodOps = Global.newHashMap();



    public static Set<String> methodExclusions = new HashSet<String>() {{
        add("hashCode");
        add("notify");
        add("notifyAll");
        add("wait");
        add("finalize");
        add("stream");
        add("getHandler");
        add("setHandler");
    }};

    private final AtomicBoolean goalInvoke = new AtomicBoolean(true);

    /** for externally-puppeted method invocation goals */
    private float invocationGoalFreq = 1.0f;
    private float invocationGoalConf = 0.9f;

//    /** for method invocation result beliefs  */
//    private float invocationResultFreq = 1f;
//    private float invocationResultConf = 0.9f;

    /** for meta-data beliefs about (classes, objects, packages, etc..) */
    private float metadataBeliefFreq = 1.0f;
    private float metadataBeliefConf = 0.99f;
    private float metadataPriority = 0.1f;


    public NALObjects(NAR n) {
        nar = n;
    }

    public static <N extends NAR> N wrap(N n) throws Exception {
        NALObjects nalObjects = new NALObjects(n);
        return nalObjects.wrap("this", n);
    }

    @Override
    protected Term termClassInPackage(Term classs, Term packagge) {
        Term t = $.inst(classs, packagge);
        nar.believe(metadataPriority, t,
                Tense.ETERNAL,
                metadataBeliefFreq, metadataBeliefConf);
        return t;
    }


    @Override
    protected void onInstanceOfClass(Object o, Term oterm, Term clas) {
        /** only point to type if non-numeric? */
        //if (!Primitives.isWrapperType(instance.getClass()))

        //nar.believe(Instance.make(oterm, clas));
    }

    protected void onInstanceOfClass(Term identifier, Term clas) {
        nar.believe(metadataPriority, $.inst(identifier, clas),
            Tense.ETERNAL,
            metadataBeliefFreq, metadataBeliefConf);
    }

    @Override
    protected void onInstanceChange(Term oterm, Term prevOterm) {

        Term s = $.sim(oterm, prevOterm);
        if (s instanceof Compound)
            nar.believe(metadataPriority, ((Compound)s),
                Tense.ETERNAL,
                metadataBeliefFreq, metadataBeliefConf);

    }

    final AtomicBoolean lock = new AtomicBoolean(false);

    /** non-null if the method is being invoked by NARS,
     * in which case it will reference the task that invoked
     * feedback will be handled by the responsible MethodOperator's execution */
    final AtomicReference<Task> volition = new AtomicReference();


//    /** when a proxy wrapped instance method is called, this can
//     *  parametrically intercept arguments and return value
//     *  and input them to the NAL in narsese.
//     */
//    @Override
//    public Object invoke(Object object, Method overridden, Method forwarder, Object[] args) throws Throwable {
//        Object result = forwarder.invoke(object, args);
//        return invoked( object, overridden, args, result);
//    }

    public static class InvocationResult {
        public final Term value;

        public InvocationResult(Term value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Puppet";
        }
    }

    //TODO run in separate execution context to avoid synchronized
    public synchronized Object invoked(Object object, Method method, Object[] args, Object result) {

        if (methodExclusions.contains(method.getName()))
            return result;

        if (!lock.compareAndSet(false,true)) {
            return result;
        }

        Operator op = getMethodOperator(method);


        Compound invocationArgs = getMethodInvocationTerms(method, object, args);



        Term effect;
        effect = result != null ? term(result) : VOID;

        //TODO re-use static copy for 'VOID' instances
        InvocationResult ir = new InvocationResult(effect);

        Task volitionTask = volition.get();

        if (volitionTask == null) {

            /** pretend as if it were a goal of its own volition, although it was invoked externally
             *  Master of puppets, I'm pulling your strings */
            nar.input( $.goal( $.oper(op, invocationArgs),
                    invocationGoalFreq, invocationGoalConf).
                    present(nar.memory).
                    because(ir)
            );

//            nar.input(
//                new FluentTask(Operation.result(op, invocationArgs, effect)).
//                        belief().
//                        truth(invocationResultFreq, invocationResultConf).
//                        present(nar.memory).parent(g).
//                        budget(g.getBudget()).
//                        because("External Invocation")
//                    );
        }
        else {
            //feedback will be returned via operation execution
            //System.out.println("VOLITION " + volitionTask);
        }


        lock.set(false);

        return result;
    }

    private Compound getMethodInvocationTerms(Method method, Object instance, Object[] args) {

        //TODO handle static methods

        boolean isVoid = method.getReturnType() == void.class;

        Term[] x = new Term[isVoid ? 2 : 3];
        x[0] = term(instance);
        x[1] = $.p(terms(args));
        if (!isVoid) {
            x[2] = $.varDep("returnValue");
        }
        return $.p(x);
    }

    private Term[] terms(Object[] args) {
        //TODO use direct array creation, not Stream
        return Stream.of(args).map(this::term).toArray(Term[]::new);
    }

    public static Operator getMethodOperator(Method overridden) {
        //dereference class to origin, not using a wrapped class
        Class c = overridden.getDeclaringClass();

        //HACK
        if (c.getName().contains("_$$_")) ////javassist wrapper class
            c = c.getSuperclass();

        return Operator.the(
            c.getSimpleName() + '_' + overridden.getName()
        );
    }

//    //TODO use a generic Consumer<Task> for recipient/recipients of these
//    public final NAR nar;
//
//    public NALProxyMethodHandler(NAR n /* options */) {
//
//    }
    //    private final List<NALObjMethodHandler> methodHandlers = Global.newArrayList();
//
//    public NALObject() {
//    }
//
//    public NALObject add(NALObjMethodHandler n) {
//        methodHandlers.add(n);
//        return this;
//    }
//

    public <T> T wrap(String id, Class<? extends T> instance) throws Exception {
        //TODO avoid creating 't' because it will not be used. create the proxy class directly from the class
        T t = instance.newInstance();
        return wrap(id, t);
    }

    /** the id will be the atom term label for an instance.
     *  the instance should not be used because a new instance
     *  will be created and its fields will be those
     *  which are manipulated, not the original prototype.
     * */
    public <T> T wrap(String id, T instance) throws Exception {

        return wrap(id, (Class<? extends T>)instance.getClass(), instance);

    }

    public synchronized Object invokeVolition(Task currentTask, Method method, Object instance, Object[] args) {

        Object result = null;

        volition.set(currentTask);

        try {
            result = method.invoke(instance, args);
        } catch (Exception e) {
            result = e;
        }

        volition.set(null);

        return result;
    }


//    final class DelegateHandler<X> implements MethodHandler {
//
//        private final X obj;
//
//        public DelegateHandler(X n) {
//            this.obj = n;
//        }
//
//        @Override public final Object invoke(Object o, Method method, Method method1, Object[] objects) throws Throwable {
//            final X obj = this.obj;
//            Object result = method.invoke(obj, objects);
//            return invoked( obj, method, objects, result);
//        }
//    }

    @Override public final Object invoke(Object obj, Method wrapped, Method wrapper, Object[] args) throws Throwable {
        Object result = wrapper.invoke(obj, args);
        return invoked( obj, wrapped, args, result);
    }

//    public <T> T build(String id, Class<? extends T> classs) throws Exception {
//        return build(id, classs, null);
//    }

    /** the id will be the atom term label for the created instance */
    public <T> T wrap(String id, Class<? extends T> classs, /* nullable */ T instance) throws Exception {


        ProxyFactory factory = proxyCache.getIfAbsentPut(classs, ProxyFactory::new);
        factory.setSuperclass(classs);

        Class clazz = factory.createClass();

        T wrappedInstance = (T) clazz.newInstance();


        Atom identifier = Atom.the(id);
        //instances.put(identifier, wrappedInstance);

        map(identifier, wrappedInstance);

//        ((ProxyObject) wrappedInstance).setHandler(
////                delegate == null ?
////                this :
//                new DelegateHandler<>(delegate)
//        );
        ((ProxyObject) wrappedInstance).setHandler(this);


        //add operators for public methods

        for (Method m :  instance.getClass().getMethods()) {
            if (isMethodVisible(m) && Modifier.isPublic(m.getModifiers())) {
                methodOps.computeIfAbsent(m, M -> {
                    MethodOperator mo = new MethodOperator(goalInvoke, M, this);
                    nar.onExec(mo);
                    return mo;
                });
            }
        }

        onInstanceOfClass(identifier, term(classs));

        return wrappedInstance;
    }

    public static boolean isMethodVisible(Method m) {
        String n = m.getName();
        if (n.contains("_d"))
            return false; //javassist wrapper method

        if (m.getDeclaringClass() == Object.class)
            return false;

        return !methodExclusions.contains(n);
    }

    public void setGoalInvoke(boolean b) {
        goalInvoke.set(b);
    }


//    @Override
//    public Term term(Object o) {
//        Term i = instances.get(o);
//        if (i!=null)
//            return i;
//        return super.term(o);
//    }


}

package nars.java;

import com.gs.collections.api.map.MutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import nars.$;
import nars.Global;
import nars.NAR;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Instance;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
public class NALObjects extends DefaultTermizer implements MethodHandler, Termizer {



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
    private float invocationGoalFreq = 1f;
    private float invocationGoalConf = 0.9f;

    /** for method invocation result beliefs  */
    private float invocationResultFreq = 1f;
    private float invocationResultConf = 0.9f;

    /** for meta-data beliefs about (classes, objects, packages, etc..) */
    private float metadataBeliefFreq = 1f;
    private float metadataBeliefConf = 0.9f;


    public NALObjects(NAR n) {
        this.nar = n;
    }

    public static <N extends NAR> N wrap(N n) throws Exception {
        final NALObjects nalObjects = new NALObjects(n);
        return nalObjects.build("this", n);
    }

    @Override
    protected Term termClassInPackage(Term classs, Term packagge) {
        Inheritance<SetExt<Term>, Term> t = $.inst(classs, packagge);
        nar.believe(t, metadataBeliefFreq, metadataBeliefConf);
        return t;
    }


    @Override
    protected void onInstanceOfClass(Object o, Term oterm, Term clas) {
        /** only point to type if non-numeric? */
        //if (!Primitives.isWrapperType(instance.getClass()))

        //nar.believe(Instance.make(oterm, clas));
    }

    protected void onInstanceOfClass(Term identifier, Term clas) {
        nar.believe(Instance.make(identifier, clas),
            metadataBeliefFreq, metadataBeliefConf);
    }

    @Override
    protected void onInstanceChange(Term oterm, Term prevOterm) {
        Compound c = Task.taskable( Similarity.make(oterm, prevOterm));
        if (c!=null)
            nar.believe(c,
                metadataBeliefFreq, metadataBeliefConf);

    }

    final AtomicBoolean lock = new AtomicBoolean(false);

    /** when a proxy wrapped instance method is called, this can
     *  parametrically intercept arguments and return value
     *  and input them to the NAL in narsese.
     */
    @Override
    public Object invoke(Object object, Method overridden, Method forwarder, Object[] args) throws Throwable {

        Object result = forwarder.invoke(object, args);

        return invoked(object, overridden, args, result);
    }


    public Object invoked(Object object, Method method, Object[] args, Object result) {

        //synchronized?

        if (methodExclusions.contains(method.getName()))
            return result;

        if (!lock.compareAndSet(false,true)) {
            return result;
        }

        final Operator op = getMethodOperator(method);


        Product invocationArgs = getMethodInvocationTerms(method, object, args);

        nar.goal(
                $.oper(op, invocationArgs),
                Tense.Present,
                invocationGoalFreq, invocationGoalConf);

        Term effect;
        if (result!=null) {
            effect = term(result);
        }
        else {
            effect = VOID;
        }


        //TODO use task of callee as Parent task, if self-invoked
        nar.believe(
                Operation.result(op, invocationArgs, effect),
                Tense.Present,
                invocationResultFreq, invocationResultConf);


        lock.set(false);

        return result;
    }

    private final Product getMethodInvocationTerms(Method method, Object instance, Object[] args) {

        //TODO handle static methods

        boolean isVoid = method.getReturnType() == void.class;

        Term[] x = new Term[isVoid ? 2 : 3];
        x[0] = term(instance);
        x[1] = $.pro(terms(args));
        if (!isVoid) {
            x[2] = $.varDep("returnValue");
        }
        return $.pro(x);
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
            c.getSimpleName() + "_" + overridden.getName()
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

    /** the id will be the atom term label for existing instance */
    public <T> T build(String id, T instance) throws Exception {

        return build(id, (Class<? extends T>)instance.getClass(), instance);

    }


    final class DelegateHandler<X> implements MethodHandler {

        private final X obj;

        public DelegateHandler(X n) {
            this.obj = n;
        }

        @Override public final Object invoke(Object o, Method method, Method method1, Object[] objects) throws Throwable {
            final X obj = this.obj;
            Object result = method.invoke(obj, objects);
            return invoked(obj, method, objects, result);
        }
    }

    public <T> T build(String id, Class<? extends T> classs) throws Exception {
        return build(id, classs, null);
    }

    /** the id will be the atom term label for the created instance */
    public <T> T build(String id, Class<? extends T> classs, /* nullable */ T delegate) throws Exception {


        ProxyFactory factory = proxyCache.getIfAbsentPut(classs, ProxyFactory::new);
        factory.setSuperclass(classs);

        Class clazz = factory.createClass();

        T instance = (T) clazz.newInstance();

        Atom identifier = Atom.the(id);
        instances.put(identifier, instance);
        objects.put(instance, identifier);

        ((ProxyObject) instance).setHandler(
                delegate == null ?
                this :
                new DelegateHandler<>(delegate)
        );


        //add operators for public methods

        for (Method m :  instance.getClass().getMethods()) {
            if (isMethodVisible(m) && Modifier.isPublic(m.getModifiers())) {
                MethodOperator op = methodOps.computeIfAbsent(m, _m -> {
                    MethodOperator mo = new MethodOperator(goalInvoke, this, m);
                    nar.onExec(mo);
                    return mo;
                });
            }
        }

        onInstanceOfClass(identifier, term(classs));

        return instance;
    }

    public static boolean isMethodVisible(Method m) {
        String n = m.getName();
        if (n.contains("_d"))
            return false; //javassist wrapper method

        return !methodExclusions.contains(n);
    }

    public void setGoalInvoke(boolean b) {
        this.goalInvoke.set(b);
    }


//    @Override
//    public Term term(Object o) {
//        Term i = instances.get(o);
//        if (i!=null)
//            return i;
//        return super.term(o);
//    }


}

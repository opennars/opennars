package nars.java;

import com.gs.collections.api.map.MutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import nars.$;
import nars.Global;
import nars.NAR;
import nars.Symbols;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Instance;
import nars.nal.nal2.Similarity;
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
    }};
    private AtomicBoolean goalInvoke = new AtomicBoolean(true);


    public NALObjects(NAR n) {
        this.nar = n;
    }

    public static <N extends NAR> N wrap(N n) throws Exception {
        final NALObjects nalObjects = new NALObjects(n);
        return nalObjects.build("this", n);
    }

    @Override
    protected Term termClassInPackage(Term classs, Term packagge) {
        Inheritance t = Instance.make(classs, packagge);
        nar.believe(t);
        return t;
    }



    @Override
    protected void onInstanceOfClass(Object instance, Term oterm, Term clas) {
        /** only point to type if non-numeric? */
        //if (!Primitives.isWrapperType(instance.getClass()))

        //nar.believe(Instance.make(oterm, clas));
    }

    @Override
    protected void onInstanceChange(Term oterm, Term prevOterm) {
        Compound c = Task.taskable( Similarity.make(oterm, prevOterm));
        if (c!=null)
            nar.believe(c);

    }

    AtomicBoolean lock = new AtomicBoolean(false);

    /** when a proxy wrapped instance method is called, this can
     *  parametrically intercept arguments and return value
     *  and input them to the NAL in narsese.
     */
    @Override
    public Object invoke(Object object, Method overridden, Method forwarder, Object[] args) throws Throwable {

        Object result = forwarder.invoke(object, args);

        return invoked(object, overridden, args, result);
    }

    public Object invoked(Object object, Method overridden, Object[] args, Object result) {
        if (methodExclusions.contains(overridden.getName()))
            return result;

        if (!lock.compareAndSet(false,true)) {
            return result;
        }


        final Term instance = term(object);
        final Term[] argterm = Stream.of(args).map(this::term).toArray(Term[]::new);

        //String opName =
        final Operator op = Operator.the(
                overridden.getDeclaringClass().getSimpleName() + "_" + overridden.getName()
        );

        Term[] instancePlusArgs = new Term[argterm.length+2];
        instancePlusArgs[0] = instance;
        System.arraycopy(argterm, 0, instancePlusArgs, 1, argterm.length);
        instancePlusArgs[instancePlusArgs.length-1] = Atom.the(Symbols.VAR_DEPENDENT + "1");


        nar.goal(
                $.op(Product.make(instancePlusArgs), op),
                Tense.Present,
                1f, 0.9f);


        Term effect;
        if (result!=null) {
            effect = term(result);
        }
        else {
            effect = VOID;
        }


        //TODO use task of callee as Parent task, if self-invoked
        nar.believe(
                Operation.result(op, Product.make(instancePlusArgs), effect),
                Tense.Present,
                1f, 0.9f);


        lock.set(false);

        return result;
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


    class DelegateHandler<X> implements MethodHandler {

        private final X obj;

        public DelegateHandler(X n) {
            this.obj = n;
        }

        @Override
        public Object invoke(Object o, Method method, Method method1, Object[] objects) throws Throwable {

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

        instances.put(Atom.the(id), instance);
        objects.put(instance, Atom.the(id));

        ((ProxyObject) instance).setHandler(
                delegate == null ?
                this :
                new DelegateHandler<>(delegate)
        );





        //add operators for public methods

        for (Method m :  instance.getClass().getMethods()) {
            if (!methodExclusions.contains(m.toString()) && Modifier.isPublic(m.getModifiers())) {
                MethodOperator op = methodOps.computeIfAbsent(m, _m -> {
                    MethodOperator mo = new MethodOperator(goalInvoke, this, m);
                    nar.on(mo);
                    return mo;
                });
            }
        }


        return instance;
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

package nars.util.bind;

import com.gs.collections.api.map.MutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import nars.NAR;
import nars.io.out.TextOutput;
import nars.nal.nal4.Product;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal8.Operation;
import nars.nar.Default;
import nars.term.Atom;
import nars.term.Term;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;


/**
 * Dynamic proxy for any POJO that intercepts specific
 * methods and generates reasoner events which can be
 * stored, input to one or more reasoners, etc..
 * <p>
 * http://bytebuddy.net/#/tutorial
 */
public class NALObjectBuilder implements MethodHandler {

    private final NAR nar;
    final MutableMap<Class, ProxyFactory> proxyCache = new UnifiedMap().asSynchronized();
    final Map<Object, Term> instances = new com.google.common.collect.MapMaker()
            .concurrencyLevel(4).weakKeys().makeMap();

    public NALObjectBuilder(NAR n) {
        this.nar = n;
    }

    /** when a proxy wrapped instance method is called, this can
     *  parametrically intercept arguments and return value
     *  and input them to the NAL in narsese.
     */
    @Override
    public Object invoke(Object self, Method overridden, Method forwarder,
                         Object[] args) throws Throwable {

        System.out.println("do something " + overridden.getName() + " " + Arrays.toString(args));


        Object result = forwarder.invoke(self, args);

        Term instance = instances.get(self);
        Term argterm = Atom.quote(Arrays.toString(args));

        Term effect;

        Term cause = Operation.make(Atom.the(overridden.getName()),
                Product.make(instance, argterm));

        if (result!=null) {
            effect = Atom.quote(result.toString());
        }
        else {
            effect = Atom.the("void");
        }

        nar.believe(Implication.make(cause, effect, TemporalRules.ORDER_FORWARD));
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
    public static class TestClass {

        public void set(double x) {

        }

        public void callable() {
            System.out.println("base call");
            //return Math.random();
        }

        public float function(int i) {
            return (float)(i * Math.PI);
        }
    }



    /** the id will be the atom term label for the created instance */
    public <T> T build(String id, Class<T> classs) throws Exception {

        ProxyFactory factory = proxyCache.getIfAbsentPut(classs, () -> new ProxyFactory());
        factory.setSuperclass(classs);


        Class clazz = factory.createClass();

        Object instance = clazz.newInstance();
        ((ProxyObject) instance).setHandler(this);

        instances.put(instance, Atom.the(id));

        return (T) instance;
    }

    public static void main(String[] args) throws Exception {

        NAR n = new NAR(new Default());
        TextOutput.out(n);

        TestClass tc = new NALObjectBuilder(n).build("myJavaObject", TestClass.class);

        tc.callable();
        tc.function(1);

        n.frame(4);



//        Class derivedClass = new NALObject().add(new TestHandler()).connect(TestClass.class, n);
//
//        System.out.println(derivedClass);
//
//        Object x = derivedClass.newInstance();
//
//        System.out.println(x);
//
//        ((TestClass)x).callable();
    }
}

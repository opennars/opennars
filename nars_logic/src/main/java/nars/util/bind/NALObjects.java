package nars.util.bind;

import com.gs.collections.api.map.MutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import nars.NAR;
import nars.nal.nal2.Instance;
import nars.nal.nal4.Product;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.term.Atom;
import nars.term.Term;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Dynamic proxy for any POJO that intercepts specific
 * methods and generates reasoner events which can be
 * stored, input to one or more reasoners, etc..
 * <p>
 * http://bytebuddy.net/#/tutorial
 */
public class NALObjects implements MethodHandler {

    private final NAR nar;
    final MutableMap<Class, ProxyFactory> proxyCache = new UnifiedMap().asSynchronized();
    final Map<Object, Term> instances = new com.google.common.collect.MapMaker()
            .concurrencyLevel(4).weakKeys().makeMap();


    final static Atom VOID = Atom.the("void");
    final static Atom NULL = Atom.the("null");

    public NALObjects(NAR n) {
        this.nar = n;
    }

    /** when a proxy wrapped instance method is called, this can
     *  parametrically intercept arguments and return value
     *  and input them to the NAL in narsese.
     */
    @Override
    public Object invoke(Object object, Method overridden, Method forwarder,
                         Object[] args) throws Throwable {

        Object result = forwarder.invoke(object, args);

        Term instance = instances.get(object);
        Term argterm = termize(args);

        Term effect;

        Term cause = Operation.make(Atom.the(overridden.getName()),
                Product.make(instance, argterm));

        if (result!=null) {
            effect = termize(result);
        }
        else {
            effect = VOID;
        }

        nar.believe(Implication.make(cause, effect, TemporalRules.ORDER_FORWARD),
                Tense.Present,
                1f, 0.9f
        );
        return result;
    }

    public Term termize(Object o) {
        if (o == null) return NULL;
        if (o instanceof String) {
            return Atom.the((String)o, true);
        }
        else if (o  instanceof Number) {
            return Atom.the((Number)o);
        }
        if (o instanceof Object[]) {
            return Product.make(
                    Arrays.stream((Object[])o).map(e -> termize(e)).collect(Collectors.toList())
            );
        }
        else if (o instanceof Iterable) {
            return Product.make(
                    (Collection<Term>) ((Collection) o).stream().map(e -> termize(e)).collect(Collectors.toList())
            );
        /*} else if (o instanceof Stream) {
            return Atom.quote(o.toString().substring(17));
        }*/
        }

        String cname = o.getClass().toString().substring(6) /* "class " */;
        int slice = cname.length();

        String instanceName = o.toString();
        if (instanceName.length() > slice)
            instanceName = instanceName.substring(slice);

        return Instance.make(Atom.quote(instanceName), Atom.quote(cname));


        //return Atom.quote(o.toString());
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


}

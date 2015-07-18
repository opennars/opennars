package nars.util.bind;

import com.gs.collections.api.map.MutableMap;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import nars.NAR;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Instance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.term.Atom;
import nars.term.Term;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


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


    public static Set methodExclusions = new HashSet() {{
        add("hashCode");
    }};

    final static Atom TRUE = Atom.the("true");
    final static Negation FALSE = Atom.notThe("true");
    final static Atom VOID = Atom.the("void");
    final static Atom EMPTY = Atom.the("empty");
    final static Atom NULL = Atom.the("null");

    final Map<Package,Atom> packages = new HashMap();
    final Map<Class, Term> classes = new HashMap();
    final Map<Object, Term> objects = new HashMap();

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
        if (methodExclusions.contains(overridden.getName()))
            return result;

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
        if (o instanceof Boolean) {
            boolean b = ((Boolean) o).booleanValue();
            if (b) return TRUE;
            else return FALSE;
        }
        else if (o  instanceof Number) {
            return Atom.the((Number)o);
        }
        else if (o instanceof Class) {
            Class oc = (Class)o;
            String cname = oc.getSimpleName();
            if (cname.isEmpty()) cname = oc.getName();

            Package p = oc.getPackage();
            if (p!=null) {

                Term cterm = Atom.quote(cname);

                Atom pkg = packages.get(p);
                if (pkg == null) {
                    pkg = Atom.quote(p.getName());
                    packages.put(p, pkg);
                    nar.believe(Inheritance.make(cterm, pkg));
                }

                return cterm;
            }
            return Atom.the("primitive");
        }
        else if (o instanceof int[]) {
            final List<Term> arg = Arrays.stream((int[]) o).boxed().map(e -> termize(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return Product.make(
                    arg
            );
        }
        else if (o instanceof Object[]) {
            final List<Term> arg = Arrays.stream((Object[]) o).map(e -> termize(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return Product.make(
                    arg
            );
        }
        else if (o instanceof List) {
            Collection<Term> arg = (Collection<Term>) ((Collection) o).stream().map(e -> termize(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return Product.make(
                   arg
            );
        /*} else if (o instanceof Stream) {
            return Atom.quote(o.toString().substring(17));
        }*/
        }

        Term i = termizeInstance(o);

        return i;


//        //ensure package is term'ed
//        String pname = p.getName();
//        int period = pname.length()-1;
//        int last = period;
//        Term child = cterm;
//        while (( period = pname.lastIndexOf('.', period)) != -1) {
//            String parname = pname.substring(0, last);
//            Term parent = packages.get(parname);
//            if (parent == null) {
//                parent = Atom.the(parname);
//                nar.believe( Inheritance.make(child, parent) );
//                packages.put()
//                last = period;
//                child = parent;
//            }
//            else {
//                break;
//            }
//        }





    }

    private Term termizeInstance(Object o) {
        //        String cname = o.getClass().toString().substring(6) /* "class " */;
//        int slice = cname.length();
//
        //TODO decide to use toString or System object id
        String instanceName = o.toString();
//        if (instanceName.length() > slice)
//            instanceName = instanceName.substring(slice);

        final Term oterm = Atom.quote(instanceName);

        Term prevOterm = objects.put(o, oterm);
        if (prevOterm == null) {



            Term clas = classes.get(o.getClass());
            if (clas == null) {
                clas = termize(o.getClass());
            }

            nar.believe(Instance.make(oterm, clas));

        }
        else {
            if (!oterm.equals(prevOterm)) {
                //toString value has changed, create similarity to associate
                nar.believe(Similarity.make(oterm, prevOterm));
            }
        }

        return oterm;
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

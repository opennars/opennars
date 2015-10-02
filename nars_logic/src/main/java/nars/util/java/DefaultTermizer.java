package nars.util.java;

import com.gs.collections.impl.bimap.mutable.HashBiMap;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.term.Atom;
import nars.term.Term;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by me on 8/19/15.
 */
public class DefaultTermizer implements Termizer {


    final Map<Package, Term> packages = new HashMap();
    final Map<Class, Term> classes = new HashMap();


    final HashBiMap<Object, Term> objects = new HashBiMap<>();



    @Override
    public Object object(final Term t) {
        if (t == NULL) return null;

        Object x = objects.inverse().get(t);
//        if (x == null) {
//            //compute it
//            if (t instanceof Atom) {
//                String s = t.toStringCompact();
//                try {
//                    return Integer.parseInt(s);
//                } catch (Exception e) {
//                }
//
//                x = s;
//            } else {
//                //TODO handle Products as lists/array, etc
//                x = null;
//            }
//
//            objects.put(x, t);
//        }
        if (x == null)
            return t; /** return the term intance itself */
        return x;
    }


    Term obj2term(Object o) {

        if (o == null)
            return NULL;

        if (o instanceof String)
            return Atom.the((String) o, true);

        if (o instanceof Boolean)
            return ((Boolean) o) ? TRUE : FALSE;

        if (o instanceof Number)
            return Atom.the((Number) o);

        if (o instanceof Class) {
            Class oc = (Class) o;

            Package p = oc.getPackage();
            if (p != null) {

                Term cterm = termClassInPackage(oc);

                Term pkg = packages.get(p);
                if (pkg == null) {
                    pkg = termPackage(p);
                    packages.put(p, pkg);
                    termClassInPackage(cterm, pkg);
                }

                return cterm;
            }
            return Atom.the("primitive");
        }

        if (o instanceof int[]) {
            final List<Term> arg = Arrays.stream((int[]) o)
                    .mapToObj(e -> Atom.the(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return Product.make(
                    arg
            );
        } else if (o instanceof Object[]) {
            final List<Term> arg = Arrays.stream((Object[]) o).map(e -> term(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return Product.make(
                    arg
            );
        } else if (o instanceof List) {
            Collection<Term> arg = (Collection<Term>) ((Collection) o).stream().map(e -> term(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return Product.make(
                    arg
            );
        /*} else if (o instanceof Stream) {
            return Atom.quote(o.toString().substring(17));
        }*/
        } else if (o instanceof Set) {
            Collection<Term> arg = (Collection<Term>) ((Collection) o).stream().map(e -> term(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return SetExt.make(arg);
        } else if (o instanceof Map) {
            Collection<Term> arg = (Collection<Term>) ((Collection) o).stream().map(e -> term(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return SetExt.make(arg);
        }

        return termInstanceInClassInPackage(o);


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

    public static Term termClass(Class c) {
        return Atom.the(c.getSimpleName());
    }

    public static Term termClassInPackage(Class c) {
        return Product.make(
            termClass(c),
            termPackage(c.getPackage())
        );
    }

    public static Term termPackage(Package p) {
        return Atom.the(p.getName());
    }


    public static Term termInstanceInClassInPackage(Object o) {
        //return o.getClass().getName() + '@' + Integer.toHexString(o.hashCode());
        //return o.getClass() + "_" + System.identityHashCode(o)
        return Product.make(
                    termPackage(o.getClass().getPackage()),
                    termClassInPackage(o.getClass()),
                    Atom.the(System.identityHashCode(o), 36)
                );
    }

    protected Term termClassInPackage(Term classs, @Deprecated Term packagge) {
        //TODO ??
        return null;
    }

    public Term term(final Object o) {
        if (o == null) return NULL;

        //        String cname = o.getClass().toString().substring(6) /* "class " */;
//        int slice = cname.length();
//
        Runnable[] post = new Runnable[1];

        Term result = objects.computeIfAbsent(o, O -> {

            Term oterm = obj2term(o);

            Term clas = classes.computeIfAbsent(o.getClass(), this::obj2term);

            final Term finalClas = clas;
            post[0] = () ->  onInstanceOfClass(o, oterm, finalClas);

            return oterm;
        });

        if (result!=null)
            if (post[0]!=null)
                post[0].run();

        return result;


        //TODO decide to use toString or System object id
        //String instanceName = o.toString();
//        if (instanceName.length() > slice)
//            instanceName = instanceName.substring(slice);

        //final Term oterm = Atom.quote(instanceName);

//        Term prevOterm = objects.put(o, oterm);
        //if (prevOterm == null) {


        //}
//        else {
//            if (!oterm.equals(prevOterm)) {
//                //toString value has changed, create similarity to associate
//                onInstanceChange(oterm, prevOterm);
//            }
//        }

        //return oterm;
    }


    protected void onInstanceChange(Term oterm, Term prevOterm) {

    }

    protected void onInstanceOfClass(Object o, Term oterm, Term clas) {

    }

    public int numInstances() {
        return objects.size();
    }
}

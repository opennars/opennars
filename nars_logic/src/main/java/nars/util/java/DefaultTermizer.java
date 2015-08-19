package nars.util.java;

import com.gs.collections.impl.bimap.mutable.HashBiMap;
import nars.nal.nal4.Product;
import nars.term.Atom;
import nars.term.Term;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by me on 8/19/15.
 */
public class DefaultTermizer implements Termizer {


    final Map<Package,Atom> packages = new HashMap();
    final Map<Class, Term> classes = new HashMap();
    final HashBiMap<Object, Term> objects = new HashBiMap();


    @Override
    public Object object(final Term _t) {
        return objects.inverse().computeIfAbsent(_t, t -> {
            //compute it
            if (t instanceof Atom) {
                String s = t.toStringCompact();
                try {
                    return Integer.parseInt(s);
                }
                catch (Exception e) {  }

                return s;
            }
            else {
                //TODO handle Products as lists/array, etc
                return null;
            }
        });
    }


    public Term term(Object o) {
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
                    onClassInPackage(cterm, pkg);
                }

                return cterm;
            }
            return Atom.the("primitive");
        }
        else if (o instanceof int[]) {
            final List<Term> arg = Arrays.stream((int[]) o).boxed().map(e -> term(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return Product.make(
                    arg
            );
        }
        else if (o instanceof Object[]) {
            final List<Term> arg = Arrays.stream((Object[]) o).map(e -> term(e)).collect(Collectors.toList());
            if (arg.isEmpty()) return EMPTY;
            return Product.make(
                    arg
            );
        }
        else if (o instanceof List) {
            Collection<Term> arg = (Collection<Term>) ((Collection) o).stream().map(e -> term(e)).collect(Collectors.toList());
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

    protected void onClassInPackage(Term classs, Atom packagge) {


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
                clas = term(o.getClass());
            }

            onInstanceOfClass(oterm, clas);

        }
        else {
            if (!oterm.equals(prevOterm)) {
                //toString value has changed, create similarity to associate
                onInstanceChange(oterm, prevOterm);
            }
        }

        return oterm;
    }






    protected void onInstanceChange(Term oterm, Term prevOterm) {

    }

    protected void onInstanceOfClass(Term oterm, Term clas) {

    }
}

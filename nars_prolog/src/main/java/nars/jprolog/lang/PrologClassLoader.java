package nars.jprolog.lang;

import java.io.Serializable;

/**
 * Prolog class loader.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class PrologClassLoader extends ClassLoader implements Serializable {

    public PrologClassLoader() {
        super();
    }

    public PrologClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Returns a <code>java.lang.Class</code> object associated with the predicate
     * class with the given arguments.
     * @param pkg package name
     * @param functor predicate name
     * @param arity predicate arity
     * @param resolve If <code>true</code> then resolve the class
     * @return a <code>java.lang.Class</code> object associated with the predicate
     * class that corresponds to <code>pkg:functor/arity</code>
     * if exists, otherwise throws <code>ClassNotFoundException</code>.
     * @exception ClassNotFoundException
     */
    public Class loadPredicateClass(String pkg, String functor, int arity,
                                    boolean resolve) throws ClassNotFoundException {
        return loadClass(PredicateEncoder.encode(pkg, functor, arity), resolve);
    }

    /**
     * Check whether the predicate class for the given arguments is defined.
     * @param pkg package name
     * @param functor predicate name
     * @param arity predicate arity
     * @return <code>true</code> if the predicate <code>pkg:functor/arity</code>
     * is defined, otherwise <code>false</code>.
     */
    public boolean definedPredicate(String pkg, String functor, int arity) {
        String cname = PredicateEncoder.encode(pkg, functor, arity);
        cname = cname.replace('.', '/') + ".class";
        java.net.URL url = getResource(cname);
        return url != null;
    }

    /**
    public Class findClass(String name) throws ClassNotFoundException {
    throw new ClassNotFoundException(name);
    }
    */
}

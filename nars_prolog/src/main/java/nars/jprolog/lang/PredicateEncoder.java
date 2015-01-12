package nars.jprolog.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * The <code>PredicateEncoder</code> class contains static methods for encoding predicate names.<br>
 * The predicate with <code>hoge:f/n</code> is encoded to <code>hoge.PRED_f_n</code>, where
 * <code>hoge</code> is package name,
 * <code>f</code> is predicate name, and 
 * <code>n</code> is arity.<br>
 *
 * When encoding a predicate name, we apply the following rules:<br>
 *<ul>
 *<li>The alphanumeric characters
 * &ldquo;<code>a</code>&rdquo; through &ldquo;<code>z</code>&rdquo;, 
 * &ldquo;<code>A</code>&rdquo; through &ldquo;<code>Z</code>&rdquo; and 
 * &ldquo;<code>0</code>&rdquo; through &ldquo;<code>9</code>&rdquo; remain the same.
 *<li>The special characters &ldquo;<code>_</code>&rdquo; and &ldquo;<code>$</code>&rdquo; remain the same.
 *<li>All other characters are first converted into a list of character codes. 
 * Then each character code is represented by the 5-character string &ldquo;<code>$XYZW</code>&rdquo;, 
 * where <code>XYZW</code> is the four-digit hexadecimal representation of the character code.
 *</ul>
 *
 * For example,
 * a predicate with <code>hoge:(=..)/2</code> is encoded to <code>hoge.PRED_$003D$002E$002E_2</code>.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class PredicateEncoder {

    /**
     * Returns a string representation of class for
     * the predicate with the given arguments.
     * @param pkg package name
     * @param functor predicate name
     * @param arity predicate arity
     * @return a string representation of class for
     * the predicate that corresponds to <code>pkg:functor/arity</code>.
     */
    public static String encode(String pkg, String functor, int arity) {
	String x = functor;
	Pattern p = Pattern.compile("([^a-zA-Z0-9_'$'])");
	Matcher m = p.matcher(x);
	StringBuffer sb = new StringBuffer();
	boolean result = m.find();
	while (result) {
	    //	    m.appendReplacement(sb, String.format("\\$%2X", (int)(m.group().charAt(0))));
	    m.appendReplacement(sb, String.format("\\$%04X", (int)(m.group().charAt(0))));
	    result = m.find();
	}
	m.appendTail(sb);
	x = sb.toString();
	if (pkg.equals("user"))
	    return "PRED_" + x + "_" + arity;
	else
	    return pkg + ".PRED_" + x + "_" + arity;
    }

    /**
     * Returns a <code>java.lang.Class</code> object associated with the predicate
     * class with the given arguments.
     * @param pkg package name
     * @param functor predicate name
     * @param arity predicate arity
     * @return a <code>java.lang.Class</code> object associated with the predicate
     * class that corresponds to <code>pkg:functor/arity</code>
     * if exists, otherwise <code>null</code>.
     * @deprecated As of Prolog Cafe 1.1, replaced by {@link PrologClassLoader#loadPredicateClass(String,String,int,boolean)};
     */
    public static Class getClass(String pkg, String functor, int arity) {
	String className = PredicateEncoder.encode(pkg, functor, arity);
	Class clazz = null;
	try {
	    clazz = Class.forName(className);
	} catch (ClassNotFoundException e) {}
	return clazz;
    }

    /**
     * Check whether the predicate class for the given arguments is defined.
     * @param pkg package name
     * @param functor predicate name
     * @param arity predicate arity
     * @return <code>true</code> if the predicate <code>pkg:functor/arity</code>
     * is defined, otherwise <code>false</code>.
     * @deprecated As of Prolog Cafe 1.1, replaced by {@link PrologClassLoader#definedPredicate(String,String,int)};
     */
    public static boolean defined(String pkg, String functor, int arity) {
	Class clazz = PredicateEncoder.getClass(pkg, functor, arity);
	return clazz != null;
    }

    public static void main(String argv[]) {
	String p = argv[0];
	String f = argv[1];
	int n = Integer.valueOf(argv[2]);
	System.out.println(p + ":" + f + "/" + n);
	System.out.println(PredicateEncoder.encode(p,f,n));
	System.out.println(PredicateEncoder.defined(p,f,n));
    }
}

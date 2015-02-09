package nars.scheme;

/**  @author Peter Norvig, peter@norvig.com http://www.norvig.com 
 * Copyright 1998 Peter Norvig, see http://www.norvig.com/license.html **/

import nars.logic.entity.Term;
import nars.util.data.sexpression.IPair;
import nars.util.data.sexpression.Pair;

import java.io.PrintWriter;

import static nars.util.data.sexpression.Pair.stringify;



public abstract class SchemeUtils  {


    /**
     * Same as Boolean.TRUE. *
     */
    public static final Boolean TRUE = Boolean.TRUE;
    /**
     * Same as Boolean.FALSE. *
     */
    public static final Boolean FALSE = Boolean.FALSE;

    public static final Double ZERO = 0.0;
    public static final Double ONE = 1.0;
    //////////////// Conversion Routines ////////////////

    // The following convert or coerce objects to the right type.

    /**
     * Convert boolean to Boolean. *
     */
    public static Boolean truth(boolean x) {
        return x ? TRUE : FALSE;
    }

    /**
     * Convert Scheme object to boolean.  Only #f is false, others are true. *
     */
    public static boolean truth(Object x) {
        return x != FALSE;
    }

    /**
     * Convert double to Double. Caches 0 and 1; makes new for others. *
     */
    public static Double num(double x) {
        return (x == 0.0) ? ZERO : (x == 1.0) ? ONE : new Double(x);
    }

    /**
     * Converts a Scheme object to a double, or calls error. *
     */
    public static double num(Object x) {
        if (x instanceof Number) return ((Number) x).doubleValue();
        else return num(error("expected a number, got: " + x));
    }

    /**
     * Converts a Scheme object to a char, or calls error. *
     */
    public static char chr(Object x) {
        if (x instanceof Character) return (Character) x;
        else return chr(error("expected a char, got: " + x));
    }

    /**
     * Converts a char to a Character. *
     */
    public static Character chr(char ch) {
        return ch;
    }

    /**
     * Coerces a Scheme object to a Scheme string, which is a char[]. *
     */
    public static char[] str(Object x) {
        if (x instanceof char[]) return (char[]) x;
        else return str(error("expected a string, got: " + x));
    }

    /**
     * Coerces a Scheme object to a Scheme symbol, which is a string. *
     */
    public static String sym(Object x) {
        if (x instanceof String) return (String) x;
        else return sym(error("expected a symbol, got: " + x));
    }

    /**
     * Coerces a Scheme object to a Scheme vector, which is a Object[]. *
     */
    public static Object[] vec(Object x) {
        if (x instanceof Object[]) return (Object[]) x;
        else return vec(error("expected a vector, got: " + x));
    }

    /**
     * Coerces a Scheme object to a Scheme input port, which is an InputPort.
     * If the argument is null, returns interpreter.input. *
     */
    public static InputPort inPort(Object x, Scheme interp) {
        if (x == null) return interp.input;
        else if (x instanceof InputPort) return (InputPort) x;
        else return inPort(error("expected an input port, got: " + x), interp);
    }

    /**
     * Coerces a Scheme object to a Scheme input port, which is a PrintWriter.
     * If the argument is null, returns System.out. *
     */
    public static PrintWriter outPort(Object x, Scheme interp) {
        if (x == null) return interp.output;
        else if (x instanceof PrintWriter) return (PrintWriter) x;
        else return outPort(error("expected an output port, got: " + x), interp);
    }

    //////////////// Error Routines ////////////////

    /**
     * A continuable error. Prints an error message and then prompts for
     * a value to eval and return. *
     */
    public static Object error(String message) {
        System.err.println("**** ERROR: " + message);
        throw new RuntimeException(message);
    }

    public static Object warn(String message) {
        System.err.println("**** WARNING: " + message);
        return "<warn>";
    }

    //////////////// Basic manipulation Routines ////////////////

    // The following are used throughout the code.

    /**
     * Like Common Lisp first; car of a Pair, or null for anything else. *
     */
    public static Object first(Object x) {
        return (x instanceof IPair) ? forScheme(((IPair) x).first()) : x;
    }

    /**
     * Like Common Lisp rest; car of a Pair, or null for anything else. *
     */
    public static Object rest(Object x) {
        return (x instanceof IPair) ? forScheme(((IPair) x).rest()) : null;
    }

    protected static Object forScheme(Object o) {
        if (o instanceof Term) {
            if (((Term) o).getComplexity()==1)
                return Primitive.stringToLiteralOrNumber(((Term) o).name().toString());
            return o;
        }
        return o;
    }


    /**
     * Like Common Lisp (setf (first ... *
     */
    public static Object setFirst(Object x, Object y) {
        if (x instanceof IPair)
            ((IPair) x).setFirst(y);
        else
            error("Attempt to set-car of a non-Pair:" + stringify(x));
        return y;
    }

    /**
     * Like Common Lisp (setf (rest ... *
     */
    public static Object setRest(Object x, Object y) {
        if (x instanceof IPair)
            ((IPair) x).setRest(y);
        else
            error("Attempt to set-cdr of a non-Pair:" + stringify(x));
        return y;
    }


    /**
     * Creates a two element list. *
     */
    public static IPair list(Object a, Object b) {
        return new Pair(a, new Pair(b, null));
    }

    /**
     * Creates a one element list. *
     */
    public static IPair list(Object a) {
        return new Pair(a, null);
    }

    /**
     * listStar(args) is like Common Lisp (apply #'list* args) *
     */
    public static Object listStar(Object args) {
        if (rest(args) == null) return first(args);
        else return cons(first(args), listStar(rest(args)));
    }

    /**
     * cons(x, y) is the same as new Pair(x, y). *
     */
    public static IPair cons(Object a, Object b) {
        return new Pair(a, b);
    }

    /**
     * Reverse the elements of a list. *
     */
    public static Object reverse(Object x) {
        Object result = null;
        while (x instanceof IPair) {
            result = cons(first(x), result);
            x = rest(x);
        }
        return result;
    }

    /**
     * Check if two objects are equal. *
     */
    public static boolean equal(Object x, Object y) {
        if (x == null || y == null) {
            return x == y;
        } else if (x instanceof char[]) {
            if (!(y instanceof char[])) return false;
            char[] xc = (char[]) x, yc = (char[]) y;
            if (xc.length != yc.length) return false;
            for (int i = xc.length - 1; i >= 0; i--) {
                if (xc[i] != yc[i]) return false;
            }
            return true;
        } else if (x instanceof Object[]) {
            if (!(y instanceof Object[])) return false;
            Object[] xo = (Object[]) x, yo = (Object[]) y;
            if (xo.length != yo.length) return false;
            for (int i = xo.length - 1; i >= 0; i--) {
                if (!equal(xo[i], yo[i])) return false;
            }
            return true;
        } else if (x instanceof IPair) {
            return pairEquals((IPair) x, y);
        } else if (y instanceof IPair) {
            return pairEquals((IPair) y, x);
        }

        return false;
    }

    /**
     * Check if two objects are == or are equal numbers or characters. *
     */
    public static boolean eqv(Object x, Object y) {
        return x == y
                || (x instanceof Double && x.equals(y))
                || (x instanceof Character && x.equals(y));
    }

    /**
     * The length of a list, or zero for a non-list. *
     */
    public static int length(Object x) {
        int len = 0;
        while (x instanceof IPair) {
            len++;
            x = ((IPair) x).rest();
        }
        return len;
    }

    /**
     * Convert a list of characters to a Scheme string, which is a char[]. *
     */
    public static char[] listToString(Object chars) {
        char[] str = new char[length(chars)];
        for (int i = 0; chars instanceof IPair; i++) {
            str[i] = chr(first(chars));
            chars = rest(chars);
        }
        return str;
    }

    /**
     * Convert a list of Objects to a Scheme vector, which is a Object[]. *
     */
    public static Object[] listToVector(Object objs) {
        Object[] vec = new Object[length(objs)];
        for (int i = 0; objs instanceof IPair; i++) {
            vec[i] = first(objs);
            objs = rest(objs);
        }
        return vec;
    }

    /**
     * Write the object to a port.  If quoted is true, use "str" and #\c,
     * otherwise use str and c. *
     */
    public static Object write(Object x, PrintWriter port, boolean quoted) {
        port.print(Pair.stringify(x, quoted));
        port.flush();
        return x;
    }

    /**
     * Convert a vector to a List. *
     */
    public static IPair vectorToList(Object x) {
        if (x instanceof Object[]) {
            Object[] vec = (Object[]) x;
            IPair result = null;
            for (int i = vec.length - 1; i >= 0; i--)
                result = cons(vec[i], result);
            return result;
        } else {
            error("expected a vector, got: " + x);
            return null;
        }
    }


    /**
     * Two pairs are equal if their first and rest fields are equal. *
     */
    public static boolean pairEquals(IPair p, Object x) {
        if (x == p) return true;
        else if (!(x instanceof IPair)) return false;
        else {
            IPair that = (IPair) x;
            return equal(p.first(), that.first())
                    && equal(p.rest(), that.rest());
        }
    }



    /**
     * For debugging purposes, prints output. *
     */
    static Object p(Object x) {
        System.out.println(stringify(x));
        return x;
    }

    /**
     * For debugging purposes, prints output. *
     */
    static Object p(String msg, Object x) {
        System.out.println(msg + ": " + stringify(x));
        return x;
    }
}

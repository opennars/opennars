package nars.util.data.sexpression;


import java.util.ArrayList;
import java.util.List;

/**
 * A Pair has two fields, first and rest (or car and cdr).
 * The empty list is represented by null. The methods that you might
 * expect here, like first, second, list, etc. are instead static methods
 * in class SchemeUtils.
 *
 * @author Peter Norvig, peter@norvig.com http://www.norvig.com
 *         Copyright 1998 Peter Norvig, see http://www.norvig.com/license.html
 */

public class Pair implements IPair {

    private Object first;

    private Object rest;

    /**
     * Build a pair from two components. *
     */
    public Pair(Object first, Object rest) {
        setFirst(first);
        setRest(rest);
    }


    /**
     * The first element of the pair. *
     */
    @Override
    public Object _car() {
        return first;
    }

    @Override
    public Object setFirst(Object first) {
        this.first = first;
        return first;
    }

    /**
     * The other element of the pair. *
     */
    @Override
    public Object _cdr() {
        return rest;
    }

    @Override
    public Object setRest(Object rest) {
        this.rest = rest;
        return rest;
    }


    /**
     * Return a String representation of the pair. *
     */
    public String toString() {
        return stringify(this, true);
    }

    /**
     * Like Common Lisp first; car of a Pair, or null for anything else. *
     */
    public static Object firstOfPairOrNull(Object x) {
        return (x instanceof IPair) ? ((IPair) x)._car() : null;
    }

    /**
     * Like Common Lisp second. *
     */
    public static Object second(Object x) {
        return firstOfPairOrNull(restOfPairOrNull(x));
    }

    /**
     * Like Common Lisp third. *
     */
    public static Object third(Object x) {
        return firstOfPairOrNull(restOfPairOrNull(restOfPairOrNull(x)));
    }


    /**
     * Convert a Scheme object to its printed representation, as
     * a java String (not a Scheme string). If quoted is true, use "str" and #\c,
     * otherwise use str and c. You need to pass in a StringBuffer that is used
     * to accumulate the results. (If the interface didn't work that way, the
     * system would use lots of little internal StringBuffers.  But note that
     * you can still call <tt>stringify(x)</tt> and a new StringBuffer will
     * be created for you. *
     */

    static void stringify(Object x, boolean quoted, StringBuffer buf) {
        //noinspection IfStatementWithTooManyBranches
        if (x == null)
            buf.append("()");
        else if (x instanceof Double) {
            double d = (Double) x;
            if (Math.round(d) == d) buf.append((long) d);
            else buf.append(d);
        } else if (x instanceof Character) {
            if (quoted) buf.append("#\\");
            buf.append(x);
        } else if (x instanceof IPair) {
            stringifyPair(((IPair) x), quoted, buf);
        } else if (x instanceof char[]) {
            char[] chars = (char[]) x;
            if (quoted) buf.append('"');
            for (char aChar : chars) {
                if (quoted && aChar == '"') buf.append('\\');
                buf.append(aChar);
            }
            if (quoted) buf.append('"');
        } else if (x instanceof Object[]) {
            Object[] v = (Object[]) x;
            buf.append("#(");
            for (int i = 0; i < v.length; i++) {
                stringify(v[i], quoted, buf);
                if (i != v.length - 1) buf.append(' ');
            }
            buf.append(')');
        } else if (x == Boolean.TRUE) {
            buf.append("#t");
        } else if (x == Boolean.FALSE) {
            buf.append("#f");
        } else {
            buf.append(x);
        }
    }

    /**
     * Like Common Lisp rest; car of a Pair, or null for anything else. *
     */
    public static Object restOfPairOrNull(Object x) {
        return (x instanceof IPair) ? ((IPair) x)._cdr() : null;
    }

    /**
     * Build up a String representation of the Pair in a StringBuffer. *
     */
    public static void stringifyPair(IPair p, boolean quoted, StringBuffer buf) {
        String special = null;
        if ((p._cdr() instanceof IPair) && restOfPairOrNull(p._cdr()) == null)
            special = (p._car() == "quote") ? "'" : (p._car() == "quasiquote") ? "`"
                    : (p._car() == "unquote") ? "," : (p._car() == "unquote-splicing") ? ",@"
                    : null;

        if (special != null) {
            buf.append(special);
            Pair.stringify(second(p), quoted, buf);
        } else {
            buf.append('(');
            Pair.stringify(p._car(), quoted, buf);
            Object tail = p._cdr();
            while (tail instanceof IPair) {
                buf.append(' ');

                IPair tp = (IPair) tail;
                Pair.stringify(tp._car(), quoted, buf);
                tail = tp._cdr();
            }
            if (tail != null) {
                buf.append(" . ");
                Pair.stringify(tail, quoted, buf);
            }
            buf.append(')');
        }
    }

    /**
     * Convert x to a Java String giving its external representation.
     * Strings and characters are quoted. *
     */
    public static String stringify(Object x) {
        return stringify(x, true);
    }

    /**
     * Convert x to a Java String giving its external representation.
     * Strings and characters are quoted iff <tt>quoted</tt> is true.. *
     */
    public static String stringify(Object x, boolean quoted) {
        StringBuffer buf = new StringBuffer();
        Pair.stringify(x, quoted, buf);
        return buf.toString();
    }

    public List toList() {
        List l = new ArrayList();
        Pair c = this;
        do {
            l.add(c._car());
            Object next = c._cdr();

            if (next instanceof Pair)
                c = (Pair)next;
            else {
                if (next!=null)
                    l.add(next);
                c = null;
            }
        }
        while (c!=null);

        return l;
    }

}

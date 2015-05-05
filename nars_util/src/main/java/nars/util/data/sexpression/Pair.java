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
        this.setFirst(first);
        this.setRest(rest);
    }


    /**
     * The first element of the pair. *
     */
    @Override
    public Object first() {
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
    public Object rest() {
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
        return (x instanceof IPair) ? ((IPair) x).first() : null;
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
            for (int i = 0; i < chars.length; i++) {
                if (quoted && chars[i] == '"') buf.append('\\');
                buf.append(chars[i]);
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
        return (x instanceof IPair) ? ((IPair) x).rest() : null;
    }

    /**
     * Build up a String representation of the Pair in a StringBuffer. *
     */
    public static void stringifyPair(IPair p, boolean quoted, StringBuffer buf) {
        String special = null;
        if ((p.rest() instanceof IPair) && restOfPairOrNull(p.rest()) == null)
            special = (p.first() == "quote") ? "'" : (p.first() == "quasiquote") ? "`"
                    : (p.first() == "unquote") ? "," : (p.first() == "unquote-splicing") ? ",@"
                    : null;

        if (special != null) {
            buf.append(special);
            Pair.stringify(second(p), quoted, buf);
        } else {
            buf.append('(');
            Pair.stringify(p.first(), quoted, buf);
            Object tail = p.rest();
            while (tail instanceof IPair) {
                buf.append(' ');

                IPair tp = (IPair) tail;
                Pair.stringify(tp.first(), quoted, buf);
                tail = tp.rest();
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
            l.add(c.first());
            Object next = c.rest();

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

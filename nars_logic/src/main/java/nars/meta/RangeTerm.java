package nars.meta;

import nars.term.Atom;

/**
 * Meta-term of the form:
 *      prefix_from..to
 * for representing a range of terms
 *
 * ex: A_1..n
 */
public class RangeTerm extends Atom {

    public final String prefix;
    public final int from;
    public final char to;

    public static RangeTerm rangeTerm(String s) {
        int uscore = s.indexOf("_");
        if (uscore == -1) return null;
        int periods = s.indexOf("..");
        if (periods == -1) return null;
        if (periods < uscore) return null;

        String prefix = s.substring(0, uscore);
        int from = Integer.parseInt( s.substring(uscore, periods) );
        String to = s.substring(periods+2);
        if (to.length() > 1) return null;

        return new RangeTerm(prefix, from, to.charAt(0));
    }

    public RangeTerm(String prefix, int from, char to) {
        super(prefix + "_" + from + ".." + to);
        this.prefix = prefix;
        this.from = from;
        this.to = to;
    }
}

package nars.nal.meta;

import nars.Op;
import nars.term.MutableAtomic;
import nars.term.Term;
import nars.term.transform.Substitution;

import java.util.Map;

/**
 * Meta-term of the form:
 *      prefix_from..to
 * for representing a range of terms
 *
 * ex: A_1..n
 */
public class Ellipsis extends MutableAtomic {

    public final String prefix;
    public final int from;
    public final char to;



    public Ellipsis(String prefix, int from, char to) {
        super(prefix + '_' + from + ".." + to);
        this.prefix = prefix;
        this.from = from;
        this.to = to;
    }

    @Override
    public final Op op() {
        return Op.ATOM;
    }

    @Override
    public int structure() {
        return 0;
    }

    @Override
    public boolean hasVar() {
        return false;
    }

    @Override
    public int vars() {
        return 0;
    }

    @Override
    public boolean hasVarIndep() {
        return false;
    }

    @Override
    public boolean hasVarDep() {
        return false;
    }

    @Override
    public boolean hasVarQuery() {
        return false;
    }

    @Override
    public Term substituted(Map<Term, Term> subs) {
        return null;
    }

    @Override
    public Term substituted(Substitution s) {
        return null;
    }

    @Override
    public int complexity() {
        return 0;
    }

    @Override
    public int varIndep() {
        return 0;
    }

    @Override
    public int varDep() {
        return 0;
    }

    @Override
    public int varQuery() {
        return 0;
    }

    //    public static RangeTerm rangeTerm(String s) {
//        int uscore = s.indexOf("_");
//        if (uscore == -1) return null;
//        int periods = s.indexOf("..");
//        if (periods == -1) return null;
//        if (periods < uscore) return null;
//
//        String prefix = s.substring(0, uscore);
//        int from = Integer.parseInt( s.substring(uscore, periods) );
//        String to = s.substring(periods+2);
//        if (to.length() > 1) return null;
//
//        return new RangeTerm(prefix, from, to.charAt(0));
//    }
}

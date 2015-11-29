package nars.nal.meta;

import nars.Op;
import nars.term.Atom;
import nars.term.MutableAtomic;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.Substitution;

import java.util.Map;

/**
 * Meta-term of the form:
 *      variable..exppression
 * for selecting a range of subterms
 *
 * ex:
 *   A..not(X)
 *   B..not(X,Y)
 *   B..not(first)
 *   B..not(first,last)
 */
public class Ellipsis extends MutableAtomic {

    public final static Atom Expand = Atom.the("..");

    public final Variable var;
    public final Term expression;

    public Ellipsis(Variable var, Term expression) {
        super(var + ".." + expression);
        this.var = var;
        this.expression = expression;
    }


    @Override
    public int volume() {
        return 0;
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

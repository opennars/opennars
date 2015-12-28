package nars.term.transform;

import nars.Global;
import nars.Op;
import nars.nal.nal8.Operator;
import nars.nal.op.ImmediateTermTransform;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.match.Ellipsis;
import nars.term.variable.Variable;

import java.util.List;


public interface Subst  {

    boolean isEmpty();

    Term getXY(Object t);

    void clear();

    /** match a range of subterms of Y.  */
    static List<Term> collect(Compound y, int from, int to) {
        int s = to-from;

        List<Term> l = Global.newArrayList(s);

        for (int i = 0; i < s; i++) {
            Term e = y.term(i+from);
            if (e.equals(Ellipsis.Shim))
                continue;
            l.add(e);
        }

        return l;
    }


    static boolean isSubstitutionComplete(Term a, Op o) {
        return o == Op.VAR_PATTERN ? !Variable.hasPatternVariable(a) : !a.hasAny(o);
    }

    default ImmediateTermTransform getTransform(Operator t) {
        return null;
    }


//
//    boolean match(final Term X, final Term Y);
//
//    /** matches when x is of target variable type */
//    boolean matchXvar(Variable x, Term y);
//
//    /** standard matching */
//    boolean next(Term x, Term y, int power);
//
//    /** compiled matching */
//    boolean next(TermPattern x, Term y, int power);
//
//    void putXY(Term x, Term y);
//    void putYX(Term x, Term y);
//





}

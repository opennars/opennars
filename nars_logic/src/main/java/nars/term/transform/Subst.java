package nars.term.transform;

import nars.Op;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;


public interface Subst  {

    boolean isEmpty();

    Term getXY(Object t);

    void clear();

    /** match a range of subterms of Y */
    static Term[] collect(Compound y, int from, int to) {
        int s = to-from;
        Term[] m = new Term[s];
        for (int i = 0; i < s; i++) {
            int k = i+from;
            m[i] = y.term(k);
        }

        return m;
    }


    static boolean isSubstitutionComplete(Term a, Op o) {
        return o == Op.VAR_PATTERN ? !Variable.hasPatternVariable(a) : !a.hasAny(o);
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

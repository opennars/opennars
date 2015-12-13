package nars.term.transform;

import nars.term.Term;


final public class NotEqualsConstraint implements MatchConstraint {

    private final Term b;

    public NotEqualsConstraint(Term b) {
        this.b = b;
    }

    @Override
    public boolean invalid(Term x, Term y, FindSubst f) {
        Term canNotEqual = f.getXY(b);
        if (canNotEqual != null) {
            return y.equals(canNotEqual);
        }
        return false;
    }

    @Override
    public String toString() {
        return "!=" + b;
    }
}

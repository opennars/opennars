package nars.term.constraint;

import nars.Global;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;

import java.util.Set;


final public class NoCommonSubtermsConstraint implements MatchConstraint {

    private final Term b;

    public NoCommonSubtermsConstraint(Term b) {
        this.b = b;
    }

    @Override
    public boolean invalid(Term x, Term y, FindSubst f) {
        Term B = f.getXY(b);
        if (B != null) {
            Set<Term> tmpSet = Global.newHashSet(0);
            return sharedSubterms(y, B, tmpSet);
        }
        return false;
    }

    @Override
    public String toString() {
        return "noCommonSubterms(" + b + ")";
    }

    static boolean sharedSubterms(Term a, Term b, Set<Term> s) {
        addSubtermsRecursivelyUntilFirstMatch(a, s, null);
        return !addSubtermsRecursivelyUntilFirstMatch(b, null, s); //we stop early this way (efficiency)
    }

    static boolean addSubtermsRecursivelyUntilFirstMatch(Term x, Set<Term> AX, Set<Term> BX) {
        if (BX != null && BX.contains(x)) { //by this we can stop early
            return false;
        }

        if (AX != null) {
            if (AX.add(x)) {

                //only on the first time it has been added:
                if (x instanceof Compound) {
                    Compound c = (Compound) x;
                    int l = c.size();
                    for (int i = 0; i < l; i++) {
                        Term d = c.term(i);
                        boolean ret = addSubtermsRecursivelyUntilFirstMatch(d, AX, BX);
                        if (!ret) { //by this we can stop early
                            return false;
                        }
                    }
                }

            }
        }

        return true;
    }



}

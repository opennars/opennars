package nars.nal.meta.pre;

import nars.Global;
import nars.nal.RuleMatch;
import nars.term.Term;
import nars.term.compound.Compound;

import java.util.Set;

/** Unique subterms */
public class NoCommonSubterm extends PreCondition2 {

    /** commutivity: sort the terms */
    public static NoCommonSubterm make(Term a, Term b) {
        if (a.compareTo(b) <= 0)
            return new NoCommonSubterm(a, b);
        else
            return new NoCommonSubterm(b, a);
    }

    NoCommonSubterm(Term arg1, Term arg2) {
        super(arg1, arg2);
    }

    @Override
    public final boolean test(RuleMatch m, Term a, Term b) {
        Set<Term> tmpSet = Global.newHashSet(0);

        return !sharedSubterms(a, b, tmpSet );
    }

    static boolean sharedSubterms(final Term a, final Term b, Set<Term> s) {
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

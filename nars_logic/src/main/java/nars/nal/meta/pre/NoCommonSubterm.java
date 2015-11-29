package nars.nal.meta.pre;

import nars.Global;
import nars.nal.RuleMatch;
import nars.term.Compound;
import nars.term.Term;

import java.util.Set;


public class NoCommonSubterm extends PreCondition2 {

    public NoCommonSubterm(Term arg1, Term arg2) {
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

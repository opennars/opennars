package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.term.Compound;
import nars.term.Term;

import java.util.Set;


public class NoCommonSubterm extends PreCondition2 {
    public NoCommonSubterm(Term arg1, Term arg2) {
        super(arg1, arg2);
    }

    @Override
    public final boolean test(RuleMatch m, Term a, Term b) {
        Set<Term> tmpSet = m.tmpSet;

        final boolean result = !Share_Any_Subterm(a, b, tmpSet );
        tmpSet.clear(); //return it in the condition you took it, empty

        return result;
    }

    public static boolean Share_Any_Subterm(final Term a, final Term b, Set<Term> s) {
        addSubtermsRecursivelyUntilFirstMatch(a, s, null);
        return !addSubtermsRecursivelyUntilFirstMatch(b, null, s); //we stop early this way (efficiency)
    }

    public static boolean addSubtermsRecursivelyUntilFirstMatch(Term x, Set<Term> AX, Set<Term> BX) {
        if (BX != null && BX.contains(x)) { //by this we can stop early
            return false;
        }

        if (AX != null) {
            if (AX.add(x)) {

                //only on the first time it has been added:
                if (x instanceof Compound) {
                    Compound c = (Compound) x;
                    for (Term d : c.term) {
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

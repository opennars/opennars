package nars.meta.pre;

import nars.meta.RuleMatch;
import nars.meta.pre.PreCondition2;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;
import nars.term.Variable;

import java.util.HashSet;

/**
 * Created by me on 8/15/15.
 */
public class NoCommonSubterm extends PreCondition2 {
    public NoCommonSubterm(Term arg1, Term arg2) {
        super(arg1, arg2);
    }

    public boolean Add_All_Subterms_Recursively_As_Long_As_Not_In_Z(Term t, HashSet<Term> map, HashSet<Term> Z) {
        if(Z.contains(t)) { //by this we can stop early
            return false;
        }
        map.add(t);
        if(t instanceof Compound) {
            Compound c = (Compound) t;
            for(Term d : c) {
                boolean ret = Add_All_Subterms_Recursively_As_Long_As_Not_In_Z(d, map, Z);
                if(!ret) { //by this we can stop early
                    return false;
                }
            }
        }
        return true;
    }

    public boolean Share_Any_Subterm(Term a, Term b) {
        HashSet<Term> A = new HashSet<>();
        HashSet<Term> B = new HashSet<>();
        Add_All_Subterms_Recursively_As_Long_As_Not_In_Z(a, A, new HashSet<Term>());
        if(!Add_All_Subterms_Recursively_As_Long_As_Not_In_Z(b, B, A)) //we stop early this way (efficiency)
            return true;
        return false;
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b) {
        if(Share_Any_Subterm(a,b))
            return false;
        return true;
    }

    @Override
    public boolean isEarly() {
        return true;
    }
}

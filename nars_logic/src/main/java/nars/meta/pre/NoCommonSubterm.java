package nars.meta.pre;

import nars.Global;
import nars.meta.RuleMatch;
import nars.term.Compound;
import nars.term.Term;

import java.util.Set;

/**
 * Created by me on 8/15/15.
 */
public class NoCommonSubterm extends PreCondition2 {
    public NoCommonSubterm(Term arg1, Term arg2) {
        super(arg1, arg2);
    }

    public boolean addSubtermsRecursivelyUntilFirstMatch(Term t, Set<Term> map, Set<Term> Z) {
        if(Z != null && Z.contains(t)) { //by this we can stop early
            return false;
        }

        if (map!=null)
            map.add(t);

        if(t instanceof Compound) {
            Compound c = (Compound) t;
            for(Term d : c.term) {
                boolean ret = addSubtermsRecursivelyUntilFirstMatch(d, map, Z);
                if(!ret) { //by this we can stop early
                    return false;
                }
            }
        }
        return true;
    }

    public boolean Share_Any_Subterm(final Term a, final Term b) {
        Set<Term> A = Global.newHashSet(a.volume()*2);
        //Set<Term> B = Global.newHashSet(a.volume()*2);
        addSubtermsRecursivelyUntilFirstMatch(a, A, null);
        return !addSubtermsRecursivelyUntilFirstMatch(b, null, A); //we stop early this way (efficiency)
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b) {
        if(Share_Any_Subterm(a,b))
            return false;
        return true;
    }

}

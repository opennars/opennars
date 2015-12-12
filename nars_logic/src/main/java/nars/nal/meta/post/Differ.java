package nars.nal.meta.post;

import com.gs.collections.api.set.MutableSet;
import nars.nal.RuleMatch;
import nars.nal.meta.pre.PreCondition3Output;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;

import static nars.term.compound.GenericCompound.COMPOUND;

/**
 * Created by me on 8/15/15.
 */
public class Differ extends PreCondition3Output {

    public Differ(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {
//
//        if (Unite.invalid(a,b,c))
//            return false;
//
//        //ok both are extensional sets or intensional sets, build difference
//        return Unite.substituteSet(m, a, c,
//            TermContainer.difference((Compound)a, (Compound)b)
//        );
//

        if (c == null) return false;
        if (!a.op().isSet() || a.op()!=b.op())
            return false;

        MutableSet<Term> x = ((Compound)a).toSet();
        boolean change = false;
        //if (b.op().isSet() && (a.op()==b.op())) {
            //if both sets, must be equal type to merge their subterms
            for (Term bb : ((Compound) b).terms()) {
                change |= x.remove(bb);
            }
        //}
//        } else {
//            //include the term itself into the new set
//            change = x.remove(b);
//        }

        if (x.isEmpty()) return false;

        Term result;
        if (change) {
            result = COMPOUND(a.op(), x);
        } else {
            result = a; /* if no change, re-use term without constructing new one */
            //maybe return false here
        }

        m.secondary.put(
                (Variable) c, result
        );

        return true;
    }
}

package nars.nal.meta.post;

import com.gs.collections.api.set.MutableSet;
import nars.nal.RuleMatch;
import nars.nal.meta.pre.PreCondition3Output;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;

import java.util.Collections;
import java.util.Set;

import static nars.term.compound.GenericCompound.COMPOUND;

/**
 * TODO lazy calculate this with a Substitution class
 */
public class Unite extends PreCondition3Output {

    public Unite(Term arg1, Term arg2, Term arg3) {
        super(arg1, arg2, arg3);
    }

    @Override
    public boolean test(RuleMatch m, Term a, Term b, Term c) {

        //if (Unite.invalid(a,b,c)) return false;

        if (c == null) return false;

        if (!a.op().isSet() || a.op()!=b.op())
            return false;

        MutableSet<Term> x = ((Compound)a).toSet();
        boolean change = false;
        //if (b.op().isSet() && (a.op()==b.op())) {
            //if both sets, must be equal type to merge their subterms
            change = Collections.addAll( x, ((Compound) b).terms() );
        /*} else {
            //include the term itself into the new set
            change = x.add(b);
        }*/

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

    public static boolean substituteSet(RuleMatch m, Term a, Term c, Set<Term> terms) {
        if (terms.isEmpty()) return false;

        m.secondary.put(
            (Variable) c, COMPOUND(a.op(), terms)
        );

        return true;
    }

    public static boolean invalid(Term a, Term b, Term c) {
        return c==null || !a.op().isSet() || a.op()!=b.op();
    }


}

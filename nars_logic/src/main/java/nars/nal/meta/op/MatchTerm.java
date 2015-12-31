package nars.nal.meta.op;

import com.google.common.collect.ListMultimap;
import com.gs.collections.api.map.ImmutableMap;
import nars.$;
import nars.Global;
import nars.Op;
import nars.nal.PremiseMatch;
import nars.nal.meta.ProcTerm;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.constraint.AndConstraint;
import nars.term.constraint.MatchConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.gs.collections.impl.factory.Maps.immutable;
import static nars.$.para;

/**
 * Establishes conditions for the Term match
 *
 * < (|, match [, constraints]) ==> (&|, derivation1, ... derivationN)>
 */
public final class MatchTerm extends GenericCompound implements ProcTerm<PremiseMatch> {

    public final Term x;
    public final ImmutableMap<Term, MatchConstraint> constraints;

    public final Derive[] derivations;

    private MatchTerm(Term x, ImmutableMap<Term, MatchConstraint> constraints,
                     Compound condition, Derive[] derivations) {
        super(Op.IMPLICATION, condition, para(derivations));

        this.x = x;
        this.constraints = constraints;
        this.derivations = derivations;
    }

    public static MatchTerm get(Term x, ListMultimap<Term, MatchConstraint> c, Derive[] derivations) {

        ImmutableMap<Term, MatchConstraint> constraints =
                ((c == null) || c.isEmpty()) ?
                        null :
                        immutable.ofAll(initConstraints(c));

        return new MatchTerm(x, constraints,
                (constraints == null) ?
                        (Compound) x : //no constraints
                        (Compound) ($.conj(x, $.the(constraints.toString()))), //constraints stored in atomic string
                derivations
        );
    }

    @NotNull
    public static Map<Term, MatchConstraint> initConstraints(ListMultimap<Term, MatchConstraint> c) {
        Map<Term, MatchConstraint> con = Global.newHashMap();
        c.asMap().forEach((t, cc) -> {
            switch (cc.size()) {
                case 0:
                    return;
                case 1:
                    con.put(t, cc.iterator().next());
                    break;
                default:
                    con.put(t, new AndConstraint(cc));
                    break;
            }
        });
        return con;
    }

    @Override
    public void accept(PremiseMatch p) {
        p.setConstraints(constraints);
        p.matchAll(x);
    }

}

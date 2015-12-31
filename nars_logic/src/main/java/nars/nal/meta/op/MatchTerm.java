package nars.nal.meta.op;

import com.google.common.collect.ListMultimap;
import com.gs.collections.api.map.ImmutableMap;
import nars.$;
import nars.Global;
import nars.nal.PremiseMatch;
import nars.nal.meta.BooleanCondition;
import nars.nal.meta.ProcTerm;
import nars.nal.meta.TaskBeliefPair;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.constraint.AndConstraint;
import nars.term.constraint.MatchConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.gs.collections.impl.factory.Maps.immutable;

/**
 * Establishes conditions for the Term match
 *
 * < (|, match [, constraints]) ==> (&|, derivation1, ... derivationN)>
 */
public final class MatchTerm extends BooleanCondition<PremiseMatch> implements ProcTerm<PremiseMatch> {

    public final TaskBeliefPair x;
    public final ImmutableMap<Term, MatchConstraint> constraints;

    private final Compound id;

    private MatchTerm(TaskBeliefPair x, ImmutableMap<Term, MatchConstraint> constraints) {
        this.id = (constraints == null) ?
                (Compound) x : //no constraints
                (Compound) ($.sect(x, $.the(constraints.toString()))); //constraints stored in atomic string

        this.x = x;
        this.constraints = constraints;
    }

    public static MatchTerm get(TaskBeliefPair x, ListMultimap<Term, MatchConstraint> c) {

        ImmutableMap<Term, MatchConstraint> constraints =
                ((c == null) || c.isEmpty()) ?
                        null :
                        immutable.ofAll(initConstraints(c));

        return new MatchTerm(x, constraints);
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

    @Override
    @Deprecated public boolean booleanValueOf(PremiseMatch versioneds) {
        return true;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}

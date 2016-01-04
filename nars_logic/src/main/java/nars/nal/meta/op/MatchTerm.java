package nars.nal.meta.op;

import com.google.common.collect.ListMultimap;
import com.gs.collections.api.map.ImmutableMap;
import nars.$;
import nars.Global;
import nars.nal.PremiseMatch;
import nars.nal.PremiseMatchFork;
import nars.nal.meta.AtomicBooleanCondition;
import nars.nal.meta.ProcTerm;
import nars.nal.meta.TaskBeliefPair;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.constraint.AndConstraint;
import nars.term.constraint.MatchConstraint;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.gs.collections.impl.factory.Maps.immutable;
import static java.util.stream.Collectors.toList;

/**
 * Establishes conditions for the Term match
 *
 * < (|, match [, constraints]) ==> (&|, derivation1, ... derivationN)>
 */
public final class MatchTerm extends AtomicBooleanCondition<PremiseMatch> implements ProcTerm<PremiseMatch> {

    public final TaskBeliefPair x;
    public final ImmutableMap<Term, MatchConstraint> constraints;

    private final Compound id;

    /** derivation handlers; use the array form for fast iteration */
    private final Set<Derive> derive = Global.newHashSet(1);
    private PremiseMatchFork onMatch = null;

    private MatchTerm(TaskBeliefPair x, ImmutableMap<Term, MatchConstraint> constraints) {
        this.id = (constraints == null) ?
                //no constraints
                x :
                //constraints stored in atomic string
                (Compound) ($.sect(x, seteMap(constraints.castToMap(), $.ToStringToTerm)));

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

    public static <X> Compound seteMap(Map<Term,? extends X> map, Function<X, Term> toTerm) {
        return $.sete(
            map.entrySet().stream().map(
                e -> $.p(e.getKey(), toTerm.apply(e.getValue())))
            .collect( toList())
        );
    }

    @Override
    public final void accept(PremiseMatch p) {
        throw new RuntimeException("n/a");
    }

    @Override
    @Deprecated public final boolean booleanValueOf(PremiseMatch p) {
        p.match(this);
        return true;
    }

    @Override
    public final String toString() {
        return id.toString();
    }

    /** add a derivation handler to be applied after a rule match */
    public void derive(Derive x) {
        derive.add(x);
    }

    /** delegates a partial or complete match to each of the known derivation handlers */
    public boolean onMatch(PremiseMatch m) {
        if (Global.DEBUG && derive.isEmpty())
            throw new RuntimeException("invalid MatchTerm with no derivation handlers:" + this);

        //TODO HACK dont lazily instantiate this but do it after the TrieDeriver has finished building the rule trie by iterating all known MatchTerm's (in the LinkGraph)
        if (onMatch == null) {
            onMatch = new PremiseMatchFork(derive.toArray(new Derive[derive.size()]));
        }

        onMatch.accept(m);
        return true;
    }
}

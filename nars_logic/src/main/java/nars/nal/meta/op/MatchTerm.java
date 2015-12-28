package nars.nal.meta.op;

import com.google.common.collect.ListMultimap;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.impl.factory.Maps;
import nars.Global;
import nars.term.Term;
import nars.term.constraint.AndConstraint;
import nars.term.constraint.MatchConstraint;
import nars.term.transform.FindSubst;

import java.util.Map;

/**
 * invokes a dynamic FindSubst match via the generic entry method: match(Term,Term)
 */
public class MatchTerm extends PatternOp {
    public final Term x;
    private final String id;
    private final ImmutableMap<Term,MatchConstraint> constraints;

    public MatchTerm(Term term, ListMultimap<Term, MatchConstraint> c) {
        x = term;

        String iid;
        if (c == null || c.isEmpty()) {
            iid = x.toString();
            this.constraints = null;
        } else {
            Map<Term,MatchConstraint> con = Global.newHashMap();
            c.asMap().forEach( (t, cc)-> {
                switch (cc.size()) {
                    case 0: return;
                    case 1: con.put(t, cc.iterator().next());
                            break;
                    default:
                        con.put(t, new AndConstraint(cc));
                        break;
                }
            });


            this.constraints = Maps.immutable.ofAll(con);
            iid = x.toStringCompact() + '^' + con;

//                this.id = new StringBuilder(x.toString() + "∧neq(").append(
//                    Joiner.on(",").join(notEquals.stream().map(v -> {
//                        return ( v.getOne() + "==" + v.getTwo() );
//                    }).collect(Collectors.toList()))
//                ).append(")").toString();
        }
        this.id = "Match:(" + iid + ")";
    }

    @Override
    public boolean run(FindSubst ff) {
        ff.setConstraints(constraints);
        ff.matchAll(x, ff.term.get());
        return true;
    }

    @Override
    public String toString() {
        return id;
    }
}

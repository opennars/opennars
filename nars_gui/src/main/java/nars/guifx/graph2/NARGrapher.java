package nars.guifx.graph2;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.term.Term;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by me on 9/6/15.
 */
public class NARGrapher implements Consumer<NARGraph1> {

    final Set<TermNode> active = Global.newHashSet(1);
    //int maxTerms = 64;

    final Map<Term, TermNode> termToAdd = new LinkedHashMap();
    //final Table<Term, Term, TermEdge> edges = HashBasedTable.create();
    final Table<Term, Term, TermEdge> edgeToAdd = HashBasedTable.create();

    @Override
    public void accept(NARGraph1 graph) {

        final NAR nar = graph.nar;

        final long now = nar.time();

        if (graph.conceptsChanged.getAndSet(false)) {
            active.clear();

            nar.concepts().forEach(/*maxTerms, */c -> {

                final Term source = c.getTerm();

                TermNode sn = getTermNode(graph, source);

                if (active.add(sn))
                    refresh(graph, sn, c, now);
            });
        } else {
            active.forEach(sn -> {
                refresh(graph, sn, sn.concept, now);
            });
        }


        final TermNode[] x;
        if (!termToAdd.isEmpty()) {
            x = termToAdd.values().toArray(new TermNode[termToAdd.size()]);
            termToAdd.clear();
        } else x = null;

        final TermEdge[] y;
        if (!edgeToAdd.isEmpty()) {
            y = edgeToAdd.values().toArray(new TermEdge[edgeToAdd.size()]);
            edgeToAdd.clear();
        } else y = null;

        if (x != null || y != null) {
            graph.commit(active, x, y);
        }
    }

    public TermEdge getConceptEdge(NARGraph1 graph, TermNode s, TermNode t) {
        //re-order
        if (!NARGraph1.order(s.term, t.term)) {
            TermNode x = s;
            s = t;
            t = x;
        }

        TermEdge e = graph.getConceptEdgeOrdered(s, t);
        if (e == null) {
            e = edgeToAdd.get(s.term, t.term);
            if (e == null) {
                e = new TermEdge(s, t);
                edgeToAdd.put(s.term, t.term, e);
            }
        }
        return e;
    }

    protected void refresh(NARGraph1 graph, TermNode sn, Concept c, long now) {
        final Term source = c.getTerm();

        sn.concept = c;

        c.getTaskLinks().forEach(t -> {
            Term target = t.getTerm();
            if (!source.equals(target.getTerm())) {
                TermNode tn = getTermNode(graph, target);
                getConceptEdge(graph, sn, tn);
            }
        });

        c.getTermLinks().forEach(t -> {
            TermNode tn = getTermNode(graph, t.getTerm());
            getConceptEdge(graph, sn, tn);
        });

    }

    public TermNode getTermNode(final NARGraph1 graph, final Term t/*, boolean createIfMissing*/) {
        TermNode tn = graph.getTermNode(t);
        if (tn == null) {
            tn = termToAdd.computeIfAbsent(t, (k) -> {
                return new TermNode(graph.nar, k);
            });
        }
        return tn;
    }

}

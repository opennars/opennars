package nars.guifx.graph2;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.beans.property.SimpleIntegerProperty;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.link.TLink;
import nars.nar.Default;
import nars.term.Term;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by me on 9/6/15.
 */
public class NARGrapher implements Consumer<NARGraph> {

    final Set<TermNode> active = Global.newHashSet(1);

    final Map<Term, TermNode> termToAdd = new LinkedHashMap();
    //final Table<Term, Term, TermEdge> edges = HashBasedTable.create();
    final Table<Term, Term, TermEdge> edgeToAdd = HashBasedTable.create();
    private final SimpleIntegerProperty maxNodes;

    DoubleSummaryReusableStatistics conPri = new DoubleSummaryReusableStatistics();

    public NARGrapher(int maxNodes) {
        this.maxNodes = new SimpleIntegerProperty(maxNodes);
    }

    @Override
    public void accept(NARGraph graph) {

        final NAR nar = graph.nar;

        final long now = nar.time();

        conPri.clear();

        if (graph.conceptsChanged.compareAndSet(true, false)) {
            active.clear();

            ((Default)nar).core.concepts().forEach(maxNodes.get(), c -> {

                final Term source = c.getTerm();

                TermNode sn = getTermNode(graph, source);

                if (active.add(sn))
                    refresh(graph, sn, c);
            });
        } else {
            active.forEach(sn -> {
                refresh(graph, sn, sn.c);
            });
        }

        //after accumulating conPri statistics, normalize each node's scale:
        active.forEach(sn -> {
            sn.priNorm = conPri.normalize(sn.c.getPriority());
        });



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




        graph.commit(active, x, y);

    }

    public void refresh(NARGraph graph, TermNode tn, Concept cc/*, long now*/) {

        //final Term source = c.getTerm();

        tn.c = cc;
        conPri.accept(cc.getPriority());

        final Term t = tn.term;
        final DoubleSummaryReusableStatistics ta = tn.taskLinkStat;
        final DoubleSummaryReusableStatistics te = tn.termLinkStat;

        tn.termLinkStat.clear();
        cc.getTermLinks().forEach(l ->
            updateConceptEdges(graph, tn, l, te)
        );


        tn.taskLinkStat.clear();
        cc.getTaskLinks().forEach(l -> {
            if (!l.getTerm().equals(t)) {
                updateConceptEdges(graph, tn, l, ta);
            }
        });

//        System.out.println("refresh " + Thread.currentThread() + " " + termLinkMean.getResult() + " #" + termLinkMean.getN() );


//        Consumer<TLink> tLinkConsumer = t -> {
//            Term target = t.getTerm();
//            if (!source.equals(target.getTerm())) {
//                TermNode tn = getTermNode(graph, target);
//                //TermEdge edge = getConceptEdge(graph, sn, tn);
//
//            }
//        };
//
//        c.getTaskLinks().forEach(tLinkConsumer);
//        c.getTermLinks().forEach(tLinkConsumer);


    }

    public void updateConceptEdges(NARGraph graph, TermNode s, TLink link, DoubleSummaryReusableStatistics accumulator) {


        Term t = link.getTerm();
        TermNode target = getTermNode(graph,t);
        if (target == null)
            return;

        TermEdge ee = getConceptEdge(graph, s, target);
        if (ee!=null) {
            ee.linkFrom(s, link);
            accumulator.accept(link.getPriority());
        }
    }

    public TermEdge getConceptEdge(NARGraph graph, TermNode s, TermNode t) {
        //re-order
        if (!NARGraph.order(s.term, t.term)) {
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


    public final TermNode getTermNode(final NARGraph<?> graph, final Term t/*, boolean createIfMissing*/) {
        TermNode tn = graph.getTermNode(t);
        if (tn == null) {
            final VisModel gv = graph.vis.get();
            tn = termToAdd.computeIfAbsent(t,
                k -> gv.newNode(k)
            );
        }

        return tn;
    }

}

//package nars.guifx.graph2;
//
//import com.google.common.collect.HashBasedTable;
//import com.google.common.collect.Table;
//import javafx.beans.property.SimpleIntegerProperty;
//import nars.Global;
//import nars.NAR;
//import nars.concept.Concept;
//import nars.link.TLink;
//import nars.nar.Default;
//import nars.term.Term;
//import nars.util.event.ArraySharingList;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Consumer;
//
//import static javafx.application.Platform.runLater;
//
///**
// * Created by me on 9/6/15.
// */
//public class NARGrapher implements Consumer<NARGraph> {
//
//    //final Set<Term> active = Global.newHashSet(1);
//
//    final ArraySharingList<TermNode> termList = new ArraySharingList<>(i->new TermNode[i]);
//
//    //final Map<Term, TermNode> termToAdd = new LinkedHashMap(64);
//    //final Table<Term, Term, TermEdge> edges = HashBasedTable.create();
//    //final Table<Term, Term, TermEdge> edgeToAdd = HashBasedTable.create();
//
//
//    DoubleSummaryReusableStatistics conPri = new DoubleSummaryReusableStatistics();
//
//
//    @Override
//    public void accept(NARGraph graph) {
//
//        final NAR nar = graph.nar;
//
//        //final long now = nar.time();
//
//        //conPri.clear();
//
//        if (graph.conceptsChanged.compareAndSet(true, false)) {
//            //active.clear();
//
//
//            ((Default)nar).core.concepts().forEach(maxNodes.get(), c -> {
//
//
//
//                final Term source = c.getTerm();
//                if (active.add(source)) {
//                    TermNode sn = graph.getTermNode(source);
//                    if (sn!=null)
//                        refresh(graph, sn, c);
//                }
//            });
//        } else {
////            active.forEach(sn -> {
////                refresh(graph, sn, sn.c);
////            });
//        }
//
//
//        //after accumulating conPri statistics, normalize each node's scale:
////        active.forEach(sn -> {
////            sn.priNorm = conPri.normalize(sn.c.getPriority());
////        });
//
//
//
////        final TermNode[] x;
////        if (!termToAdd.isEmpty()) {
////            x = termToAdd.values().toArray(new TermNode[termToAdd.size()]);
////            termToAdd.clear();
////        } else x = null;
//
////        runLater(() -> graph.commit(active));
//
//    }
//
//    public void refresh(NARGraph graph, TermNode tn, Concept cc/*, long now*/) {
//
//        //final Term source = c.getTerm();
//
//        tn.c = cc;
//        conPri.accept(cc.getPriority());
//
//        final Term t = tn.term;
//        final DoubleSummaryReusableStatistics ta = tn.taskLinkStat;
//        final DoubleSummaryReusableStatistics te = tn.termLinkStat;
//
//        tn.termLinkStat.clear();
//        cc.getTermLinks().forEach(l ->
//            updateConceptEdges(graph, tn, l, te)
//        );
//
//
//        tn.taskLinkStat.clear();
//        cc.getTaskLinks().forEach(l -> {
//            if (!l.getTerm().equals(t)) {
//                updateConceptEdges(graph, tn, l, ta);
//            }
//        });
//
////        System.out.println("refresh " + Thread.currentThread() + " " + termLinkMean.getResult() + " #" + termLinkMean.getN() );
//
//
////        Consumer<TLink> tLinkConsumer = t -> {
////            Term target = t.getTerm();
////            if (!source.equals(target.getTerm())) {
////                TermNode tn = getTermNode(graph, target);
////                //TermEdge edge = getConceptEdge(graph, sn, tn);
////
////            }
////        };
////
////        c.getTaskLinks().forEach(tLinkConsumer);
////        c.getTermLinks().forEach(tLinkConsumer);
//
//
//    }
//
//    public void updateConceptEdges(NARGraph graph, TermNode s, TLink link, DoubleSummaryReusableStatistics accumulator) {
//
//
//        Term t = link.getTerm();
//        TermNode target = getTermNode(graph,t);
//        if ((target == null) || (s == target)) return;
//
//        TermEdge ee = getConceptEdge(graph, s, target);
//        if (ee!=null) {
//            ee.linkFrom(s, link);
//            accumulator.accept(link.getPriority());
//        }
//    }
//
//    public TermEdge getConceptEdge(NARGraph graph, TermNode s, TermNode t) {
//        //re-order
//        if (!NARGraph.order(s.term, t.term)) {
//            TermNode x = s;
//            s = t;
//            t = x;
//        }
//
//        TermEdge e = graph.getConceptEdgeOrdered(s, t);
//        if (e == null) {
//            s.putEdge(t.term, e);
//        }
//        return e;
//    }
//
//
//
//}

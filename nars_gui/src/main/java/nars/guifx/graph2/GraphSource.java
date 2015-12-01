package nars.guifx.graph2;

import nars.guifx.graph2.source.SpaceGrapher;
import nars.term.Termed;

import java.util.function.Consumer;

/**
 * Created by me on 9/6/15.
 */
@FunctionalInterface
public interface GraphSource<K extends Termed> extends Consumer<SpaceGrapher<K,TermNode<K>>> {


    default void start(SpaceGrapher<K,? extends TermNode<K>> spaceGrapher) {


    }

    default void refresh(SpaceGrapher<K,? extends TermNode<K>> spaceGrapher, K k, TermNode<K> t) {

    }

    /** called ex: invalidation */
    default void refresh() {

    }

    default void stop(SpaceGrapher<K,? extends TermNode<K>> spaceGrapher) {

    }

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

    //DoubleSummaryReusableStatistics conPri = new DoubleSummaryReusableStatistics();



//    public void accept(SpaceGrapher graph) {
//
////        final NAR nar = graph.nar;
////
////        //final long now = nar.time();
////
////        //conPri.clear();
////
////        if (graph.refresh.compareAndSet(true, false)) {
////            //active.clear();
////
////
////            ((Default)nar).core.concepts().forEach(maxNodes.get(), c -> {
////
////
////
////                final Term source = c.getTerm();
////                if (active.add(source)) {
////                    TermNode sn = graph.getTermNode(source);
////                    if (sn!=null)
////                        refresh(graph, sn, c);
////                }
////            });
////        } else {
//////            active.forEach(sn -> {
//////                refresh(graph, sn, sn.c);
//////            });
////        }
////
////
////        //after accumulating conPri statistics, normalize each node's scale:
//////        active.forEach(sn -> {
//////            sn.priNorm = conPri.normalize(sn.c.getPriority());
//////        });
////
////
////
//////        final TermNode[] x;
//////        if (!termToAdd.isEmpty()) {
//////            x = termToAdd.values().toArray(new TermNode[termToAdd.size()]);
//////            termToAdd.clear();
//////        } else x = null;
////
//////        runLater(() -> graph.commit(active));
//
//    }
//
//    public void refresh(NARGraph graph, TermNode tn, Concept cc/*, long now*/) {
//
//    }
}

package nars.guifx.graph2;

import nars.bag.BLink;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.term.Termed;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static javafx.application.Platform.runLater;


public abstract class GraphSource/* W? */ {

    public final AtomicBoolean refresh = new AtomicBoolean(true);

    /** current grapher after it starts this */
    protected SpaceGrapher grapher;


    public abstract void forEachOutgoingEdgeOf(Termed src, Consumer eachTarget);


    public abstract Termed getTargetVertex(Termed edge);

    /** if the grapher is ready */
    public final boolean isReady() {
        SpaceGrapher grapher = this.grapher;
        if (grapher == null) return false;
        return grapher.isReady();
    }

    public void updateNode(SpaceGrapher g, Termed s, TermNode sn) {



        forEachOutgoingEdgeOf(s, t -> {

            if (t == null) return;

            TermNode tn = g.getTermNode(((BLink<Termed>)t).get());
            if (tn == null)
                return;

            TermEdge ee = getEdge(g, sn, tn, g.edgeVis);
            if (ee != null) {
                updateEdge(ee, t);
            }

        });
        sn.commitEdges();
    }

    protected void updateNode(TermNode tn, Object indexing) {

    }


    public void updateEdge(TermEdge ee, Object link) {


    }


    public static
        TermEdge
    getEdge(SpaceGrapher g, TermNode s, TermNode t, BiFunction<TermNode, TermNode, TermEdge> edgeBuilder) {

        //re-order
        int i = s.getTerm().compareTo(t.getTerm());
        if (i == 0) return null;
            /*throw new RuntimeException(
                "order=0 but must be non-equal: " + s.term + " =?= " + t.term + ", equal:"
                        + s.term.equals(t.term) + " " + t.term.equals(s.term) + ", hash=" + s.term.hashCode() + "," + t.term.hashCode() );*/

        if (!(i < 0)) { //swap
            TermNode x = s;
            s = t;
            t = x;
        }

        return g.getConceptEdgeOrdered(s, t, edgeBuilder);
//        if (e == null) {
//            e = new TermEdge(s, t);
//        }
//        s.putEdge(t.term, e);
//        return e;
    }

    public void stop() {
        grapher = null;
    }

    public void start(SpaceGrapher g) {
        if (grapher!=null) {
            throw new RuntimeException(this + " already attached to grapher " + grapher + ", can not switch to " + g);
        }

        updateGraph();
        setUpdateable();

        grapher = g;
    }

//    public Animate start(SpaceGrapher<V, N> g, int loopMS) {
//        start(g);
//
//        System.out.println(this + " start: " + loopMS + "ms loop");
//
//        Animate an = new Animate(loopMS, a -> {
//
//            setUpdateable();
//            updateGraph();
//
//        });
//
//        an.start();
//
//        return an;
//    }


//    public void updateGraph() {
//
//    }

    /** called once per frame to update anything about the grapher scope */
    public final void updateGraph() {

        //System.out.println(isReady() + " " + canUpdate() + " " + graph);

        if (!isReady())
            return;


        if (canUpdate()) {
            commit();
        }

    }


    public final boolean canUpdate() {
        return refresh.compareAndSet(true, false);
    }

    public final void setUpdateable() {
        runLater(() -> refresh.set(true));
    }

    public void stop(SpaceGrapher g) {

    }

    /** applies updates each frame */
    public abstract void commit();

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

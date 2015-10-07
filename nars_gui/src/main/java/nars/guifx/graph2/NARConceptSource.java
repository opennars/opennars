package nars.guifx.graph2;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import nars.Global;
import nars.NAR;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TLink;
import nars.nar.Default;
import nars.term.Term;
import nars.util.event.Active;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by me on 10/6/15.
 */
public class NARConceptSource extends NARGrapher {


    private final NAR nar;
    private Active regs;
    private Set<TermNode> prevActive;

    public final SimpleDoubleProperty maxPri = new SimpleDoubleProperty(1.0);
    public final SimpleDoubleProperty minPri = new SimpleDoubleProperty(0.0);

    public final SimpleStringProperty includeString = new SimpleStringProperty("");

    public NARConceptSource(NAR nar) {
        this.nar = nar;
    }

    final AtomicBoolean conceptsChanged = new AtomicBoolean();

    @Override
    public synchronized void start(NARGraph g) {


        //.stdout()
        //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")

        if (regs != null)
            regs.off();

        conceptsChanged.set(true);
        regs = new Active(
                nar.memory.eventConceptActivated.on(
                        (c) -> conceptsChanged.set(true)
                ),
                nar.memory.eventFrameStart.on(
                        h -> update(g)
                )
        );

        update(g);

    }

    @Override
    public <V> void stop(NARGraph vnarGraph) {
        regs.off();
        regs = null;
    }

    public final void update(NARGraph g) {


        if (conceptsChanged.compareAndSet(true, false)) {

            //synchronized (conceptsChanged)
            {
                int maxNodes = g.maxNodes.get();
                Set<TermNode> active = Global.newHashSet(maxNodes);

                Consumer<Concept> each = c -> {
                    TermNode tn = g.getOrCreateTermNode(c.getTerm());
                    if (tn != null) {

                        active.add(tn);
                        refresh(g, tn, c);
                    }
                };

                Bag<Term, Concept> x = ((Default) nar).core.concepts();

                    //x.forEach(maxNodes, each);


                    String filter = includeString.get();

                        double minPri = this.minPri.get();
                        double maxPri = this.maxPri.get();

                        Iterator<Concept> cc = x.iterator();
                        int n = 0;
                        while (cc.hasNext() && n < maxNodes) {
                            Concept c = cc.next();
                            float p = c.getPriority();
                            if ((p < minPri) || (p > maxPri)) continue;
                            if ((filter != null) && (!c.getTerm().toString().contains(filter))) continue;

                            each.accept(c);
                        }



                if (prevActive != null && !prevActive.equals(active)) {

                    g.setVertices(active);
                }

                prevActive = active;
            }
        }

    }


    public void refresh(NARGraph g, TermNode tn, Concept cc/*, long now*/) {

        //final Term source = c.getTerm();

        tn.c = cc;
        //conPri.accept(cc.getPriority());
        tn.priNorm = cc.getPriority();

        final Term t = tn.term;
        final DoubleSummaryReusableStatistics ta = tn.taskLinkStat;
        final DoubleSummaryReusableStatistics te = tn.termLinkStat;

        tn.termLinkStat.clear();
        cc.getTermLinks().forEach(l ->
                updateConceptEdges(g, tn, l, te)
        );


        tn.taskLinkStat.clear();
        cc.getTaskLinks().forEach(l -> {
            if (!l.getTerm().equals(t)) {
                updateConceptEdges(g, tn, l, ta);
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

    public void updateConceptEdges(NARGraph g, TermNode s, TLink link, DoubleSummaryReusableStatistics accumulator) {


        Term t = link.getTerm();
        TermNode target = g.getTermNode(t);
        if ((target == null) || (s == target)) return;

        TermEdge ee = getConceptEdge(g, s, target);
        if (ee != null) {
            ee.linkFrom(s, link);
            accumulator.accept(link.getPriority());
        }
    }

    public TermEdge getConceptEdge(NARGraph g, TermNode s, TermNode t) {
        //re-order
        if (!NARGraph.order(s.term, t.term)) {
            TermNode x = s;
            s = t;
            t = x;
        }

        TermEdge e = g.getConceptEdgeOrdered(s, t);
        if (e == null) {
            e = new TermEdge(s, t);
        }
        s.putEdge(t.term, e);

        return e;
    }

}

package nars.guifx.graph2;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import nars.NAR;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.link.TLink;
import nars.nar.Default;
import nars.term.Term;
import nars.util.event.Active;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * Example Concept supplier with some filters
 */
public class ConceptsSource extends GraphSource<Object> {


    private final NAR nar;
    private Active regs;
    private Set<TermNode> prevActive;

    public final SimpleDoubleProperty maxPri = new SimpleDoubleProperty(1.0);
    public final SimpleDoubleProperty minPri = new SimpleDoubleProperty(0.0);

    public final SimpleStringProperty includeString = new SimpleStringProperty("");

    public ConceptsSource(NAR nar) {
        this.nar = nar;

        includeString.addListener((e) -> {
            System.out.println(includeString.getValue());
            refresh();
        });
    }

    final AtomicBoolean refresh = new AtomicBoolean();


    @Override
    public synchronized void start(SpaceGrapher g) {


        //.stdout()
        //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")

        if (regs != null)
            regs.off();

        refresh();

        regs = new Active(
                nar.memory.eventConceptActivated.on(
                        (c) -> refresh.set(true)
                ),
                nar.memory.eventFrameStart.on(
                        h -> update(g)
                )
        );

        update(g);

    }


    @Override public void refresh() {
        runLater( () -> {
            refresh.set(true);
            if (prevActive!=null)
                prevActive.clear();
        });
    }

    @Override
    public void stop(SpaceGrapher vnarGraph) {
        regs.off();
        regs = null;
    }

    public final void update(SpaceGrapher g) {


        if (refresh.compareAndSet(true, false)) {

            int maxNodes = g.maxNodes.get();

            Set<TermNode> active = new LinkedHashSet(maxNodes); //Global.newHashSet(maxNodes);

            //synchronized (refresh)
            {

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


                if (prevActive == null || !prevActive.equals(active)) {
                    g.setVertices(active);
                } else {
                    prevActive = active;
                }

            }
        }

    }


    public void refresh(SpaceGrapher g, TermNode tn, Concept cc/*, long now*/) {

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

    public void updateConceptEdges(SpaceGrapher g, TermNode s, TLink link, DoubleSummaryReusableStatistics accumulator) {


        Term t = link.getTerm();
        TermNode target = g.getTermNode(t);
        if ((target == null) || (s.equals(target))) return;

        TermEdge ee = getConceptEdge(g, s, target);
        if (ee != null) {
            ee.linkFrom(s, link);
            accumulator.accept(link.getPriority());
        }
    }

    public TermEdge getConceptEdge(SpaceGrapher g, TermNode s, TermNode t) {

        //re-order
        final int i = s.term.compareTo(t.term);
        if (i == 0) return null;
            /*throw new RuntimeException(
                "order=0 but must be non-equal: " + s.term + " =?= " + t.term + ", equal:"
                        + s.term.equals(t.term) + " " + t.term.equals(s.term) + ", hash=" + s.term.hashCode() + "," + t.term.hashCode() );*/

        if (!(i < 0)) {
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

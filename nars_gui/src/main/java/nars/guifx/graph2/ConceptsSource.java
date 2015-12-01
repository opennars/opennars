package nars.guifx.graph2;

import com.google.common.collect.Iterables;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import nars.NAR;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.link.TLink;
import nars.nar.Default;
import nars.term.Term;
import nars.term.Termed;
import nars.util.event.Active;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * Example Concept supplier with some filters
 */
public class ConceptsSource<T extends Termed> implements GraphSource<T> {


    private final NAR nar;
    private Active regs = null;
    private Set<TermNode> prevActive = null;

    public final SimpleDoubleProperty maxPri = new SimpleDoubleProperty(1.0);
    public final SimpleDoubleProperty minPri = new SimpleDoubleProperty(0.0);

    public final SimpleStringProperty includeString = new SimpleStringProperty("");

    public ConceptsSource(NAR nar) {
        this.nar = nar;

        includeString.addListener((e) -> {
            //System.out.println(includeString.getValue());
            refresh();
        });
    }

    final AtomicBoolean refresh = new AtomicBoolean(true);

    @Override
    public void start(SpaceGrapher g) {

        //.stdout()
        //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")

        if (regs != null)
            regs.off();

        refresh();

        regs = new Active(
            nar.memory.eventConceptActivated.on(
                c -> refresh.set(true)
            ),
            nar.memory.eventFrameStart.on(
                h -> update(g)
            )
        );

        update(g);

    }


    @Override
    public void refresh() {
        runLater(() -> {
            refresh.set(true);
            if (prevActive != null)
                prevActive.clear();
        });
    }

    @Override
    public void stop(SpaceGrapher vnarGraph) {
        regs.off();
        regs = null;
    }


    public final void update(SpaceGrapher g) {


        if (g.ready.get() && refresh.compareAndSet(true, false)) {


            Bag<Term, Concept> x = ((Default) nar).core.concepts();


            String keywordFilter, _keywordFilter = includeString.get();
            if (_keywordFilter != null && _keywordFilter.isEmpty())
                keywordFilter = null;
            else
                keywordFilter = _keywordFilter;

            double minPri = this.minPri.get();
            double maxPri = this.maxPri.get();

            final Iterable<Term> ii = Iterables.transform( Iterables.filter(x, cc -> {

                float p = cc.getPriority();
                if ((p < minPri) || (p > maxPri))
                    return false;


                if (keywordFilter != null) {
                    if (cc.getTerm().toString().contains(keywordFilter))
                        return false;
                }

                return true;

            }), c -> c.getTerm());

            g.setVertices(ii);


        }

    }


//    public static void updateConceptEdges(SpaceGrapher g, TermNode s, TLink link, DoubleSummaryReusableStatistics accumulator) {
//
//
//        Term t = link.getTerm();
//        TermNode target = g.getTermNode(t);
//        if ((target == null) || (s.equals(target))) return;
//
//        TermEdge ee = getConceptEdge(g, s, target);
//        if (ee != null) {
//            ee.linkFrom(s, link);
//            accumulator.accept(link.getPriority());
//        }
//    }


    @Override
    public void refresh(SpaceGrapher<T, ? extends TermNode<T>> sg, T k, TermNode<T> tn) {

        //final Term source = c.getTerm();

        if (k instanceof Concept) {
            Concept cc = (Concept)k;

            tn.c = cc;
            tn.priNorm = cc.getPriority();

            final int maxNodeLinks = 24;

            Set<T> missing = tn.getEdgeSet();

            Consumer<? super TLink<?>> linkUpdater = link -> {


                T target = (T) link.getTerm();
                if ((tn == null) || (k.getTerm().equals(target))) return;

                TermEdge ee = getConceptEdge(sg, tn, sg.getTermNode(target));
                if (ee != null) {
                    ee.linkFrom(tn, link);
                }

                missing.remove(tn.term);
            };

            cc.getTermLinks().forEach(maxNodeLinks, linkUpdater);
            cc.getTaskLinks().forEach(maxNodeLinks, linkUpdater);

            tn.removeEdges(missing);
        }

        //final Term t = tn.term;
        //final DoubleSummaryReusableStatistics ta = tn.taskLinkStat;
        //final DoubleSummaryReusableStatistics te = tn.termLinkStat;


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


    public TermEdge<TermNode<T>>
    getConceptEdge(SpaceGrapher<T, ? extends TermNode<T>> g, TermNode<T> s, TermNode<T> t) {

        //re-order
        final int i = s.term.getTerm().compareTo(t.term.getTerm());
        if (i == 0) return null;
            /*throw new RuntimeException(
                "order=0 but must be non-equal: " + s.term + " =?= " + t.term + ", equal:"
                        + s.term.equals(t.term) + " " + t.term.equals(s.term) + ", hash=" + s.term.hashCode() + "," + t.term.hashCode() );*/

        if (!(i < 0)) { //swap
            TermNode x = s;
            s = t;
            t = x;
        }

        return g.getConceptEdgeOrdered(s, t, defaultEdgeBuilder);
//        if (e == null) {
//            e = new TermEdge(s, t);
//        }
//        s.putEdge(t.term, e);
//        return e;
    }

    static final BiFunction<TermNode, TermNode, TermEdge> defaultEdgeBuilder = (a, b) ->
            new TermEdge(a, b);


    @Override
    public void accept(SpaceGrapher<T, TermNode<T>> tTermNodeSpaceGrapher) {

    }
}

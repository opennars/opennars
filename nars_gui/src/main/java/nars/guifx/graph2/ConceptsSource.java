package nars.guifx.graph2;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import nars.Global;
import nars.NAR;
import nars.bag.BLink;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.guifx.graph2.impl.TLinkEdge;
import nars.guifx.graph2.source.SpaceGrapher;
import nars.nar.Default;
import nars.term.Termed;
import nars.util.event.Active;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static nars.util.data.Util.lerp;

/**
 * Example Concept supplier with some filters
 */
public class ConceptsSource extends GraphSource {


    public final NAR nar;
    private Active regs = null;

    final int maxNodes = 128;
    final int maxNodeLinks = 16; //per type

    public final SimpleDoubleProperty maxPri = new SimpleDoubleProperty(1.0);
    public final SimpleDoubleProperty minPri = new SimpleDoubleProperty(0.0);
    public final SimpleStringProperty includeString = new SimpleStringProperty("");

    private final BiFunction<TermNode, TermNode, TermEdge> edgeBuilder =
            TLinkEdge::new;

    private float _maxPri = 0, _minPri = 0;
    protected final List<Termed> concepts = Global.newArrayList();
    private String keywordFilter = null;
    private final Predicate<BLink> eachConcept = cc -> {

        float p = cc.getPriority();
        if ((p < _minPri) || (p > _maxPri))
            return true;


        if (keywordFilter != null) {
            if (cc.get().toString().contains(keywordFilter))
                return true;
        }

        concepts.add((Concept)cc.get());

        return concepts.size() < maxNodes;
    };

    public ConceptsSource(NAR nar) {

        this.nar = nar;

        includeString.addListener((e) -> {
            //System.out.println(includeString.getValue());
            setUpdateable();
        });
    }


    @Override
    public void updateEdge(TermEdge ee, Object link) {
        //rolling average
        ee.pri = lerp(
                ((BLink)link).getPriority(), ee.pri,
                      0.05f);
    }




    public float getConceptPriority(Termed cc) {
        BLink<Concept> ccc = ((Default) nar).core.active.get(cc);
        if (ccc == null) return 0;
        return ccc.getPriorityIfNaNThenZero();
    }

    @Override
    public void updateNode(SpaceGrapher g, Termed s, TermNode sn) {
        sn.priNorm = getConceptPriority(s);
        super.updateNode(g, s, sn);
    }

    @Override
    public void forEachOutgoingEdgeOf(Termed cc,
                                      Consumer eachTarget) {


        SpaceGrapher sg = grapher;
        if (sg == null) return; //???




        Consumer linkUpdater = link -> {

            Termed target = ((BLink<Termed>)link).get();

            if (cc.term().equals(target.term())) //self-loop
                return;

            TermNode tn = sg.getTermNode(target);
            if (tn == null)
                return;

            eachTarget.accept(link); //tn.c);

//            TermEdge.TLinkEdge ee = (TermEdge.TLinkEdge) getEdge(sg, sn, tn, edgeBuilder);
//
//            if (ee != null) {
//                ee.linkFrom(tn, link);
//            }
//
//            //missing.remove(tn.term);
        };

        ((Concept)cc).getTermLinks().topN(maxNodeLinks, linkUpdater);
        ((Concept)cc).getTaskLinks().topN(maxNodeLinks, linkUpdater);

        //sn.removeEdges(missing);

    }

    @Override
    public Termed getTargetVertex(Termed edge) {
        return grapher.getTermNode(edge.term()).c;
    }


    @Override
    public void start(SpaceGrapher g) {


        //.stdout()
        //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")

        if (regs != null)
            regs.off();


        regs = new Active(
                /*nar.memory.eventConceptActivated.on(
                        c -> refresh.set(true)
                ),*/
                nar.memory.eventFrameStart.on(h -> {
                    refresh.set(true);
                    updateGraph();
                })
        );

        super.start(g);


    }

    @Override
    public void stop(SpaceGrapher vnarGraph) {
        regs.off();
        regs = null;
    }


    @Override
    public void commit() {

        Bag<Concept> x = ((Default) nar).core.active;

        String _keywordFilter = includeString.get();
        this.keywordFilter = _keywordFilter != null && _keywordFilter.isEmpty() ? null : _keywordFilter;

        _minPri = this.minPri.floatValue();
        _maxPri = this.maxPri.floatValue();

        //final int maxNodes = this.maxNodes;

        //TODO use forEach witha predicate return to stop early
        x.topWhile(eachConcept);

//        Iterable<Termed> _concepts = StreamSupport.stream(x.spliterator(), false).filter(cc -> {
//
//            float p = getConceptPriority(cc);
//            if ((p < minPri) || (p > maxPri))
//                return false;
//
//
//            if (keywordFilter != null) {
//                if (cc.get().toString().contains(keywordFilter))
//                    return false;
//            }
//
//            return true;
//
//        }).collect(Collectors.toList());

        commit(concepts);

        concepts.clear();
    }


    protected final void commit(Collection<Termed> ii) {
        grapher.setVertices(ii);
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




//    public final void updateNodeOLD(SpaceGrapher sg, BagBudget<Concept> cc, TermNode sn) {
//
//        sn.c = cc.get();
//        sn.priNorm = cc.getPriority();
//
//
//
//        //final Term t = tn.term;
//        //final DoubleSummaryReusableStatistics ta = tn.taskLinkStat;
//        //final DoubleSummaryReusableStatistics te = tn.termLinkStat;
//
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


}

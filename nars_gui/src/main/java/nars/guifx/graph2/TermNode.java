package nars.guifx.graph2;

import javafx.scene.paint.Color;
import nars.Global;
import nars.Op;
import nars.concept.Concept;
import nars.guifx.graph2.layout.GraphNode;
import nars.guifx.util.ColorMatrix;
import nars.term.Term;
import nars.term.Termed;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class TermNode extends GraphNode {


    public static final TermNode[] empty = new TermNode[0];


    public final Map<Termed, TermEdge> edge;

    /**
     * copy of termedge values for fast iteration during rendering
     */
    boolean modified = false;

    public final Termed term;

    /** priority normalized to visual context */
    public double priNorm = 0;

    public Concept c = null;
    private TermEdge[] edges = TermEdge.empty;

    /*
    DoubleSummaryReusableStatistics termLinkStat = new DoubleSummaryReusableStatistics();
    DoubleSummaryReusableStatistics taskLinkStat = new DoubleSummaryReusableStatistics();
    */






    public TermNode(Termed t, int maxEdges) {

        if (t instanceof Concept) c = (Concept)t; //HACK

        edge = new FixedLinkedHashMap<>(maxEdges);


        term = t;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermNode termNode = (TermNode) o;

        return term.equals(termNode.term);

    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }
//    /**
//     * NAR update thread
//     */
//    public void update() {
//
//
////        double vertexScale = NARGraph1.visModel.getVertexScale(c);
//
//        /*if ((int) (priorityDisplayedResolution * priorityDisplayed) !=
//                (int) (priorityDisplayedResolution * vertexScale))*/ {
//
//
////            System.out.println("update " + Thread.currentThread() + " " + vertexScale + " " + getParent() + " " + isVisible() + " " + localToScreen(0,0));
//
//            double scale = minSize + (maxSize - minSize) * priNorm;
//            scale(scale);
//
//
//        }
//
//
//    }


    public final TermEdge putEdge(Termed b, TermEdge e) {
        TermEdge r = edge.put(b, e);
        modified |= (e != r);
        return r;
    }

    public TermEdge[] updateEdges() {
        int s = edge.size();

        TermEdge[] edges = this.edges;

        TermEdge[] e;
        e = edges.length != s ? new TermEdge[s] : edges;

        return this.edges = edge.values().toArray(e);
    }

//    /**
//     * untested
//     */
//    public void removeEdge(TermEdge e) {
//        if (edge.remove(e.bSrc) != e) {
//            throw new RuntimeException("wtf");
//        }
//        edges = null;
//    }

    @Override
    public final TermEdge[] getEdges() {
        return edges;
    }





    public static Color getTermColor(Termed term, ColorMatrix colors, double v) {
        return colors.get(
                (term.term().op().ordinal() % colors.cc.length) / ((double) Op.values().length),
                v);
    }

    public TermEdge getEdge(Termed b) {
        return edge.get(b);
    }

    public Set<Termed> getEdgeSet() {
        if (edges.length == 0) return Collections.emptySet();

        Set<Termed> ss = Global.newHashSet(edges.length);
        for (TermEdge ee : edges)
            ss.add(ee.bSrc.term);
        return ss;
        //edge.keySet());
    }

    public void removeEdges(Set<Termed> toRemove) {
        if (!toRemove.isEmpty()) {

            toRemove.forEach(edge::remove);
            modified = true;
        }

    }

    public final Term getTerm() {
        return term.term();
    }

    public void commitEdges() {
        if (modified) {
            modified = false;
            edges = !edge.isEmpty() ? updateEdges() : TermEdge.empty;

        }

    }


    public static final class FixedLinkedHashMap<K,V> extends LinkedHashMap<K, V> {

        final int max_cap;

        public FixedLinkedHashMap(int cap) {
            super(cap, 0.75f, true);
            max_cap = cap;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > max_cap;
        }
    }
}

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


public class TermNode<K extends Termed> extends GraphNode {


    public static final TermNode[] empty = new TermNode[0];


    final public Map<K, TermEdge> edge;

    /**
     * copy of termedge values for fast iteration during rendering
     */
    boolean modified = false;

    public final K term;

    /** priority normalized to visual context */
    public double priNorm = 0;

    public Concept c = null;
    private TermEdge[] edges = TermEdge.empty;

    /*
    DoubleSummaryReusableStatistics termLinkStat = new DoubleSummaryReusableStatistics();
    DoubleSummaryReusableStatistics taskLinkStat = new DoubleSummaryReusableStatistics();
    */






    public TermNode(K t, int maxEdges) {
        super();

        if (t instanceof Concept) c = (Concept)t; //HACK

        edge = new FixedLinkedHashMap<>(maxEdges);


        this.term = t;

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


    public final TermEdge putEdge(K b, TermEdge e) {
        TermEdge r = edge.put(b, e);
        modified |= (e != r);
        return r;
    }

    public TermEdge[] updateEdges() {
        final int s = edge.size();

        final TermEdge[] edges = this.edges;

        TermEdge[] e;
        if (edges.length != s)
            e = new TermEdge[s];
        else
            e = edges; //re-use existing array

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

    public final TermEdge[] getEdges() {
        return this.edges;
    }





    public static Color getTermColor(Termed term, ColorMatrix colors, double v) {
        return colors.get(
                (term.getTerm().op().ordinal() % colors.cc.length) / ((double) Op.values().length),
                v);
    }

    public TermEdge getEdge(K b) {
        return edge.get(b);
    }

    public Set<K> getEdgeSet() {
        if (edges.length == 0) return Collections.emptySet();

        Set<K> ss = Global.newHashSet(edges.length);
        for (TermEdge<TermNode<K>> ee : edges)
            ss.add(ee.bSrc.term);
        return ss;
        //edge.keySet());
    }

    public void removeEdges(Set<K> toRemove) {
        if (!toRemove.isEmpty()) {

            toRemove.forEach(edge::remove);
            modified = true;
        }

    }

    public final Term getTerm() {
        return term.getTerm();
    }

    public void commitEdges() {
        if (modified) {
            modified = false;
            if (edge.size() > 0)
                edges = updateEdges();
            else
                edges = TermEdge.empty;

        }

    }

    public static final class FixedLinkedHashMap<K,V> extends LinkedHashMap<K, V> {

        final int max_cap;

        public FixedLinkedHashMap(int cap) {
            super(cap, 0.75f, true);
            this.max_cap = cap;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > this.max_cap;
        }
    }
}

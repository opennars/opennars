package nars.guifx.graph2;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import nars.Global;
import nars.Op;
import nars.concept.Concept;
import nars.guifx.util.ColorMatrix;
import nars.term.Term;
import nars.term.Termed;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class TermNode<K extends Termed> extends Group {


    public static final TermNode[] empty = new TermNode[0];

    final public Map<K, TermEdge> edge = new FixedLinkedHashMap<>(4);

    /**
     * copy of termedge values for fast iteration during rendering
     */
    TermEdge[] edges = TermEdge.empty;
    boolean modified = false;

    public final K term;

    /** priority normalized to visual context */
    public double priNorm = 0;

    public Concept c = null;

    /*
    DoubleSummaryReusableStatistics termLinkStat = new DoubleSummaryReusableStatistics();
    DoubleSummaryReusableStatistics taskLinkStat = new DoubleSummaryReusableStatistics();
    */

    /**
     * cached from last set
     */
    private double scaled = 0.0;
    private double tx = 0.0;
    private double ty = 0.0;




    public TermNode(K t) {
        super();

        if (t instanceof Concept) c = (Concept)t; //HACK

        setManaged(false);
        setPickOnBounds(true);

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

    public void scale(double scale) {
        this.scaled = scale;


        setScaleX(scale);
        setScaleY(scale);

        //float conf = c != null ? c.getBeliefs().getConfidenceMax(0, 1) : 0;
            /*base.setFill(NARGraph.vis.get().getVertexColor(priNorm, conf));*/

        //setOpacity(0.75f + 0.25f * vertexScale);

        //System.out.println(scale + " " + vertexScale + " " + (int)(priorityDisplayedResolution * vertexScale));
    }


    public final void getPosition(final double[] v) {
        v[0] = tx;
        v[1] = ty;
    }

    //Point2D sceneCoord;// = new Point2D(0,0);

    final public TermNode move(final double x, final double y) {
        setTranslateX(this.tx = x);
        setTranslateY(this.ty = y);

        //sceneCoord = null;
        return this;
    }

    final public void move(final double[] v, final double speed, final double threshold) {
        move(v[0], v[1], speed, threshold);
    }
    final public void move(final double v0, final double v1, final double speed, final double threshold) {
        final double px = tx;
        final double py = ty;
        final double momentum = 1f - speed;
        final double nx = v0 * speed + px * momentum;
        final double ny = v1 * speed + py * momentum;
        final double dx = Math.abs(px - nx);
        final double dy = Math.abs(py - ny);
        if ((dx > threshold) || (dy > threshold)) {
            move(nx, ny);
        }
    }

    final public boolean move(final double[] v, final double threshold) {
        final double x = tx;
        final double y = ty;
        final double nx = v[0];
        final double ny = v[1];
        if (!((Math.abs(x - nx) < threshold) && (Math.abs(y - ny) < threshold))) {
            move(nx, ny);
            return true;
        }
        return false;
    }

    public final double width() {
        return scaled; //getScaleX();
    }

    public final double height() {
        return scaled; //getScaleY();
    }

//    public double sx() {
//        if (sceneCoord == null) sceneCoord = localToParent(0, 0);
//        return sceneCoord.getX();
//    }
//
//    public double sy() {
//        if (sceneCoord == null) sceneCoord = localToParent(0, 0);
//        return sceneCoord.getY();
//    }

    public final double x() {
        return tx;
    }

    public final double y() {
        return ty;
    }

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



    public boolean visible() {
        return isVisible() && getParent()!=null;
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

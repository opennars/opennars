package nars.guifx.graph2;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import nars.concept.Concept;
import nars.guifx.JFX;
import nars.guifx.NARfx;
import nars.term.Term;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by me on 9/5/15.
 */
public class TermNode extends Group {


    final public Map<Term, TermEdge> edge = new LinkedHashMap(8);

    /**
     * copy of termedge values for fast iteration during rendering
     */
    TermEdge[] edges = null;

    protected final Term term;
    private final Text label;
    public final Polygon base;
    Concept c = null;

    /** priority normalized to visual context */
    public double priNorm = 0;



    DoubleSummaryReusableStatistics termLinkStat = new DoubleSummaryReusableStatistics();
    DoubleSummaryReusableStatistics taskLinkStat = new DoubleSummaryReusableStatistics();


    /**
     * cached from last set
     */
    private double scaled;
    private double tx;
    private double ty;

    private boolean hover = false;
    private Color stroke;

    private static TermEdge[] empty = new TermEdge[0];


    public TermNode(Term t) {
        super();

        this.label = new Text(t.toStringCompact());
        base = JFX.newPoly(6, 2.0);


        this.term = t;
        //this.c = nar.concept(t);

        randomPosition(150, 150);

        label.setFill(Color.WHITE);
        label.setBoundsType(TextBoundsType.VISUAL);

        label.setPickOnBounds(false);
        label.setMouseTransparent(true);
        label.setFont(HexagonsVis.nodeFont);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setSmooth(false);
        //titleBar.setManaged(false);
        //label.setBoundsType(TextBoundsType.VISUAL);

        base.setStrokeType(StrokeType.INSIDE);

        base.setOnMouseClicked(e -> {
            //System.out.println("click " + e.getClickCount());
            if (e.getClickCount() == 2) {
                if (c != null)
                    NARfx.run((a,b) -> {
                        //...
                    });
            }
        });

        EventHandler<MouseEvent> mouseActivity = e -> {
            if (!hover) {
                base.setStroke(Color.ORANGE);
                base.setStrokeWidth(0.05);
                hover = true;
            }
        };
        //base.setOnMouseMoved(mouseActivity);
        base.setOnMouseEntered(mouseActivity);
        base.setOnMouseExited(e -> {
            if (hover) {
                base.setStroke(null);
                base.setStrokeWidth(0);
                hover = false;
            }
        });

        setPickOnBounds(false);


        getChildren().setAll(base, label);//, titleBar);


        //update();

        base.setLayoutX(-0.5f);
        base.setLayoutY(-0.5f);


        label.setLayoutX(-getLayoutBounds().getWidth() / (2) + 0.25);

        base.setCacheHint(CacheHint.SCALE_AND_ROTATE);
        base.setCache(true);

        label.setCacheHint(CacheHint.DEFAULT);
        label.setCache(true);




    }


    public void randomPosition(double bx, double by) {
        move(NARGraph.rng.nextDouble() * bx, NARGraph.rng.nextDouble() * by);
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

        float conf = c != null ? c.getBeliefs().getConfidenceMax(0, 1) : 0;
            /*base.setFill(NARGraph.vis.get().getVertexColor(priNorm, conf));*/

        //setOpacity(0.75f + 0.25f * vertexScale);

        //System.out.println(scale + " " + vertexScale + " " + (int)(priorityDisplayedResolution * vertexScale));
    }


    public final void getPosition(final double[] v) {
        v[0] = tx;
        v[1] = ty;
    }

    Point2D sceneCoord;// = new Point2D(0,0);

    final public TermNode move(final double x, final double y) {
        setTranslateX(this.tx = x);
        setTranslateY(this.ty = y);

        sceneCoord = null;
        return this;
    }


    final public void move(final double[] v, final double speed, final double threshold) {
        final double px = tx;
        final double py = ty;
        final double momentum = 1f - speed;
        final double nx = v[0] * speed + px * momentum;
        final double ny = v[1] * speed + py * momentum;
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

    public double sx() {
        if (sceneCoord == null) sceneCoord = localToParent(0, 0);
        return sceneCoord.getX();
    }

    public double sy() {
        if (sceneCoord == null) sceneCoord = localToParent(0, 0);
        return sceneCoord.getY();
    }

    public final double x() {
        return tx;
    }

    public final double y() {
        return ty;
    }

    public final TermEdge putEdge(Term b, TermEdge e) {
        TermEdge r = edge.put(b, e);
        if (e != r)
            edges = null;
        return r;
    }

    public TermEdge[] updateEdges() {
        final int s = edge.size();
        //if (s == 0) return edges = empty;

        //return edges = edge.values().toArray(new TermEdge[s]);

        TermEdge[] e;
        if (edges == null || edges.length == s)
            e = new TermEdge[s];
        else
            e = edges; //re-use existing array

        return edges = edge.values().toArray(e);
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

    public TermEdge[] getEdges() {
        if (edges == null) {
            if (edge.size() > 0)
                updateEdges();
            else
                edges = empty;
        }

        return edges;
    }


}

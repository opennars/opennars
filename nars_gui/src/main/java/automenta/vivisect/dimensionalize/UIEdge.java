package automenta.vivisect.dimensionalize;

import nars.gui.output.graph.nengo.UIVertex;
import ca.nengo.ui.lib.world.piccolo.primitive.ShapeObject;
import javolution.util.FastSet;
import nars.logic.entity.Named;
import nars.util.graph.NARGraph;
import org.apache.commons.math3.util.FastMath;

import java.awt.*;
import java.util.Set;

/**
* Created by me on 3/12/15.
*/
public class UIEdge<V extends Named> extends ShapeObject implements Named<String> {

    final V s, t;

    /** items contained in this edge */
    public final Set<Named> e = new FastSet<Named>().atomic();
    private final String name;

    float termlinkPriority, tasklinkPriority, priority;


    public Shape shape;
    private float angle;
    private double dist;
    boolean halfQuad = true; //use triangle of half a quad, to show different non-overlapping bidirectional edges

    public UIEdge(V s, V t) {
        super();
        this.s = s;
        this.t = t;
        this.name = s.name().toString() + ':' + t.name();

        this.shape = new Polygon();
        getGeometry().setPathTo(shape); //forces update

    }

    @Override
    protected void init() {
        super.init();
        //setSelectable(true);
        //setPickable(true);
        setStroke(null);
    }

    @Override
    public String name() {
        return name;
    }


    public void update() {



        tasklinkPriority = termlinkPriority = priority = 0;
        int ntask = 0, nterm = 0, np = 0;
        for (Named n : e) {
            if (n.getClass() == NARGraph.TaskLinkEdge.class) {
                float bp = ((NARGraph.TaskLinkEdge) n).getBudget().getPriority();
                priority += bp;
                tasklinkPriority += bp;
                ntask++;
                np++;
            }
            else if (n.getClass() == NARGraph.TermLinkEdge.class) {
                float bp = ((NARGraph.TermLinkEdge) n).getBudget().getPriority();
                priority += bp;
                termlinkPriority += bp;
                nterm++;
                np++;
            }
            else {

            }
        }
        if (np > 0) priority /= np;
        if (ntask > 0) tasklinkPriority /= ntask;
        if (nterm > 0) termlinkPriority /= nterm;


        UIVertex source = (UIVertex) getSource();
        if (source == null) return;
        if (!source.ui.getVisible()) return;

        UIVertex target = (UIVertex) getTarget();
        if (target == null) return;
        if (!target.ui.getVisible()) return;




        double tx = target.getX()-source.getX();
        double ty = target.getY()-source.getY();

        double pscale = getPNode().getParent().getParent().getScale(); //parent of the parent because nodes are collected in a container node of the vertex, which is beneath the icon
        tx/=pscale;
        ty/=pscale;


        final float sourceRadius = 48;//(float)target.ui.getWidth();
        final float targetRadius = 12;//(float)target.ui.getWidth();

        setPaint(getEdgeColor(this));


        shape = drawArrow((Polygon) shape, null, sourceRadius, 0, 0, (int) tx, (int) ty, targetRadius);
        if (shape == null) {
            setVisible(false);
        }
        else {
            setVisible(true);

            //equivalent to: setPathTo(shape) but without firing an event
            getGeometry().getPath().reset();
            getGeometry().getPath().append(shape, false);
            getGeometry().updateBoundsFromPath();
            getGeometry().invalidatePaint();

            //getGeometry().setPathTo(shape); //forces update
        }


    }


    public float getPriorityMean() {
        return priority;
    }

    public float getTermlinkPriority() {
        return termlinkPriority;
    }

    public float getTasklinkPriority() {
        return tasklinkPriority;
    }

    public UIEdge<V> add(Named item) {
        e.add(item);
        return this;
    }
    public UIEdge<V> remove(Named item) {
        e.remove(item);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return name().equals(((UIEdge)obj).name());
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public String toString() {
        return name();
    }

    public V getSource() {
        return s;
    }

    public V getTarget() {
        return t;
    }

    float arrowHeadScale = 1f / 2f;

    public Polygon drawArrow(Polygon p, final Color color, final float thick, final int x1, final int y1, final int x2, final int y2, float destinationRadius) {
        final float arrowHeadRadius = /*len **/ arrowHeadScale * (thick);
        if (arrowHeadRadius > 0) {

            float dx = x2 - x1;
            float dy = y2 - y1;

            float angle = (float) (Math.atan2(dy, dx));
            final float arrowAngle = (float) FastMath.PI / 4;

            float len = (float) Math.sqrt(dx * dx + dy * dy);// - destinationRadius;
            if (len <= 0) return null;

            final int ix2 = (int) (Math.cos(angle) * len + x1);
            final int iy2 = (int) (Math.sin(angle) * len + y1);

            final double aMin = angle - Math.PI - arrowAngle;
            final double aMax = angle - Math.PI + arrowAngle;

            int plx =0 , ply = 0;
            if (!halfQuad) {
                plx = (int) (Math.cos(aMin) * arrowHeadRadius);
                ply = (int) (Math.sin(aMin) * arrowHeadRadius);
            }
            int prx = (int) Math.floor(Math.cos(aMax) * arrowHeadRadius);
            int pry = (int) Math.floor(Math.sin(aMax) * arrowHeadRadius);



            //Triangle
            //g.triangle(x2, y2, x2 + prx, y2 + pry, x2 + plx, y2 + ply);

            //Quad
            //(x2, y2, x2 + prx, y2 + pry, x1, y1, x2 + plx, y2 + ply);


            if (p!=null)
                p.reset();
            else
                p = new Polygon(); //TODO recycle this .reset()
            p.addPoint(ix2 - x2, iy2 - y2);
            p.addPoint(ix2 + prx - x2, iy2 + pry - y2);
            p.addPoint(x1 - x2, y1 - y2);
            if (!halfQuad)
                p.addPoint(x2 + plx - x2, y2 + ply - y2);

            //g.setPaint(color);
            //g.fillPolygon(p);
            return p;
        }

        return null;
    }





    //final ColorArray red = new ColorArray(64, new Color(0.4f, 0.2f, 0.2f, 0.5f), new Color(1f, 0.7f, 0.3f, 1.0f));
    //final ColorArray blue = new ColorArray(64, new Color(0.2f, 0.2f, 0.4f, 0.5f), new Color(0.3f, 0.7f, 1f, 1.0f));



    public Color getEdgeColor(UIEdge e) {
        float priority = (float) e.getPriorityMean();
        float termlinkPriority = (float) e.getTermlinkPriority();
        float tasklinkPriority = (float) e.getTasklinkPriority();


        return new Color(0.25f + 0.75f * termlinkPriority, 0.25f, 0.25f + 0.75f * tasklinkPriority, 0.25f + 0.75f * priority);

//        final Object x = e.e;
//        if (x instanceof TermLink) {
//            float p = ((TermLink)x).budget.getPriority();
//            return red.get(p);
//        }
//        else if (e.e instanceof TaskLink) {
//            float p = ((TaskLink)x).budget.getPriority();
//            return blue.get(p);
//        }
//        return Color.WHITE;
    }

    @Override
    public void layoutChildren() {


    }
}

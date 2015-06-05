package nars.gui.output.graph.nengo;

import ca.nengo.ui.lib.world.piccolo.primitive.ShapeObject;
import nars.util.data.id.Named;
import nars.util.graph.NARGraph;
import org.apache.commons.math3.util.FastMath;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
* Created by me on 3/12/15.
*/
public class UIEdge<V extends UIVertex> extends ShapeObject implements Named<String> {

    final V s, t;
    final double tEpsilon = 2; //# pixel difference to cause repaint

    /** items contained in this edge */
    public final Set<Object> components = new CopyOnWriteArraySet(); //new FastSet<Named>().atomic();
    private final String name;

    float termlinkPriority, tasklinkPriority, priority;


    public Shape shape;
    private float angle;
    private double dist;
    boolean halfQuad = true; //use triangle of half a quad, to show different non-overlapping bidirectional edges
    private double lastTX = Double.NaN;
    private double lastTY = Double.NaN;
    private double pscale;

    public UIEdge(V s, V t) {
        super();
        this.s = s;
        this.t = t;
        this.name = s.name() + ':' + t.name();

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


    public void render() {

        if (pscale == 0) return;

        UIVertex source = (UIVertex) getSource();
        if (source == null || !source.ui.getVisible()) {
            setVisible(false);
            return;
        }

        UIVertex target = (UIVertex) getTarget();
        if (target == null || !target.ui.getVisible()) {
            setVisible(false);
            return;
        }


        double tx = (target.getX()-source.getX())/pscale;
        double ty = (target.getY()-source.getY())/pscale;

        if (Double.isFinite(lastTX)) {
            double dtx = FastMath.abs(lastTX - tx);
            double dty = FastMath.abs(lastTY - ty);
            if ((dtx < tEpsilon) && (dty < tEpsilon)) {
                return;
            }
        }


        final float sourceRadius = 48;//(float)target.ui.getWidth();
        final float targetRadius = 12;//(float)target.ui.getWidth();



        shape = drawArrow((Polygon) shape, null, sourceRadius, (int) tx, (int) ty, 0, 0,targetRadius);
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

        lastTX = tx;
        lastTY = ty;

    }

    @Override
    public void setVisible(boolean isVisible) {
        super.setVisible(isVisible);
        if (!isVisible) {
            lastTX = lastTY = Double.NaN;
        }
    }

    public void update() {

        pscale = 0;
        if (getPNode()==null) return;
        if (getPNode().getParent()==null) return;
        if (getPNode().getParent().getParent()==null) return;
        pscale = getPNode().getParent().getParent().getScale(); //parent of the parent because nodes are collected in a container node of the vertex, which is beneath the icon

        //TODO move this to a TermNode specific subclass

        tasklinkPriority = termlinkPriority = priority = 0;
        int ntask = 0, nterm = 0, np = 0;
        for (Object n : components) {
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

        setPaint(getEdgeColor());


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

    public UIEdge<V> add(Object item) {
        components.add(item);
        return this;
    }
    public UIEdge<V> remove(Object item) {
        components.remove(item);
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



    public Color getEdgeColor() {
        float priority = (float) getPriorityMean();
        float termlinkPriority = (float) getTermlinkPriority();
        float tasklinkPriority = (float) getTasklinkPriority();
        // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality

        return new Color(0.5f + 0.5f * termlinkPriority, 0.5f, 0.5f + 0.5f * tasklinkPriority, 0.5f + 0.5f * priority);

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

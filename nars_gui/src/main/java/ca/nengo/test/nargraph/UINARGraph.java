package ca.nengo.test.nargraph;

import automenta.vivisect.dimensionalize.UIEdge;
import automenta.vivisect.swing.ColorArray;
import ca.nengo.ui.lib.world.piccolo.WorldGroundImpl;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NetworkViewer;
import ca.nengo.ui.model.viewer.NodeViewer;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import org.apache.commons.math3.util.FastMath;

import java.awt.*;

/**
* Created by me on 3/12/15.
*/
public class UINARGraph extends UINetwork {

    float arrowHeadScale = 1f / 4f;

    private final TestNARGraph.NARGraphNode nargraph;

    public UINARGraph(TestNARGraph.NARGraphNode n) {
        super(n);
        this.nargraph = n;
    }

    @Override
    public ModelIcon getIcon() {
        return (ModelIcon) super.getIcon();
    }

    @Override
    public NodeViewer createViewerInstance() {
        return new UINARGraphViewer(this);
    }

    public Polygon drawArrow(final Graphics2D g, Polygon p, final Color color, final float thick, final int x1, final int y1, final int x2, final int y2, float destinationRadius) {
        final float arrowHeadRadius = /*len **/ arrowHeadScale * (thick);
        if (arrowHeadRadius > 0) {

            float dx = x2 - x1;
            float dy = y2 - y1;

            float angle = (float) (FastMath.atan2(dy, dx));
            final float arrowAngle = (float) FastMath.PI / 12f + thick / 200f;

            float len = (float) FastMath.sqrt(dx * dx + dy * dy) - destinationRadius;
            if (len <= 0) return null;

            final int ix2 = (int) (FastMath.cos(angle) * len + x1);
            final int iy2 = (int) (FastMath.sin(angle) * len + y1);

            final double aMin = angle - Math.PI - arrowAngle;
            final double aMax = angle - Math.PI + arrowAngle;

            int plx = (int) (FastMath.cos(aMin) * arrowHeadRadius);
            int ply = (int) (FastMath.sin(aMin) * arrowHeadRadius);
            int prx = (int) (FastMath.cos(aMax) * arrowHeadRadius);
            int pry = (int) (FastMath.sin(aMax) * arrowHeadRadius);



            //Triangle
            //g.triangle(x2, y2, x2 + prx, y2 + pry, x2 + plx, y2 + ply);

            //Quad
            //(x2, y2, x2 + prx, y2 + pry, x1, y1, x2 + plx, y2 + ply);


            if (p!=null)
                p.reset();
            else
                p = new Polygon(); //TODO recycle this .reset()
            p.addPoint(ix2, iy2);
            p.addPoint( ix2 + prx, iy2 + pry);
            p.addPoint( x1, y1);
            p.addPoint(  x2 + plx, y2 + ply );

            g.setPaint(color);
            g.fillPolygon(p);
            return p;
        }

        return null;
    }




    protected void drawEdges(ca.nengo.ui.lib.world.PaintContext paintContext) {

        UIEdge<UIVertex>[] ee = nargraph.getEdges();
        if (ee == null) return;

        Graphics2D g = paintContext.getGraphics();


            for (final UIEdge e : ee) {

                UIVertex source = (UIVertex) e.getSource();
                if (source == null) continue;
                UIVertex target = (UIVertex) e.getTarget();
                if (target == null) continue;

                if ((source.ui.isDestroyed() || target.ui.isDestroyed())) {
                    //System.out.println(source + " or " + target + " destroyed");
                    continue;
                }

                double sx = source.getX();
                double sy = source.getY();
                double tx = target.getX();
                double ty = target.getY();

                //System.out.println(source + " " + target + " " + sx + " " + sy + " " + tx + " "+ ty);

                final float sourceRadius = (float) source.ui.getWidth() / 2f;
                final float targetRadius = (float) target.ui.getWidth() / 2f;
                //g.drawLine((int)sx, (int)sy, (int)tx, (int)ty);
                e.shape = drawArrow(g, (Polygon) e.shape, getEdgeColor(e), sourceRadius, (int) sx, (int) sy, (int) tx, (int) ty, targetRadius);

        }
    }

    final ColorArray red = new ColorArray(64, new Color(0.4f, 0.2f, 0.2f, 0.5f), new Color(1f, 0.7f, 0.3f, 1.0f));
    final ColorArray blue = new ColorArray(64, new Color(0.2f, 0.2f, 0.4f, 0.5f), new Color(0.3f, 0.7f, 1f, 1.0f));



    public Color getEdgeColor(UIEdge e) {

        final Object x = e.e;
        if (x instanceof TermLink) {
            float p = ((TermLink)x).budget.getPriority();
            return red.get(p);
        }
        else if (e.e instanceof TaskLink) {
            float p = ((TaskLink)x).budget.getPriority();
            return blue.get(p);
        }
        return Color.WHITE;
    }

    class UINARGraphGround extends WorldGroundImpl /*ElasticGround*/ {

        @Override
        public void paint(ca.nengo.ui.lib.world.PaintContext paintContext) {
            drawEdges(paintContext);
            super.paint(paintContext);
        }


    }

    final private class UINARGraphViewer extends NetworkViewer {
        public UINARGraphViewer(UINARGraph g) {
            super(g, new UINARGraphGround());
        }

        @Override
        protected boolean isDropEffect() {
            return false;
        }
    }
}

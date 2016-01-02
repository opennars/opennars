package nars.guifx.graph2.impl;

import javafx.scene.canvas.GraphicsContext;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.scene.DefaultNodeVis;
import nars.term.Termed;

/** (fast, simple rendering) half edges are drawn as lines of certain thickness, meeting at the center point */
public class HalfHalfLineCanvasEdgeRenderer extends CanvasEdgeRenderer {

    @Override
    public void draw(TermEdge i, TermNode aSrc, TermNode bSrc, double x1, double y1, double x2, double y2) {
        double cx = 0.5 * (x1+x2);
        double cy = 0.5 * (y1+y2);

        drawHalf(i, aSrc, x1, y1, cx, cy);
        drawHalf(i, bSrc, x2, y2, cx, cy);
    }


    public void drawHalf(TermEdge i, TermNode t, double x1, double y1, double x2, double y2) {

        double p = i.getWeight();

        //double np = normalize(p);

        //System.out.println(p + " " + np + " " + minPri + " " + maxPri);

        //gfx.setStroke(colors.get(np));
        //gfx.setStroke(colors.get(np, te/(te+ta)));

        GraphicsContext gfx = this.gfx;

        //HACK specific to Term instances
        if (t.term instanceof Termed) {
            gfx.setStroke(
                    TermNode.getTermColor(t.term,
                        DefaultNodeVis.colorsTransparent,
                        0.6 /* baesOpacity */ +
                        0.4f * 0.5f * (i.pri + t.priNorm) )
            );
        }

    /*
        colors.get(
            (t.term.op().ordinal()%colors.cc.length)/((double) Op.values().length),
            p
        )
    );*/

        double mw = minWidth;
        gfx.setLineWidth(mw + p * (maxWidth-mw));

        gfx.strokeLine(x1, y1, x2, y2);
    }

}

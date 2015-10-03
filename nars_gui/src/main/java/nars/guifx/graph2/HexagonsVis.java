package nars.guifx.graph2;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import nars.guifx.NARfx;
import nars.term.Term;

/**
 * Created by me on 10/2/15.
 */
public class HexagonsVis implements VisModel {

    final static Font nodeFont = NARfx.mono(0.25);

    protected double minSize = 16;
    protected double maxSize = 64;




    @Override
    public TermNode newNode(Term term) {
        TermNode t = new TermNode(term);
        return t;
    }

    @Override
    public void accept(TermNode t) {
        float p, q;
        if (t == null) {
            return;
        }
        else {
            p = t.c.getPriority();
            q = t.c.getQuality();
        }


        Color cc = NARfx.colors.get(p, q);

        t.base.setFill(cc);


        t.scale(minSize + (maxSize - minSize) * t.priNorm);
    }


//
//    public double getVertexScaleByPri(Concept c) {
//        return c.getPriority();
//        //return (c != null ? c.getPriority() : 0);
//    }
//
//    public double getVertexScaleByConf(Concept c) {
//        if (c.hasBeliefs()) {
//            double conf = c.getBeliefs().getConfidenceMax(0, 1);
//            if (Double.isFinite(conf)) return conf;
//        }
//        return 0;
//    }


//
//    public Color getEdgeColor(double termMean, double taskMean) {
////            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality
////
////            return Color.hsb(25.0 + 180.0 * (1.0 + (termMean - taskMean)),
////                    0.95f,
////                    Math.min(0.75f + 0.25f * (termMean + taskMean) / 2f, 1f)
////                    //,0.5 * (termMean + taskMean)
////            );
////
//////            return new Color(
//////                    0.5f + 0.5f * termMean,
//////                    0,
//////                    0.5f + 0.5f * taskMean,
//////                    0.5f + 0.5f * (termMean + taskMean)/2f
//////            );
//        return null;
//    }

}

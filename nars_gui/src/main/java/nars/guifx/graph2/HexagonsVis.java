package nars.guifx.graph2;

import javafx.scene.paint.Color;
import nars.guifx.util.ColorMatrix;

/**
 * Created by me on 10/2/15.
 */
public class HexagonsVis implements VisModel {

    static final ColorMatrix colors = new ColorMatrix(24, 24,
            (priority, conf) -> {
                return Color.hsb(250.0 + 75.0 * (conf),
                        0.10f + 0.85f * priority,
                        0.10f + 0.5f * priority);
            }
    );

    double minSize = 16;
    double maxSize = 64;

    @Override
    public void accept(TermNode t) {
        float p = t.c.getPriority();
        float q = t.c.getQuality();

        t.base.setFill(colors.get(p, q));


        t.scale(minSize + (maxSize - minSize) * t.priNorm); //where is this generated
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

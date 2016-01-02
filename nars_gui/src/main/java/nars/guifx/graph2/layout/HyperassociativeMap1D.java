package nars.guifx.graph2.layout;

import nars.guifx.graph2.TermNode;
import nars.term.Term;
import nars.term.Termed;
import nars.util.data.Util;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * aligns the entries in a line
 */
public class HyperassociativeMap1D<N extends Termed> extends HyperassociativeMap2D {

    public HyperassociativeMap1D() {
    }

    @Override
    public double getSpeedFactor(TermNode termNode) {

            //return 120 + 120 / termNode.width(); //heavier is slower, forcing smaller ones to move faster around it
            return scaleFactor*5.0;

    }

    @Override
    public double getRadius(TermNode termNode) {
        return termNode.priNorm * 0.165;
    }

    @Override
    protected void init() {
        resetLearning();
        setLearningRate(0.5f);
        setRepulsiveWeakness(12.0);
        setAttractionStrength(12.0);
        setMaxRepulsionDistance(50);
        setEquilibriumDistance(0.0f);
    }

    @Override
    protected void recenterNode(TermNode node, ArrayRealVector v, ArrayRealVector center) {
        /*if (v!=null)
            sub(v, center);*/

        double[] vv = v.getDataRef();

        //int volume = node.c.getTerm().volume();
        double xTarget, yTarget, speedX, speedY;

//            //vertical line
//            {
//                xTarget = 0;
//                yTarget = 0;
//
//                speedX = 0.05f;
//                speedY = 0.01f;
//            }

        //radiating circle
        Term term = node.c.get();
        double theta = (term.hashCode() % 64) / 64.0 * (3.14159 * 2);
        int complexity = term.volume();

        double r = 100 * complexity + 200;

        xTarget = r * Math.cos( theta);
        yTarget = r * Math.sin( theta);

        speedX = 0.05f;
        speedY = 0.05f;


        vv[0] = Util.lerp(xTarget, vv[0], speedX);
        vv[1] = Util.lerp(yTarget, vv[1], speedY);
    }
}

package nars.guifx.graph2;

import nars.NAR;
import nars.guifx.IOPane;
import nars.guifx.NARide;
import nars.guifx.util.TabX;
import nars.nar.Default;
import nars.term.Term;
import nars.util.data.Util;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.io.IOException;

/**
 * Created by me on 10/2/15.
 */
public class WordCloud extends DefaultNARGraph {

    public WordCloud(NAR nar) {
        super(nar, new V(), new L(), new NARGrapher(64));
    }
    public static void main(String[] args) throws IOException {


        NAR n = new Default(256, 1,2,2);

        NARide.show(n, ide -> {

            n.input("a:b.");
            n.input("b:c.");
            n.input("c:d.");
            n.frame(10);

            ide.content.getTabs().setAll(new TabX("Graph", new WordCloud(n)));
            ide.addView(new IOPane(n));


            n.frame(5);

        });

    }

    static class V extends HexagonsVis  {

        public V() {
            minSize = 32;
            maxSize = 64;
        }

        @Override
        public TermNode newNode(Term term) {
            return super.newNode(term);
        }

//            @Override
//            public void accept(TermNode t) {
//
//            }

    }


    /**
     * aligns the entries in a line
     */
    static class L extends HyperassociativeMapLayout {



        public L() {
            super();
        }

        @Override
        public double getSpeedFactor(TermNode termNode) {

                //return 120 + 120 / termNode.width(); //heavier is slower, forcing smaller ones to move faster around it
                return scaleFactor*2.0;

        }

        @Override
        public double getRadius(TermNode termNode) {
            return termNode.priNorm * 0.165;
        }

        @Override
        protected void init() {
            resetLearning();
            setLearningRate(0.5f);
            setRepulsiveWeakness(6.0);
            setAttractionStrength(6.0);
            setMaxRepulsionDistance(50);
            setEquilibriumDistance(0.0f);
        }

        @Override
        protected void recenterNode(TermNode node, ArrayRealVector v, ArrayRealVector center) {
            super.recenterNode(node, v, center);
            double[] vv = v.getDataRef();

            float xTarget = 0;
            float yTarget = 0;

            float speedX = 0.05f;
            float speedY = 0.001f;

            vv[0] = Util.lerp(xTarget, vv[0], speedX);
            vv[1] = Util.lerp(yTarget, vv[1], speedY);
        }
    }
}

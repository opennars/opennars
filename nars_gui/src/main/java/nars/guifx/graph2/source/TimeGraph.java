//package nars.guifx.graph2.source;
//
//import nars.guifx.graph2.GraphSource;
//import nars.guifx.graph2.TermNode;
//import nars.guifx.graph2.impl.CanvasEdgeRenderer;
//import nars.guifx.graph2.layout.HyperassociativeMap2D;
//import nars.guifx.graph2.layout.IterativeLayout;
//import nars.guifx.graph2.scene.DefaultVis;
//import nars.term.Termed;
//import nars.util.data.random.XORShiftRandom;
//
//import java.util.Random;
//
///**
// * Created by me on 10/9/15.
// */
//abstract public class TimeGraph<K extends Termed> extends SpaceGrapher<K,TermNode<K>> implements GraphSource<K>, IterativeLayout<TermNode<K>> {
//
//    public TimeGraph(int size) {
//        super(null, new DefaultVis(), new CanvasEdgeRenderer(), size);
//    }
//
//    double axisTheta = 0;
//
//    final double thickness = 250;
//    double timeScale = 10f;
//    double now = 0; /* center of view */
//    double cutoff = 50;
//
//    final Random rng = new XORShiftRandom();
//
//    final HyperassociativeMap2D hmap = new HyperassociativeMap2D() {
//
//
//        @Override
//        public void init(TermNode n) {
//            resetLearning();
//            setLearningRate(0.4f);
//            setRepulsiveWeakness(repulseWeakness.get());
//            setAttractionStrength(attractionStrength.get());
//            setMaxRepulsionDistance(250);
//            setEquilibriumDistance(0.05f);
//        }
//
//        @Override
//        public void apply(TermNode node, double[] dataRef) {
//
//            long tt = getTime(node);
//
//            double dt = now - tt;
//            if (dt > cutoff) {
//                node.setVisible(false);
//                return;
//            }
//
//            now = Math.max(now, tt);
//
//            double y = dataRef[1];
//            if ((y > thickness) || (y < -thickness)) {
//                //TODO scale this by a realtime time-amount
//                dataRef[1] *= 0.99; //pressure to shrink y-axis to zero
//            }
//
//            dataRef[0] = -(dt * timeScale);
//            node.move(dataRef[0], dataRef[1], 0.25, 0.01);
//
//        }
//
//    };
//
//
//    abstract long getTime(TermNode node);
//
//
//    @Override
//    public void accept(SpaceGrapher graph) {
//    }
//
//
//    @Override
//    public void run(SpaceGrapher graph, int iterations) {
//
//    }
//
// }

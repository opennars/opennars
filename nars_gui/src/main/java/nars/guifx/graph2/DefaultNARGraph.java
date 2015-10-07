package nars.guifx.graph2;

import automenta.vivisect.dimensionalize.IterativeLayout;
import javafx.beans.InvalidationListener;
import nars.NAR;
import nars.guifx.annotation.Implementation;
import nars.guifx.annotation.ImplementationProperty;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * provides defalut settings for a NARGraph view
 */
public class DefaultNARGraph extends NARGraph {

//    @Implementation(values = {HexagonsVis.class})
//    public final ImplementationProperty<EdgeRenderer> visType = new ImplementationProperty();

    @Implementation(HyperassociativeMapLayout.class)
    @Implementation(CircleLayout.class)
    public final ImplementationProperty<IterativeLayout> layoutType = new ImplementationProperty();

    public DefaultNARGraph(NAR nar, int capacity) {
        this(nar,
                new HexagonsVis(),
                capacity);
    }

    public static class NullLayout implements IterativeLayout {

        public final ArrayRealVector zero = new ArrayRealVector(2);

        @Override
        public ArrayRealVector getPosition(Object vertex) {
            return zero;
        }

        @Override
        public void run(NARGraph graph, int iterations) {

        }

        @Override
        public void resetLearning() {

        }

        @Override
        public double getRadius(Object vertex) {
            return 0;
        }

        @Override
        public void init(Object n) {

        }
    }

    public DefaultNARGraph(NAR nar, VisModel v, int size) {

        super(new NARConceptSource(nar), size);

        InvalidationListener layoutChange = e -> {
            Class<? extends IterativeLayout> lc = layoutType.get();
            if (lc != null) {
                try {
                    IterativeLayout il = lc.newInstance();
                    layout.set(il);
                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            layout.set(new NullLayout());
        };
        layoutType.addListener(layoutChange);


        vis.set(v);


        edgeRenderer.set(new CanvasEdgeRenderer());
        //g.setEdgeRenderer(new QuadPolyEdgeRenderer());

        //g.setLayout(new CircleLayout<>());
        layoutChange.invalidated(null);

        //g.setLayout(new TimelineLayout());

    }

}

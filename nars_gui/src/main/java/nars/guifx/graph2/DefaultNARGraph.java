package nars.guifx.graph2;

import automenta.vivisect.dimensionalize.HyperOrganicLayout;
import automenta.vivisect.dimensionalize.IterativeLayout;
import javafx.beans.InvalidationListener;
import nars.NAR;
import nars.guifx.annotation.Implementation;
import nars.guifx.annotation.ImplementationProperty;
import nars.guifx.graph2.layout.*;

import static javafx.application.Platform.runLater;

/**
 * provides defalut settings for a NARGraph view
 */
public class DefaultNARGraph extends NARGraph<Object> {

//    @Implementation(values = {HexagonsVis.class})
//    public final ImplementationProperty<EdgeRenderer> visType = new ImplementationProperty();

    @Implementation(HyperOrganicLayout.class)
    @Implementation(HyperassociativeMap2D.class)
    @Implementation(Spiral.class)
    @Implementation(Circle.class)
    @Implementation(Grid.class)
    @Implementation(HyperassociativeMap1D.class)
    public final ImplementationProperty<IterativeLayout> layoutType = new ImplementationProperty();

    public DefaultNARGraph(NAR nar, int capacity) {
        this(nar,
                new HexagonsVis(),
                capacity, new CanvasEdgeRenderer());
    }

    public DefaultNARGraph(NAR nar, VisModel v, int size, CanvasEdgeRenderer edgeRenderer) {

        super(new NARConceptSource(nar), size);

        InvalidationListener layoutChange = e -> {
            Class<? extends IterativeLayout> lc = layoutType.get();
            if (lc != null) {
                try {
                    IterativeLayout il = lc.newInstance();
                    layout.set(il);
                    source.getValue().refresh();
                    rerender();
                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            else {
                layout.set(nullLayout);
            }
        };
        layoutType.addListener(layoutChange);


        vis.set(v);


        this.edgeRenderer.set(edgeRenderer);
        //g.setEdgeRenderer(new QuadPolyEdgeRenderer());

        //g.setLayout(new CircleLayout<>());
        runLater(()->
            layoutChange.invalidated(null));

        //g.setLayout(new TimelineLayout());

    }

}

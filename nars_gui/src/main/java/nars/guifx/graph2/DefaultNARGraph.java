package nars.guifx.graph2;

import automenta.vivisect.dimensionalize.IterativeLayout;
import nars.NAR;

/** provides defalut settings for a NARGraph view */
public class DefaultNARGraph extends NARGraph {

    public DefaultNARGraph(NAR nar, int capacity) {
        this(nar, new HexagonsVis(), new HyperassociativeMapLayout(), new NARGrapher(capacity));
    }

    public DefaultNARGraph(NAR nar, VisModel v, IterativeLayout l, NARGrapher grapher) {

        super(nar);

        input(grapher);

        vis.set( v );


        edgeRenderer.set(new CanvasEdgeRenderer());
        //g.setEdgeRenderer(new QuadPolyEdgeRenderer());

        //g.setLayout(new CircleLayout<>());
        layout.set(l);
        //g.setLayout(new TimelineLayout());

    }

}

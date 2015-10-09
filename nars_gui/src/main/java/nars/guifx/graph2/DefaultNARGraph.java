package nars.guifx.graph2;

import nars.NAR;
import nars.guifx.demo.GenericControlPane;
import nars.guifx.graph2.layout.CanvasEdgeRenderer;

/**
 * provides defalut settings for a NARGraph view
 */
public class DefaultNARGraph extends SpaceGrapher<Object> {

//    @Implementation(values = {HexagonsVis.class})
//    public final ImplementationProperty<EdgeRenderer> visType = new ImplementationProperty();


    public DefaultNARGraph(NAR nar, int capacity) {
        this(nar,
                new HexagonsVis(),
                capacity, new CanvasEdgeRenderer());
    }

    public DefaultNARGraph(NAR nar, VisModel v, int size, CanvasEdgeRenderer edgeRenderer) {

        super(new ConceptsSource(nar), v, edgeRenderer, size);

        GenericControlPane c = new GenericControlPane(this);
        c.layout();
        c.autosize();
        getChildren().add(c);

    }

}

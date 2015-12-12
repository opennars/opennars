package nars.guifx.graph2.source;

import javafx.beans.InvalidationListener;
import nars.guifx.annotation.Implementation;
import nars.guifx.annotation.ImplementationProperty;
import nars.guifx.graph2.GraphSource;
import nars.guifx.graph2.NodeVis;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.impl.CanvasEdgeRenderer;
import nars.guifx.graph2.layout.*;
import nars.guifx.util.POJOPane;

import java.util.function.BiFunction;

/**
 * provides defalut settings for a NARGraph view
 */
public class DefaultGrapher extends SpaceGrapher {

    @Implementation(HyperOrganicLayout.class)
    @Implementation(HyperassociativeMap2D.class)
    @Implementation(Spiral.class)
    @Implementation(Circle.class)
    @Implementation(Grid.class)
    @Implementation(HyperassociativeMap1D.class)
    @Implementation(Hilbert.class)
    //@Implementation(TimeGraph.class)
    public final ImplementationProperty<IterativeLayout> layoutType = new ImplementationProperty();

    final InvalidationListener layoutChange;
    final POJOPane controls = new POJOPane(this);

//    public DefaultGrapher(int capacity, ConceptsSource source) {
//        this(
//                source, new DefaultNodeVis(), capacity, edg
//                new CanvasEdgeRenderer());
//    }

    public DefaultGrapher(GraphSource source,
                          int size,
                          NodeVis v,
                          BiFunction<TermNode, TermNode, TermEdge> edgeBuilder,
                          CanvasEdgeRenderer edgeRenderer) {

        super(source, v, size, edgeBuilder, edgeRenderer);

        layoutChange = e -> {
            updateLayoutType();
        };

        layoutType.addListener(layoutChange);





        controls.layout();
        controls.autosize();
        getChildren().add(controls);

        layoutChange.invalidated(null);




    }

    private void updateLayoutType() {
        IterativeLayout il = layoutType.getInstance();
        if (il!=null) {
            layout.set(il);
            layoutUpdated();
        } else {
            layout.set(nullLayout);
        }

    }

//    public void setLayout(Class<? extends IterativeLayout> c) {
//
//        layoutType.setValue(c);
//        layoutChange.invalidated(null);
//
//
//
//        //layoutType.unbind();
//        //layoutType.setValue(c);
//        //layoutType.addListener(layoutChange);
//        //updateLayoutType();
//        //layoutChange.invalidated(null);
//    }
}

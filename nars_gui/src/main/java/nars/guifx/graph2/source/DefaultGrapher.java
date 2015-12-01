package nars.guifx.graph2.source;

import javafx.beans.InvalidationListener;
import nars.guifx.annotation.Implementation;
import nars.guifx.annotation.ImplementationProperty;
import nars.guifx.graph2.ConceptsSource;
import nars.guifx.graph2.GraphSource;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.VisModel;
import nars.guifx.graph2.impl.CanvasEdgeRenderer;
import nars.guifx.graph2.layout.*;
import nars.guifx.graph2.scene.DefaultVis;
import nars.guifx.util.POJOPane;
import nars.term.Termed;

import static javafx.application.Platform.runLater;

/**
 * provides defalut settings for a NARGraph view
 */
public class DefaultGrapher<K extends Termed, V extends TermNode<K>> extends SpaceGrapher<K,V> {

    @Implementation(HyperOrganicLayout.class)
    @Implementation(HyperassociativeMap2D.class)
    @Implementation(Spiral.class)
    @Implementation(Circle.class)
    @Implementation(Grid.class)
    @Implementation(HyperassociativeMap1D.class)
    @Implementation(Hilbert.class)
    //@Implementation(TimeGraph.class)
    public final ImplementationProperty<IterativeLayout> layoutType = new ImplementationProperty();


    public DefaultGrapher(int capacity, ConceptsSource source) {
        this(
                source, capacity, new DefaultVis(),
                new CanvasEdgeRenderer());
    }

    public DefaultGrapher(GraphSource source, int size, VisModel v, CanvasEdgeRenderer edgeRenderer) {

        super(source, v, edgeRenderer, size);

        InvalidationListener layoutChange = e -> {
            IterativeLayout il = layoutType.getInstance();
            if (il!=null) {
                layout.set(il);
                layoutUpdated();
            } else {
                layout.set(nullLayout);
            }
        };

        layoutType.addListener(layoutChange);

        runLater(() -> layoutChange.invalidated(null));

        POJOPane c = new POJOPane(this);
        c.layout();
        c.autosize();
        getChildren().add(c);


    }

}

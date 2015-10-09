package nars.guifx.graph2;

import javafx.beans.InvalidationListener;
import nars.guifx.annotation.Implementation;
import nars.guifx.annotation.ImplementationProperty;
import nars.guifx.demo.GenericControlPane;
import nars.guifx.graph2.layout.*;
import nars.term.Termed;

import static javafx.application.Platform.runLater;

/**
 * provides defalut settings for a NARGraph view
 */
public class DefaultNARGraph<K extends Termed, V extends TermNode<K>> extends SpaceGrapher<K,V> {

    @Implementation(HyperOrganicLayout.class)
    @Implementation(HyperassociativeMap2D.class)
    @Implementation(Spiral.class)
    @Implementation(Circle.class)
    @Implementation(Grid.class)
    @Implementation(HyperassociativeMap1D.class)
    @Implementation(ConceptComet.class)
    public final ImplementationProperty<IterativeLayout> layoutType = new ImplementationProperty();


    public DefaultNARGraph(int capacity, ConceptsSource source) {
        this(
                source, capacity, new DefaultVis(),
                new CanvasEdgeRenderer());
    }

    public DefaultNARGraph(GraphSource source, int size, VisModel v, CanvasEdgeRenderer edgeRenderer) {

        super(source, v, edgeRenderer, size);

        InvalidationListener layoutChange = e -> {
            Class<? extends IterativeLayout> lc = layoutType.get();
            if (lc != null) {
                try {
                    IterativeLayout il = lc.newInstance();
                    layout.set(il);
                    layoutUpdated();


                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                layout.set(nullLayout);
            }
        };
        layoutType.addListener(layoutChange);

        runLater(() -> layoutChange.invalidated(null));

        GenericControlPane c = new GenericControlPane(this);
        c.layout();
        c.autosize();
        getChildren().add(c);


    }

}

package nars.guifx;

import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import nars.concept.Concept;

/**
 * Created by me on 10/15/15.
 */
public class ConceptSummaryPane extends BorderPane {

    private final Concept koncept;
    private final StringProperty bottomText;

    public ConceptSummaryPane(Concept koncept) {
        super();

        this.koncept = koncept;

        setCenter(new Button(koncept.getTerm().toStringCompact()));
        Label l;
        setBottom(
                l = new Label()
        );
        bottomText = l.textProperty();

    }

    protected void update() {
        String s = " ";
        if (koncept.hasBeliefs())
            s += koncept.getBeliefs().top().toString() + " ";
        if (koncept.hasGoals())
            s += koncept.getGoals().top().toString();
        bottomText.set(s);
    }

}

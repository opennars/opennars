package nars.guifx;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import nars.NAR;
import nars.concept.Concept;

/**
 * Created by me on 8/10/15.
 */
public class ConceptPane extends BorderPane {

    public ConceptPane(NAR nar, Concept c) {

        setCenter(new Label(c.toInstanceString()));
    }
}

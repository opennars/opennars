package nars.guifx;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import nars.NAR;
import nars.concept.Concept;
import nars.task.Task;

import static nars.guifx.NARfx.scrolled;

/**
 * Created by me on 8/14/15.
 */
public class TaskPane extends BorderPane {

    public TaskPane(NAR nar, Task c) {
        super();

        TextArea ta = new TextArea(c.getExplanation());
        ta.setEditable(false);

        setTop(scrolled(ta, true, false));

        Button conceptButton = new Button(c.getTerm().toStringCompact());
        conceptButton.setOnMouseClicked(e -> {
            Concept concept = nar.concept(c.getTerm());
            if (concept!=null) {
                    NARfx.window(nar, concept);
//                ConceptPane cp = new ConceptPane(nar, concept);
//                cp.setPrefSize(getWidth(), 300);
//                setCenter(cp);
//
//                conceptButton.setVisible(false);
//                autosize();
//                layout();
            }
        });
        conceptButton.setTooltip(new Tooltip("Goto Concept"));

        FlowPane ctl = new FlowPane(
                conceptButton
        );
        setBottom(ctl);
    }

}

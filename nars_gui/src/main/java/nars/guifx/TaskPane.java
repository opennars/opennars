package nars.guifx;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

        TextArea ta = new TextArea(c.getExplanation());
        ta.setEditable(false);

        setCenter(scrolled(ta, true, false));

        Button reinforceButton = new Button("Reinforce");
        reinforceButton.setAccessibleText("Re-input task");
        reinforceButton.setOnMouseClicked(e -> {
            //nar.input(new AbstractTask(c));
        });

        Button conceptButton = new Button("Concept" + c.term().toStringCompact());
        conceptButton.setOnMouseClicked(e -> {
            Concept concept = nar.concept(c);
            if (concept!=null) {
                NARfx.newWindow(nar, concept);
//                ConceptPane cp = new ConceptPane(nar, concept);
//                cp.setPrefSize(getWidth(), 300);
//                setCenter(cp);
//
//                conceptButton.setVisible(false);
//                autosize();
//                layout();
            }
        });
        conceptButton.setAccessibleText("Concept");

        FlowPane ctl = new FlowPane(
            conceptButton
        );
        setBottom(ctl);

//        if (c.isQuestOrQuestion()) {
//            setCenter(new QuestionPane(c));
//        }
//        else {
//            //??
//        }
    }

    private static class QuestionPane extends BorderPane {
        public QuestionPane(Task c) {
            setTop(new Label("Answers:"));
            //TODO ..
        }
    }
}

package nars.guifx;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import nars.concept.Concept;

import java.util.concurrent.atomic.AtomicBoolean;

import static javafx.application.Platform.runLater;


public class ConceptSummaryPane extends BorderPane {


    private final Concept obj;
    private final Button label;
    //final Label subLabel = new Label();
    final AtomicBoolean pendingUpdate = new AtomicBoolean(false);

    public ConceptSummaryPane(Concept obj) {
        super();

        this.obj = obj;

        setCenter(label = new Button(obj.getTerm().toStringCompact()));
        //label.getStylesheets().clear();
        label.setTextAlignment(TextAlignment.LEFT);
        setAlignment(label, Pos.CENTER_LEFT);

//        setAlignment(subLabel, Pos.CENTER_LEFT);
//        subLabel.setTextAlignment(TextAlignment.LEFT);
//
////        subLabel.setScaleX(0.5f);
////        subLabel.setScaleY(0.5f);
//        setAlignment(subLabel, Pos.CENTER_LEFT);
//        subLabel.getStyleClass().add("sublabel");
//
//        setBottom(subLabel);

        update();
    }

    public void update() {

        if (pendingUpdate.compareAndSet(false, true)) {
            runLater(() -> {
                pendingUpdate.set(false);

                float pri = obj.getPriority();

                label.setStyle(JFX.fontSize(((1.0f + pri) * 100.0f)));

                label.setTextFill(NARfx.hashColor(obj.getTerm().hashCode(),
                        pri, Plot2D.ca));

                /*setBackground(new Background(
                        new BackgroundFill(
                            Color.BLUE, new CornerRadii(0), new Insets(0,0,0,0)
                )));*/

//                StringBuilder sb = new StringBuilder();
//
//                if (obj.hasBeliefs()) {
//                    Task topBelief = obj.getBeliefs().top();
//                    topBelief.appendTo(sb, obj.getMemory(), false);
//                }
//                if (obj.hasGoals()) {
//                    Task topGoal = obj.getGoals().top();
//                    topGoal.appendTo(sb, obj.getMemory(), false);
//                }
//
//                subLabel.setText(sb.toString());

                layout();
            });
        }
    }

}

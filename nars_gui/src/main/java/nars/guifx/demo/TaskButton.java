package nars.guifx.demo;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.TaskPane;
import nars.guifx.util.NSlider;
import nars.task.Task;
import org.jetbrains.annotations.NotNull;

import static javafx.application.Platform.runLater;
import static javafx.scene.paint.Color.hsb;

/**
 * Created by me on 12/13/15.
 */ //public static class TaskButton extends ToggleButton {
public class TaskButton extends Label {

    private final Task task;

    public TaskButton(NAR nar, Task t) {
        super(labelize(t.toStringWithoutBudget(null)));
        this.task = t;

        getStyleClass().clear();


        setCursor(Cursor.CROSSHAIR);
        setWrapText(true);

        hoverProperty().addListener(h -> {

            if (isHover()) {
                setTextFill(Color.WHITE);
            } else {
                setTextFill(getColor());
            }

        });


        setOnMouseClicked(c -> {
            //setSelected(false);
            setFocused(false);

            Popup p = new Popup();
            TaskPane tp = new TaskPane(nar,t);
            tp.setTop(new FlowPane(
                new NSlider("pri", 100, 25, 0.5f),
                new Button("+")
            ));
            tp.setBackground(Background.EMPTY);
            ((Pane)tp.getTop()).setBackground(Background.EMPTY);
            p.getContent().add(tp);

            p.setOpacity(0.75f);
            p.setAutoHide(true);
            p.setAutoFix(true);

            p.show(getScene().getWindow(), c.getSceneX(), c.getSceneY());
        });

        runLater(this::update);

    }

    private static String labelize(String s) {
        //https://en.wikipedia.org/wiki/List_of_logic_symbols
        return s.replace("-->","→")
                .replace("<->","↔")
                //.replace("==>","⇒")
                .replace("<=>","⇄")
                .replace("||", "⇵")
                ;
        //↔ ⇔ ⇒ ⇄ ⇾ ⇥ ⇵
    }

    public void update() {
        float pri = task.getPriority();
        float priToFontSize = pri * 30f;
//            getStyleClass().clear();
//            getStylesheets().clear();
//            //setStyle("-fx-background-color: #FFFFFF !important;");
//            setStyle("-fx-base: #FFFFFF !important;");
//            setStyle("-fx-padding: 5px !important;");
//            //setStyle("-fx-border-radius: 20;");
        final float minFontSize = 12;
        setFont(NARfx.mono(minFontSize + priToFontSize));

        Color c = getColor();
//            setBackground(new Background(
//                    new BackgroundFill(
//                        c,
//                        CornerRadii.EMPTY,
//                        Insets.EMPTY)));
        setTextFill(c);


    }

    @NotNull
    private Color getColor() {
        float pri = task.getPriority();
        return hsb(
                (task.op().ordinal() / 64f) * 360.0,
                0.4, 0.7, 0.75f + pri * 0.25f
        );
    }

}

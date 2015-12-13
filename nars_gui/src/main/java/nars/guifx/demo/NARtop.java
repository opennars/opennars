package nars.guifx.demo;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Stage;
import nars.Global;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.TaskPane;
import nars.guifx.util.NSlider;
import nars.nar.Default;
import nars.task.Task;
import nars.task.flow.SetTaskPerception;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static javafx.application.Platform.runLater;
import static javafx.scene.paint.Color.hsb;

/**
 * Created by me on 12/12/15.
 */
public class NARtop extends BorderPane {

    final SetTaskPerception active;
    //final FlowPane buttons = new FlowPane();
    final TextFlow buttons = new TextFlow();
    final Map<Task,TaskButton> taskButtons = new HashMap();
    private final NAR nar;

    public NARtop(NAR d) {
        super();

        this.nar = d;

        setCenter(buttons);
        //setLeft(new TreePane(d));

        active = new SetTaskPerception(d.memory, f -> {
            update();
        });
        d.memory.eventTaskProcess.on(tp -> {
            Task t = (Task) tp.getTask();
            if (t.isInput()) {
                runLater( () -> {
                    addInput(t);
                });

            }
        });

    }

    protected void update() {
        taskButtons.forEach( (k,v) -> {
           v.update();
        });
    }

    //public static class TaskButton extends ToggleButton {
    public static class TaskButton extends Label {

        private final Task task;

        public TaskButton(NAR nar, Task t) {
            super(t.toStringWithoutBudget(null));
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
                {
                    p.getContent().add(new TaskPane(nar, task));
                    p.getContent().add(new NSlider("pri", 100, 25, 0.5f));
                    Button b1 = new Button("+");
                    p.getContent().add(b1);
                }
                p.setOpacity(0.75f);
                p.setAutoHide(true);
                p.setAutoFix(true);

                p.show(getScene().getWindow(), c.getSceneX(), c.getSceneY());
            });

            runLater(this::update);

        }

        private void update() {
            float pri = task.getPriority();
            float priToFontSize = pri * 40f;
//            getStyleClass().clear();
//            getStylesheets().clear();
//            //setStyle("-fx-background-color: #FFFFFF !important;");
//            setStyle("-fx-base: #FFFFFF !important;");
//            setStyle("-fx-padding: 5px !important;");
//            //setStyle("-fx-border-radius: 20;");
            setFont(NARfx.mono(priToFontSize));

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
                (task.getTerm().op().ordinal()/64f)*360.0,
                    0.4, 0.7, 0.75f + pri * 0.25f
            );
        }

    }

    /**
     * adds a task to be managed/displayed by this widget
     */
    protected void addInput(Task t) {
        
        taskButtons.computeIfAbsent(t, k -> {
            TaskButton b = new TaskButton(nar,k);
            buttons.getChildren().add(b);
            return b;
        });
    }

    public static void main(String[] args) {


        Global.DEBUG = false;

        Default d = new Default(1000, 1, 1, 3);

        NARide.show(d.loop(), (i) -> {
            Stage s = NARfx.newWindow("x", new NARtop(d));
            s.show();

//                //NARfx.run((a, b) -> {
//                    b.setScene(new Scene(, 500, 300));
//                    b.getScene().getStylesheets().setAll(NARfx.css);
//                    b.show();
//                //});



            d.input("$0.70$ <groceries --> [bought]>!");
            d.input("$0.40$ <plants --> [watered]>!");
            d.input("$0.30$ <<perimeter --> home> --> secure>?");
            d.input("$0.50$ <weather <-> [dangerous]>?");
            //d.frame();
            //d.loop(100);
        });
    }
}

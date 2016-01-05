package nars.guifx.demo;

import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import nars.Global;
import nars.NAR;
import nars.budget.BudgetMerge;
import nars.guifx.NARfx;
import nars.nar.Default;
import nars.task.Task;
import nars.task.flow.SetTaskPerception;

import java.util.HashMap;
import java.util.Map;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 12/12/15.
 */
public class NARtop extends BorderPane {

    final SetTaskPerception active;
    //final FlowPane buttons = new FlowPane();
    final TextFlow buttons = new TextFlow();
    final Map<Task,SubButton> taskButtons = new HashMap();
    private final NAR nar;

    public NARtop(NAR d) {
        super();

        this.nar = d;

        setCenter(buttons);
        //setLeft(new TreePane(d));

        active = new SetTaskPerception(d.memory, f -> {
            update();
        }, BudgetMerge.plusDQDominated);
        d.memory.eventTaskProcess.on(t -> {
            if (t.isInput()) {
                runLater( () -> {
                    addInput(t);
                });
            }
        });

    }

    protected void update() {
        /*taskButtons.forEach( (k,v) -> {
           v.update();
        });*/
    }

    /**
     * adds a task to be managed/displayed by this widget
     */
    protected void addInput(Task t) {
        
        taskButtons.computeIfAbsent(t, k -> {
            //TaskButton b = new TaskButton(nar,k);
            SubButton b = SubButton.make(nar, k);
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
            d.input("$0.70$ prompt(string, \"Reason?\").");
            d.input("$0.40$ emote(happy)!");
            d.input("$0.80$ plot(line, (0, 2, 1, 3), \"Chart\").");
            d.frame(6);
            //d.loop(100);
        });
    }
}

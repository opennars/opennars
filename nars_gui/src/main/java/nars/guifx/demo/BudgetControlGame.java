package nars.guifx.demo;

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import nars.NAR;
import nars.guifx.LinePlot;
import nars.guifx.NARfx;
import nars.guifx.util.NSlider;
import nars.meter.MemoryBudget;
import nars.nar.Default;

/**
 * Created by me on 10/10/15.
 */
public class BudgetControlGame {

    MemoryBudget m = new MemoryBudget();

    public BudgetControlGame() {
        NARfx.run((a,b)-> {

            LinePlot lp;
            HBox r = new HBox();
            r.getChildren().setAll(
                new NSlider(300, 150, 0.75),
                lp = new LinePlot(
                        "Total Budget", () -> m.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum), 256)
            );

            b.setScene(new Scene(r, 400, 200));
            b.show();

            NAR n = new Default();
            n.input("a:b. b:c. c:d. d:e.");
            n.loop(3.5f);
            n.onEachFrame( x -> {
                m = new MemoryBudget(x.memory);
                lp.render();
                System.out.println(m);
            });

            b.getScene().getStylesheets().add(NARfx.css);
        });

    }

    public static void main(String[] args) {
        new BudgetControlGame();
    }
}

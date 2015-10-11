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

            int h = 250;

            LinePlot lp;
            HBox r = new HBox();
            //r.addColumn(0,
            r.getChildren().setAll(

                new NSlider("?", 150, h, 0.75)

                ,

                lp = new LinePlot(
                        "Total Budget", () -> m.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum),
                        256,
                        400, h
                )
            );

            r.getChildren().forEach(cr -> {
                cr.minWidth(300);
                cr.prefWidth(300);
                cr.maxWidth(300);
                cr.minHeight(150);
                cr.prefHeight(150);
                cr.maxHeight(150);
            });


            b.setScene(new Scene(r));
            b.getScene().getStylesheets().setAll(NARfx.css);

            b.show();



            NAR n = new Default();
            n.input("a:b. b:c. c:d. d:e.");
            n.loop(3.5f);
            n.onEachFrame( x -> {
                m = new MemoryBudget(x.memory);
                lp.update();
                System.out.println(m);
            });

        });

    }

    public static void main(String[] args) {
        new BudgetControlGame();
    }
}

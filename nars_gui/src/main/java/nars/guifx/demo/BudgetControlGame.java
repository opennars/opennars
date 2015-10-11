package nars.guifx.demo;

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.Plot2D;
import nars.guifx.util.NSlider;
import nars.meter.MemoryBudget;
import nars.nar.Default;

/**
 * Created by me on 10/10/15.
 */
public class BudgetControlGame {

    MemoryBudget m = new MemoryBudget();

    public BudgetControlGame() {
        NARfx.run((a, b) -> {

            int h = 250;

            Plot2D lp,lp2;
            HBox r = new HBox();
            //r.addColumn(0,
            r.getChildren().setAll(

                    new NSlider("?", 150, h, 0.75)

                    ,

                    new VBox(
                            lp = new Plot2D(
                                    "Concept Pri StdDev",
                                    Plot2D.Line,
                                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveConceptPriorityStdDev),
                                    256,
                                    400, h
                            ),
                            lp2 = new Plot2D(
                                    "Concept Pri Sum",
                                    Plot2D.BarWave,
                                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum),
                                    256,
                                    400, h
                            )
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
            n.onEachFrame(x -> {
                m = new MemoryBudget(x.memory);
                lp.update(); lp2.update();
                System.out.println(m);
            });

        });

    }

    public static void main(String[] args) {
        new BudgetControlGame();
    }
}

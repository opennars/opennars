package nars.guifx.demo;

import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.StatusPane;
import nars.guifx.util.NSlider;
import nars.nar.Default;
import nars.util.meter.MemoryBudget;

/**
 * Created by me on 10/10/15.
 */
public class BudgetControlGame {

	public static final int historySize = 256;

	MemoryBudget m = new MemoryBudget();

	public BudgetControlGame() {
        NARfx.run((a, b) -> {

            int h = 250;


            HBox r = new HBox();
            //r.addColumn(0,
            PlotBox plots;

            NAR n = new Default();

            double w = 256;
            r.getChildren().setAll(

                    new NSlider("?", 150, h, 0.75)

                    ,

                    plots = new StatusPane(n, 384)

            );

//            for (MemoryBudget.Budgeted bb : MemoryBudget.Budgeted.values()) {
//                lp.add(bb.name(), ()->m.getDouble(bb));
//            }

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


            n.input("a:b. b:c. c:d. d:e.");
            n.loop(8.5f);
            n.onEachFrame(x -> {
                m = new MemoryBudget(x);
                plots.update();
                System.out.println(m);
            });

        });

    }
	public static void main(String[] args) {
		new BudgetControlGame();
	}
}

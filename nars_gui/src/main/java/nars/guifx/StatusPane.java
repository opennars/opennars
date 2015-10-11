package nars.guifx;

import javafx.scene.layout.HBox;
import nars.Global;
import nars.NAR;
import nars.event.FrameReaction;
import nars.meter.MemoryBudget;

import java.util.List;

/**
 * Created by me on 9/6/15.
 */
public class StatusPane extends HBox {


    final List<LinePlot> plots = Global.newArrayList();

    final MemoryBudget b = new MemoryBudget();
    private long nConcepts;

    public StatusPane(NAR nar) {
        super();


        getChildren().addAll(
                addPlot(new LinePlot(
                        "Concepts",
                        () -> {
                            nConcepts = b.getLong(MemoryBudget.Budgeted.ActiveConcepts);
                            return nConcepts;
                        },
                        300,
                        200, 200)
                ),

                addPlot(new LinePlot(
                        "Concept Pri Avg",
                        () -> {
                            if (nConcepts == 0) return 0;
                            return b.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum)/nConcepts;
                        },
                        300,
                        200, 200)
                ),

                addPlot(new LinePlot(
                        "Concept StdDev",
                        () -> b.getDouble(MemoryBudget.Budgeted.ActiveConceptPriorityStdDev),
                        300,
                        200, 200)
                )
        );

        new FrameReaction(nar) {

            @Override
            public void onFrame() {

                b.update(nar.memory);

                for (LinePlot p: plots) {
                    p.update();
                }
            }
        };


        maxWidth(Double.MAX_VALUE);
        maxHeight(Double.MAX_VALUE);

        layout();

    }

    private LinePlot addPlot(LinePlot p) {
        plots.add(p);
        return p;
    }
}

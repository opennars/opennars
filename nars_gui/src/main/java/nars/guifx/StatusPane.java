package nars.guifx;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import nars.Global;
import nars.NAR;
import nars.event.FrameReaction;
import nars.meter.MemoryBudget;

import java.util.List;

/**
 * Created by me on 9/6/15.
 */
public class StatusPane extends GridPane {


    final List<LinePlot> plots = Global.newArrayList();

    final MemoryBudget b = new MemoryBudget();
    private long nConcepts;

    public StatusPane(NAR nar) {
        super();


        addColumn(0,
                addPlot(new LinePlot(
                    "Concepts",
                        () -> {
                            nConcepts = b.getLong(MemoryBudget.Budgeted.ActiveConcepts);
                            return nConcepts;
                        },
                        300,
                        200,200
                )),

                addPlot(new LinePlot(
                        "Concept Pri Avg",
                        () -> {
                            if (nConcepts == 0) return 0;
                            return b.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum)/nConcepts;
                        },
                        300,
                        200,200)
                ),
                addPlot(new LinePlot(
                        "Concept StdDev",
                        () -> b.getDouble(MemoryBudget.Budgeted.ActiveConceptPriorityStdDev),
                        300,
                        200,200
                ))
        );

        new FrameReaction(nar) {

            @Override
            public void onFrame() {

                b.update(nar.memory);

                for (LinePlot p: plots)
                    p.update();
            }
        };

    }

    private Node addPlot(LinePlot p) {
        plots.add(p);
        p.maxWidth(Double.MAX_VALUE);
        p.maxHeight(Double.MAX_VALUE);
        return p;
    }
}

package nars.guifx;

import nars.Global;
import nars.NAR;
import nars.event.FrameReaction;
import nars.guifx.demo.PlotBox;
import nars.meter.MemoryBudget;

import java.util.List;

/**
 * Created by me on 9/6/15.
 */
public class StatusPane extends PlotBox {


    final List<Plot2D> plots = Global.newArrayList();

    final MemoryBudget m = new MemoryBudget();
    final int historySize = 256;
    final int w = 0;

    public StatusPane(NAR nar) {
        super();
        getChildren().addAll(

            new Plot2D(Plot2D.Line, 128,
                    w, 100
            ).add("Concept Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTermLinkPrioritySum)
            ),

            new Plot2D(Plot2D.Line, historySize,
                    w, 150
            ).add("TermLink Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTermLinkPrioritySum)
            ).add("TaskLink Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTaskLinkPrioritySum)
            ),

            new Plot2D(Plot2D.Line, 256,
                    w, 150
            ).add("Concept Change",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveConceptPriorityStdDev)
            ).add("TermLink Change",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTermLinkPriorityStdDev)
            ).add("TaskLink Change",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTaskLinkPriorityStdDev)
            )
        );

        new FrameReaction(nar) {
            @Override public void onFrame() {
                m.update(nar.memory);
                update();
            }
        };

        maxWidth(Double.MAX_VALUE);
        maxHeight(Double.MAX_VALUE);

        layout();

    }

    private Plot2D addPlot(Plot2D p) {
        plots.add(p);
        return p;
    }
}

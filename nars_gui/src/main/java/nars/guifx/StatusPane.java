package nars.guifx;

import nars.NAR;
import nars.event.FrameReaction;
import nars.guifx.demo.PlotBox;
import nars.meter.MemoryBudget;

/**
 * Created by me on 9/6/15.
 */
public class StatusPane extends PlotBox {

    final MemoryBudget m = new MemoryBudget();
    final int historySize = 256;
    final double w = 0;

    public StatusPane(NAR nar, double height) {
        super();
        getChildren().addAll(

            new Plot2D(Plot2D.Line, historySize,
                    w, height/3
            ).add("Concept Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTermLinkPrioritySum)
            ),

            new Plot2D(Plot2D.Line, historySize,
                    w, height/3
            ).add("TermLink Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTermLinkPrioritySum)
            ).add("TaskLink Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTaskLinkPrioritySum)
            ),

            new Plot2D(Plot2D.Line, historySize,
                    w, height/3
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


        layout();

    }

//    private Plot2D addPlot(Plot2D p) {
//        plots.add(p);
//        return p;
//    }
}

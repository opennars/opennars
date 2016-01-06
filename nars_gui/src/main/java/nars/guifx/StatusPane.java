package nars.guifx;

import nars.NAR;
import nars.guifx.demo.PlotBox;
import nars.util.event.FrameReaction;
import nars.util.meter.MemoryBudget;

/**
 * Created by me on 9/6/15.
 */
public class StatusPane extends PlotBox {

	final MemoryBudget m;
	final double w = 0;

	public StatusPane(NAR nar, double height) {
		this(nar, new MemoryBudget(), 256, height);
	}

	public StatusPane(NAR nar, MemoryBudget m, int historySize, double height) {
        super(

            new Plot2D(Plot2D.Line, historySize,
                    height/2
            ).add("Concept Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveConceptPrioritySum)
            ),

            new Plot2D(Plot2D.Line, historySize,
                    height/2
            ).add("TermLink Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTermLinkPrioritySum)
            ).add("TaskLink Energy",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTaskLinkPrioritySum)
            )

                /*
            new Plot2D(Plot2D.Line, historySize,
                    w, height/3
            ).add("Concept Change",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveConceptPriorityStdDev)
            ).add("TermLink Change",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTermLinkPriorityStdDev)
            ).add("TaskLink Change",
                    () -> m.getDouble(MemoryBudget.Budgeted.ActiveTaskLinkPriorityStdDev)
            )
            */
        );
        this.m = m;

        new FrameReaction(nar) {
            @Override public void onFrame() {
                m.update(nar);
                update();
                m.clear();
            }
        };


        layout();

    }
	// private Plot2D addPlot(Plot2D p) {
	// plots.add(p);
	// return p;
	// }
}

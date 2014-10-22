package nars.gui.output.timeline;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import nars.core.EventEmitter.Observer;
import nars.core.Events.CycleEnd;
import nars.core.NAR;
import nars.gui.NPanel;
import nars.gui.output.timeline.Timeline2DCanvas.StackedPercentageChart;
import nars.util.NARTrace;

/**
 *
 * @author me
 */


public class TimelinePanel extends NPanel implements Observer {
    
    private List<Timeline2DCanvas.Chart> charts;
    private final Timeline2DCanvas timeline;
    private final JPanel controls;
    private final NAR nar;
    private final NARTrace trace;

    public TimelinePanel(NAR n) {
        super(new BorderLayout());
        
        this.nar = n;
        this.trace = new NARTrace(n);
        trace.setActive(false);
        
        
        charts = new ArrayList();

        
        charts.add(new StackedPercentageChart(trace, "concept.priority.hist.0", "concept.priority.hist.1", "concept.priority.hist.2", "concept.priority.hist.3").height(2));
        charts.add(new Timeline2DCanvas.EventChart(trace, false, true, false).height(3));
        
        this.timeline = new Timeline2DCanvas(charts);
        
        
        this.controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
        
        controls.add(new JCheckBox("x"));

        add(timeline, BorderLayout.CENTER);
        add(controls, BorderLayout.EAST);
        
        n.on(CycleEnd.class, this);
    }

    @Override
    public void event(Class event, Object[] arguments) {
        if (event == CycleEnd.class) {
            timeline.updateNext();
        }
    }

    
    
    @Override
    protected void onShowing(boolean showing) {
        trace.setActive(showing);
    }
    
}

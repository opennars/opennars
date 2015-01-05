package automenta.vivisect.timeline;

import automenta.vivisect.swing.PCanvas;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;


public abstract class MultiTimeline extends JPanel {

    List<TimelineVis> timeline = new ArrayList();

    final int hgap = 4;
    final int vgap = 4;
    
    public MultiTimeline(int n, int rows, int cols) {
        this(n);
        setLayout(new GridLayout(rows, cols, hgap, vgap));
        doLayout();
    }
    public MultiTimeline(int n) {
        super(new GridLayout());
        TimelineVis.Camera sharedCam = new TimelineVis.Camera();
        for (int i = 0; i < n; i++) {
            TimelineVis t = new TimelineVis(sharedCam, getCharts(i));
            timeline.add(t);
            add(new PCanvas(t));
        }
        doLayout();
    }

    public abstract Chart[] getCharts(int experiment);
}

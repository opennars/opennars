package nars.gui.output.timeline;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import nars.gui.output.timeline.Timeline2DCanvas;


public abstract class MultiTimeline extends JPanel {

    List<Timeline2DCanvas> timeline = new ArrayList();

    public MultiTimeline(int n) {
        super(new GridLayout());
        Timeline2DCanvas.Camera sharedCam = new Timeline2DCanvas.Camera();
        for (int i = 0; i < n; i++) {
            Timeline2DCanvas t = new Timeline2DCanvas(sharedCam, getCharts(i));
            timeline.add(t);
            add(t);
        }
        doLayout();
    }

    public abstract Chart[] getCharts(int experiment);
}

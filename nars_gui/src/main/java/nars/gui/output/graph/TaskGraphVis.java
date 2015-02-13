package nars.gui.output.graph;

import automenta.vivisect.dimensionalize.FastOrganicLayout;
import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;
import automenta.vivisect.swing.NWindow;
import nars.core.NAR;


public class TaskGraphVis extends NARGraphVis {

    public TaskGraphVis(NAR n) {
        super(n, new NARGraphDisplay(), new FastOrganicLayout<>() /*new TaskGraphLayout()*/);

    }

    public NWindow newWindow() {
        NARGraphPanel pan = new NARGraphPanel(nar, this);
        //pan.setZoom(10f);
        return new NWindow("", pan);
    }

    @Override
    public GraphMode getInitialMode() {
        return new TaskGraphMode();
    }

    private static class TaskGraphLayout implements GraphDisplay {
        @Override
        public void vertex(AbstractGraphVis g, VertexVis v) {

        }

        @Override
        public void edge(AbstractGraphVis g, EdgeVis e) {

        }
    }
}

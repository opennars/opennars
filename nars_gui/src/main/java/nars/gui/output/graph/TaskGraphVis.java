package nars.gui.output.graph;

import automenta.vivisect.Video;
import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;
import automenta.vivisect.swing.NWindow;
import nars.core.NAR;
import nars.logic.entity.Task;
import nars.util.graph.TaskGraph;


public class TaskGraphVis extends NARGraphVis {


    private TaskGraph tg;

    public TaskGraphVis(NAR n) {
        super(n, null, null /*new TaskGraphLayout()*/);

        update(new TaskGraphDisplay(), new TaskGraphLayout());

    }

    public NWindow newWindow() {
        NARGraphPanel pan = new NARGraphPanel(nar, this);
        //pan.setZoom(10f);
        return new NWindow("", pan);
    }

    @Override
    public GraphMode getInitialMode() {
        TaskGraphMode tgm = new TaskGraphMode();
        this.tg = tgm.taskGraph;
        return tgm;
    }

    class TaskGraphLayout implements GraphDisplay {

        @Override
        public void vertex(AbstractGraphVis g, VertexVis v) {
            if (tg == null) return;

            Object vv = v.getVertex();
            if (vv instanceof Task) {
                Task t = (Task)vv;
                //float y = t.getCreationTime();

                Float y = tg.y.get(t);
                if (y == null) return;
                y *= 30f;

                v.setPosition(0, y);

            }
        }

        @Override
        public void edge(AbstractGraphVis g, EdgeVis e) {

        }
    }

    static class TaskGraphDisplay extends NARGraphDisplay {

        public TaskGraphDisplay() {
            setTextSize(1f, 100);
        }

        @Override
        public void vertex(AbstractGraphVis g, VertexVis v) {
            super.vertex(g, v);

            Object vv = v.getVertex();
            if (vv instanceof Task) {
                Task t = (Task)vv;
                v.shape = Shape.Rectangle;

                float p = t.getPriority();
                /*float d = t.getDurability();
                float q = t.getQuality();*/

                float s = 255f * (0.25f + 0.75f * t.budget.summary());


                //v.color = Video.color(p / 4f, p / 4f, p / 4f, 1f);
                v.textColor = Video.color(s, s, s, 255f*(0.5f + 0.5f * p));

                //v.radius = 1f;
            }

        }

        @Override
        public void edge(AbstractGraphVis g, EdgeVis e) {
            super.edge(g, e);
        }
    }
}

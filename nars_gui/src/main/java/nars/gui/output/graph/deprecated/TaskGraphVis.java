//package nars.gui.output.graph;
//
//import automenta.vivisect.Video;
//import automenta.vivisect.dimensionalize.FastOrganicLayout;
//import automenta.vivisect.graph.AbstractGraphVis;
//import automenta.vivisect.graph.EdgeVis;
//import automenta.vivisect.graph.GraphDisplay;
//import automenta.vivisect.graph.VertexVis;
//import automenta.vivisect.swing.NWindow;
//import nars.NAR;
//import nars.nal.entity.Task;
//import nars.util.graph.TaskGraph;
//
//
//public class TaskGraphVis extends NARGraphVis {
//
//
//    GraphDisplay layout; //layout for non-list items
//
//    private TaskGraph tg;
//
//    public TaskGraphVis(NAR n) {
//        super(n, null, null /*new TaskGraphLayout()*/);
//
//        /*
//        layout = new HyperassociativeLayout() {
//
//            @Override
//            public boolean canChangePosition(Object vertex) {
//                if (tg.contains(vertex)) return false;
//                return true;
//            }
//
//
//        };
//            layout.setEdgeDistance(0.4f);
//            layout.setScale(100f);
//            layout.setNormalize(false);
//        */
//
//        FastOrganicLayout layout = new FastOrganicLayout<>();
//        layout.setMaxDistanceLimit(20f);
//        layout.setForceConstant(600f);
//        layout.setMinDistanceLimit(10f);
//        this.layout = layout;
//
//        update(new TaskGraphDisplay(n), new TaskGraphLayout());
//
//    }
//
//    @Override
//    public void update(GraphDisplay style, GraphDisplay l) {
//        super.update(style, layout, l);
//    }
//
//    public NWindow newWindow() {
//        NARGraphPanel pan = new NARGraphPanel(nar, this);
//        //pan.setZoom(10f);
//        return new NWindow("", pan);
//    }
//
//    @Override
//    public GraphMode getInitialMode() {
//        TaskGraphMode tgm = new TaskGraphMode();
//        this.tg = tgm.taskGraph;
//        return tgm;
//    }
//
//    class TaskGraphLayout implements GraphDisplay {
//
//        @Override
//        public boolean preUpdate(AbstractGraphVis g) {
//            return true;
//        }
//
//        @Override
//        public void vertex(AbstractGraphVis g, VertexVis v) {
//            if (tg == null) return;
//
//            float lineHeight = 120;
//            //float y0 = lineHeight * tg.y.size();
//            float y0 = -1500;
//            float x0 = 950;
//
//            Object vv = v.getVertex();
//            if (tg.contains(vv)) {
//                if (vv instanceof Task) {
//                    Task t = (Task) vv;
//                    //float y = t.getCreationTime();
//
//                    Float y = tg.y.get(t);
//                    if (y == null) return;
//                    y*= lineHeight;
//
//                    v.setPosition(x0, y+y0);
//
//                }
//            }
//
//        }
//
//        @Override
//        public void edge(AbstractGraphVis g, EdgeVis e) {
//        }
//    }
//
//    class TaskGraphDisplay extends NARGraphDisplay {
//
//        public TaskGraphDisplay(NAR n) {
//            super(n);
//            setTextSize(1f, 100);
//        }
//
//        @Override
//        public void vertex(AbstractGraphVis g, VertexVis v) {
//            super.vertex(g, v);
//
//            Object vv = v.getVertex();
//            if (tg.contains(vv)) {
//                if (vv instanceof Task) {
//                    Task t = (Task) vv;
//                    v.shape = Shape.Rectangle;
//
//                    float p = t.getPriority();
//                    /*float d = t.getDurability();
//                    float q = t.getQuality();*/
//
//                    float s = 255f * (0.25f + 0.75f * t.budget.summary());
//
//
//                    //v.color = Video.color(p / 4f, p / 4f, p / 4f, 1f);
//                    v.textColor = Video.color(s, s, s, 255f * (0.5f + 0.5f * p));
//
//                    //v.radius = 1f;
//                }
//            }
//
//        }
////
////        @Override
////        public void edge(AbstractGraphVis g, EdgeVis e) {
////            super.edge(g, e);
////        }
//    }
// }

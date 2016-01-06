//package nars.guifx.graph2.source;
//
//import nars.Global;
//import nars.NAR;
//import nars.guifx.graph2.GraphSource;
//import nars.guifx.graph2.TermNode;
//import nars.guifx.graph2.layout.IterativeLayout;
//import nars.task.Task;
//import nars.term.Atom;
//import nars.util.event.Active;
//import nars.util.event.Topic;
//import nars.util.time.Between;
//import nars.util.time.IntervalTree;
//
//import java.util.Set;
//import java.util.function.BiConsumer;
//
///**
// * Created by me on 10/9/15.
// */
//public class EventGraphSource implements GraphSource<Task>,
//        IterativeLayout<TermNode<Task>>,
//        BiConsumer<String /* fieldName*/, Object /* value */>
//{
//
//    public final IntervalTree<Long,Set<TermNode<Task>>> map = new IntervalTree();
//
//
//    private final NAR nar;
//    private SpaceGrapher graph = null;
//    private Active regs = null;
//
//    public EventGraphSource(NAR n) {
//        this.nar = n;
//        Topic.all(n.memory, this);
//    }
//
//
//    @Override
//    public void start(SpaceGrapher g) {
//
//        this.graph = g;
//
//        if (map.isEmpty()) {
//            Set<TermNode<Task>> start = Global.newHashSet(1);
//            start.add(g.getOrNewTermNode(nar.task("start:now.")));
//            map.put(nar.time(), nar.time(), start);
//        }
//
//        //g.layout.set(this);
//
//
//        //.stdout()
//        //.stdoutTrace()
////                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
////                        "<b --> c>. %1.00;0.7%")
//
//        if (regs != null)
//            regs.off();
//
//        update();
//
//        regs = Topic.all(nar.memory, this);
//
//
//
//
//    }
//
//    final Set<TermNode<Task>> l = Global.newHashSet(4);
//
//    @Override
//    public void accept(SpaceGrapher<Task, TermNode<Task>> g) {
//        long now = 100;
//        long oldest = 0;
//
//
//        map.root.getOverlap(new Between(oldest, now),
//                l::addAll
//        );
//
//
//        g.setVertices(l.toArray(new TermNode[l.size()]));
//
//        l.clear();
//
//    }
//
//    @Override
//    public void accept(String s, Object o) {
//
//
//
//        //temporal features, extract
//        if (o instanceof Task) {
//            Task t = (Task)o;
//            Between<Long> ii = new Between<>(
//                    (long)(t.getCreationTime()-1.0),
//                    t.getCreationTime());
//
//            Set<TermNode<Task>> exists = map.getEqual(ii);
//            if (exists == null) {
//                exists = Global.newHashSet(1);
//                map.put(ii, exists);
//            }
//
//            exists.add(
//                    taskAsTerm(t)
//                    );
//
//
//
//        }
//
//        if (s.equals("eventFrameStart")) {
//            update(graph);
//        }
//    }
//
//    public TermNode taskAsTerm(Task t) {
//        return graph.getOrNewTermNode(Atom.quote(t.toString()));
//    }
//
//    @Override
//    public void init(TermNode<Task> n) {
//
//        System.out.println("EVeNT: " + n );
//        System.out.println("\t" + n.getLayoutBounds());
//        System.out.println("\t" + n.isVisible());
//        System.out.println("\t" + n.getChildren());
//    }
//
//    @Override
//    public void run(SpaceGrapher graph, int iterations) {
//
//    }
//
//
// }

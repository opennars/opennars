package nars.guifx.graph2;

import nars.Global;
import nars.NAR;
import nars.task.Task;
import nars.term.Atom;
import nars.util.event.Active;
import nars.util.event.Topic;
import nars.util.time.Interval;
import nars.util.time.IntervalTree;

import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Created by me on 10/9/15.
 */
public class EventGraphSource implements GraphSource,
        BiConsumer<String /* fieldName*/, Object /* value */> {

    public final IntervalTree<Long,Set<TermNode>> map = new IntervalTree();


    private final NAR nar;
    private SpaceGrapher graph;
    private Active regs;

    public EventGraphSource(NAR n) {
        this.nar = n;
        Topic.all(n.memory, this);
    }


    @Override
    public void start(SpaceGrapher g) {

        this.graph = g;

        Set<TermNode> start = Global.newHashSet(1);
        start.add(g.getOrCreateTermNode(Atom.the("START")));
        map.put(nar.time(), nar.time(), start);

        g.layout.set(this);


        //.stdout()
        //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")

        if (regs != null)
            regs.off();

        refresh();

        regs = Topic.all(nar.memory, this);




    }

    Set<TermNode> l = Global.newHashSet(4);

    private void update(SpaceGrapher g) {
        long now = 100;
        long oldest = 0;

        map.root.getOverlap(new Interval<Long>(oldest, now),
            (s)-> l.addAll(s)
        );

        g.setVertices(l);

        l.clear();

    }


    @Override
    public void accept(String s, Object o) {

        System.out.println(s + " " + o );

        //temporal features, extract
        if (o instanceof Task) {
            Task t = (Task)o;
            Interval<Long> ii = new Interval<>(
                    (long)(t.getCreationTime()-1.0),
                    (long)t.getCreationTime());

            Set<TermNode> exists = map.getEqual(ii);
            if (exists == null) {
                exists = Global.newHashSet(1);
                map.put(ii, exists);
            }

            exists.add(
                    taskAsTerm(t)
                    );

        }

        if (s.equals("eventFrameStart")) {
            update(graph);
        }
    }

    public TermNode taskAsTerm(Task t) {
        return graph.getOrCreateTermNode(Atom.quote(t.toString()));
    }

    @Override
    public void accept(Object o) {

    }
}

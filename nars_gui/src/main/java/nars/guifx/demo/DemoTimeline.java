package nars.guifx.demo;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import nars.Global;
import nars.NAR;
import nars.NARStream;
import nars.guifx.NARfx;
import nars.guifx.TaskLabel;
import nars.nar.NewDefault;
import nars.task.Task;
import nars.util.time.IntervalTree;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by me on 9/2/15.
 */
public class DemoTimeline  {


    abstract public static class TaskTimeline<W> {

        public final IntervalTree<Float,Set<W>> map = new IntervalTree();

        private final int dur;
        private final float durHalf;

        public TaskTimeline(NAR n) {

            this.dur = n.memory.duration();
            this.durHalf = dur / 2.0f;
        }

        public void addOccurrence(final Task task) {
            if (!task.isEternal()) {


                final long ot = task.getOccurrenceTime();
                final long ct = ot;
                /*long start = Math.min(ct, ot);
                long end = Math.max(ct, ot);*/
                final float s = (ot - durHalf);
                final float e = (ct + durHalf);

                Set<W> c = map.getEqual(s, e);
                if (c == null) {
                    c = Global.newHashSet(1);
                    map.put(s, e, c);
                    //System.out.println("new " + s + " " + e);
                }
                else {
                    //System.out.println("exist " + s + " " + e + " " + task + " -> " + c);
                }
                c.add(get(task));


                //System.out.println("overlap: " + c);

            }

        }

        abstract W get(Task task);

        public void forEachOverlapping(float start, float stop, Consumer<W> w) {
            final List<Set<W>> x = map.searchOverlapping(start, stop);
            if (x == null) return;

            x.forEach( c -> c.forEach(w) );
        }
    }

    abstract public static class TaskTimelinePane {

        private final TaskTimeline<Node> time;
        private final Pane view;

        public TaskTimelinePane(NAR n, Pane view) {
            super();

            this.view = view;

            this.time = new TaskTimeline<Node>(n) {

                @Override
                Node get(Task task) {
                    return TaskTimelinePane.this.get(task);
                }
            };

            NARStream s = new NARStream(n);

            s.forEachDerived(task -> { time.addOccurrence((Task)task[0]); });

        }

        abstract protected Node get(Task task);

        final List<Node> visible = Global.newArrayList();

        public synchronized void view(float start, float stop) {

            visible.clear();
            time.forEachOverlapping(start, stop, (w) -> {
                visible.add(w);
            });

            view.getChildren().setAll(visible);

        }
    }

    public static void main(String[] args) {
        NARfx.run((a, b) -> {

            NAR n = new NAR(new NewDefault());

            VBox tpView = new VBox();
            TaskTimelinePane tp = new TaskTimelinePane(n, tpView) {

                @Override
                protected Node get(Task task) {
                    return new TaskLabel(task, n);
                }
            };

            n.input("b:a. :|:");
            n.frame(4);
            n.input("c:b. :/:");

            n.frame(1);

            n.frame(10);

            tp.view(-1f, 10f);

            System.out.println(tp.time.map.searchOverlapping(9f, 12f));

            //t.map.entrySet().forEach( System.out::println );



            b.setScene(new Scene(tpView, 500, 400));

            b.show();

            //runLater(() -> {


        });
    }


}

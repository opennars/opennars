package nars.guifx.demo;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by me on 9/2/15.
 */
public class DemoTimeline  {


    abstract public static class TaskTimeline<W> {

        public final IntervalTree<Float,Map<Task,W>> map = new IntervalTree();

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

                Map<Task,W> c = map.getEqual(s, e);
                if (c == null) {
                    c = Global.newHashMap(1);
                    map.put(s, e, c);
                    //System.out.println("new " + s + " " + e);
                }
                else {
                    //System.out.println("exist " + s + " " + e + " " + task + " -> " + c);
                }
                int n = c.size();
                c.computeIfAbsent(task, t -> build(t, s, e, n) );


                //System.out.println("overlap: " + c);

            }

        }

        abstract W build(Task task, float start, float end, int nth);

        public void forEachOverlapping(float start, float stop, Consumer<Map.Entry<Task,W>> w) {
            final List<Map<Task,W>> x = map.searchOverlapping(start, stop);
            if (x == null) return;

            x.forEach( c -> c.entrySet().forEach(w) );
        }
    }

    abstract public static class TaskTimelinePane {

        private final TaskTimeline<TaskEventButton> time;
        private final Pane view;

        public TaskTimelinePane(NAR n, Pane view) {
            super();

            this.view = view;

            this.time = new TaskTimeline(n) {

                @Override
                TaskEventButton build(Task task, float start, float end, int nth) {
                    return TaskTimelinePane.this.build(task, start, end, nth);
                }
            };

            NARStream s = new NARStream(n);

            s.forEachDerived(task -> { time.addOccurrence((Task)task[0]); });

        }

        abstract protected TaskEventButton build(Task task, float start, float end, int nth);



        public List<Node> visible(float start, float stop) {
            final List<Node> visible = Global.newArrayList();


            TaskEventButton[] prev = new TaskEventButton[1];
            time.forEachOverlapping(start, stop, (w) -> {
                TaskEventButton v = w.getValue();
                if (prev[0]!=null) {
                    double prevTime = prev[0].end;
                    double distance = v.start - prevTime;
                    if (distance > 0) {
                        visible.add(new Label("/" + distance));
                    }
                }
                visible.add(v);
                prev[0] = v;
            });

            return visible;
        }

        public void view(float start, float stop) {
            view.getChildren().setAll(visible(start,stop));
        }
    }

    public static class TaskEventButton extends TaskLabel {

        public final float start, end;

        public TaskEventButton(Task t, NAR nar, float start, float end) {
            super(t, nar);
            this.start = start;
            this.end = end;
        }
    }

//    public static class TaskTimelineVis() {
//
//    }

    public static void main(String[] args) {
        NARfx.run((a, b) -> {

            NAR n = new NAR(new NewDefault());


            Pane tpView = new VBox();
            ScrollPane sp = new ScrollPane(tpView);

            sp.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            TaskTimelinePane tp = new TaskTimelinePane(n, tpView) {

                int scaleX = 50;
                int scaleY = 50;

                @Override
                protected TaskEventButton build(Task task, float start, float end, int nth) {
                    TaskEventButton te = new TaskEventButton(task, n, start, end);
                    return te;
                }

                protected TaskEventButton build2(Task task, float start, float end, int nth) {
                    TaskEventButton te = new TaskEventButton(task, n, start, end);

                    te.label.setWrappingWidth(1 * scaleX);

                    te.setManaged(false);

                    te.setPrefWidth(1*scaleX);
                    te.setMaxWidth(1*scaleX);

                    double tx = nth * scaleX;
                    double ty = (start + end) / 2.0 * scaleY;
                    System.out.println(start + " " + end + " " + nth + " - " + tx + " " + ty);

                    te.setTranslateX(tx);

                    te.setTranslateY(ty);

                    te.setPrefHeight((end-start)*scaleY);

                    return te;
                }
            };

            n.input("b:a. :|:");
            n.frame(4);
            n.input("c:b. :|:");
            n.frame(4);
            n.input("b:a. :|:");
            n.frame(4);
            n.input("c:b. :|:");
            n.input("<(&/, c:b, b:a, #x) =/> #x --> x>>.");

            n.frame(4);

            n.frame(55);

            tp.view(-1f, 55f);

            //System.out.println(tp.time.map.searchOverlapping(9f, 12f));

            //t.map.entrySet().forEach( System.out::println );



            b.setScene(new Scene(sp, 500, 400));

            b.show();

            //runLater(() -> {


        });
    }




}

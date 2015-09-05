package nars.guifx.demo;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import nars.Global;
import nars.NAR;
import nars.NARStream;
import nars.guifx.NARfx;
import nars.guifx.TaskLabel;
import nars.nar.experimental.Equalized;
import nars.task.Task;
import nars.util.time.IntervalTree;

import java.util.ArrayList;
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
        private final NAR nar;

        /**previous/current mouse coord*/
        double mx = Double.NaN, my = Double.NaN;

        double t = 0.0;  //time about which the timeline is centered
        double d = 25.0; //duration window

        public TaskTimelinePane(NAR n, StackPane view) {
            super();

            this.nar = n;
            this.view = view;

            this.time = new TaskTimeline(n) {

                @Override
                TaskEventButton build(Task task, float start, float end, int nth) {
                    return TaskTimelinePane.this.build(task, start, end, nth);
                }
            };

            NARStream s = new NARStream(n);

            s.forEachDerived(task -> { time.addOccurrence((Task)task[0]); });



            view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            view.setMouseTransparent(false);
            view.setPickOnBounds(true);
            view.setOnMouseDragged((e) -> {
                double nx = e.getSceneX(), ny = e.getSceneY();

                if (Double.isFinite(mx)) {

                    double dx = nx - mx;
                    double dy = ny - my;

                    push(dx, dy);
                }

                mx = nx; my = ny;
            });
            view.setOnMouseReleased((e) -> {
                mx = my = Double.NaN;
            });

        }


        public final void push(final double dx, final double dy) {
            //System.out.println("dx " + dx + " " + dy);
            //view.layoutXProperty().add(dx);
            //view.layoutYProperty().add(dy);

            if (dy < 0.01) return;

            t+=dy * 0.25;
            updateAll();
        }

        //abstract protected TaskEventButton build(Task task, float start, float end, int nth);


        public double timeToPosition(double eventTime) {
            double H = view.getHeight();
            return ((eventTime - t) / d) * (H) + H/2;
        }

        int scaleX = 25;

        final List<TaskEventButton> shown = new ArrayList();

                /*
                @Override
                protected TaskEventButton build(Task task, float start, float end, int nth) {
                    TaskEventButton te = new TaskEventButton(task, n, start, end);
                    return te;
                }*/

        protected TaskEventButton build(Task task, float start, float end, int nth) {
            TaskEventButton te = new TaskEventButton(task, nar, start, end);

            //te.setScaleX(0.75f+task.getPriority()*0.25f);
            //te.setScaleY(0.75f+task.getPriority()*0.25f);
            te.setOpacity(task.getPriority()*0.5f + 0.5f);

            te.label.setWrappingWidth(1 * scaleX);

            //te.setManaged(false);

            //te.setPrefWidth(1*scaleX);



            double tx = nth * scaleX + scaleX/2;


            te.setTranslateX(tx);

            shown.add(te);
            update(te);

            return te;
        }

        protected void updateAll() {
            for (TaskEventButton t : shown) {
                update(t);
            }
        }

        private void update(final TaskEventButton te) {
            double ty1 = timeToPosition(te.start); // nth * (scaleX+marginX);
            double ty2 = timeToPosition(te.end);
            //System.out.println(start + " " + end + " " + nth + " - " + tx + " " + ty);

            double dur = ty2-ty1;
            double mid = ty1+dur/2.0;
            te.setTranslateY(mid);
            //te.setPrefHeight(dur);
        }


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
            label.setScaleX(1);
            label.setScaleY(1);
        }
    }

//    public static class TaskTimelineVis() {
//
//    }

    public static void main(String[] args) {
        NARfx.run((a, b) -> {

            NAR n = new NAR(new Equalized(4,3,3));


            StackPane sp = new StackPane();
            //sp.setBackground(new Background(new BackgroundFill(Color.gray(0.5), null, new Insets(0,0,0,0))));

            //ScrollPane sp = new ScrollPane(sp);

            //sp.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            sp.setCenterShape(false);

            TaskTimelinePane tp = new TaskTimelinePane(n, sp) {

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

            tp.view(-1f, 25);

            //System.out.println(tp.time.map.searchOverlapping(9f, 12f));

            //t.map.entrySet().forEach( System.out::println );



            b.setScene(new Scene(/*scrolled*/(sp), 500, 400));

            b.show();

            //runLater(() -> {


        });
    }




}

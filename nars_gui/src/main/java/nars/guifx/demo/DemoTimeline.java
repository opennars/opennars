//package nars.guifx.demo;
//
//import javafx.scene.Node;
//import javafx.scene.Scene;
//import nars.Global;
//import nars.NAR;
//import nars.guifx.AutoLabel;
//import nars.guifx.NARfx;
//import nars.guifx.graph2.ConceptsSource;
//import nars.guifx.graph2.DefaultVis;
//import nars.guifx.graph2.SpaceGrapher;
//import nars.guifx.graph2.layout.CanvasEdgeRenderer;
//import nars.nal.nal7.Temporal;
//import nars.nar.Default;
//import nars.task.Task;
//import nars.util.time.IntervalTree;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Consumer;
//
//import static javafx.application.Platform.runLater;
//
///**
// * Created by me on 9/2/15.
// */
//public class DemoTimeline  {
//
//
//    abstract public static class TaskTimeline<W> {
//
//        public final IntervalTree<Float,Map<Task,W>> map = new IntervalTree();
//
//        private final int dur;
//        private final float durHalf;
//
//        public TaskTimeline(NAR n) {
//
//            this.dur = n.memory.duration();
//            this.durHalf = dur / 2.0f;
//        }
//
//        public void addOccurrence(final Task task) {
//            if (!Temporal.isEternal(task.getOccurrenceTime())) {
//
//
//                final long ot = task.getOccurrenceTime();
//                final long ct = ot;
//                /*long start = Math.min(ct, ot);
//                long end = Math.max(ct, ot);*/
//                final float s = (ot - durHalf);
//                final float e = (ct + durHalf);
//
//                Map<Task,W> c = map.getEqual(s, e);
//                if (c == null) {
//                    c = Global.newHashMap(1);
//                    map.put(s, e, c);
//                    //System.out.println("new " + s + " " + e);
//                }
//                else {
//                    //System.out.println("exist " + s + " " + e + " " + task + " -> " + c);
//                }
//                int n = c.size();
//                c.computeIfAbsent(task, t -> build(t, s, e, n) );
//
//
//                //System.out.println("overlap: " + c);
//
//            }
//
//        }
//
//        abstract W build(Task task, float start, float end, int nth);
//
//        public void forEachOverlapping(float start, float stop, Consumer<Map.Entry<Task,W>> w) {
//            final List<Map<Task,W>> x = map.searchOverlapping(start, stop);
//            if (x == null) return;
//
//            x.forEach( c -> c.entrySet().forEach(w) );
//        }
//    }
//
//    public static class TaskTimelinePane extends SpaceGrapher {
//
//        private final TaskTimeline<TaskEventButton> time;
//        private final NAR nar;
//
//        /**previous/current mouse coord*/
//        double mx = Double.NaN, my = Double.NaN;
//
//        double t = 0.0;  //time about which the timeline is centered
//        double d = 150.0; //duration window
//
//        public TaskTimelinePane(NAR n) {
//            super(new ConceptsSource(n), new DefaultVis(), new CanvasEdgeRenderer(), 64);
//
//            this.nar = n;
//
//            this.time = new TaskTimeline(n) {
//
//                @Override
//                TaskEventButton build(Task task, float start, float end, int nth) {
//                    return TaskTimelinePane.this.build(task, start, end, nth);
//                }
//            };
//
//            runLater(() -> {
//
//                //n.forEachDerived(task -> { time.addOccurrence((Task)task[0]); });
//
//                updateAll();
//            });
//
//
////            //view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
////            setMouseTransparent(false);
////            setPickOnBounds(true);
////            setOnMouseDragged((e) -> {
////                double nx = e.getX(), ny = e.getY();
////
////                if (!Double.isNaN(mx)) {
////
////                    double dx = nx - mx;
////                    double dy = ny - my;
////
////                    push(dx, dy);
////                }
////
////                mx = nx; my = ny;
////            });
////            view.setOnMouseReleased((e) -> {
////                mx = my = Double.NaN;
////            });
//
//        }
//
//
//        public final void push(final double dx, final double dy) {
//            //System.out.println("dx " + dx + " " + dy);
//            //view.layoutXProperty().add(dx);
//            //view.layoutYProperty().add(dy);
//
//            if (Math.abs(dy) < 0.01) return;
//
//            t += dy * 0.15;
//
//            updateAll();
//        }
//
//        //abstract protected TaskEventButton build(Task task, float start, float end, int nth);
//
//
//        public double timeToPosition(double H, double eventTime) {
//            //System.out.println(eventTime + " x " + H);
//            return ((eventTime - t) / d) * (H) + H/2;
//        }
//
//        double scaleY = 125.0;
//        double scaleX = 125.0;
//
//        List<Node> shown = new ArrayList();
//
//                /*
//                @Override
//                protected TaskEventButton build(Task task, float start, float end, int nth) {
//                    TaskEventButton te = new TaskEventButton(task, n, start, end);
//                    return te;
//                }*/
//
//        protected TaskEventButton build(Task task, float start, float end, int nth) {
//            TaskEventButton te = new TaskEventButton(task, nar, start, end);
//
//            //te.setScaleX(0.75f+task.getPriority()*0.25f);
//            //te.setScaleY(0.75f+task.getPriority()*0.25f);
//            te.setOpacity(task.getPriority()*0.5f + 0.5f);
//
//            //te.label.setWrappingWidth(1 * scaleX);
//
//            //te.setManaged(false);
//
//            //te.setWrapText(true);
//            //te.setPrefWidth(1*scaleX);
//
//
//
//
//            double tx = nth * scaleX + scaleX/2;
//
//
//            te.setTranslateX(tx);
//            /*te.setBackground(new Background(new BackgroundFill(
//                    Color.hsb(te.hashCode() % 360, 0.8, 0.3),
//                    CornerRadii.EMPTY, new Insets(0,0,0,0))));*/
//
//            shown.add(te);
//            update(te);
//
//            return te;
//        }
//
//        protected void updateAll() {
//            double H = getLayoutBounds().getHeight();
//
//
//            shown = visible((float)(t), (float)(t+d));
//
//            for (Node t : shown) {
//                //t.setVisible(true);
//                if (t instanceof TaskEventButton) {
//                    TaskEventButton tb = (TaskEventButton) t;
//                    System.out.println(tb.start + " " + tb.end + " " + t);
//                    update(H, (TaskEventButton) t);
//                }
//            }
//
//            setNodes(shown);
//
//        }
//        private void update(final TaskEventButton te) {
//            update(getLayoutBounds().getHeight(), te);
//        }
//
//        private void update(double H, final TaskEventButton te) {
//
//            double ty1 = timeToPosition(H, te.start); // nth * (scaleX+marginX);
//            double ty2 = timeToPosition(H, te.end);
//            //System.out.println(start + " " + end + " " + nth + " - " + tx + " " + ty);
//
//            /*if ((ty2 < 0) || (ty1 > H)) {
//                te.setVisible(false);
//            }
//            else {
//                te.setVisible(true);
//            }*/
//
//
//            double dur = ty2-ty1;
//            double mid = ty1+dur/2.0;
//            te.setTranslateY(mid);
//
//            //te.setPrefWidth(scaleX);
//            //te.setPrefHeight(scaleY);
//        }
//
//
//        public List<Node> visible(float start, float stop) {
//            final List<Node> visible = Global.newArrayList();
//
//
//            TaskEventButton[] prev = new TaskEventButton[1];
//            time.forEachOverlapping(start, stop, (w) -> {
//                TaskEventButton v = w.getValue();
//                /*if (prev[0]!=null) {
//                    double prevTime = prev[0].end;
//                    double distance = v.start - prevTime;
//                    if (distance > 0) {
//                        visible.add(new Label("/" + distance));
//                    }
//                }*/
//                System.out.println("add: " + w + " -> " + v);
//                visible.add(v);
//                prev[0] = v;
//            });
//
//            return visible;
//        }
//
////
////        public void view(float start, float stop) {
////            this.t =
////            getChildren().setAll(visible(start,stop));
////        }
//    }
//
//    public static class TaskEventButton extends AutoLabel {
//
//        public final float start, end;
//
//        public TaskEventButton(Task t, NAR nar, float start, float end) {
//            super(t, nar);
//            this.start = start;
//            this.end = end;
//        }
//    }
//
//
//    public static void main(String[] args) {
//        NARfx.run((a, b) -> {
//
//            NAR n = new Default();
//
//            TaskTimelinePane tp = new TaskTimelinePane(n);
//
//            n.input("<a ==> x>! :|:");
//            n.frame(1);
//            n.input("b:a. :|:");
//            n.frame(2);
//            n.input("x:y. :\\:");
//            n.frame(3);
//            n.input("c:b. :|:");
//            n.frame(4);
//            n.input("b:a. :|:");
//            n.frame(3);
//            n.input("c:b. :|:");
//            n.frame(2);
//            //n.input("<(&/, c:b, b:a, #x) =/> #x --> x>>.");
//            n.frame(1);
//
//            n.frame(55);
//
//            //tp.view(-1f, 15);
//
//            //System.out.println(tp.time.map.searchOverlapping(9f, 12f));
//
//            //t.map.entrySet().forEach( System.out::println );
//
//
//            b = NARfx.newWindow("timeline", new Scene(tp), b);
//
//            final javafx.stage.Stage finalB = b;
//            runLater(() -> {
//                finalB.setWidth(800);
//                finalB.setHeight(800);
//                finalB.show();
//            });
//
//
//
//        });
//    }
//
//
//
//    //public void start2(Stage primaryStage) {
////
////
//////        Scene scene = space.newScene(1200, 800);
//////
//////        // init and show the stage
//////        primaryStage.setTitle("WignerFX Spacegraph Demo");
//////        primaryStage.setScene(scene);
//////        primaryStage.show();
//////
////
////        Platform.runLater(() -> {
////            start();
////        });
////    }
//
////    protected void start() {
////
////        //BrowserWindow.createAndAddWindow(space, "http://www.google.com");
////
////        Spacegraph space = new Spacegraph();
////        ObservableList<PieChart.Data> pieChartData =
////                FXCollections.observableArrayList(
////                        new PieChart.Data("Grapefruit", 13),
////                        new PieChart.Data("Oranges", 25),
////                        new PieChart.Data("Plums", 10),
////                        new PieChart.Data("Pears", 22),
////                        new PieChart.Data("Apples", 30));
////        final PieChart chart = new PieChart(pieChartData);
////        chart.setTitle("Imported Fruits");
////        chart.setCacheHint(CacheHint.SPEED);
////
////        Windget cc = new Windget("Edit", new CodeInput("ABC"), 300, 200).move(-100,-100);
////        cc.addOverlay(new Windget.RectPort(cc, true, +1, -1, 30, 30));
////
////        Windget wc = new Windget("Chart", chart, 400, 400);
////        wc.addOverlay(new Windget.RectPort(wc, true, -1, +1, 30, 30));
////
////
////        //Region jps = new FXForm(new NAR(new Default()));  // create the FXForm node for your bean
////
////
////        TaggedParameters taggedParameters = new TaggedParameters();
////        List<String> range = new ArrayList<>();
////        range.add("Ay");
////        range.add("Bee");
////        range.add("See");
////        taggedParameters.addTag("range", range);
////        Pane jps = POJONode.build(new SampleClass(), taggedParameters);
////
//////        Button button = new Button("Read in");
//////        button.setOnAction(new EventHandler<ActionEvent>() {
//////            @Override
//////            public void handle(ActionEvent actionEvent) {
//////                //SampleClass sample = POJONode.read(mainPane, SampleClass.class);
//////                //System.out.println(sample.getTextString());
//////            }
//////        });
////
////        jps.setStyle("-fx-font-size: 75%");
////        Windget wd = new Windget("WTF",
////                jps,
////                //new Button("XYZ"),
////                400, 400);
////        wd.addOverlay(new Windget.RectPort(wc, true, 0, +1, 10, 10));
////
////        space.addNodes(
////                wc,
////                cc,
////                wd
////        );
////
////
////
////
////        TerminalPane np = new TerminalPane(new NAR(new Default()));
////
////        Windget nd = new Windget("NAR",
////                np, 200, 200
////        ).move(-200,300);
////
////        space.addNodes(nd);
////
////    }
//
// }

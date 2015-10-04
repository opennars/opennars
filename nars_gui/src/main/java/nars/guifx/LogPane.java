package nars.guifx;

import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.guifx.treemap.Item;
import nars.guifx.treemap.TreemapChart;
import nars.premise.Premise;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.util.data.list.CircularArrayList;
import nars.util.event.Topic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javafx.application.Platform.runLater;
import static nars.guifx.NARfx.scrolled;

/**
 * Created by me on 8/2/15.
 */
public class LogPane extends BorderPane implements Runnable {

    final Pane content;

    //private final Output incoming;
    private final NAR nar;
    final int maxLines = 64;
    final CircularArrayList<Node> toShow = new CircularArrayList<>(maxLines);
    List<Node> pending;

    NSliderSet filter = new NSliderSet();

    ScrollPane scrollParent = null;
    private Node prev; //last node added


    /* to be run in javafx thread */
    @Override public void run() {

        Node[] c = toShow.toArray(new Node[toShow.size()]);

        content.getChildren().setAll(c);


        scrollBottom.run();
    }

    final Runnable scrollBottom = () -> scrollParent.setVvalue(1f);

    void updateParent() {
//        if (content.getParent()!=null) {
//            if (content.getParent().getParent() != null)
//                if (content.getParent().getParent().getParent() != null) {
//                    Node s = content.getParent().getParent().getParent();
//                    if (s instanceof ScrollPane)
//                        scrollParent = (ScrollPane) s;
//                    else
//                        scrollParent = null;
//                }
//        }

    }

    public LogPane(NAR nar, Object... enabled) {
        super();

        this.nar = nar;
        content = new VBox(1);


        for (Object o : enabled)
            filter.value(o, 1);


        setCenter(scrollParent = scrolled(content));
        setTop(filter);


        sceneProperty().addListener((c) -> {
            updateParent();
        });
        Topic.all(nar.memory(), (k,v) -> {
            output(k,v);
        });

        nar.onEachFrame( (n) -> {
            List<Node> p = pending;
            if (p!=null) {
                pending = null;
                //synchronized (nar) {
                    int ps = p.size();
                    int start = ps - Math.min(ps, maxLines);

                    int tr = ((ps - start) + toShow.size()) - maxLines;
                    if (tr > ps) {
                        toShow.clear();
                    }
                    else {
                        //remove first N
                        for (int i = 0; i < tr; i++)
                            toShow.removeFirst();
                    }

                    for (int i = start; i < ps; i++) {
                        Node v = p.get(i);
                        toShow.add(v);
                    }
                //}

                //if (queueUpdate)
                    runLater(LogPane.this);
            }
        });

        /*incoming = new Output(nar) {

            @Override
            protected boolean output(Channel channel, Class event, Object... args) {
                Node n = getNode(channel, event, args);
                if (n!=null) {

                    if (pending==null)
                        pending = Global.newArrayList();

                    pending.add(n);

                }
                return false;
            }

        };*/
    }



    protected void output(Object channel, Object signal) {
        boolean trace=false;

        //double f = filter.value(channel);

        //temporary until filter working
        if (!trace && ((channel.equals("eventDerived") ||
                channel.equals("eventTaskRemoved"))))
            return;

        Node n = getNode(channel, signal);
        if (n != null) {
            synchronized (toShow) {
                if (pending == null)
                    pending = Global.newArrayList();

                pending.add(n);
                prev = n;
            }
        }
    }

    ActivationTreeMap activationSet = null;
    //Pane cycleSet = null; //either displays one cycle header, or a range of cycles, including '...' waiting for next output while they queue

    public static class ActivationTreeMap extends TreemapChart {

        private final Item.DefaultItem r;
        @Deprecated Set<Concept> concept = new HashSet();

        public ActivationTreeMap(Object firstChild, float firstChildSize) {
            super(Item.get("", 0f, firstChild, firstChildSize));

            r = (Item.DefaultItem) root;
        }

        public ActivationTreeMap(Concept signal) {
            this(signal, signal.getPriority());
        }


        /** setup before display */
        public void commit() {
            for (Concept c : concept)
                r.add(c, c.getPriority());
            runLater(() -> {
                update();
                /*setCacheHint(CacheHint.SPEED);
                setCache(true);*/
            });
        }

        public void add(Concept c) {
            concept.add(c);
        }


    }

//    public static class ActivationSet extends FlowPane {
//        private final ObservableList pri;
//        private final BarChart<String, Number> bc;
//        private final XYChart.Series priSeries;
//        Set<Concept> concept = new HashSet();
//
//        public ActivationSet() {
//            super();
//
//            final CategoryAxis xAxis = new CategoryAxis();
//
//
//            final NumberAxis yAxis = new NumberAxis();
//            //yAxis.setForceZeroInRange(true);
//            yAxis.setLabel("Activation: Current Priority");
//            yAxis.setAutoRanging(true);
//
//            bc = new BarChart<>(xAxis,yAxis);
//
//            bc.setLegendVisible(false);
//            bc.setAnimated(false);
//            bc.setTitleSide(Side.LEFT);
//
//
//            priSeries = new XYChart.Series();
//            priSeries.setName("Priority");
//
//            this.pri = priSeries.getData();
//
//
//
//        }
//
//        /** setup before display */
//        public void commit() {
//            for (Concept c : concept)
//                pri.add(new XYChart.Data(
//                        c.getTerm().toString(),
//                        c.getBudget().getPriority()));
//            runLater(() -> {
//                bc.getData().add(priSeries);
//                getChildren().add(bc);
//                setCacheHint(CacheHint.SPEED);
//                setCache(true);
//            });
//        }
//
//        void onAdd(Concept c) {
//            //getChildren().add( new ConceptActivationIcon(c) );
//        }
//
//        public void add(Concept c) {
//            if (concept.add(c)) {
//                onAdd(c);
//            }
//        }
//
//    }


    public class PremisePane extends TextFlow {

        public PremisePane(Premise p) {
            super(
                    new Label(p.getClass().getSimpleName()),
                    new AutoLabel(p.getTask(),nar)
            );

            if (p.getBelief()!=null)
                getChildren().add(
                        new AutoLabel( p.getBelief(),nar )
                );

            /*setScaleX(0.5);
            setScaleY(0.5);*/

            setTextAlignment(TextAlignment.LEFT);

            setCenterShape(false);


            autosize();
            //TODO deferred Tooltip with details

            setCacheHint(CacheHint.SPEED);
            setCache(true);
        }

    }

    boolean activationTreeMap = false;

    private Node getNode(Object channel, Object signal) {
        if (channel.equals("eventConceptActivated")) {
            boolean newn = false;
            if (activationSet==null && activationTreeMap) {
                activationSet =
                        //new ActivationSet();
                        new ActivationTreeMap((Concept)signal);

                //activationSet.prefWidth(getWidth());
                activationSet.width.set(400);
                activationSet.height.set(100);
                newn = true;
            }
            else if (activationSet!=null) {
                activationSet.add((Concept) signal);
                newn = false;
            }

            if (!newn) return null;
            else
                return activationSet;
        }
        else if (channel.equals("eventCycleEnd")) {
            if (prev!=null && (prev instanceof CycleActivationBar)) {
                ((CycleActivationBar)prev).setTo(nar.time());
                return null;
            }
            else {
                return new CycleActivationBar(nar.time());
            }
//            if (activationSet!=null) {
//                activationSet.commit();
//                activationSet = null; //force a new one
//            }
            //return null;
            //
        }
        else if (channel.equals("eventFrameStart")) {
            return null;
            //
        } else if (channel.equals("eventInput")) {
            return new AutoLabel((Task) signal, nar);

        } else if (signal instanceof Premise) {
            //return new PremisePane((Premise)signal);
            return null;
        }

        else {
            return new Label(
                    //channel.toString() + ": " +
                    channel + ": " +
                    signal.toString());
        }
    }


    static class ConceptActivationIcon extends Button {

        public ConceptActivationIcon(Concept c) {
           super(c.getTerm().toStringCompact());
            setCache(true);
        }
    }

    /** displays a specific (from) or a range (from..to) of cycle time values */
    static class CycleActivationBar extends Label {
        private final long from;
        private long to = Stamp.TIMELESS;

        public CycleActivationBar(long from) {
            super(Long.toString(from));
            this.from = from;
            setCache(true);
        }

        public void setTo(long to) {
            this.to = to;
            runLater(this::update);
        }

        protected void update() {
            if (to == Stamp.TIMELESS)
                setText(Long.toString(from));
            else {
                setText(Long.toString(from) + " .. " + Long.toString(to) );
            }
        }
    }

//    public Node getNode(Output.Channel channel, Class event, Object[] args) {
//
//        if (args[0] instanceof Task) {
//            /*TaskLabel tl = new TaskLabel(channel.getLinePrefix(event, args) + ' ',
//                    (Task)args[0], nar);*/
//
//            Task t = (Task)args[0];
//            /*ItemButton tl = new ItemButton( t, (i) -> i.toString(),
//                    (i) -> {
//                        NARfx.window(nar, t);
//                    }
//            );*/
//            //AutoLabel tl = new AutoLabel( "", t, nar);
//            //tl.enablePopupClickHandler(nar);
//            //return tl;
//
//            return new AutoLabel(t, nar);
//        }
//
//        StringBuilder sb = TextOutput.append(event, args, false, nar, new StringBuilder());
//        final String s;
//        if (sb != null)
//            s = sb.toString();
//        else
//            s = "null: " + channel.get(event, args) + " " + event + " " + Arrays.toString(args);
//
//        Text t = new Text(s.toString());
//        t.setFill(Color.ORANGE);
//        t.setCache(true);
//        return t;
//    }

}

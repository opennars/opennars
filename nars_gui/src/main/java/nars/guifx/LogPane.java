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
import nars.NAR;
import nars.Premise;
import nars.concept.Concept;
import nars.guifx.treemap.Item;
import nars.guifx.treemap.TreemapChart;
import nars.nal.nal7.Tense;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static javafx.application.Platform.runLater;
import static nars.guifx.NARfx.scrolled;

/**
 * Created by me on 8/2/15.
 */
public class LogPane extends BorderPane  {

    final Pane content;

    final int maxLines = 64;




    //NSliderSet filter = new NSliderSet();

    ScrollPane scrollParent = null;


    public void commit(Node[] c) {
        content.getChildren().setAll(c);
        scrollBottom.run();
    }
    public void commit(Collection<? extends Node> c) {
        content.getChildren().setAll(c);
        scrollBottom.run();
    }

    final Runnable scrollBottom = () -> scrollParent.setVvalue(1.0f);

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

    public LogPane() {

        //content = new FlowPane();
        content = new VBox(1);

        //content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        //((VBox)content).setFillWidth(false);

        setCenter(scrollParent = scrolled(content));



        sceneProperty().addListener((c) -> {
            updateParent();
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





    public static class ActivationTreeMap extends TreemapChart {

        private final Item.DefaultItem r;
        @Deprecated Set<Concept> concept = new HashSet();

        public ActivationTreeMap(Object firstChild, float firstChildSize) {
            super(Item.get("", 0.0f, firstChild, firstChildSize));

            r = (Item.DefaultItem) root;
        }

        public ActivationTreeMap(Concept signal) {
            this(signal, 1f /*signal.getPriority()*/);
        }


        /** setup before display */
        public void commit() {
            for (Concept c : concept)
                r.add(c, 1f /*c.getPriority()*/);
            runLater(this::update);
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


    public static class PremisePane extends TextFlow {

        public PremisePane(Premise p, NAR nar) {
            super(
                    new Label(p.getClass().getSimpleName()),
                    new TaskLabel(p.getTask(),nar)
            );

            if (p.getBelief()!=null)
                getChildren().add(
                        new TaskLabel( p.getBelief(),nar )
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


    static class ConceptActivationIcon extends Button {

        public ConceptActivationIcon(Concept c) {
           super(c.get().toStringCompact());
            setCache(true);
        }
    }

    /** displays a specific (from) or a range (from..to) of cycle time values */
    static class CycleActivationBar extends Label {
        private final long from;
        private long to = Tense.TIMELESS;

        public CycleActivationBar(long from) {
            super(Long.toString(from));
            this.from = from;
            setCache(true);
        }

        public void setTo(long to) {
            if (to!=this.to) {
                this.to = to;
                runLater(this::update);
            }
        }

        protected void update() {
            if (to == Tense.TIMELESS)
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

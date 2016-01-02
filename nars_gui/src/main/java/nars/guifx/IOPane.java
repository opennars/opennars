package nars.guifx;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebView;
import nars.NAR;
import nars.Op;
import nars.guifx.demo.TaskButton;
import nars.guifx.util.NSlider;
import nars.nal.nal8.Operator;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.util.event.On;
import org.jetbrains.annotations.NotNull;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/2/15.
 */
public class IOPane extends BorderPane /*implements FXIconPaneBuilder*/ {

    final NSlider vs = new NSlider("Volume", 100, 25, NSlider.BarSlider, 1f);
    final DoubleProperty volume = vs.value[0];

    private final NAR nar;

    public class DefaultTracePane extends TracePane {

        public DefaultTracePane(NAR nar, DoubleProperty volume) {
            super(nar, volume);
        }

        @Override
        public Node getNode(Object channel, Object signal) {

            String channelString = channel.toString();
            switch (channelString) {
                /*case "eventConceptActivated":
                    boolean newn = false;
                    if (activationSet == null && activationTreeMap) {
                        activationSet =
                                //new ActivationSet();
                                new ActivationTreeMap((Concept) signal);

                        //activationSet.prefWidth(getWidth());
                        activationSet.width.set(400);
                        activationSet.height.set(100);
                        newn = true;
                    } else if (activationSet != null) {
                        activationSet.add((Concept) signal);
                        newn = false;
                    }

                    return !newn ? null : activationSet;*/
                case "eventCycleEnd":
                    if ((prev instanceof CycleActivationBar)) {
                        ((CycleActivationBar) prev).setTo(nar.time());
                        return null;
                    } else {
                        return new CycleActivationBar(nar.time());
                    }
//            if (activationSet!=null) {
//                activationSet.commit();
//                activationSet = null; //force a new one
//            }
                    //return null;
                    //
                case "eventFrameStart":
                case "eventInput":
                    break;
                case "eventRevision":
                case "eventTaskProcess":
                    Task t = (Task) signal;
                    return t.getPriority() > (1f-volume.floatValue())
                            //? new TaskLabel(t, nar) : null;
                            ?
                            getTaskNode(t) : null;
                default:
                    return new Label(
                            //channel.toString() + ": " +
                            channel + ": " +
                                    signal);

            }

            return null;
        }

        @NotNull
        public Node getTaskNode(Task t) {
            return IOPane.this.newTaskNode(t);
        }

        @Override
        public void appear() {
            super.appear();
            On commands = nar.onExec("html", (e) -> {

                Term[] a = e.argArray();
                String content = ((Atom)a[0]).toStringUnquoted();

                runLater(() -> {
                    WebView w = new WebView();
                    //w.resize(400,200);
                    w.getEngine().loadContent(
                            content
                    );
                    w.setMinHeight(100);

                    w.autosize();
                    w.layout();
                    append(w);
                } );
            });
        }

        @Override
        public void disappear() {
            super.disappear();
            //TODO unregister
        }
    }

    public Node newTaskNode(Task t) {
        Term tt = t.term();
        if (Op.isOperation(tt)) {
            Compound ct = (Compound) tt;
            Term[] a = Operator.opArgsArray(ct);
            switch (Operator.operatorName(ct).toString()) {
                case "html":
                    WebView w = new WebView();
                    //w.resize(400,200);
                    w.getEngine().loadContent(
                        ((Atom)a[0]).toStringUnquoted()
                    );
                    return w;

            }
        }
        return new TaskButton(nar, t.getTask());
        //return SubButton.make(nar, t);
    }

    public class OutputPane extends BorderPane {

        public OutputPane() {
            super();

            FlowPane menu = new FlowPane( vs );
            setTop(menu);

            setCenter(
                new DefaultTracePane(nar, volume)
            );
        }
    }

    public IOPane(NAR nar) {


        this.nar = nar;

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);

        split.getItems().addAll(
                new OutputPane(),
                new InputPane(nar));

        split.setDividerPosition(0,0.85);


        setMinSize(400, 300);
        split.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        setCenter(split);
    }

//    @Override
//    public Node newIconPane() {
//
//
//        BorderPane b = new BorderPane();
//        b.setBottom(vs);
//        b.setCenter(new StatusPane(nar, 384));
//        return b;
//
//        //return new NSlider(150, 60);
//
//        /*return new HBox(
//            new LinePlot(
//                    "# Concepts",
//                    () -> nar.concepts().size(),
//                    300,
//                    200,200
//            ),
//            new LinePlot(
//                    "Memory",
//                    () -> Runtime.getRuntime().freeMemory(),
//                    300,
//                    200,200
//            )
//        );*/
//    }
}

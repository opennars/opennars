package nars.guifx;

import automenta.vivisect.javafx.demo.NARGraph1;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import nars.NAR;
import nars.NARStream;
import nars.event.CycleReaction;
import nars.event.NARReaction;

/**
 * Created by me on 1/21/15.
 */
public class NARPane extends SplitPane {


    private final BorderPane menu = new BorderPane();

    private final TabPane content = new TabPane();
    public final NARControlFX controlStrip;


    Tab console = null;

    public void console(boolean enabled) {

        if (enabled) {
            Platform.runLater(() -> {
                contentUpdate(true);
            });
        } else if (console != null) {
            Platform.runLater(() -> {
                contentUpdate(false);
            });
        }

    }

    public static class ResizableCanvas extends Canvas {

        private final Pane parent;

        public ResizableCanvas(Pane parent) {
            super();
            this.parent = parent;

            // Bind canvas size to stack pane size.
            widthProperty().bind(parent.widthProperty());
            heightProperty().bind(parent.heightProperty());


            final boolean bindRedraw = false; //TODO parameter to make this optional to avoid unnecessary event being attached
            if (bindRedraw) {
                // Redraw canvas when size changes.
                widthProperty().addListener(evt -> draw());
                heightProperty().addListener(evt -> draw());
            }
        }

        protected void draw() {
            /*double width = getWidth();
            double height = getHeight();

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);

            gc.setStroke(Color.RED);
            gc.strokeLine(0, 0, width, height);
            gc.strokeLine(0, height, width, 0);*/
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }
    }

    //TODO detect when component is hidden and disable the event
    abstract public static class FXReaction extends NARReaction implements ChangeListener {

        private final Node fx;

        public FXReaction(NAR nar, Node fx, Class... events) {
            super(nar, true, events);
            this.fx = fx;


            //fx.visibleProperty().addListener(this);
            //if (fx.visibleProperty().get())
            //setActive(true);

        }

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            System.out.println(newValue);

            if ((boolean) newValue == true) {
                setActive(true);
            } else {
                setActive(false);
            }
        }
    }

    private final NAR nar;


    public NARPane(NAR n) {
        super();
        this.nar = n;

        getStylesheets().addAll(NARfx.css, "dark.css" );

        NARStream s = new NARStream(n);

        controlStrip = new NARControlFX(nar, true, true, true) {
            @Override
            protected void onConsole(boolean selected) {


                NARGraph1 g = new NARGraph1(nar);

                final TilePane lp = new TilePane(4,4,
//                        new LinePlot("Total Priority", () ->
//                            nar.memory.getActivePrioritySum(true, true, true)
//                        , 128),
                        new LinePlot("Concept Priority", () -> {
                            int c = nar.memory.getControl().size();
                            if (c == 0) return 0;
                            else return nar.memory.getActivePrioritySum(true, false, false) / (c);
                        }, 128),
                        new LinePlot("TermLink Priority", () ->
                            nar.memory.getActivePrioritySum(false, true, false)
                        , 128),
                        new LinePlot("TaskLink Priority", () ->
                            nar.memory.getActivePrioritySum(false, false, true)
                        , 128)
                );
                lp.setPrefColumns(2);
                lp.setPrefRows(2);

                new CycleReaction(nar) {

                    @Override
                    public void onCycle() {
                        for (Object o : lp.getChildren()) {
                            if (o instanceof LinePlot)
                                ((LinePlot)o).update();
                        }
                    }
                };

                lp.setOpacity(0.5f);
                lp.setPrefSize(200,200);
                lp.maxWidth(Double.MAX_VALUE);
                lp.maxHeight(Double.MAX_VALUE);
                lp.setMouseTransparent(true);
                lp.autosize();


//                StackPane s = new StackPane(lp);
//                s.maxWidth(Double.MAX_VALUE);
//                s.maxHeight(Double.MAX_VALUE);


                content.getTabs().add(new Tab("Graph", new StackPane(g, lp) ));

                System.out.println(lp.getLayoutBounds());
            }
        };


        final BorderPane f = new BorderPane();


        LinePlot lp = new LinePlot(
                "Concepts",
                () -> (nar.memory.getConcepts().size()),
                300
        );
//        LinePlot lp2 = new LinePlot(
//                "Happy",
//                () -> nar.memory.emotion.happy(),
//                300
//        );
//
//        VBox vb = new VBox(lp, lp2);
//        vb.autosize();


        s.forEachCycle(lp::update);

        //f.setCenter( scrolled(lp)       );
        f.setCenter(scrolled(new TreePane(n)));
        f.setRight(controlStrip);


        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
      //  content.getTabs().add(new Tab("I/O", new TerminalPane(nar)));


        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        f.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        content.setVisible(true);

        getItems().setAll(f, content);
        setDividerPositions(0.5f);

        //autosize();

    }

    public Stage newStage() {

        return NARfx.getStage(nar.toString(), this);

    }


    public void contentUpdate(boolean show) {

        Platform.runLater(() -> {
            if (!show) {
                content.setVisible(false);
            } else {
                content.setVisible(true);
            }

            layout();
            //g.autosize();

            setDividerPosition(0, 0.25);

//                if (!isMaximized())
//                    sizeToScene();
        });

    }

    public static final javafx.scene.control.ScrollPane scrolled(Node n) {
        return scrolled(n, true, true);
    }

    public static final javafx.scene.control.ScrollPane scrolled(Node n, boolean stretchwide, boolean stretchhigh) {
        javafx.scene.control.ScrollPane s = new javafx.scene.control.ScrollPane();
        s.setHbarPolicy(stretchwide ? ScrollPane.ScrollBarPolicy.AS_NEEDED : ScrollPane.ScrollBarPolicy.NEVER);
        s.setVbarPolicy(stretchwide ? ScrollPane.ScrollBarPolicy.AS_NEEDED : ScrollPane.ScrollBarPolicy.NEVER);

        s.setContent(n);

        if (stretchhigh) {
            s.setMaxHeight(Double.MAX_VALUE);
        }
        s.setFitToHeight(true);

        if (stretchwide) {
            s.setMaxWidth(Double.MAX_VALUE);
        }
        s.setFitToWidth(true);

        //s.autosize();
        return s;
    }
}

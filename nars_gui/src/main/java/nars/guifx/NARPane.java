package nars.guifx;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nars.NAR;
import nars.NARStream;
import nars.event.NARReaction;

/**
 * Created by me on 1/21/15.
 */
public class NARPane extends SplitPane {


    private final BorderPane menu = new BorderPane();

    private final TabPane content = new TabPane();
    public final NARControlFX controlStrip;
    public final BorderPane f;

    Tab console = null;

    public void console(boolean enabled) {

        if (enabled && console == null) {

            //console = new Tab("I/O", new TerminalPane(nar));

            Platform.runLater(() -> {
                content.getTabs().add(console);
                contentUpdate();
            });
        }
        else if (console !=null) {
            Platform.runLater(() -> {
                content.getTabs().remove(console);
                console = null;
                contentUpdate();
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

            if ((boolean)newValue == true) {
                setActive(true);
            }
            else {
                setActive(false);
            }
        }
    }

    private final NAR nar;


    public NARPane(NAR n) {
        super();
        this.nar = n;

        NARStream s = new NARStream(n);

        controlStrip = new NARControlFX(nar, true, true, true) {
            @Override protected void onConsole(boolean selected) {
                console(selected);
            }
        };


        f = new BorderPane();


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


        s.forEachCycle( lp::update );

        f.setCenter( scrolled(lp)       );

        f.setRight(controlStrip);



        f.setMaxHeight(Double.MAX_VALUE);

        //content.setMaxWidth(Double.MAX_VALUE);
        content.setMaxHeight(Double.MAX_VALUE);


        getChildren().setAll(f);

        //g.setDividerPositions(0.5f);
        //f.setMaxWidth(Double.MAX_VALUE);

    }

    public Stage newStage() {

        contentUpdate();
        return NARfx.getStage(nar.toString(), this);

    }



    public void contentUpdate() {

            Platform.runLater(() -> {
                if (content.getTabs().size() == 0) {
                    content.setVisible(false);
                    getItems().setAll(f);
                }
                else {
                    content.setVisible(true);
                    getItems().setAll(f, content);

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

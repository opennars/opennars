package nars.guifx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
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
import nars.util.event.Reaction;

import java.util.function.Consumer;

import static javafx.application.Platform.runLater;
import static nars.guifx.NARfx.scrolled;

/**
 * Created by me on 1/21/15.
 */
public class NARPane extends BorderPane {


    private final TabPane menu = new TabPane();

    public final TabPane content = new TabPane();

    public final NARControlFX controlStrip;
    private final NARStream narstream;

    Tab console = null;

    public void console(boolean enabled) {

        if (enabled) {
            runLater(() -> {
                contentUpdate(true);
            });
        } else if (console != null) {
            runLater(() -> {
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

    public final NAR nar;


    public NARPane(NAR n) {
        super();
        this.nar = n;

        runLater(() -> {
                    TabPaneDetacher tabDetacher = new TabPaneDetacher();
                    tabDetacher.makeTabsDetachable(content);
                    tabDetacher.stylesheets(getScene().getStylesheets().toArray(new String[getScene().getStylesheets().size()]));
        });

        NARStream s = this.narstream = new NARStream(n);

        controlStrip = new NARControlFX(nar, true, true, true) {
            @Override
            protected void onConsole(boolean selected) {


            }
        };


        final BorderPane f = new BorderPane();


        /*LinePlot lp = new LinePlot(
                "Concepts",
                () -> (nar.memory.getConcepts().size()),
                300
        );*/
//        LinePlot lp2 = new LinePlot(
//                "Happy",
//                () -> nar.memory.emotion.happy(),
//                300
//        );
//
//        VBox vb = new VBox(lp, lp2);
//        vb.autosize();


        //s.forEachCycle(lp::update);

        //f.setCenter( scrolled(lp)       );

        menu.setSide(Side.LEFT);
        menu.getTabs().addAll(

                new TabX("Tasks",
                        new TreePane(n)).closeable(false),

                new TabX.TabButton("+",
                        scrolled( new NARReactionPane() ) )
                        .button("I/O", (e) -> {  })
                        .button("Graph", (e) -> {  })
                        .button("About", (e) -> {  })
                ,


                new TabX("Concepts",
                        new VBox()).closeable(false),



//                new TabX("Stats",
//                    new VBox()).closeable(false),

                new TabX("InterNAR",
                        new VBox()).closeable(false)




        );

        menu.setRotateGraphic(true);

        f.setCenter(menu);

        f.setRight(controlStrip);


        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
      //  content.getTabs().add(new Tab("I/O", new TerminalPane(nar)));


        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        f.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        content.setVisible(true);

        SplitPane p = new SplitPane();
        p.getItems().setAll(f, content);
        p.setDividerPositions(0.5f);

        setCenter(p);

        //autosize();

    }

    public Stage newStage() {

        return NARfx.getStage(nar.toString(), this);

    }


    public void contentUpdate(boolean show) {

        runLater(() -> {
            if (!show) {
                content.setVisible(false);
            } else {
                content.setVisible(true);
            }

            layout();
            //g.autosize();

            //p.setDividerPosition(0, 0.25);

//                if (!isMaximized())
//                    sizeToScene();
        });

    }


    public class NARReactionPane extends NARCollectionPane<Reaction> {

        public NARReactionPane() {
            super(narstream, r ->
                    new Label(r.toString())
            );
        }

        @Override
        public void collect(Consumer<Reaction> c) {
            narstream.nar.memory.exe.forEachReaction(c);
        }
    }
}

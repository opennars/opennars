package nars.guifx;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.gui.NARControlPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by me on 1/21/15.
 */
public class NARWindow extends Stage {

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

    /**
     * small VBox vertically oriented component which can be attached
     * to the left or right of anything else, which contains a set of
     * buttons for controlling a nar
     */
    public static class NARControlFX extends StackPane {

        private final FXReaction busyBackgroundColor;

        public NARControlFX(NAR n, boolean runButtons, boolean memoryButtons, boolean guiButtons) {
            super();

            Canvas canvas = new ResizableCanvas(this);
            //canvas.maxWidth(Double.MAX_VALUE);
            //canvas.maxHeight(Double.MAX_VALUE);

            VBox v = new VBox();

            getChildren().add(canvas);
            getChildren().add(v);



            //b.getChildren().add(new Separator(Orientation.HORIZONTAL));


            //b.getChildren().add(new Separator(Orientation.HORIZONTAL));


            if (runButtons) {
                Button bp = GlyphsDude.createIconButton(FontAwesomeIcon.PLAY);
                bp.setTooltip(new Tooltip("Play"));
                v.getChildren().add(bp);


                Button bs = GlyphsDude.createIconButton(FontAwesomeIcon.STEP_FORWARD);
                bs.setTooltip(new Tooltip("Step"));
                v.getChildren().add(bs);
            }

            if (memoryButtons) {
                Button b0 = GlyphsDude.createIconButton(FontAwesomeIcon.FOLDER);
                b0.setTooltip(new Tooltip("Open"));
                v.getChildren().add(b0);

                Button b1 = GlyphsDude.createIconButton(FontAwesomeIcon.SAVE);
                b1.setTooltip(new Tooltip("Save"));
                v.getChildren().add(b1);

                Button b2 = GlyphsDude.createIconButton(FontAwesomeIcon.CODE_FORK);
                b2.setTooltip(new Tooltip("Clone"));
                v.getChildren().add(b2);
            }

            if (guiButtons) {
                Button bi = GlyphsDude.createIconButton(FontAwesomeIcon.CODE);
                bi.setTooltip(new Tooltip("Input..."));
                v.getChildren().add(bi);

                Button bo = GlyphsDude.createIconButton(FontAwesomeIcon.TACHOMETER);
                bo.setTooltip(new Tooltip("Output..."));
                v.getChildren().add(bo);
            }

            v.getChildren().forEach(c -> {
                if (c instanceof Control)
                    ((Control) c).setMaxWidth(Double.MAX_VALUE);
            });
            //b.setFillWidth(true);




            this.busyBackgroundColor = new FXReaction(n, this, Events.FrameEnd.class) {

                @Override
                public void event(Class event, Object[] args) {

                    if (event == Events.FrameEnd.class) {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                float b = 0, h = 0;

                                if (n.isRunning()) {
                                    b = n.memory.emotion.busy();
                                    h = n.memory.emotion.happy();
                                }

                                if ((canvas.getWidth()!=getWidth()) || (canvas.getHeight()!=getHeight()))
                                    canvas.resize(Double.MAX_VALUE, Double.MAX_VALUE);

                                GraphicsContext g = canvas.getGraphicsContext2D();
                                g.setFill(new javafx.scene.paint.Color(0.25 * b, 0.25 * h, 0, 1.0));
                                g.fillRect(0, 0, getWidth(), getHeight());

                            }
                        });

                    }

                }
            };


        }


    }

    private final NAR nar;

    public NARWindow(NAR n) {
        super();
        this.nar = n;
        setTitle("NAR " + n.hashCode());


        StackPane b = new NARControlFX(nar, true, true, true);

        /*.root {
            -fx-base: rgb(50, 50, 50);
            -fx-background: rgb(50, 50, 50);
            -fx-control-inner-background:  rgb(50, 50, 50);
        }*/


        VBox f = new VBox();

        TitledPane tp1 = new TitledPane("Options", new Button("Button"));


        SwingNode mn = new SwingNode();
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(new NARControlPanel(nar, null, false));
        mn.setContent(jp);
        tp1.setContent(mn);


        f.getChildren().add(tp1);

        TitledPane tp2 = new TitledPane("Tasks", new Button("Button"));

        f.getChildren().add(new NARTree(n));


        Scene scene = new Scene(new HBox(2, b, f));
        scene.getStylesheets().add("dark.css");

        setScene(scene);
    }
}

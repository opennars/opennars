package nars.guifx;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import nars.Events;
import nars.NAR;
import nars.NARStream;
import nars.guifx.util.NSlider;

import java.util.concurrent.atomic.AtomicBoolean;

import static javafx.application.Platform.runLater;

/**
 * small VBox vertically oriented component which can be attached
 * to the left or right of anything else, which contains a set of
 * buttons for controlling a nar
 */
public class NARControlFX extends HBox {


    //private final NARWindow.FXReaction busyBackgroundColor;


    final NAR nar;
    private final NARThreadControlPane threadControl;
    public final Menu tool;


    public static class NARThreadControlPane extends FlowPane implements Runnable {

        final static Text play = GlyphsDude.createIcon(FontAwesomeIcon.PLAY, GlyphIcon.DEFAULT_FONT_SIZE);
        final static Text stop = GlyphsDude.createIcon(FontAwesomeIcon.STOP, GlyphIcon.DEFAULT_FONT_SIZE);
        final Label clock = new Label("0");
        private final NAR nar;
        boolean wasRunning = false;
        final AtomicBoolean pendingClockUpdate = new AtomicBoolean(false);
        ////TODO: public final SimpleBooleanProperty pendingClockUpdate

        private final long defaultNARPeriodMS = 75;

        public void run() {
            if (pendingClockUpdate.getAndSet(true) == false) {

                runLater(() -> {
                    pendingClockUpdate.set(false);
                    boolean running = nar.isRunning();
                    if (running != wasRunning) {
                        //bp.setGraphic(running ? stop : play);
                        wasRunning = running;
                    }

                    clock.setText("" + nar.time());
                });
            }
        }

        public NARThreadControlPane(NAR n) {
            super(Orientation.HORIZONTAL, 1, 0);

            getStyleClass().add("thread_control");

            setAlignment(Pos.CENTER_RIGHT);
            setColumnHalignment(HPos.RIGHT);

            this.nar = n;


            new NARStream(n).on(this, Events.FrameEnd.class, Events.ResetStart.class);

            Button runButton = JFX.newIconButton(FontAwesomeIcon.PLAY);
            Button stepButton = JFX.newIconButton(FontAwesomeIcon.STEP_FORWARD);
            NSlider speedSlider = new NSlider(100, 25.0);

            runButton.setTooltip(new Tooltip("Toggle run/stop"));


            runButton.setOnAction(e -> {

                if (!n.isRunning()) {
                    synchronized (n) {
                        //TODO make sure only one thread is running, maybe with singleThreadExecutor

                        stepButton.setDisable(true);
                        speedSlider.setOpacity(1.0);

                        new Thread(() -> {
                            n.loop(defaultNARPeriodMS);
                        }).start();
                    }
                } else {
                    n.stop();

                    if (!n.isRunning()) {
                        stepButton.setDisable(false);
                        speedSlider.setOpacity(0.5);
                    }
                }

            });


            stepButton.setTooltip(new Tooltip("Step"));
            stepButton.setOnAction(e -> {
                if (n.isRunning())
                    n.frame();
            });


//        Slider speedSlider = new Slider(0, 1, 0);
//        speedSlider.setOrientation(Orientation.VERTICAL);
//        speedSlider.setTooltip(new Tooltip("Speed"));
//        speedSlider.setMinorTickCount(10);
//        speedSlider.setShowTickMarks(true);
//        getChildren().add(speedSlider);


            getChildren().setAll( runButton, speedSlider, stepButton, clock);


            autosize();
        }

    }

    public NARControlFX(NAR n, boolean runButtons, boolean memoryButtons, boolean guiButtons) {
        super();

        this.nar = n;
        //Canvas canvas = new NARWindow.ResizableCanvas(this);
        //canvas.maxWidth(Double.MAX_VALUE);
        //canvas.maxHeight(Double.MAX_VALUE);


        //b.getChildren().add(new Separator(Orientation.HORIZONTAL));


        //b.getChildren().add(new Separator(Orientation.HORIZONTAL));


        {
            Button iconButton = JFX.newIconButton(FontAwesomeIcon.GEAR);
            iconButton.setMouseTransparent(true);
            Menu main = new Menu("", iconButton);
            main.getStyleClass().add("nar_main_menu");
            main.getItems().add(new MenuItem("New..."));
            main.getItems().add(new MenuItem("Save..."));
            main.getItems().add(new MenuItem("Fork..."));
            main.getItems().add(new MenuItem("Discard..."));
            main.getItems().add(new SeparatorMenuItem());
            main.getItems().add(new MenuItem("Exit..."));

            Button button2 = JFX.newIconButton(FontAwesomeIcon.NAVICON);
            button2.setMouseTransparent(true);
            tool = new Menu("", button2);
            tool.getItems().add(new MenuItem("Interface..."));
            tool.getItems().add(new MenuItem("Cognition..."));
            tool.getItems().add(new MenuItem("Sensor..."));

            getChildren().add(new MenuBar(main, tool));
        }


        getChildren().add(threadControl = new NARThreadControlPane(nar));

//        if (memoryButtons) {
//            Button b0 = JFX.newIconButton(FontAwesomeIcon.FOLDER);
//            b0.setTooltip(new Tooltip("Open"));
//            getChildren().add(b0);
//
//            Button b1 = JFX.newIconButton(FontAwesomeIcon.SAVE);
//            b1.setTooltip(new Tooltip("Save"));
//            getChildren().add(b1);
//
//            Button b2 = JFX.newIconButton(FontAwesomeIcon.CODE_FORK);
//            b2.setTooltip(new Tooltip("Clone"));
//            getChildren().add(b2);
//        }

//        if (guiButtons) {
//            consoleButton = JFX.newToggleButton(FontAwesomeIcon.CODE);
//            consoleButton.setTooltip(new Tooltip("I/O..."));
//            getChildren().add(consoleButton);
////            consoleButton.setOnAction(e -> {
////                onConsole(consoleButton.isSelected());
////            });
//
////            Button bo = newIconButton(FontAwesomeIcon.TACHOMETER);
////            bo.setTooltip(new Tooltip("Output..."));
////            v.getChildren().add(bo);
//        } else {
//            consoleButton = null;
//        }
//
//
//        getChildren().forEach(c -> {
//            if (c instanceof Control)
//                ((Control) c).setMaxWidth(Double.MAX_VALUE);
//        });
//        setMaxWidth(Double.MAX_VALUE);
        //setFillHeight(true);


        NSlider fontSlider = new NSlider(25f, 25f);
        getChildren().add(0, fontSlider);
        fontSlider.value.addListener((a, b, c) -> {
            runLater(() -> {
                double pointSize = 6 + 12 * c.doubleValue();
                getScene().getRoot().setStyle("-fx-font-size: " + pointSize + "pt;");
                //+ 100*(0.5 + c.doubleValue()) + "%");
            });
        });
        fontSlider.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                //double click
                System.out.println("double click fontSlider");
            }
        });


//        this.busyBackgroundColor = new NARWindow.FXReaction(n, this, Events.FrameEnd.class) {
//
//            @Override
//            public void event(Class event, Object[] args) {
//
//                if (event == Events.FrameEnd.class) {
//                    Platform.runLater(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            float b = 0, h = 0;
//
//                            if (n.isRunning()) {
//                                b = n.memory.emotion.busy();
//                                h = n.memory.emotion.happy();
//                            }
//
//                            if ((canvas.getWidth()!=getWidth()) || (canvas.getHeight()!=getHeight()))
//                                canvas.resize(Double.MAX_VALUE, Double.MAX_VALUE);
//
//                            GraphicsContext g = canvas.getGraphicsContext2D();
//                            g.setFill(new javafx.scene.paint.Color(0.25 * b, 0.25 * h, 0, 1.0));
//                            g.fillRect(0, 0, getWidth(), getHeight());
//
//                        }
//                    });
//
//                }
//
//            }
//        };

        threadControl.run();

    }


}

package nars.guifx;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nars.NAR;
import nars.guifx.util.NSlider;
import nars.util.NARLoop;
import nars.util.event.Active;

import static javafx.application.Platform.runLater;

/**
 * small VBox vertically oriented component which can be attached
 * to the left or right of anything else, which contains a set of
 * buttons for controlling a nar
 */
public class NARControlFX extends HBox {


    //private final NARWindow.FXReaction busyBackgroundColor;


    final NAR nar;

    public final Menu tool;


    public NARControlFX(NAR n) {
        super();

        this.nar = n;
        //Canvas canvas = new NARWindow.ResizableCanvas(this);
        //canvas.maxWidth(Double.MAX_VALUE);
        //canvas.maxHeight(Double.MAX_VALUE);


        //b.getChildren().add(new Separator(Orientation.HORIZONTAL));


        //b.getChildren().add(new Separator(Orientation.HORIZONTAL));

        NSlider fontSlider = new NSlider(25f, 25f, 0.5);
        {
            //getChildren().add(0, fontSlider);
            fontSlider.value[0].addListener((a, b, c) -> {
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

        }
        {
            Button iconButton = JFX.newIconButton(FontAwesomeIcon.GEAR);
            iconButton.setMouseTransparent(true);


            Menu main = new Menu("", iconButton);
            main.getStyleClass().add("nar_main_menu");
            main.getItems().add(new MenuItem("", fontSlider));
            main.getItems().add(new MenuItem("New..."));
            main.getItems().add(new MenuItem("Save..."));
            main.getItems().add(new MenuItem("Fork..."));
            main.getItems().add(new MenuItem("Discard..."));
            main.getItems().add(new SeparatorMenuItem());
            main.getItems().add(new MenuItem("Exit..."));

            Button button2 = JFX.newIconButton(FontAwesomeIcon.NAVICON);
            button2.setMouseTransparent(true);
            tool = new Menu("", button2);

            getChildren().add(new MenuBar(main, tool));
        }


        //getChildren().add(threadControl = new CycleClockPane(nar));

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

        //threadControl.run();

    }


    public static class RTClockPane extends CycleClockPane {
        public RTClockPane(NAR nar) {
            super(nar);

            getChildren().addAll(
                    new FlowPane(
                            new NSlider("Power", 48, 48, NSlider.BarSlider, 0.5),
                            new NSlider("Duration", 48, 48, NSlider.CircleKnob, 0.75),
                            new NSlider("Focus", 48, 48, NSlider.CircleKnob, 0.6),
                            new Button("Relax")
                    )
            );
        }
    }


    public static class LoopPane extends VBox {

        final Label label = new Label();
//        final static Text play = GlyphsDude.createIcon(FontAwesomeIcon.PLAY, GlyphIcon.DEFAULT_FONT_SIZE);
//        final static Text stop = GlyphsDude.createIcon(FontAwesomeIcon.STOP, GlyphIcon.DEFAULT_FONT_SIZE);
        private final NARLoop loop;
        private final Button runButton;
        private final Button stepButton;
        private final SimpleStringProperty cpuLabel;

        private final NSlider cpuSlider;


        public LoopPane(NARLoop loop) {
            super();

            this.loop = loop;

            final NAR n = loop.nar;
            runButton = JFX.newIconButton(FontAwesomeIcon.PLAY);
            stepButton = JFX.newIconButton(FontAwesomeIcon.STEP_FORWARD);
            cpuLabel = new SimpleStringProperty("CPU");

            cpuSlider = new NSlider(cpuLabel, 100, 30.0, NSlider.BarSlider, -1);
            //cpuSlider.min.set(0);
            //cpuSlider.max.set(2000);



            runButton.setTooltip(new Tooltip("Toggle run/pause"));


            runButton.setOnAction(e -> {

                if (loop.getPeriodMS() < 0) {
                    tryStart();
                } else {
                    pause();
                }

            });


            stepButton.setTooltip(new Tooltip("Step"));
            stepButton.setOnAction(e -> {

                if (!n.running()) {
                    n.frame();
                    say("stepped to time " + n.time());
                } else {
                    say("already running");
                }
            });

            pause();

//        Slider cpuSlider = new Slider(0, 1, 0);
//        cpuSlider.setOrientation(Orientation.VERTICAL);
//        cpuSlider.setTooltip(new Tooltip("Speed"));
//        cpuSlider.setMinorTickCount(10);
//        cpuSlider.setShowTickMarks(true);
//        getChildren().add(cpuSlider);
//

            getChildren().addAll(
                    new FlowPane(runButton, cpuSlider, stepButton),
                    new FlowPane(label)
            );

        }

        private void tryStart() {
            //TODO make sure only one thread is running, maybe with singleThreadExecutor




            //cpuSlider.value(-1);
            cpuSlider.setOpacity(1.0);

            cpuLabel.setValue("ON " + cpuSlider.v());


            //-2 here is a magic number to indicate that nothing is pending and can be changed now
            cpuSlider.value[0].addListener((s, p, c) -> {

                updateLoop();

            });

            updateLoop();

            say("ready");

        }

        private void updateLoop() {
            double v = cpuSlider.value[0].get();

            //slider (0..1.0) -> millisecond fixed period
            /*int nMS = (int) FastMath.round(
                    //1000.0 * (1.0 / (0.05 + c.doubleValue()))
                    2000.0 * v // / (1.0 - v)
            );*/
            float logScale = 50f;
            int nMS = (int)Math.round((1.0 - Math.log(1+v*logScale)/Math.log(1+logScale)) * 1024.0);

            if (loop.setPeriodMS(nMS)) {

                //new delay set:

                final int MS = nMS;

                runLater(() -> {
                    stepButton.setDisable(true);
                    say("set period=" + MS + "ms");
                });
            }
        }

        private void pause() {
            loop.pause();

            runLater(() -> {
                say("ready");

                stepButton.setDisable(false);
                cpuSlider.setOpacity(0.25);
                cpuLabel.setValue("OFF");
            });
        }

        protected void say(String text) {
            label.setText(text);
        }
    }

    public static class CycleClockPane extends VBox implements Runnable {

        final Label clock = new Label("?");
        private final NAR nar;
        private final Active regs;
        //final AtomicBoolean pendingClockUpdate = new AtomicBoolean(false);
        ////TODO: public final SimpleBooleanProperty pendingClockUpdate


        public void run() {
//            if (pendingClockUpdate.compareAndSet(false, true))
             {

//                runLater(() -> {
//                    pendingClockUpdate.set(false);
//                    boolean running = nar.running();
//                    if (running != wasRunning) {
//                        //bp.setGraphic(running ? stop : play);
//                        wasRunning = running;
//                    }
//
//
//                });
                clock.setText("" + nar.time());
            }
        }

        public CycleClockPane(NAR n) {
            super();

            getStyleClass().add("thread_control");

            setAlignment(Pos.CENTER_LEFT);
            //setColumnHalignment(HPos.RIGHT);

            this.nar = n;

            this.regs = new Active().add(
                    n.memory.eventFrameStart.on(nn -> {
                        //System.out.println("frame: " + nn.time());
                        run();
                    }),
                    n.memory.eventReset.on(nn -> {
                        run();
                    })
            );


            autosize();
        }

    }


}

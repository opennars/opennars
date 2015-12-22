package nars.guifx;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import nars.NAR;
import nars.guifx.nars.LoopPane;
import nars.guifx.util.NSlider;
import nars.task.in.NQuadsRDF;
import nars.util.event.Active;

import java.io.File;
import java.io.FileInputStream;

import static javafx.application.Platform.runLater;

/**
 * small VBox vertically oriented component which can be attached
 * to the left or right of anything else, which contains a set of
 * buttons for controlling a nar
 */
public class NARMenu extends HBox {


    //private final NARWindow.FXReaction busyBackgroundColor;


    final NAR nar;

    public final Menu tool;

    public static NSlider vol = new NSlider("Volume", 100, 30, NSlider.BarSlider, 0.0);

    public NARMenu(NAR n) {
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


            Button button2 = JFX.newIconButton(FontAwesomeIcon.NAVICON);
            button2.setMouseTransparent(true);
            tool = new Menu("", button2);

            Menu main = new Menu("", iconButton);
            main.getStyleClass().add("nar_main_menu");
            tool.getItems().add(new MenuItem("Font Size", fontSlider));

            Menu main2 = new Menu("", vol);

            Button finish = JFX.newIconButton(FontAwesomeIcon.FLAG);
            finish.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    vol.value(100);
                                    LoopPane.cpuSlider.value(1.0);
                                    LoopPane.multiplier.setValue(512);
                                    LoopPane.runButton.getOnAction().handle(null);
                                }
                            });


            Menu main3 = new Menu("", finish);
            main.getItems().add(new MenuItem("New..."));


            MenuItem res = new MenuItem("Reset");
            res.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    nar.reset();
                                }
                            }
            );
            tool.getItems().add(res);

            Menu loadMenu;
            /*main.getItems().add(loadMenu = new Menu("Load..."));
            {
                loadMenu.getItems().add(new AsyncMenuItem(n, ".n3 RDF") {
                    @Override public void run(NAR n) {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Load RDF File");
                        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("n3","n4","turtle","rdf" ));
                        File f = fileChooser.showOpenDialog(null);
                        if (f!=null) {
                            try {
                                NQuadsRDF.input(n, new FileInputStream(f));
                            } catch (Exception e) {
                                n.memory.eventError.emit(e);
                            }
                        }
                    }
                });
            }*/

            //main.getItems().add(new MenuItem("Save..."));
            //main.getItems().add(new MenuItem("Fork..."));
            //main.getItems().add(new MenuItem("Discard..."));
          //  main.getItems().add(new SeparatorMenuItem());
            //main.getItems().add(new MenuItem("Exit..."));

            MenuBar menubar = new MenuBar(tool, main2, main3);
            menubar.prefWidthProperty().bind(this.widthProperty());
            getChildren().add(menubar);
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


    abstract static class AsyncMenuItem extends MenuItem {

        public AsyncMenuItem(NAR n, String label) {
            super(label);
            this.setOnAction((e) -> run(n));
        }

        abstract public void run(NAR n);
    }
}

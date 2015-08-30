package nars.guifx;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import nars.Events;
import nars.NAR;
import nars.NARStream;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * small VBox vertically oriented component which can be attached
 * to the left or right of anything else, which contains a set of
 * buttons for controlling a nar
 */
abstract public class NARControlFX extends VBox implements Runnable {

    public final ToggleButton consoleButton;
    private final long defaultNARPeriodMS = 75;

    //private final NARWindow.FXReaction busyBackgroundColor;

    boolean wasRunning = false;

    final static Text play = GlyphsDude.createIcon(FontAwesomeIcon.PLAY, GlyphIcon.DEFAULT_FONT_SIZE);
    final static Text stop = GlyphsDude.createIcon(FontAwesomeIcon.STOP, GlyphIcon.DEFAULT_FONT_SIZE);

    final Label clock = new Label("0");
    final Button bp;

    final AtomicBoolean pending = new AtomicBoolean(false);

    final NAR nar;

    public void run() {
        if (pending.getAndSet(true)==false) {

            Platform.runLater(() -> {
                pending.set(false);
                boolean running = nar.isRunning();
                if (running != wasRunning) {
                    //bp.setGraphic(running ? stop : play);
                    wasRunning = running;
                }

                clock.setText("" + nar.time());
            });
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


        getChildren().add(clock);

        bp = JFX.newIconButton(FontAwesomeIcon.PLAY);
        bp.setTooltip(new Tooltip("Toggle run/stop"));


        new NARStream(n).on(this, Events.FrameEnd.class, Events.ResetStart.class);

        if (runButtons) {
            bp.setOnAction(e -> {
                synchronized (n) {
                    if (!n.isRunning()) {

                        //TODO make sure only one thread is running, maybe with singleThreadExecutor

                        new Thread(() -> {
                            n.loop(defaultNARPeriodMS);
                        }).start();
                    } else {
                        n.stop();
                    }
                }
            });
            getChildren().add(bp);


            Button bs = JFX.newIconButton(FontAwesomeIcon.STEP_FORWARD);
            bs.setTooltip(new Tooltip("Step"));
            bs.setOnAction(e -> {
                if (!n.isRunning())
                    n.frame();
            });
            getChildren().add(bs);
        }


        Slider speedSlider = new Slider(0, 1, 0);
        speedSlider.setOrientation(Orientation.VERTICAL);
        speedSlider.setTooltip(new Tooltip("Speed"));
        speedSlider.setMinorTickCount(10);
        speedSlider.setShowTickMarks(true);
        getChildren().add(speedSlider);



        if (memoryButtons) {
            Button b0 = JFX.newIconButton(FontAwesomeIcon.FOLDER);
            b0.setTooltip(new Tooltip("Open"));
            getChildren().add(b0);

            Button b1 = JFX.newIconButton(FontAwesomeIcon.SAVE);
            b1.setTooltip(new Tooltip("Save"));
            getChildren().add(b1);

            Button b2 = JFX.newIconButton(FontAwesomeIcon.CODE_FORK);
            b2.setTooltip(new Tooltip("Clone"));
            getChildren().add(b2);
        }

        if (guiButtons) {
            consoleButton = JFX.newToggleButton(FontAwesomeIcon.CODE);
            consoleButton.setTooltip(new Tooltip("I/O..."));
            getChildren().add(consoleButton);
            consoleButton.setOnAction(e -> {
                onConsole(consoleButton.isSelected());
            });

//            Button bo = newIconButton(FontAwesomeIcon.TACHOMETER);
//            bo.setTooltip(new Tooltip("Output..."));
//            v.getChildren().add(bo);
        }
        else {
            consoleButton = null;
        }


        getChildren().forEach(c -> {
            if (c instanceof Control)
                ((Control) c).setMaxWidth(Double.MAX_VALUE);
        });
        setFillWidth(true);




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

        run();

    }

    protected abstract void onConsole(boolean selected);


}

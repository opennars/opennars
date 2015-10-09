package nars.guifx.nars;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import nars.NAR;
import nars.guifx.JFX;
import nars.guifx.util.NSlider;
import nars.io.Texts;
import nars.util.NARLoop;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 10/9/15.
 */
public class LoopPane extends VBox {

    final Label label = new Label();
    //        final static Text play = GlyphsDude.createIcon(FontAwesomeIcon.PLAY, GlyphIcon.DEFAULT_FONT_SIZE);
//        final static Text stop = GlyphsDude.createIcon(FontAwesomeIcon.STOP, GlyphIcon.DEFAULT_FONT_SIZE);
    private final NARLoop loop;
    private final Button runButton;
    private final Button stepButton;
    private final SimpleStringProperty cpuLabel;

    private final NSlider cpuSlider;

    boolean running = false;

    public LoopPane(NARLoop loop) {
        super();

        this.loop = loop;

        final NAR n = loop.nar;
        runButton = JFX.newIconButton(FontAwesomeIcon.PLAY);
        stepButton = JFX.newIconButton(FontAwesomeIcon.STEP_FORWARD);
        cpuLabel = new SimpleStringProperty("CPU");

        cpuSlider = new NSlider(cpuLabel, 100, 30.0, NSlider.BarSlider,
                0.5 /* initial value */);
        //cpuSlider.min.set(0);
        //cpuSlider.max.set(2000);


        runButton.setTooltip(new Tooltip("Toggle run/pause"));


        runButton.setOnAction(e -> {

            running = (!running);

            updateLoop();

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


//        Slider cpuSlider = new Slider(0, 1, 0);
//        cpuSlider.setOrientation(Orientation.VERTICAL);
//        cpuSlider.setTooltip(new Tooltip("Speed"));
//        cpuSlider.setMinorTickCount(10);
//        cpuSlider.setShowTickMarks(true);
//        getChildren().add(cpuSlider);
//


        //cpuSlider.value(-1);
        cpuSlider.setOpacity(1.0);

        cpuLabel.setValue("ON " + cpuSlider.v());


        //-2 here is a magic number to indicate that nothing is pending and can be changed now
        cpuSlider.value[0].addListener((s, p, c) -> {

            updateLoop();

        });

        pause();

        say("ready");

        getChildren().addAll(
                new FlowPane(runButton, cpuSlider, stepButton),
                new FlowPane(label)
        );

    }

    private void updateLoop() {
        if (!running) {
            pause();
            return;
        }

        double v = cpuSlider.value[0].get();

        //slider (0..1.0) -> millisecond fixed period
        /*int nMS = (int) FastMath.round(
                //1000.0 * (1.0 / (0.05 + c.doubleValue()))
                2000.0 * v // / (1.0 - v)
        );*/
        float logScale = 50f;
        int minDelay = 17; //60hz
        int nMS = (int) Math.round((1.0 - Math.log(1 + v * logScale) / Math.log(1 + logScale)) * 1024.0) + minDelay;

        if (loop.setPeriodMS(nMS)) {

            //new delay set:

            final int MS = nMS;

            runLater(() -> {
                cpuSlider.setMouseTransparent(false);
                stepButton.setDisable(true);
                say("cycle period=" + MS + "ms (" + Texts.n4(1000f / MS) + "hz)");
            });
        }
    }

    private void pause() {
        loop.pause();

        runLater(() -> {
            say("ready");

            stepButton.setDisable(false);
            cpuSlider.setMouseTransparent(true);
            cpuSlider.setOpacity(0.25);
            cpuLabel.setValue("OFF");
        });
    }

    protected void say(String text) {
        label.setText(text);
    }
}

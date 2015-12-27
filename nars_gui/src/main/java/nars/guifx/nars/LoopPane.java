package nars.guifx.nars;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import nars.NAR;
import nars.NARLoop;
import nars.guifx.JFX;
import nars.guifx.util.NSlider;
import nars.util.Texts;

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
    private final ComboBox<Integer> multiplier;

    boolean running = false;

    final ChangeListener<Number> updateLoopOnChange = (s, p, c) -> updateLoop();

    @SuppressWarnings("HardcodedFileSeparator")
    public LoopPane(NARLoop loop) {

        this.loop = loop;

        NAR n = loop.nar;
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

        cpuLabel.setValue("");


        //-2 here is a magic number to indicate that nothing is pending and can be changed now
        cpuSlider.value[0].addListener(updateLoopOnChange);

        multiplier = new ComboBox<>();
        multiplier.getItems().addAll( 1, 4, 16, 64, 128, 512);
        multiplier.setValue(1);
        multiplier.valueProperty().addListener(updateLoopOnChange);

        pause();

        say("ready");

        getChildren().addAll(
                new FlowPane(runButton, cpuSlider, stepButton, multiplier),
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
        float logScale = 50.0f;
        int minDelay = 20; //slightly slower than 60hz, which is what javafx pulse runs at
        int nMS = (int) Math.round((1.0 - Math.log(1 + v * logScale) / Math.log(1 + logScale)) * 1024.0) + minDelay;

        loop.cyclesPerFrame = (multiplier.getValue());
        setSpeed(nMS);

    }

    //warning: doesnt set the slider to match
    public void setSpeed(int nMS) {
        running = true;
        updateSpeed(nMS);
    }

    private void updateSpeed(int nMS) {
        if (loop.setPeriodMS(nMS)) {

            //new delay set:

            int MS = nMS;

            runLater(() -> {
                unpause();
                say("@" + MS + "ms (" + Texts.n2(1000.0f / MS) + "hz)");
            });
        }
    }

    private void unpause() {
        cpuSlider.setMouseTransparent(false);
        cpuSlider.setOpacity(1.0);
        stepButton.setDisable(true);
        cpuLabel.setValue("ON");
    }

    public void pause() {
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

package nars.guifx;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import nars.Audio;
import nars.NAR;
import nars.sonification.ConceptSonification;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 10/1/15.
 */
public class ConceptSonificationPanel extends BorderPane {

    private final Label info;
    static final int maxVoices = 4;
    private final NAR nar;
    private ConceptSonification son;

    public ConceptSonificationPanel(NAR nar) {

        this.nar = nar;
        info = new Label();
        info.setWrapText(true);
        setCenter(info);

        CheckBox b = new CheckBox("Sonify");
        ToggleButton r = new ToggleButton("Record (to file)...");
        setLeft(b);
        setRight(r);
        b.selectedProperty().addListener(c -> {
            if (b.isSelected()) {
                start();
            } else {
                stop();
            }
        });
    }

    protected void stop() {
        info.setText("Stopping");
        if (son != null) {
            son.off();
            son.sound.shutDown();
            son = null;
        }
        info.setText("Silent");
    }

    protected void start() {

        try {
            son = new ConceptSonification(nar, new Audio(maxVoices)) {

                @Override
                public void onFrame() {
                    super.onFrame();
                    //String pp = playing.keySet().toString();
                    runLater(() -> {
                        info.setText("Sonifying..");
                        //info.setText(this.playing.toString());
                    });

                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            info.setText(e.toString());
            son = null;
        }

    }
}

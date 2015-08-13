package nars;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import nars.guifx.NarseseInput;


/**
 * Created by me on 8/11/15.
 */
public class InputPane extends TabPane {

    public InputPane(NAR n) {
        super();

        setSide(Side.BOTTOM);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        getTabs().add(new Tab("Narsese", new NarseseInput(n)));
        getTabs().add(new Tab("En"));
        getTabs().add(new Tab("Es"));
        getTabs().add(new Tab("Fr"));
        getTabs().add(new Tab("De"));
        getTabs().add(new Tab("Draw"));
        getTabs().add(new Tab("Webcam"));
        getTabs().add(new Tab("Audio"));

    }
}

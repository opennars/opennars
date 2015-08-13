package nars.guifx;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import nars.InputPane;
import nars.NAR;

/**
 * Created by me on 8/2/15.
 */
public class TerminalPane extends BorderPane {


    public TerminalPane(NAR nar) {
        super();


        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);

        split.getItems().addAll(NARPane.scrolled(
                NARPane.scrolled(new LogPane(nar))),
                new InputPane(nar));

        split.setDividerPosition(0,0.85);


        setMinSize(400, 300);
        split.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        setCenter(split);
    }

}

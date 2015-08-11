package nars.guifx;

import automenta.vivisect.javafx.CodeInput;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import nars.InputPane;
import nars.NAR;
import nars.io.in.TextInput;

/**
 * Created by me on 8/2/15.
 */
public class TerminalPane extends SplitPane {


    public TerminalPane(NAR nar) {
        super();


        setOrientation(Orientation.VERTICAL);
        getItems().addAll(NARPane.scrolled(

                NARPane.scrolled(new LogPane(nar))),

                (new InputPane(nar)));

        setDividerPosition(0,0.85);


        setMinSize(400, 300);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


    }

}

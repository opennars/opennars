package nars.guifx;

import automenta.vivisect.javafx.CodeInput;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import nars.NAR;
import nars.io.in.TextInput;

/**
 * Created by me on 8/2/15.
 */
public class TerminalPane extends SplitPane {


    public TerminalPane(NAR nar) {
        super();


        setOrientation(Orientation.VERTICAL);
        getItems().addAll(NARWindow.scrolled(

                NARWindow.scrolled(new LogPane(nar))),

                (new CodeInput() {
                    @Override
                    public boolean onInput(String s) {
                        TextInput i = nar.input(s);

                        if (!nar.isRunning())
                            nar.frame(1);

                        return i!=null;
                    }
                }));

        setDividerPosition(0,0.85);



    }
}

package nars.guifx;

import automenta.vivisect.javafx.CodeInput;
import nars.NAR;
import nars.io.in.TextInput;

/**
 * Created by me on 8/11/15.
 */
public class NarseseInput extends CodeInput {
    private final NAR nar;

    public NarseseInput(NAR nar) {
        this.nar = nar;
    }

    @Override
    public boolean onInput(String s) {
        TextInput i = null;

        if (!s.isEmpty()) {
            i = nar.input(s);
        }

        if (!nar.isRunning())
            nar.frame();

        return i != null;
    }
}

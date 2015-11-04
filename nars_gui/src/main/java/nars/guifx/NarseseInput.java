package nars.guifx;

import nars.NAR;
import nars.guifx.util.CodeInput;
import nars.task.in.TextInput;

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

        if (!nar.running())
            nar.frame();

        return i != null;
    }
}

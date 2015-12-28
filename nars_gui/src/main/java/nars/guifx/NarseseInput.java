package nars.guifx;

import nars.NAR;
import nars.guifx.util.CodeInput;

/**
 * TODO use looping state, not nar.running() likely to be false
 */
public class NarseseInput extends CodeInput {
    private final NAR nar;

    public NarseseInput(NAR nar) {
        this.nar = nar;
    }

    @Override
    public boolean onInput(String s) {

        if (!s.isEmpty()) {
            nar.beforeNextFrame(() -> {
                nar.input(s);
            });
        }

        try {
            nar.frame();
        } catch (NAR.AlreadyRunningException e) {
            //no problem it is already running and will get the queued event
        }

        return true;
    }
}

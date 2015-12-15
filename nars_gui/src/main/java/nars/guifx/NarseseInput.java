package nars.guifx;

import nars.NAR;
import nars.guifx.util.CodeInput;
import nars.task.in.TextInput;

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
        TextInput i = null;

        boolean running = nar.running();

        if (!s.isEmpty()) {
            if (!running) {
                i = nar.input(s);
            } else {
                nar.beforeNextFrame(() -> {
                    nar.input(s);
                });
            }

        }

        if (!running)
            nar.frame();

        return i != null;
    }
}

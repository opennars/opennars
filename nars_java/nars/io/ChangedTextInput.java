package nars.io;

import nars.core.NAR;
import nars.io.TextInput;


/** TextInput subclass that only inputs when the next input value changes from previous */
public class ChangedTextInput extends TextInput {

    private final NAR nar;
    private String last = null;

    public ChangedTextInput(NAR n) {
        this.nar = n;
    }

    public void set(String s) {
        if ((last == null) || (!last.equals(s))) {
            nar.addInput(s);
        }
        //TODO else add with lower budget
        last = s;
    }
}


package nars.io;

import nars.core.NAR;


/** TextInput subclass that only inputs when the next input value changes from previous */
public class ChangedTextInput  {

    private final NAR nar;
    private String last = null;

    public ChangedTextInput(NAR n) {
        this.nar = n;
    }

    public boolean set(String s) {
        if ((last == null) || (!last.equals(s))) {
            nar.addInput(s);
            last = s;
            return true;
        }
        //TODO option to, when else, add with lower budget ?
        return false;
    }
}


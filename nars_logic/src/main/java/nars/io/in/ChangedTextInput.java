package nars.io.in;

import nars.NAR;
import nars.nal.Task;


/** TextInput subclass that only inputs when the next input value changes from previous */
public class ChangedTextInput  {

    private final NAR nar;
    private String last = null;
    private boolean allowRepeats = false;

    boolean direct = true;

    public ChangedTextInput(NAR n) {
        this.nar = n;
    }

    public boolean set(String s) {
        if (allowRepeats() || (last == null) || (!last.equals(s)) && enable()) {
            if (direct) {
                Task t = nar.task(s);
                nar.inputDirect(t);
            }
            else {
                nar.input(s);
            }
            last = s;
            return true;
        }
        //TODO option to, when else, add with lower budget ?
        return false;
    }

    public boolean allowRepeats() {
        return allowRepeats;
    }
    public boolean enable() {
        return true;
    }

    public void setAllowRepeatInputs(boolean b) {
        this.allowRepeats = b;
    }
}


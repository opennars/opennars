package nars.io;

import nars.main_nogui.NAR;


public interface InputParser {
    public boolean parse(NAR nar, String input, InputParser lastHandler);
}

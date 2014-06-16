package nars.io;

import nars.core.NAR;


public interface InputParser {
    public boolean parse(NAR nar, String input, InputParser lastHandler);
}

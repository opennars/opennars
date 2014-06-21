package nars.io;

import nars.core.NAR;


public interface TextInputParser {
    public boolean parse(NAR nar, String input, TextInputParser lastHandler);
}

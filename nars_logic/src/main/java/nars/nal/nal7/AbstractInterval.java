package nars.nal.nal7;

import nars.Memory;
import nars.nal.term.Term;

/**
 * Created by me on 6/8/15.
 */
public interface AbstractInterval extends Term {
    long cycles(Memory m);
}

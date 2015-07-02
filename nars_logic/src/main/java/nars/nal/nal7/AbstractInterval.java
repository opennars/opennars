package nars.nal.nal7;

import nars.Memory;
import nars.term.Term;

/**
 * Created by me on 6/8/15.
 */
public interface AbstractInterval extends Term {

    public long cycles(@Deprecated Memory m);

    /** returns a count of how many interval terms are in the array */
    public static int intervalCount(Term[] a) {
        int c = 0;
        for (Term x : a) {
            if (x instanceof AbstractInterval) c++;
        }
        return c;
    }
}

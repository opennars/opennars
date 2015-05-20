package nars.op.math;

import nars.nal.nal8.TermFunction;
import nars.nal.term.Atom;
import nars.nal.term.Term;

import static nars.io.Texts.f;

/**
 * Created by me on 5/19/15.
 */
public class lessThan extends TermFunction<Boolean> {
    @Override
    public Boolean function(Term... x) {
        if (x.length!=2)
            return null;
        float a = f(Atom.unquote(x[0]));
        float b = f(Atom.unquote(x[1]));
        return (a < b);
    }
}

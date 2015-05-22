package nars.op.math;

import nars.nal.DefaultTruth;
import nars.nal.Truth;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.nal.nal8.operator.TermPredicate;

import static nars.io.Texts.f;

/**
 * Created by me on 5/19/15.
 */
public class lessThan extends TermPredicate {

    @Override
    public Truth function(Term... x) {
        if (x.length!=2)
            return null;
        float a = f(Atom.unquote(x[0]));
        float b = f(Atom.unquote(x[1]));

        return new DefaultTruth((a < b) ? 1f : 0f, 0.99f);
    }

}

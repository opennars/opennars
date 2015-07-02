package nars.op.math;

import nars.nal.nal8.operator.TermPredicate;
import nars.term.Atom;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import static nars.io.Texts.f;


public class lessThan extends TermPredicate {

    @Override
    public Truth truth(Term... x) {
        if (x.length<2) //allow SELF in 3rd
            return null;

        float v;
        try {
            float a = f(Atom.unquote(x[0]));
            float b = f(Atom.unquote(x[1]));

            v = (a < b) ? 1f : 0f;
        }
        catch (NumberFormatException e) {
            return null;
        }

        return new DefaultTruth(v, 0.9f);
    }


}

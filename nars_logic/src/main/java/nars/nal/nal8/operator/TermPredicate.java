package nars.nal.nal8.operator;

import nars.Memory;
import nars.truth.Truth;
import nars.concept.ConstantConceptBuilder;
import nars.nal.nal8.Operation;
import nars.term.Atom;
import nars.term.Term;

/**
 * A termfunction which evaluates to a Truth value,
 * and allows invocation by question
 */
abstract public class TermPredicate extends ConstantConceptBuilder {

    final Atom opPredicate = Atom.the(getClass().getSimpleName());

    /*public Decider decider() {
        return DecideAboveDecisionThresholdAndQuestions.the;
    }*/

    @Override
    protected Truth truth(Term t, Memory m) {
        if (t instanceof Operation) {
            Operation o = (Operation)t;

            if (eval.ENABLED)
                o = o.inline(m, false);

            return truth(o.getPredicate(), o.arg().terms());
        }
        return null;
    }

    protected Truth truth(Term operator, Term[] terms) {
        if (operator.equals(opPredicate)) {
            return truth(terms);
        }
        return null;
    }

    protected abstract Truth truth(Term... terms);
}

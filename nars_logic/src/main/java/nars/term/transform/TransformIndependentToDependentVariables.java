package nars.term.transform;

import nars.Op;
import nars.term.Term;
import nars.term.Variable;

/**
 * Created by me on 6/2/15.
 */
public class TransformIndependentToDependentVariables extends VariableSubstitution {

    int counter = 0;

    @Override
    public final boolean test(final Term possiblyAVariable) {
        if (super.test(possiblyAVariable))
            return possiblyAVariable.hasVarIndep();
        return false;
    }

    @Override
    protected final Variable getSubstitute(final Variable v) {
        return Variable.the(Op.VAR_DEPENDENT, counter++);
    }
}

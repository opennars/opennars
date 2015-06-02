package nars.nal.transform;

import nars.Symbols;
import nars.nal.term.Term;
import nars.nal.term.Variable;

/**
 * Created by me on 6/2/15.
 */
public class TransformIndependentToDependentVariables extends VariableSubstitution {
    int counter = 0;

    @Override
    public boolean test(Term possiblyAVariable) {
        if (super.test(possiblyAVariable))
            return ((Variable) possiblyAVariable).hasVarIndep();
        return false;
    }

    @Override
    protected Variable getSubstitute(Variable v) {
        return Variable.the(Symbols.VAR_DEPENDENT, counter++);
    }
}

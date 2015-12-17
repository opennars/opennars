package nars.term.match;

import nars.term.transform.VariableNormalization;
import nars.term.variable.Variable;

/**
 * Created by me on 12/5/15.
 */
public class EllipsisZeroOrMore extends Ellipsis {
    public EllipsisZeroOrMore(Variable name) {
        super(name, "..*");
    }

    @Override
    public boolean valid(int collectable) {
        return collectable >= 0;
    }

    @Override
    public Variable clone(Variable newVar, VariableNormalization normalizer) {
        return new EllipsisZeroOrMore(newVar);
    }
}

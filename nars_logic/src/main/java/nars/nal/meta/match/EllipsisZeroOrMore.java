package nars.nal.meta.match;

import nars.term.Variable;
import nars.term.transform.VariableNormalization;

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

package nars.nal.meta.match;

import nars.term.Variable;
import nars.term.transform.VariableNormalization;

/**
 * Created by me on 12/5/15.
 */
public class EllipsisOneOrMore extends Ellipsis {

    public EllipsisOneOrMore(Variable name) {
        this(name, "..+");
    }

    @Override
    public Variable clone(Variable newVar, VariableNormalization normalizer) {
        return new EllipsisOneOrMore(newVar);
    }

    public EllipsisOneOrMore(Variable name, String s) {
        super(name, s);
    }

    @Override
    public boolean valid(int collectable) {
        return collectable > 0;
    }
}

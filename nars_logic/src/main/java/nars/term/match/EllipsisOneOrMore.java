package nars.term.match;

import nars.$;
import nars.Op;
import nars.term.transform.VariableNormalization;
import nars.term.variable.Variable;

/**
 * Created by me on 12/5/15.
 */
public class EllipsisOneOrMore extends Ellipsis {

    public EllipsisOneOrMore(Variable name) {
        this(name, "..+");
    }

    @Override
    public Variable normalize(int serial) {
        return new EllipsisOneOrMore($.v(Op.VAR_PATTERN, serial));
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

package nars.term.match;

import nars.Op;
import nars.term.Term;
import nars.term.transform.Subst;
import nars.term.variable.Variable;

/**
 * Created by me on 12/5/15.
 */
public class VarPattern extends Variable {


    public VarPattern(String name) {
        super(name);
    }


    @Override
    public final int structure() {
        return 0;
    }

    @Override
    public final Op op() {
        return Op.VAR_PATTERN;
    }

    /**
     * pattern variable hidden in the count 0
     */
    @Override
    public final int vars() {
        return 0;
    }

    @Override
    public final int varDep() {
        return 0;
    }

    @Override
    public final int varIndep() {
        return 0;
    }

    @Override
    public final int varQuery() {
        return 0;
    }

    @Override
    public Term apply(Subst f, boolean fullMatch) {
        Term y = f.getXY(this);
        if (y == null) {
            return fullMatch ? null : this;
        }
        return y;
    }


}

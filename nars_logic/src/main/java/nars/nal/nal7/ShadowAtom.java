package nars.nal.nal7;

import nars.Op;
import nars.term.atom.AbstractStringAtom;

/**
 * Atom which is invisible to most if not all reasoner
 * processes, useful for placeholders and intermediate
 * representations.
 *
 */
public class ShadowAtom extends AbstractStringAtom {

    public ShadowAtom(String id) {
        super(id);
    }


    @Override
    public int varIndep() {
        return 0;
    }

    @Override
    public int varDep() {
        return 0;
    }

    @Override
    public int varQuery() {
        return 0;
    }


//    @Override
//    public final int structure() { return 0;     }

    @Override
    public Op op() {
        return Op.NONE;
    }

    @Override
    public int vars() {
        return 0;
    }

    @Override
    public int complexity() {
        return 0;
    }

    @Override
    public int volume() {
        return 0;
    }

}

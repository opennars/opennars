package nars.term.atom;

import nars.Op;


/** atom backed by a native java String */
public class StringAtom extends AbstractStringAtomRaw {

    public StringAtom(String id) {
        super(id);
    }

    @Override
    public Op op() {
        return Op.ATOM;
    }





    @Override
    public int complexity() {
        return 1;
    }

    @Override
    public int structure() {
        return Op.ATOM.bit();
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

    @Override
    public int vars() {
        return 0;
    }

}

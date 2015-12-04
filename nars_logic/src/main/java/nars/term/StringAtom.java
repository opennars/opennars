package nars.term;

import nars.Op;
import nars.term.transform.Substitution;
import nars.util.utf8.Utf8;


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
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode() {
        /** for Op.ATOM, we use String hashCode() as-is, avoiding need to calculate or store a hash mutated by the Op */
        return id.hashCode();
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
    public byte[] bytes() {
        return Utf8.toUtf8(id);
    }

    @Override
    public Term substituted(Substitution s) {
        return this;
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

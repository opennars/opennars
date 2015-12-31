package nars.nal.meta;

import nars.nal.PremiseMatch;
import nars.term.atom.Atom;

/**
 * Created by me on 12/31/15.
 */
public final class Return extends Atom implements ProcTerm<PremiseMatch> {

    public static final ProcTerm<PremiseMatch> the = new Return();

    private Return() {
        super("return");
    }


    @Override
    public void appendJavaProcedure(StringBuilder s) {
        s.append("return;");
    }

    @Override
    public void accept(PremiseMatch versioneds) {

    }

}

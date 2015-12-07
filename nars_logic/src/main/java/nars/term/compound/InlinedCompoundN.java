package nars.term.compound;

import nars.Op;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.TermVector;
import nars.term.visit.SubtermVisitor;

/**
 * Created by me on 12/6/15.
 */
public class InlinedCompoundN<T extends Term> extends TermVector<T> implements Compound<T> {

    @Override
    public Term clone(Term[] replaced) {
        return null;
    }

    @Override
    public TermContainer subterms() {
        return null;
    }

    @Override
    public Op op() {
        return null;
    }



    @Override
    public void recurseTerms(SubtermVisitor v, Term parent) {
        v.visit(this, parent);
        super.visit(v, this);
    }

    @Override
    public boolean isCommutative() {
        return false;
    }

    @Override
    public Term clone() {
        return null;
    }

    @Override
    public byte[] bytes() {
        return new byte[0];
    }

    @Override
    public int bytesLength() {
        return 0;
    }

}

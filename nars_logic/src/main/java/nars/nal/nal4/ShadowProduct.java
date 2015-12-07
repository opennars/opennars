package nars.nal.nal4;

import nars.Op;
import nars.term.Term;
import nars.term.TermVector;
import nars.term.transform.Subst;
import nars.term.visit.SubtermVisitor;

import java.io.IOException;
import java.util.Collection;

/** lightweight vector of terms which is useful only for
 *  holding a vector of subterms and not much else.
 *  this makes it generally invisible to reasoning since
 *  behaves only partially like a real Product term. */
public class ShadowProduct extends TermVector implements Term {

    public ShadowProduct(Collection<Term> subterms) {
        super(subterms);
    }

    public ShadowProduct(Term[] subterms) {
        super(subterms);
    }

    @Override
    public final Term[] terms() {
        return term;
    }

    @Override public int init() {
        //nothing else
        return 0;
    }

    @Override
    public Op op() {
        return Op.PRODUCT;
    }

    @Override
    public void recurseTerms(SubtermVisitor v, Term parent) {
        throw new RuntimeException("unimpl");
    }


    @Override
    public Term normalized() {
        return this;
    }

    @Override
    public boolean containsTermRecursively(Term target) {
        throw new RuntimeException("unimpl");
    }

    @Override
    public boolean isCommutative() {
        return false;
    }

    @Override
    public Term clone() {
        return this;
    }


    @Override
    public byte[] bytes() {
        throw new RuntimeException("unimpl");
    }

    @Override
    public int bytesLength() {
        throw new RuntimeException("unimpl");
    }

    @Override
    public void append(Appendable w, boolean pretty) throws IOException {
        throw new RuntimeException("unimpl");
    }

    @Override
    public StringBuilder toStringBuilder(boolean pretty) {
        throw new RuntimeException("unimpl");
    }

    @Override
    public String toString(boolean pretty) {
        throw new RuntimeException("unimpl");
    }

    @Override
    public Term substituted(Subst s) {
        return this;
    }

}

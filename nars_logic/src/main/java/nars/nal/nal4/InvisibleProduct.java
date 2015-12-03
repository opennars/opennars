package nars.nal.nal4;

import nars.Op;
import nars.term.Term;
import nars.term.TermVector;
import nars.term.compile.TermIndex;
import nars.term.transform.Substitution;
import nars.term.transform.TermVisitor;

import java.io.IOException;
import java.util.Map;

/** lightweight vector of terms which is useful only for
 *  holding a vector of subterms and not much else.
 *  this makes it generally invisible to reasoning since
 *  behaves only partially like a real Product term. */
public final class InvisibleProduct extends TermVector implements Term {

    public InvisibleProduct(Term[] subterms) {
        init(subterms);
    }

    @Override public int init(Term[] term) {
        this.term = term;
        //nothing else
        return 0;
    }

    @Override
    public Op op() {
        return Op.PRODUCT;
    }

    @Override
    public void recurseTerms(TermVisitor v, Term parent) {
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
    public Term cloneDeep() {
        return this;
    }

    @Override
    public byte[] bytes() {
        throw new RuntimeException("unimpl");
    }

    @Override
    public int getByteLen() {
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
    public Term substituted(Map<Term, Term> subs) {
        return this;
    }

    @Override
    public Term substituted(Substitution s) {
        return this;
    }

    @Override
    public void rehash() {

    }

    @Override
    public Term normalized(TermIndex termIndex) {
        return this;
    }

    public Term[] terms() {
        return term;
    }
}

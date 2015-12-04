package nars.term;

import nars.Op;
import nars.term.compile.TermIndex;
import nars.term.visit.SubtermVisitor;
import nars.term.visit.TermPredicate;
import nars.util.utf8.Utf8;

import java.io.Externalizable;
import java.io.IOException;


public abstract class AbstractAtomic implements Term, Externalizable {

    public AbstractAtomic() {

    }

    @Override
    public abstract Op op();

    @Override
    public abstract int structure();


    public final void rehash() {
        /** do nothing */
    }

    @Override public final boolean isCommutative() {
        return false;
    }

    @Override
    public final Term term(int n) {
        throw new RuntimeException("Atoms have no subterms");
    }
    @Override
    public final Term termOr(int n, Term x) {
        return term(n);
    }

    @Override
    public void append(final Appendable w, final boolean pretty) throws IOException {
        Utf8.fromUtf8ToAppendable(bytes(), w);
    }

    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    public StringBuilder toStringBuilder(final boolean pretty) {
        StringBuilder sb = new StringBuilder();
        Utf8.fromUtf8ToStringBuilder(bytes(), sb);
        return sb;
    }

    @Override
    public String toString() {
        return Utf8.fromUtf8toString(bytes());
    }




    @Override public int getByteLen() {
        return bytes().length;
    }

    /**
     * Atoms are singular, so it is useless to clone them
     */
    @Override
    public final Term clone() {
        return this;
    }

    @Override
    public final Term cloneDeep() {
        return this;
    }

    @Override
    public final void recurseTerms(final SubtermVisitor v, final Term parent) {
        v.visit(this, parent);
    }


    @Override public boolean and(TermPredicate v) {
        return v.test(this);
    }

    @Override public boolean or(TermPredicate v) {
        return and(v); //re-use and, even though it's so similar
    }

    @Override
    public final String toString(boolean pretty) {
        return toString();
    }

    @Override public abstract boolean hasVar();

    @Override public abstract int vars();

    @Override public abstract boolean hasVarIndep();

    @Override public abstract boolean hasVarDep();

    @Override public abstract boolean hasVarQuery();

    @Override public abstract int complexity();

    @Override
    public final int size() {
        return 0;
    }

    @Override public int volume() { return 1; }

    public final boolean impossibleSubTermVolume(final int otherTermVolume) {
        return true;
    }

    @Override public abstract byte[] bytes();

    /** atomic terms contain nothing */
    @Override public final boolean containsTerm(Term target) {
        return false;
    }

    /** atomic terms contain nothing */
    @Override public final boolean containsTermRecursively(Term target) {
        return false;
    }

    @Override
    public abstract int varIndep();

    @Override
    public abstract int varDep();

    @Override
    public abstract int varQuery();

    @Override
    public final AbstractAtomic normalized() {
        return this;
    }

    @Override
    public final Term normalized(TermIndex termIndex) {
        //if this is called, this atom will be the unique reference for any subsequent equivalent atomic terms which are normalized
        return this;
    }



}

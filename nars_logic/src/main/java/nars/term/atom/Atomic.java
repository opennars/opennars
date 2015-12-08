package nars.term.atom;

import nars.term.Term;
import nars.term.transform.Subst;
import nars.term.visit.SubtermVisitor;
import nars.term.visit.TermPredicate;
import nars.util.utf8.Byted;

import java.io.IOException;


public abstract class Atomic implements Term, Byted {

    @Override
    public final boolean containsTerm(Term t) {
        return false;
    }




    public final void rehash() {
        /** do nothing */
    }

    @Override public final boolean isCommutative() {
        return false;
    }

//    @Override
//    public final Term term(int n) {
//        throw new RuntimeException("Atoms have no subterms");
//    }
//    @Override
//    public final Term termOr(int n, Term x) {
//        return term(n);
//    }

    @Override
    public void append(final Appendable w, final boolean pretty) throws IOException {
        //Utf8.fromUtf8ToAppendable(bytes(), w);
        w.append(toString());
    }
//
    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    @Override
    public StringBuilder toStringBuilder(final boolean pretty) {
        StringBuilder sb = new StringBuilder(toString());
//        Utf8.fromUtf8ToStringBuilder(bytes(), sb);
        return sb;
    }



    @Override public int bytesLength() {
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


    @Override
    public int size() {
        return 0;
    }

    @Override public int volume() { return 1; }

    @Override
    public final boolean impossibleSubTermVolume(final int otherTermVolume) {
        return true;
    }

    @Override public abstract byte[] bytes();


    @Override
    public abstract int varIndep();

    @Override
    public abstract int varDep();

    @Override
    public abstract int varQuery();

    @Override
    public final Atomic normalized() {
        return this;
    }


    @Override public final boolean containsTermRecursively(Term target) {
        return false;
    }


    public abstract Term apply(Subst s);
}

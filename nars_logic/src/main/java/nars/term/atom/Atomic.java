package nars.term.atom;

import nars.term.Term;
import nars.term.transform.Subst;
import nars.term.visit.SubtermVisitor;
import nars.util.utf8.Byted;

import java.io.IOException;
import java.util.function.Predicate;


public abstract class Atomic implements Term, Byted {

    @Override
    public final boolean containsTerm(Term t) {
        return false;
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
    public void append(Appendable w, boolean pretty) throws IOException {
        //Utf8.fromUtf8ToAppendable(bytes(), w);
        w.append(toString());
    }
//
    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    @Override
    public StringBuilder toStringBuilder(boolean pretty) {
        return new StringBuilder(toString());
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
    public final void recurseTerms(SubtermVisitor v, Term parent) {
        v.accept(this, parent);
    }


    @Override public boolean and(Predicate<Term> v) {
        return v.test(this);
    }

    @Override public boolean or(Predicate<Term> v) {
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
    public final boolean impossibleSubTermVolume(int otherTermVolume) {
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
    public Term apply(Subst f, boolean fullMatch) {
        return this;
    }

    @Override public final boolean containsTermRecursively(Term target) {
        return false;
    }


    //public abstract Term apply(Subst s, boolean fullMatch);
}

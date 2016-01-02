package nars.term.atom;

import nars.term.Term;
import nars.term.Termed;
import nars.term.visit.SubtermVisitor;

import java.io.IOException;
import java.util.function.Predicate;

/** Base class for Atomic types. */
public abstract class Atomic implements Term {


    /** Assumes that the op()
     *  is encoded within its string such that additional op()
     *  comparison would be redundant. */
    @Override public final boolean equals(Object u) {
        if (this == u) return true;
        if (!(u instanceof Termed)) return false;
        Termed t = ((Termed)u).term();
        return (t.op() == op()) && toString().equals(t.toString());
    }

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    /**
     * @param that The Term to be compared with the current Term
     */
    @Override public final int compareTo(Object that) {
        if (that==this) return 0;

        Termed t = (Termed)that;
        //TODO compare
        //int d = op().compareTo(t.op());
        int d = Integer.compare(op().ordinal(), t.op().ordinal());
        if (d!=0) return d;

        //if the op is the same, it is required to be a subclass of Atomic
        //which should have an ordering determined by its toString()
        return toString().compareTo((/*(Atomic)*/that).toString());
    }



    @Override
    public final void recurseTerms(SubtermVisitor v, Term parent) {
        v.accept(this, parent);
    }

    @Override public final boolean and(Predicate<? super Term> v) {
        return v.test(this);
    }

    @Override public final boolean or(Predicate<? super Term> v) {
        return and(v); //re-use and, even though it's so similar
    }

    @Override
    public final String toString(boolean pretty) {
        return toString();
    }

    @Override
    public final void append(Appendable w, boolean pretty) throws IOException {
        w.append(toString());
    }

    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    @Override
    public final StringBuilder toStringBuilder(boolean pretty) {
        return new StringBuilder(toString());
    }

    /** number of subterms; for atoms this must be zero */
    @Override public final int size() {
        return 0;
    }

    /** atoms contain no subterms so impossible for anything to fit "inside" it */
    @Override public final boolean impossibleSubTermVolume(int otherTermVolume) {
        return true;
    }
    @Override public final boolean containsTermRecursively(Term target) {
        return false;
    }
    @Override public final boolean containsTerm(Term t) {
        return false;
    }

    @Override public final boolean isCommutative() {
        return false;
    }


    /** default volume = 1 */
    @Override public int volume() { return 1; }


    @Override
    public abstract int varIndep();

    @Override
    public abstract int varDep();

    @Override
    public abstract int varQuery();

    @Override
    public int structure() {
        return op().bit();
    }

}

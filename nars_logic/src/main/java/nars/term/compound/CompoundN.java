package nars.term.compound;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.term.Term;
import nars.term.TermVector;
import nars.term.visit.SubtermVisitor;
import nars.term.visit.TermPredicate;
import nars.util.utf8.ByteBuf;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import static nars.Symbols.COMPOUND_TERM_CLOSERbyte;


public abstract class CompoundN<T extends Term> implements Compound<T> {

    protected final TermVector<T> terms;

    /**
     * true iff definitely normalized, false to cause it to update on next normalization.
     * used to prevent repeated normalizations
     */

    protected transient final int hash;


    protected CompoundN(Term... t) {
        this(new TermVector(t));
    }
    protected CompoundN(Term[] t, int hashSalt) {
        this(new TermVector(t), hashSalt);
    }

    protected CompoundN(TermVector subterms) {
        this(subterms, 0);
    }

    /** if hash salt is non-zero, it will be combined with the default hash value of the compound */
    protected CompoundN(TermVector subterms, int hashSalt) {
        this.terms = subterms;

        this.hash = Compound.hash(this, hashSalt);
    }

    public CompoundN(T t) {
        this(new Term[] { t } );
    }

    @Override
    public String toString() {
        return toString(true); //TODO make this default to false
    }

    @Override
    public int compareTo(final Object o) {
        if (this == o) return 0;

        Term t = (Term) o;
        int diff = Integer.compare(op().ordinal(), t.op().ordinal());
        //int diff = op().compareTo(t.op());
        if (diff != 0) return diff;

        return subterms().compareTo( ((Compound)o).subterms() );
    }


    @Override
    public final void addAllTo(Collection<Term> set) {
        terms.addAllTo(set);
    }

    @Override
    public final TermVector<T> subterms() {
        return terms;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that)
            return true;

        if (hash != that.hashCode()) return false;

        if (!(that instanceof Compound))
            return false;

        return equalsCompound((Compound) that);
    }

    public final boolean equalsCompound(Compound that) {
        return subterms().equals(that.subterms()) && (op() == that.op());
    }





    /**
     * recursively set duration to interval subterms
     */
    @Override
    public void setDuration(int duration) {
        int n = size();
        for (int i = 0; i < n; i++)
            term(i).setDuration(duration);
    }


    @Override
    public final int hashCode() {
        return hash;
    }


    @Override
    public final int varDep() {
        return terms.varDep();
    }

    @Override
    public final int varIndep() {
        return terms.varIndep();
    }

    @Override
    public final int varQuery() {
        return terms.varQuery();
    }

    @Override
    public final int vars() {
        return terms.vars();
    }

    public final Term[] cloneTermsReplacing(int index, Term replaced) {
        return terms.cloneTermsReplacing(index, replaced);
    }


    public final boolean isEmpty() {
        return terms.isEmpty();
    }

    public final boolean contains(Object o) {
        return terms.contains(o);
    }


    @Override
    public final void forEach(Consumer<? super T> action, int start, int stop) {
        terms.forEach(action, start, stop);
    }

    @Override
    public final void forEach(Consumer<? super T> c) {
        terms.forEach(c);
    }

    @Override public T[] terms() {
        return terms.terms();
    }

    @Override
    public Term[] terms(IntObjectPredicate<T> filter) {
        return terms.terms(filter);
    }

    @Override
    public final Iterator<T> iterator() {
        return terms.iterator();
    }

    @Override
    public final  T[] termsCopy() {
        return terms.termsCopy();
    }

    @Override
    public final int structure() {
        return terms.structure() | (1 << op().ordinal());
    }

    @Override
    public final T term(int i) {
        return terms.term(i);
    }

    @Override
    public final Term termOr(int index, Term resultIfInvalidIndex) {
        return terms.termOr(index, resultIfInvalidIndex);
    }

    @Override
    public final boolean containsTerm(Term target) {
        return terms.containsTerm(target);
    }

    @Override
    public final int size() {
        return terms.size();
    }

    @Override
    public final int complexity() {
        return terms.complexity();
    }

    @Override
    public final int volume() {
        return terms.volume();
    }

    @Override
    public final boolean impossibleSubTermVolume(int otherTermVolume) {
        return terms.impossibleSubTermVolume(otherTermVolume);
    }

    /**
     * searches for a subterm
     * TODO parameter for max (int) level to scan down
     */
    @Override
    public boolean containsTermRecursively(final Term target) {

        if (impossibleSubterm(target)) return false;

        return terms.containsTermRecursively(target);
    }


    @Override
    abstract public Term clone();

    @Override public boolean isNormalized() {
        return terms.isNormalized();
    }


    @Override
    public void setNormalized(boolean b) {
        terms.setNormalized(b);
    }

    @Override
    public int bytesLength() {
        int len = /* opener byte */1;

        final int n = size();
        for (int i = 0; i < n; i++) {
            len += term(i).bytesLength() + 1 /* separator or closer if end*/;
        }

        return len;
    }


    @Override
    public byte[] bytes() {

        ByteBuf b = ByteBuf.create(bytesLength());

        b.add((byte) op().ordinal()); //header

        appendSubtermBytes(b);

        b.add(COMPOUND_TERM_CLOSERbyte); //closer

        return b.toBytes();
    }


    @Override
    public void appendSubtermBytes(ByteBuf b) {
        terms.appendSubtermBytes(b);
    }

    @Override
    public boolean and(TermPredicate v) {
        return v.test(this) && terms.and(v);
    }
    @Override
    public boolean or(TermPredicate v) {
        return v.test(this) || terms.or(v);
    }


    @Override
    public final void recurseTerms(final SubtermVisitor v, final Term parent) {
        v.visit(this, parent);
        terms.visit(v, this);
    }


}

package nars.term;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.term.compound.Compound;
import nars.term.visit.SubtermVisitor;
import nars.util.data.Util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Arrays.copyOf;

/**
 * Holds a vector or tuple of terms.
 * Useful for storing a fixed number of subterms
 *
 * TODO make this class immutable and term field private
 * provide a MutableTermVector that holds any write/change methods
 */
public class TermVector<T extends Term> implements TermContainer<T>, Comparable, Serializable {

    /**
     * list of (direct) term
     * TODO make not public
     */
    public final T[] term;


    @Override public T[] terms() {
        return term;
    }



    @Override public final Term[] terms(IntObjectPredicate<T> filter) {
        return Terms.filter(term, filter);
    }


    /**
     * bitvector of subterm types, indexed by NALOperator's .ordinal() and OR'd into by each subterm
     */
    protected transient int structureHash;
    protected transient int contentHash;
    protected transient int varTotal;
    protected transient int volume;
    protected transient int complexity;

    /**
     * # variables contained, of each type
     */
    protected transient byte hasVarQueries;
    protected transient byte hasVarIndeps;
    protected transient byte hasVarDeps;

    transient boolean normalized;

    //    public TermVector() {
//        this(null);
//    }

    public TermVector(Collection<T> t) {
        this((T[]) t.toArray(new Term[t.size()]));
    }

    @SafeVarargs
    public TermVector(T... t) {
        term = t;
        init();
    }

    @Override
    public final int structure() {
        return structureHash;
    }

    @Override
    public final T term(int i) {
        return term[i];
    }

    @Override
    public final Term termOr(int index, Term resultIfInvalidIndex) {
        Term[] term = this.term;
        if (term.length <= index)
            return resultIfInvalidIndex;
        return term[index];
    }

    @Override
    public final int volume() {
        return volume;
    }



    /**
     * report the term's syntactic complexity
     *
     * @return the complexity value
     */
    @Override
    public final int complexity() {
        return complexity;
    }

    /**
     * get the number of term
     *
     * @return the size of the component list
     */
    @Override
    public int size() {
        return term.length;
    }

    /**
     * (shallow) Clone the component list
     */
    public final T[] termsCopy() {
        return copyOf(term, size());
    }

    @Override
    public void setNormalized(boolean b) {
        normalized = true;
    }

    @Override
    public boolean isNormalized() {
        return normalized;
    }

    @Override
    public String toString() {
        return '(' + Arrays.toString(term) + ')';
    }


    public final int varDep() {
        return hasVarDeps;
    }

    public final int varIndep() {
        return hasVarIndeps;
    }

    public final int varQuery() {
        return hasVarQueries;
    }

    public final int vars() {
        return varTotal;
    }

    public Term[] cloneTermsReplacing(int index, Term replaced) {
        Term[] y = termsCopy();
        y[index] = replaced;
        return y;
    }



    public final boolean isEmpty() {
        return size() != 0;
    }


    /**
     * first level only, not recursive
     */
    public final boolean contains(Object o) {
        if (o instanceof Term)
            return containsTerm((Term) o);
        return false;
    }

    @Override
    public final Iterator<T> iterator() {
        return Arrays.stream(term).iterator();
    }


    public final void forEach(Consumer<? super T> action, int start, int stop) {
        T[] tt = term;
        for (int i = start; i < stop; i++) {
            action.accept(tt[i]);
        }
    }

    @Override
    public final void forEach(Consumer<? super T> action) {
        T[] tt = term;
        for (T t : tt)
            action.accept(t);
    }

    /**
     * Check the subterms (first level only) for a target term
     *
     * @param t The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public final boolean containsTerm(Term t) {
        if (impossibleSubterm(t))
            return false;
        return Terms.contains(term, t);
    }


//    static int nextContentHash(int hash, int subtermHash) {
//        return Util.PRIME2 * hash + subtermHash;
//        //return (hash << 4) +  subtermHash;
//        //(Util.PRIME2 * contentHash)
//    }


    /** returns hashcode */
    public int init() {

        int deps = 0, indeps = 0, queries = 0;
        int compl = 1, vol = 1;

        int subt = 0;
        int contentHash = 1;

        for (Term t : term) {

            if (t == this)
                throw new RuntimeException("term can not contain itself");

            contentHash = Util.hashCombine(contentHash, t.hashCode());

            compl += t.complexity();
            vol += t.volume();
            deps += t.varDep();
            indeps += t.varIndep();
            queries += t.varQuery();
            subt |= t.structure();
        }

        Compound.ensureFeasibleVolume(vol, this);

        hasVarDeps = (byte) deps;
        hasVarIndeps = (byte) indeps;
        hasVarQueries = (byte) queries;
        structureHash = subt;
        normalized =
                (varTotal = (short) (deps + indeps + queries)) == 0;

        complexity = (short) compl;
        volume = (short) vol;

        if (contentHash == 0) contentHash = 1; //nonzero to indicate hash calculated
        this.contentHash = contentHash;
        return contentHash;
    }

    @Override
    public final void addAllTo(Collection<Term> set) {
        Collections.addAll(set, term);
    }

    @Override
    public final int hashCode() {
        return contentHash;
//        final int h = contentHash;
//        if (h == 0) {
//            //if hash is zero, it means it needs calculated
//            //return init(term);
//            throw new RuntimeException("unhashed");
//        }
//        return h;
    }

    @Override
    public boolean equals(Object that) {

        if (this == that) return true;

        if (!(that instanceof TermContainer)) return false;

        TermVector c = (TermVector) that;
        return (contentHash == c.contentHash) &&
                (structureHash == c.structureHash) &&
                (volume == c.volume) &&
                equalTerms(c);
    }

    private final boolean equalTerms(TermContainer c) {
        int s = size();
        if (s!=c.size())
            return false;

        for (int i = 0; i < s; i++) {
            if (!term[i].equals(c.term(i)))
                return false;
        }

        return true;
    }


    @Override
    public final int compareTo(Object o) {

        int diff;
        if ((diff = Integer.compare(hashCode(), o.hashCode())) != 0)
            return diff;

        //TODO dont assume it's a TermVector
        TermVector c = (TermVector) o;
        if ((diff = Integer.compare(structure(), c.structure())) != 0)
            return diff;


        int s = size();
        if ((diff = Integer.compare(s, c.size())) != 0)
            return diff;


        for (int i = 0; i < s; i++) {
            Term a = term(i);
            Term b = c.term(i);
            int d = a.compareTo(b);

        /*
        if (Global.DEBUG) {
            int d2 = b.compareTo(a);
            if (d2!=-d)
                throw new RuntimeException("ordering inconsistency: " + a + ", " + b );
        }
        */

            if (d != 0) return d;
        }

        return 0;
    }

    public final void visit(SubtermVisitor v, Compound parent) {
        for (Term t : term)
            v.accept(t, parent);
    }

    /** returns true if evaluates true for all terms */
    public final boolean and(Predicate<Term> p) {
        for (Term t : term) {
            if (!p.test(t))
                return false;
        }
        return true;
    }
    /** returns true if evaluates true for any terms */
    public final boolean or(Predicate<Term> p) {
        for (Term t : term) {
            if (t.or(p))
                return true;
        }
        return false;
    }


}

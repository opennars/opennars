package nars.term;

import com.google.common.collect.Iterators;
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
    transient protected byte hasVarQueries;
    transient protected byte hasVarIndeps;
    transient protected byte hasVarDeps;

    transient boolean normalized;

    //    public TermVector() {
//        this(null);
//    }

    public TermVector(Collection<T> t) {
        this((T[]) t.toArray(new Term[t.size()]));
    }

    public TermVector(T... t) {
        super();
        this.term = t;
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
        final Term term[] = this.term;
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
        this.normalized = true;
    }

    @Override
    public boolean isNormalized() {
        return normalized;
    }

    @Override
    public String toString() {
        return "(" + Arrays.toString(term) + ')';
    }

    //    /**
//     * Cloned array of Terms, except for one or more Terms.
//     *
//     * @param toRemove
//     * @return the cloned array with the missing terms removed,
//     * OR null if no terms were actually removed when requireModification=true
//     */
//    public Term[] cloneTermsExcept(final boolean requireModification, final Term... toRemove) {
//
//        final int toRemoveLen = toRemove.length;
//        if (toRemoveLen == 0)
//            throw new RuntimeException("no removals specified");
//        else if (toRemoveLen == 1) {
//            //use the 1-term optimized version of this method
//            return cloneTermsExcept(requireModification, toRemove[0]);
//        }
//
//        final int n = length();
//        final Term[] l = new Term[n];
//
//        final Set<Term> toRemoveSet = Terms.toSet(toRemove);
//
//
//        int remain = 0;
//        for (int i = 0; i < n; i++) {
//            final Term x = term(i);
//            if (!toRemoveSet.contains(x))
//                l[remain++] = x;
//        }
//
//        return Compound.resultOfCloneTermsExcept(requireModification, l, remain);
//    }

//    /**
//     * Cloned array of Terms, except for a specific Term.
//     *
//     * @param toRemove
//     * @return the cloned array with the missing terms removed,
//     * OR null if no terms were actually removed when requireModification=true
//     */
//    public Term[] cloneTermsExcept(final boolean requireModification, final Term toRemove) {
//
//        final int n = length();
//        final Term[] l = new Term[n];
//
//
//        int remain = 0;
//        for (int i = 0; i < n; i++) {
//            final Term x = term(i);
//            if (!toRemove.equals(x))
//                l[remain++] = x;
//        }
//
//        return Compound.resultOfCloneTermsExcept(requireModification, l, remain);
//    }
//
//    /**
//     * creates a new ArrayList for terms
//     */
//    public List<Term> asTermList() {
//        List<Term> l = Global.newArrayList(length());
//        addTermsTo(l);
//        return l;
//    }


//    /**
//     * clones all non-constant sub-compound terms, excluding the variables themselves which are not cloned. they will be replaced in a subsequent transform step
//     */
//    public Compound cloneVariablesDeep() {
//        return (Compound) clone(cloneTermsDeepIfContainingVariables());
//    }
//
//    public Term[] cloneTermsDeepIfContainingVariables() {
//        Term[] l = new Term[length()];
//        for (int i = 0; i < l.length; i++) {
//            Term t = term[i];
//
//            if ((!(t instanceof Variable)) && (t.hasVar())) {
//                t = t.cloneDeep();
//            }
//
//            //else it is an atomic term or a compoundterm with no variables, so use as-is:
//            l[i] = t;
//        }
//        return l;
//    }


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

//    final public void addTermsTo(final Collection<Term> c) {
//        Collections.addAll(c, term);
//    }

    public Term[] cloneTermsReplacing(int index, final Term replaced) {
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
        return Iterators.forArray(term);
    }


    public final void forEach(Consumer<? super T> action, int start, int stop) {
        final T[] tt = this.term;
        for (int i = start; i < stop; i++) {
            action.accept(tt[i]);
        }
    }

    @Override
    public final void forEach(final Consumer<? super T> action) {
        final T[] tt = this.term;
        for (final T t : tt)
            action.accept(t);
    }

    /**
     * Check the subterms (first level only) for a target term
     *
     * @param t The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public final boolean containsTerm(final Term t) {
        if (impossibleSubterm(t))
            return false;
        return Terms.contains(term, t);
    }


    static int nextContentHash(int hash, int subtermHash) {
        return Util.PRIME2 * hash + subtermHash;
        //return (hash << 4) +  subtermHash;
        //(Util.PRIME2 * contentHash)
    }


    /** returns hashcode */
    public int init() {

        int deps = 0, indeps = 0, queries = 0;
        int compl = 1, vol = 1;

        int subt = 0;
        int contentHash = 1;

        for (final Term t : term) {

            if (t == this)
                throw new RuntimeException("term can not contain itself");
            if (t == null)
                throw new RuntimeException("null subterm");

            contentHash = nextContentHash(contentHash, t.hashCode());

            compl += t.complexity();
            vol += t.volume();
            deps += t.varDep();
            indeps += t.varIndep();
            queries += t.varQuery();
            subt |= t.structure();
        }

        Compound.ensureFeasibleVolume(vol);

        this.hasVarDeps = (byte) deps;
        this.hasVarIndeps = (byte) indeps;
        this.hasVarQueries = (byte) queries;
        this.varTotal = (short) (deps + indeps + queries);
        this.structureHash = subt;

        this.complexity = (short) compl;
        this.volume = (short) vol;

        this.normalized = varTotal == 0;

        if (contentHash == 0) contentHash = 1; //nonzero to indicate hash calculated
        this.contentHash = contentHash;
        return contentHash;
    }

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

        if (this == that)
            return true;
        if (!(that instanceof TermVector)) return false;

        TermVector c = (TermVector) that;
        if (contentHash != c.contentHash ||
            structureHash != c.structureHash ||
            volume != c.volume)
                return false;

        final int s = this.size();
        if (s!=c.size())
            return false;

        for (int i = 0; i < s; i++) {
            Term a = term(i);
            Term b = c.term(i);
            if (!a.equals(b)) return false;
        }

        return true;
    }




    @Override
    public final int compareTo(Object o) {

        int diff;
        if ((diff = Integer.compare(hashCode(), o.hashCode())) != 0)
            return diff;

        //TODO dont assume it's a TermVector
        final TermVector c = (TermVector) o;
        if ((diff = Integer.compare(structure(), c.structure())) != 0)
            return diff;


        final int s = this.size();
        if ((diff = Integer.compare(s, c.size())) != 0)
            return diff;


        for (int i = 0; i < s; i++) {
            final Term a = term(i);
            final Term b = c.term(i);
            final int d = a.compareTo(b);

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

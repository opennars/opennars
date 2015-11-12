package nars.term;

import com.google.common.collect.Iterators;
import nars.util.data.Util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;

import static java.util.Arrays.copyOf;

/**
 * Holds a vector or tuple of terms.
 * Useful for storing a fixed number of subterms
 *
 * TODO make this class immutable and term field private
 * provide a MutableTermVector that holds any write/change methods
 */
public class TermVector<T extends Term> implements Iterable<T>, Subterms<T>, Serializable {
    /**
     * list of (direct) term
     */
    public T[] term;


    /**
     * bitvector of subterm types, indexed by NALOperator's .ordinal() and OR'd into by each subterm
     */
    protected transient int structureHash;
    protected transient int contentHash;
    protected transient int varTotal;
    protected transient int volume;
    protected transient int complexity;
    /**
     * Whether contains a variable
     */
    transient protected byte hasVarQueries;
    transient protected byte hasVarIndeps;
    transient protected byte hasVarDeps;

    //    public TermVector() {
//        this(null);
//    }

    public TermVector() {
        super();
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
    public final int volume() {
        return volume;
    }






    @Override
    public final boolean impossibleSubTermVolume(final int otherTermVolume) {
        return otherTermVolume >
                volume()
                        - 1 /* for the compound itself */
                        - (size() - 1) /* each subterm has a volume >= 1, so if there are more than 1, each reduces the potential space of the insertable */
                ;
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
    public final int size() {
        return term.length;
    }

    /**
     * (shallow) Clone the component list
     */
    public final T[] cloneTerms() {
        return copyOf(term, term.length);
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

    /**
     * forced deep clone of terms - should not be necessary
     */
    public final Term[] cloneTermsDeep() {
        int s = size();
        Term[] l = new Term[s];
        final Term[] t = this.term;
        for (int i = 0; i < s; i++)
            l[i] = t[i].cloneDeep();
        return l;
    }

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

    public final boolean hasVar() {
        return varTotal > 0;
    }


//    final public void addTermsTo(final Collection<Term> c) {
//        Collections.addAll(c, term);
//    }

    public Term[] cloneTermsReplacing(int index, final Term replaced) {
        Term[] y = cloneTerms();
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

    @Override
    public final void forEach(Consumer<? super T> action, int start, int stop) {
        final T[] tt = this.term;
        for (int i = start; i < stop; i++) {
            action.accept(tt[i]);
        }
    }

    @Override
    public final void forEach(final Consumer<? super T> action) {
        for (final T t : this.term)
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

    static int newContentHash(int subt, int hashSeed) {
        //int contentHash =  (Util.PRIME3 * subt) + getHashSeed();
        return (Util.PRIME3 * subt) + hashSeed;
    }
    static int nextContentHash(int hash, int subtermHash) {
        return (hash << 4) +  subtermHash;
        //(Util.PRIME2 * contentHash)
    }


    /** returns hashcode */
    public int init(T[] term) {


        int deps = 0, indeps = 0, queries = 0;
        int compl = 1, vol = 1;

        int subt = 0;
        int contentHash = 1;

        for (final Term t : term) {

            if (t == this)
                throw new RuntimeException("term can not contain itself");

            contentHash = nextContentHash(contentHash, t.rehashCode());

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

        this.term = term;

        if (contentHash == 0) contentHash = 1; //nonzero to indicate hash calculated
        this.contentHash = contentHash;
        return contentHash;
    }

    public final void addAllTo(Collection<Term> set) {
        Collections.addAll(set, term);
    }

    public Term[] newArray() {
        return copyOf(term, term.length);
    }

    @Override
    public final int hashCode() {
        final int h = contentHash;
        if (h == 0) {
            //if hash is zero, it means it needs calculated
            return init(term);
        }
        return h;
    }

    @Override
    public boolean equals(Object that) {

        if (this == that) return true;
        if (!(that instanceof TermVector)) return false;

        TermVector c = (TermVector) that;
        if (contentHash != c.contentHash ||
                structureHash != c.structureHash ||
                volume != c.volume)
            return false;

        final int s = this.size();
        for (int i = 0; i < s; i++) {
            Term a = term(i);
            Term b = c.term(i);
            if (!a.equals(b)) return false;
        }

        return true;
    }

    @Override
    public int compareTo(Object o) {

        int diff;
        if ((diff = Integer.compare(o.hashCode(), hashCode())) != 0)
            return diff;


        //TODO dont assume it's a TermVector
        final TermVector c = (TermVector) o;

        final int s = this.size();
        if ((diff = Integer.compare(s, c.size())) != 0)
            return diff;

        if ((diff = Integer.compare(structure(), c.structure())) != 0)
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
}

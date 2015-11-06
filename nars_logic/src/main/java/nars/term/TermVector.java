package nars.term;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterators;
import nars.term.transform.CompoundTransform;

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
abstract public class TermVector<T extends Term> implements Iterable<T>, Subterms<T>, Serializable {
    /**
     * list of (direct) term
     */
    public final T[] term;


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

    public TermVector(@JsonProperty("term") final T... components) {
        super();
        this.complexity = -1;
        this.term = components;
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
    public final int length() {
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
        Term[] l = new Term[length()];
        for (int i = 0; i < l.length; i++)
            l[i] = term[i].cloneDeep();
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

    public boolean hasVar() {
        return varTotal > 0;
    }


    final public void addTermsTo(final Collection<Term> c) {
        Collections.addAll(c, term);
    }

    public Term[] cloneTermsReplacing(int index, final Term replaced) {
        Term[] y = cloneTerms();
        y[index] = replaced;
        return y;
    }

    protected <I extends Compound, T extends Term> Term[] cloneTermsTransforming(final CompoundTransform<I, T> trans, final int level) {
        final Term[] y = new Term[length()];
        int i = 0;
        for (Term x : this.term) {
            if (trans.test(x)) {
                x = trans.apply((I) this, (T) x, level);
            } else if (x instanceof Compound) {
                //recurse
                Compound cx = (Compound) x;
                if (trans.testSuperTerm(cx)) {
                    Term[] cls = cx.cloneTermsTransforming(trans, level + 1);
                    if (cls == null) return null;
                    x = cx.clone(cls);
                }
            }
            if (x == null) return null;
            y[i++] = x;
        }
        return y;
    }


    public final boolean isEmpty() {
        return length() != 0;
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

}

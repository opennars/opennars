package nars.term;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.factory.Sets;
import nars.Global;

import java.util.List;
import java.util.function.Consumer;


/**
 * Methods common to both Term and Subterms
 * @param T subterm type
 */
public interface TermContainer<T extends Term> extends Termlike, Comparable, Iterable<T> {

    int varDep();

    int varIndep();

    int varQuery();

    int vars();

    /** gets subterm at index i */
    T term(int i);

    T termOr(int index, T resultIfInvalidIndex);

    T[] termsCopy();

    default Term[] termsCopy(Term... additional) {
        if (additional.length == 0) return termsCopy();
        return Terms.concat(terms(), additional);
    }

    default MutableSet<Term> toSet() {
        return Sets.mutable.of(terms());
    }
    static MutableSet<Term> intersect(TermContainer a, TermContainer b) {
        return Sets.intersect(a.toSet(),b.toSet());
    }
    static MutableSet<Term> difference(TermContainer a, TermContainer b) {
        return Sets.difference(a.toSet(), b.toSet());
    }

    /** expected to provide a non-copy reference to an internal array,
     *  if it exists. otherwise it should create such array.
     *  if this creates a new array, consider using .term(i) to access
     *  subterms iteratively.
     */
    T[] terms();


    default Term[] terms(IntObjectPredicate<T> filter) {
        List<T> l = Global.newArrayList(size());
        int s = size();
        for (int i = 0; i < s; i++) {
            T t = term(i);
            if (filter.accept(i, t))
                l.add(t);
        }
        if (l.isEmpty()) return Terms.EmptyTermArray;
        return l.toArray(new Term[l.size()]);
    }


    void forEach(Consumer<? super T> action, int start, int stop);


    static Term[] copyByIndex(TermContainer c) {
        int s = c.size();
        Term[] x = new Term[s];
        for (int i = 0; i < s; i++) {
            x[i] = c.term(i);
        }
        return x;
    }


    static String toString(TermContainer t) {
        StringBuilder sb = new StringBuilder("{[(");
        int s = t.size();
        for (int i = 0; i < s; i++) {
            sb.append(t.term(i));
            if (i < s-1)
                sb.append(", ");
        }
        sb.append(")]}");
        return sb.toString();

    }

    /** extract a sublist of terms as an array */
    default Term[] terms(int start, int end) {
        Term[] t = new Term[end-start];
        int j = 0;
        for (int i = start; i < end; i++)
            t[j++] = term(i);
        return t;
    }

}

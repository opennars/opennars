package nars.term;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.Global;
import nars.Op;

import java.util.List;


/**
 * Methods common to both Term and Subterms
 */
public interface TermContainer extends Comparable {

    int structure();
    int volume();
    int complexity();
    int size();

    /** nth subterm */
    Term term(int n);
    Term termOr(int index, Term resultIfInvalidIndex);

    //TODO rename: impossibleToContain
    boolean impossibleSubTermVolume(final int otherTermVolume);


    /** TODO use hasAll or hasAny and test them */
    @Deprecated default boolean impossibleToMatch(final int possibleSubtermStructure) {
        return Term.impossibleToMatch(
                structure(),
                possibleSubtermStructure
        );
    }

    /** if it's larger than this term it can not be equal to this.
     * if it's larger than some number less than that, it can't be a subterm.
     */
    default boolean impossibleSubTermOrEqualityVolume(int otherTermsVolume) {
        return otherTermsVolume > volume();
    }


    /** tests if contains a term in the structural hash
     *  WARNING currently this does not detect presence of pattern variables
     * */
    default boolean hasAny(final Op op) {
//        if (op == Op.VAR_PATTERN)
//            return Variable.hasPatternVariable(this);
        return hasAny((1<<op.ordinal()));
    }

//    default boolean hasAll(int structuralVector) {
//        final int s = structure();
//        return (s & structuralVector) == s;
//    }
//

    default boolean hasAny(final int structuralVector) {
        final int s = structure();
        return (s & structuralVector) != 0;
    }

    default boolean impossibleSubterm(final Term target) {
        return ((impossibleToMatch(target.structure())) ||
                (impossibleSubTermVolume(target.volume())));
    }
    default boolean impossibleSubTermOrEquality(final Term target) {
        return ((impossibleToMatch(target.structure())) ||
                (impossibleSubTermOrEqualityVolume(target.volume())));
    }
    default boolean impossibleToMatch(final Term c) {
        return impossibleToMatch(c.structure());
    }

    /** this is not required to produce a cloned copy; so be careful with the result.
     *  ex: TermVector's implementation will provide a reference to its internal array
     */
    default Term[] toArray() {
        int s = size();
        Term[] x = new Term[s];
        for (int i = 0; i < s; i++) {
            x[i] = term(i);
        }
        return x;
    }

    default Term[] toArray(IntObjectPredicate<Term> filter) {
        List<Term> l = Global.newArrayList(size());
        int s = size();
        for (int i = 0; i < s; i++) {
            Term t = term(i);
            if (filter.accept(i, t))
                l.add(t);
        }
        if (l.isEmpty()) return Terms.EmptyTermArray;
        return l.toArray(new Term[l.size()]);
    }


}

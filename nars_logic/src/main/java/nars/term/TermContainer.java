package nars.term;

import nars.Op;

/**
 * Methods common to both Term and Subterms
 */
public interface TermContainer extends Comparable {

    int structure();
    int volume();
    int complexity();
    int size();

    //TODO rename: impossibleToContain
    boolean impossibleSubTermVolume(final int otherTermVolume);


    default boolean impossibleToMatch(final int possibleSubtermStructure) {
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


    /** tests if contains a term in the structural hash */
    default boolean has(final Op op) {
        return (structure() & (1<<op.ordinal())) > 0;
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

    boolean equalsAll(Term[] cls);
}

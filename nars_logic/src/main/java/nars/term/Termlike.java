package nars.term;

import java.util.function.Predicate;

/**
 * Features exhibited by, and which can classify terms
 * and termlike productions
 */
public interface Termlike  {

    int volume();
    int complexity();
    int structure();
    int size();

    /** if contained within; doesnt match this term (if it's a term);
     *  false if term is atomic since it can contain nothing
     * */
    boolean containsTerm(Term t);



    default boolean hasAny(int structuralVector) {
        return (structure() & structuralVector) != 0;
    }


    default boolean impossibleStructureMatch(int possibleSubtermStructure) {
        return impossibleStructureMatch(
                structure(),
                possibleSubtermStructure
        );
    }

    boolean containsTermRecursively(Term target);

    static boolean impossibleStructureMatch(int existingStructure, int possibleSubtermStructure) {
        //if the OR produces a different result compared to subterms,
        // it means there is some component of the other term which is not found
        return ((possibleSubtermStructure | existingStructure) != existingStructure);
    }

    default boolean impossibleSubterm(Term target) {
        return ((impossibleStructureMatch(structure(), target.structure()))) ||
                (impossibleSubTermVolume(target.volume()));
    }

    /** if it's larger than this term it can not be equal to this.
     * if it's larger than some number less than that, it can't be a subterm.
     */
    default boolean impossibleSubTermOrEqualityVolume(int otherTermsVolume) {
        return otherTermsVolume > volume();
    }


    default boolean impossibleSubTermVolume(int otherTermVolume) {
//        return otherTermVolume >
//                volume()
//                        - 1 /* for the compound itself */
//                        - (size() - 1) /* each subterm has a volume >= 1, so if there are more than 1, each reduces the potential space of the insertable */

        /*
        otherTermVolume > volume - 1 - (size - 1)
                        > volume - size
         */
        return otherTermVolume > volume() - size();
    }


    default boolean impossibleSubTermOrEquality(Term target) {
        return ((impossibleStructureMatch(target.structure())) ||
                (impossibleSubTermOrEqualityVolume(target.volume())));
    }

    /** recurses all subterms while the result of the predicate is true;
     *  returns true if all true
     *
     * @param v*/
    boolean and(Predicate<? super Term> v);

    /** recurses all subterms until the result of the predicate becomes true;
     *  returns true if any true
     *
     * @param v*/
    boolean or(Predicate<? super Term> v);





}

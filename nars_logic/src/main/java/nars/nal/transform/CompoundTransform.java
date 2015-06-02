package nars.nal.transform;

import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.function.Predicate;

/** I = input term type, T = transformable subterm type */
public interface CompoundTransform<I extends Compound, T extends Term> extends Predicate<Term> {
    T apply(I containingCompound, T v, int depth);

    /** enable predicate determined by the superterm, tested before processing any subterms */
    default boolean testSuperTerm(Compound terms) {
        return true;
    }

}

package nars.nal.meta.match;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.ShadowAtom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.Substitution;

import java.util.Collection;
import java.util.function.Function;

/**
 * Holds results of an ellipsis match and
 * implements a pre-filter before forming the
 * subterm collection, and post-filter before
 * forming a resulting substituted term.
 */
abstract public class EllipsisMatch<T extends Term> extends ShadowAtom {

    public EllipsisMatch() {
        super("");
    }

    public static ArrayEllipsisMatch matchedSubterms(Compound Y, IntObjectPredicate<Term> filter) {
        Function<IntObjectPredicate,Term[]> arrayGen =
                !(Y instanceof Sequence) ?
                        Y::terms :
                        ((Sequence)Y)::toArrayWithIntervals;

        return new ArrayEllipsisMatch(arrayGen.apply( filter ));
    }

    /**
     *
     * @param subterms
     * @param superterm acting as a template which is cloned for the result
     */
    abstract public T build(Term[] subterms, Compound superterm);

    /**
     * yields the expanded terms used in a substitution
     * when constructing a compound containing the ellipsis result.
     *
     * by default, just return the terms held,
     * although it can perform late matching by
     * using the Substitution parameters .getXY() method
     *
     * */
    abstract public boolean resolve(Substitution substitution, Collection<Term> target);

    @Override
    abstract public int size();
}

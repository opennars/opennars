package nars.term.match;

import com.google.common.collect.Iterables;
import nars.nal.nal7.ShadowAtom;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.Subst;

import java.util.Collection;
import java.util.Set;

/**
 * Holds results of an ellipsis match and
 * implements a pre-filter before forming the
 * subterm collection, and post-filter before
 * forming a resulting substituted term.
 */
public abstract class EllipsisMatch extends ShadowAtom implements Iterable<Term> {

    public EllipsisMatch() {
        super();
    }

//    public static ArrayEllipsisMatch matchedSubterms(Compound Y, IntObjectPredicate<Term> filter) {
//        Function<IntObjectPredicate,Term[]> arrayGen =
//                !(Y instanceof Sequence) ?
//                        Y::terms :
//                        ((Sequence)Y)::toArrayWithIntervals;
//
//        return new ArrayEllipsisMatch(arrayGen.apply( filter ));
//    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return Iterables.elementsEqual(this, ((Iterable)obj));
    }

    /**
     * yields the expanded terms used in a substitution
     * when constructing a compound containing the ellipsis result.
     *
     * by default, just return the terms held,
     * although it can perform late matching by
     * using the Substitution parameters .getXY() method
     *
     * */
    @Override
    public abstract boolean applyTo(Subst substitution, Collection<Term> target, boolean fullMatch);

    @Override
    public abstract int size();

    abstract public boolean addContained(Compound Y, Set<Term> ineligible);

}

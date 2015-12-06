package nars.nal.meta.match;

import nars.nal.nal4.ShadowProduct;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.Substitution;

import java.util.Collection;
import java.util.Collections;

/**
 * Holds results of an ellipsis match and
 * implements a pre-filter before forming the
 * subterm collection, and post-filter before
 * forming a resulting substituted term.
 */
abstract public class AbstractEllipsisTransform<T extends Term> extends ShadowProduct {

    public AbstractEllipsisTransform(Term[] t) {
        super(t);
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
    public boolean resolve(Substitution substitution, Collection<Term> target) {
        Collections.addAll(target, term);
        return true;
    }
}

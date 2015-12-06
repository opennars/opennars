package nars.nal.meta.match;

import nars.term.Compound;
import nars.term.Term;

/**
 * the indicated relation term is inserted
 * at the index location of the image
 */
public class ImageGrowEllipsisMatch extends TransformingEllipsisMatch<Term> {

    public ImageGrowEllipsisMatch(Term[] t, EllipsisTransform et, Compound y) {
        super(t);
    }

    @Override
    public Term build(Term[] subterms, Compound superterm) {
        return superterm.clone(subterms);
    }
}

package nars.nal.meta.match;

import nars.nal.nal4.Image;
import nars.term.Term;
import nars.term.transform.Subst;

import java.util.Collection;

/**
 * the indicated relation term is inserted
 * at the index location of the original image
 * used to make products from image subterms
 */
public class ImagePutMatch extends ArrayEllipsisMatch<Term> {

    private final Term to;
    private final Image origin;

    public ImagePutMatch(Term[] t, Term relationTerm, Image y) {
        super(t);
        this.to = relationTerm;
        this.origin = y;
    }

    @Override
    public boolean applyTo(Subst substitution, Collection<Term> target, boolean fullMatch) {
        Term relation = substitution.getXY(this.to);
        if (relation == null) {
            if (fullMatch)
                return false;
            else
                relation = this.to;
        }

        Term[] t = origin.terms();
        Image origin = this.origin;

        int ri = origin.relationIndex;

        int ot = t.length;
        //int j = (ri == ot) ? 1 : 0; //shift
        int j = 0;
        int r = ri - 1;
        for (int i = 0; i < ot; i++) {
            target.add( i==r ? relation : t[j]);
            j++;
        }
//        System.arraycopy(ot, 0, t, 0, r);
//        t[r] = relation;
//        System.arraycopy(ot, r, t, r+1, ot.length - r);

        return true;
    }

}

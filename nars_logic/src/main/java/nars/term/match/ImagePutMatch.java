package nars.term.match;

import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.Subst;

import java.util.Collection;

/**
 * the indicated relation term is inserted
 * at the index location of the original image
 * used to make products from image subterms
 */
public class ImagePutMatch extends ArrayEllipsisMatch<Term> {

    private final Term to;
    private final Compound origin;

    public ImagePutMatch(Term[] t, Term relationTerm, Compound y) {
        super(t);
        to = relationTerm;
        origin = y;
    }

    @Override
    public boolean applyTo(Subst substitution, Collection<Term> target, boolean fullMatch) {
        Term relation = substitution.getXY(to);
        if (relation == null) {
            if (fullMatch)
                return false;
            else
                relation = to;
        }

        Term[] t = origin.terms();
        Compound origin = this.origin;

        int ri = origin.relation();

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

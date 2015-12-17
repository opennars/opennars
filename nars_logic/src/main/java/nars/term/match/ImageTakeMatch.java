package nars.term.match;

import nars.Op;
import nars.term.Term;

/**
 * Masks image subterms selected by an ellipsis
 * for constructing an Image
 */
public class ImageTakeMatch extends ArrayEllipsisMatch {

    //private final int imageIndex;

    public ImageTakeMatch(Term[] t, int imageIndex) {
        super(t);
        //this.imageIndex = imageIndex;

        //mask the relation term
        t[imageIndex] = Op.Imdex;
    }


}

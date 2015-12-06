package nars.nal.meta.match;

import nars.nal.nal4.Image;
import nars.term.Compound;
import nars.term.Term;

/**
 * Masks image subterms selected by an ellipsis
 * for constructing an Image
 */
public class ImageTakeTransform extends AbstractEllipsisTransform<Image> {

    //private final int imageIndex;

    public ImageTakeTransform(Term[] t, int imageIndex) {
        super(t);
        //this.imageIndex = imageIndex;

        //mask the relation term
        t[imageIndex] = Image.Index;
    }

    @Override
    public Image build(Term[] t, Compound superterm) {
        return Image.build(superterm.op(), t);
    }

}

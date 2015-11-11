package nars.nal.nal4;

import nars.Op;
import nars.term.DefaultCompound;
import nars.term.Term;

import java.io.IOException;

/**
 * Created by me on 5/20/15.
 */
public class ProductN<T extends Term> extends DefaultCompound<T> implements Product<T> {


    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    public ProductN(final T... arg) {
        super();

        init(arg);
    }

    @Override public final boolean isCommutative() {
        return false;
    }

    @Override
    public final Op op() {
        return Op.PRODUCT;
    }

    /*public ProductN(final List<Term> x) {
        this(x.toArray(new Term[x.size()]));
    }*/


    /**
     * Clone a Product
     * @return A new object, to be casted into an ImageExt
     */
    @Override
    public Product clone() {
        return Product.make(term);
    }

    @Override
    public Product clone(Term[] replaced) {
        return Product.make(replaced);
    }


    @Override
    public final T[] terms() {
        return term;
    }


    @Override
    public boolean appendOperator(Appendable p) throws IOException {
        //skip
        return false;
    }


}

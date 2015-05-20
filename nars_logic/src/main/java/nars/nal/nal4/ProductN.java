package nars.nal.nal4;

import nars.nal.NALOperator;
import nars.nal.term.Compound;
import nars.nal.term.DefaultCompound;
import nars.nal.term.Term;

import java.util.List;

/**
 * Created by me on 5/20/15.
 */
public class ProductN extends DefaultCompound implements Product {
    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    public ProductN(final Term... arg) {
        super(arg);

        init(arg);
    }

    @Override
    public NALOperator operator() {
        return NALOperator.PRODUCT;
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
    public Term[] terms() {
        return term;
    }




}

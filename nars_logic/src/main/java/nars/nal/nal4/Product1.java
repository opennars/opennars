package nars.nal.nal4;

import nars.Op;
import nars.term.Compound1;
import nars.term.Term;

import java.io.IOException;

/**
 * Higher efficiency 1-subterm implementation of Product
 */
public class Product1<T extends Term> extends Compound1<T>  implements Product<T> {

    public Product1(T the) {
        super();

        init(the);
    }

    @Override public final boolean isCommutative() {
        return false;
    }


    @Override
    public final Op op() {
        return Op.PRODUCT;
    }

    @Override
    public T[] terms() {
        return (T[])new Term[]{the()};
    }

    @Override
    public Product1<T> clone() {
        return Product.only(the());
    }

    @Override
    public Term clone(Term[] replaced) {
        return Product.make(replaced);
    }


    @Override
    public boolean appendTermOpener() {
        return super.appendTermOpener();
    }

    @Override
    public boolean appendOperator(Appendable p) throws IOException {
        //skip
        return false;
    }

}

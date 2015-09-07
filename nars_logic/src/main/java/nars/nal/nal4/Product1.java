package nars.nal.nal4;

import nars.Op;
import nars.term.Compound1;
import nars.term.Term;

import java.io.IOException;
import java.io.Writer;

/**
 * Higher efficiency 1-subterm implementation of Product
 */
public class Product1<T extends Term> extends Compound1<T>  implements Product<T> {

    public Product1(T the) {
        super(the);

        init(term);
    }

    @Override
    public Op op() {
        return Op.PRODUCT;
    }

    @Override
    public Term[] terms() {
        return new Term[]{the()};
    }

    @Override
    public Term clone() {
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
    public boolean appendOperator(Writer p) throws IOException {
        //skip
        return false;
    }

}

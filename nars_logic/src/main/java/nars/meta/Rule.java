package nars.meta;

import nars.nal.nal4.Product;
import nars.nal.nal4.ProductN;
import nars.term.Term;

/**
 * Abstract MetaNAL Rule
 */
abstract public class Rule<X,Y> extends ProductN {

    public Rule(Product premises, Term result) {
        super(premises, result);
    }

    abstract public Y generate(X context);

}

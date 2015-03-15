package nars.operator.data;

import nars.logic.entity.CompoundTerm;
import nars.logic.entity.Term;
import nars.logic.nal3.SetTensional;
import nars.logic.nal4.Product;
import nars.logic.nal5.Conjunction;
import nars.logic.nal8.TermFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * recursively collects the contents of set/list compound term argument's
 * into a list, to one of several resulting term types:
 *      product
 *      set (TODO)
 *      conjunction (TODO)
 *
 * TODO recursive version with order=breadth|depth option
 */
abstract public class Flat extends TermFunction {

    public Flat(String name) {
        super(name);
    }

    @Override
    public Term function(Term[] x) {
        List<Term> l = new ArrayList();
        collect(x, l);
        return result(l);
    }
    protected static void collect(Term[] x, List<Term> l) {
        for (Term a : x) {
            if ((a instanceof Product) || (a instanceof SetTensional) || (a instanceof Conjunction)) {
                collect( ((CompoundTerm)a).term, l);
            }
            else
                l.add(a);
        }
    }

    abstract public Term result(List<Term> terms);

    public static class AsProduct extends Flat {

        public AsProduct() {
            super("^flatProduct");
        }

        @Override
        public Term result(List<Term> terms) {
            return new Product(terms);
        }

    }





    //public Flat(boolean productOrSet, boolean breadthOrDepth) {
        //generate each of the 4 different operator names

    //}
}

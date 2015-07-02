package nars.op.data;

import nars.nal.nal3.SetTensional;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal8.operator.TermFunction;
import nars.term.Compound;
import nars.term.Term;

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

    @Override
    public Term function(Term[] x) {
        List<Term> l = new ArrayList();
        collect(x, l);
        return result(l);
    }
    public static List<Term> collect(Term[] x, List<Term> l) {
        for (Term a : x) {
            if ((a instanceof Product) || (a instanceof SetTensional) || (a instanceof Conjunction)) {
                collect( ((Compound)a).term, l);
            }
            else
                l.add(a);
        }
        return l;
    }

    abstract public Term result(List<Term> terms);

    public static class flatProduct extends Flat {


        @Override
        public Term result(List<Term> terms) {
            return Product.make(terms);
        }

    }





    //public Flat(boolean productOrSet, boolean breadthOrDepth) {
        //generate each of the 4 different operate names

    //}
}

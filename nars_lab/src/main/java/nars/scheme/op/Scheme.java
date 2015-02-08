package nars.scheme.op;

import nars.core.Memory;
import nars.logic.entity.Term;
import nars.logic.nal4.Product;
import nars.logic.nal8.SynchronousFunctionOperator;
import nars.util.data.sexpression.Pair;


public class Scheme extends SynchronousFunctionOperator {

    public Scheme() {
        super("^scheme");
    }

    public Object eval(Product p) {
        final nars.scheme.Scheme env = new nars.scheme.Scheme();
        return env.eval(p);
    }

    //TODO make narsToScheme method

    public static Term schemeToNars(Object schemeObj) {
        if (schemeObj instanceof Pair) {
            return new Product( ((Pair)schemeObj).toList() );
        }
        //TODO handle other types, like Object[] etc
        else {
            return Term.get("\"" + schemeObj.toString() + "\"" );
        }
    }

    @Override
    protected Term function(Memory memory, Term[] x) {
        Term code = x[0];
        if (code instanceof Product) {
            //evaluate as an s-expression
            return schemeToNars(eval((Product) code));
        }
        //Set = evaluate as a cond?
        else {

        }

        return null;
    }

    @Override
    protected Term getRange() {
        return null;
    }


}

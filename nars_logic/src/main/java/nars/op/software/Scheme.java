package nars.op.software;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nars.nal.nal4.Product;
import nars.nal.nal8.TermFunction;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import nars.op.software.scheme.DefaultEnvironment;
import nars.op.software.scheme.Environment;
import nars.op.software.scheme.Evaluator;
import nars.op.software.scheme.cons.Cons;
import nars.op.software.scheme.expressions.Expression;
import nars.op.software.scheme.expressions.ListExpression;
import nars.op.software.scheme.expressions.NumberExpression;
import nars.op.software.scheme.expressions.SymbolExpression;

import java.util.List;


public class Scheme extends TermFunction {

    public static final Environment env = DefaultEnvironment.newInstance();

    final static Function<Term,Expression> narsToScheme = new Function<Term, Expression>() {

        @Override
        public Expression apply(Term term) {

            if (term instanceof Product) {
                //return ListExpression.list(SymbolExpression.symbol("quote"), new SchemeProduct((Product)term));
                return new SchemeProduct((Product)term);
            }
            else if (term instanceof Atom) {

                String s = term.toString();

                //attempt to parse as number
                try {
                    double d = Double.parseDouble(s);
                    return new NumberExpression((long)d);
                }
                catch (NumberFormatException e) { }
                //atomic symbol
                return new SymbolExpression(s);
            }
            throw new RuntimeException("Invalid term for scheme: " + term);
        }
    };

    /** adapter class for NARS term -> Scheme expression; temporary until the two API are merged better */
    public static class SchemeProduct extends ListExpression {

        public SchemeProduct(Product p) {
            super(Cons.copyOf( Iterables.transform(p, narsToScheme)));
        }
    }

    public Scheme() {
        super("^scheme");
    }

    public Expression eval(Product p) {
        return Evaluator.evaluate(new SchemeProduct(p), env);
    }

    //TODO make narsToScheme method

    public final Function<Expression, Term> schemeToNars = new Function<Expression, Term>() {
        @Override
        public Term apply(Expression schemeObj) {
            if (schemeObj instanceof ListExpression) {
                List<Term> elements = Lists.newArrayList( Iterables.transform(((ListExpression)schemeObj).value, schemeToNars));
                return new Product( elements );
            }
            //TODO handle other types, like Object[] etc
            else {
                //return Term.get("\"" + schemeObj.print() + "\"" );
                return get(schemeObj.print());
            }
            //throw new RuntimeException("Invalid expression for term: " + schemeObj);

        }
    };

    @Override
    public Term function(Term[] x) {
        Term code = x[0];
        if (code instanceof Product) {
            //evaluate as an s-expression
            //System.out.println( ((Product) code).first() );
            //System.out.println( ((Product) code).rest() );

            return schemeToNars.apply(eval( ((Product) code)  ));
        }
        //Set = evaluate as a cond?
        else {

        }

        return null;
    }


}

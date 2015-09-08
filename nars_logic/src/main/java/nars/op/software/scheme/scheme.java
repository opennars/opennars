package nars.op.software.scheme;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nars.nal.nal4.Product;
import nars.nal.nal4.Product1;
import nars.nal.nal4.ProductN;
import nars.nal.nal8.operator.TermFunction;
import nars.op.software.scheme.cons.Cons;
import nars.op.software.scheme.expressions.Expression;
import nars.op.software.scheme.expressions.ListExpression;
import nars.op.software.scheme.expressions.NumberExpression;
import nars.op.software.scheme.expressions.SymbolExpression;
import nars.term.Atom;
import nars.term.Term;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class scheme extends TermFunction {

    public static final SchemeClosure env = DefaultEnvironment.newInstance();

    final static Function<Term,Expression> narsToScheme = new Function<Term, Expression>() {

        @Override
        public Expression apply(Term term) {

            if (term instanceof ProductN) {
                //return ListExpression.list(SymbolExpression.symbol("quote"), new SchemeProduct((Product)term));
                return new SchemeProduct((ProductN)term);
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

        public SchemeProduct(Product1 p) {
            super(Cons.copyOf(narsToScheme.apply(p.the())));
        }

        public SchemeProduct(ProductN p) {
            super(Cons.copyOf(Iterables.transform(p, narsToScheme)));
        }
    }

    public Expression eval(Product1 p) {
        return Evaluator.evaluate(new SchemeProduct(p), env);
    }
    public Expression eval(ProductN p) {
        return Evaluator.evaluate(new SchemeProduct(p), env);
    }

    //TODO make narsToScheme method

    public static final Function<Expression, Term> schemeToNars = new Function<Expression, Term>() {
        @Override
        public Term apply(Expression schemeObj) {
            if (schemeObj instanceof ListExpression) {
                List<Term> elements = Lists.newArrayList(StreamSupport.stream(((ListExpression) schemeObj).value.spliterator(), false).map(schemeToNars::apply).collect(Collectors.toList()));
                return Product.make( elements );
            }
            //TODO handle other types, like Object[] etc
            else {
                //return Term.get("\"" + schemeObj.print() + "\"" );
                return Atom.the(schemeObj.print());
            }
            //throw new RuntimeException("Invalid expression for term: " + schemeObj);

        }
    };

    @Override
    public Term function(Term[] x) {
        Term code = x[0];

        if (code instanceof Product1) {
            return schemeToNars.apply(eval( ((Product1) code)  ));
        }
        else if (code instanceof ProductN) {
            return schemeToNars.apply(eval( ((ProductN) code)  ));
        }
        else {
            return schemeToNars.apply(eval( new ProductN(x) ));
        }
        //Set = evaluate as a cond?
//        else {
//
//        }

        //return null;
    }


}

package nars.op.software.scheme;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import nars.$;
import nars.nal.nal8.operator.TermFunction;
import nars.op.software.scheme.cons.Cons;
import nars.op.software.scheme.expressions.*;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compile.TermBuilder;
import nars.term.compound.Compound;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static nars.op.software.scheme.DefaultEnvironment.load;


public class scheme extends TermFunction {

    @Deprecated public static final SchemeClosure env = DefaultEnvironment.newInstance();



    /** adapter class for NARS term -> Scheme expression; temporary until the two API are merged better */
    public static class SchemeProduct extends ListExpression {


        public SchemeProduct(Iterable p) {
            super(Cons.copyOf(Iterables.transform(p, term -> {

                if (term instanceof Iterable) {
                    //return ListExpression.list(SymbolExpression.symbol("quote"), new SchemeProduct((Product)term));
                    return new SchemeProduct((Iterable) term);
                }
                if (term instanceof Atom) {

                    String s = ((Atom)term).toStringUnquoted();

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
            })));
        }
    }

    //TODO make narsToScheme method

    public static final Function<Expression, Term> schemeToNars = new Function<Expression, Term>() {
        @Override
        public Term apply(Expression schemeObj) {
            if (schemeObj instanceof ListExpression) {
                return apply( ((ListExpression)schemeObj).value );
            } else if (schemeObj instanceof SymbolicProcedureExpression) {
                Cons<Expression> exp = ((SymbolicProcedureExpression) schemeObj).exps;
                return apply(exp);
            }
            //TODO handle other types, like Object[] etc
            else {
                //return Term.get("\"" + schemeObj.print() + "\"" );
                return Atom.the(schemeObj.print());
            }
            //throw new RuntimeException("Invalid expression for term: " + schemeObj);

        }

        public Term apply(Iterable<Expression> e) {
            List<Term> elements = Lists.newArrayList(StreamSupport.stream(e.spliterator(), false).map(schemeToNars::apply).collect(Collectors.toList()));
            return $.p( elements );
        }
    };

    @Override
    public Term function(Compound o, TermBuilder i) {
        Term[] x = o.terms();
        Term code = x[0];

        if (code instanceof Atom) {
            //interpret as eval string
            Atom a = (Atom)code;

            return schemeToNars.apply(
                Evaluator.evaluate(
                    load(a.toStringUnquoted(), env), env)
            );

        }

        return code instanceof Compound ?
                schemeToNars.apply(
                    Evaluator.evaluate(
                        new SchemeProduct(((Iterable) code)), env)) :
                schemeToNars.apply(Evaluator.evaluate(new SchemeProduct($.p(x)), env));
        //Set = evaluate as a cond?
//        else {
//
//        }

        //return null;
    }


}

package nars.operate.scheme;

import nars.operate.software.scheme.DefaultEnvironment;
import nars.operate.software.scheme.Environment;
import nars.operate.software.scheme.Evaluator;
import nars.operate.software.scheme.expressions.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static nars.operate.software.scheme.expressions.BooleanExpression.bool;
import static nars.operate.software.scheme.expressions.ListExpression.list;
import static nars.operate.software.scheme.expressions.NumberExpression.number;
import static nars.operate.software.scheme.expressions.SymbolExpression.symbol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class EvaluatorTest {

    @Test
    public void number_expression() {
        NumberExpression l = number(123);

        Expression result = Evaluator.evaluate(l, emptyEnvironment());

        assertThat(result, is(number(123)));
    }

    @Test
    public void variable_lookup() {
        Environment e = new Environment(bindings(symbol("foo"), number(123)));

        Expression result = Evaluator.evaluate(symbol("foo"), e);

        assertThat(result, is(number(123)));
    }

    @Test
    public void quoted_text() {
        ListExpression exp = list(symbol("quote"), symbol("foo"));

        Expression result = Evaluator.evaluate(exp, emptyEnvironment());

        assertThat(result, is(symbol("foo")));
    }

    @Test
    public void variable_assignment() {
        Environment environment = new Environment(bindings(symbol("foo"), number(123)));
        ListExpression exp = list(symbol("set!"), symbol("foo"), number(321));

        Expression result = Evaluator.evaluate(exp, environment);

        assertThat(result, is(Expression.none()));
        assertThat(environment.lookup(symbol("foo")), is(number(321)));
    }

    @Test
    public void variable_definition() {
        Environment environment = emptyEnvironment();
        ListExpression exp = list(symbol("define"), symbol("foo"), list(symbol("quote"), symbol("bar")));

        Expression result = Evaluator.evaluate(exp, environment);

        assertThat(result, is(Expression.none()));
        assertThat(environment.lookup(symbol("foo")), is(symbol("bar")));
    }

    @Test
    public void boolean_expression() {
        BooleanExpression bool = bool(true);

        Expression result = Evaluator.evaluate(bool, emptyEnvironment());

        assertThat(result, is(bool(true)));
    }

    @Test
    public void if_expression_true() {
        ListExpression exp = list(symbol("if"), bool(true), number(1), number(2));

        Expression result = Evaluator.evaluate(exp, emptyEnvironment());

        assertThat(result, is(number(1)));
    }

    @Test
    public void if_expression_false() {
        ListExpression exp = list(symbol("if"), bool(false), number(1), number(2));

        Expression result = Evaluator.evaluate(exp, emptyEnvironment());

        assertThat(result, is(number(2)));
    }

    @Test
    public void if_expression_false_no_else() {
        ListExpression exp = list(symbol("if"), bool(false), number(1));

        Expression result = Evaluator.evaluate(exp, emptyEnvironment());

        assertThat(result, is(Expression.none()));
    }

    @Test
    public void lambda_expression() {
        ListExpression exp = list(symbol("lambda"), list(), list());

        Expression result = Evaluator.evaluate(exp, emptyEnvironment());

        assertThat(result, instanceOf(ProcedureExpression.class));
    }

    @Test
    public void primitive_procedure_addition() {
        ListExpression exp = list(symbol("+"), number(5), number(7), number(11));

        Expression result = Evaluator.evaluate(exp, DefaultEnvironment.newInstance());

        assertThat(result, is(number(23)));
    }

    @Test
    public void compound_procedure() {
        ListExpression lambda = list(symbol("lambda"), list(symbol("a"), symbol("b")), list(symbol("+"), symbol("a"), symbol("b")));
        ListExpression lambdaCall = list(lambda, number(1), number(2));

        Expression result = Evaluator.evaluate(lambdaCall, DefaultEnvironment.newInstance());

        assertThat(result, is(number(3)));
    }

    @Test
    public void begin() {
        Environment environment = DefaultEnvironment.newInstance().extend(bindings(symbol("foo"), number(123)));
        ListExpression exp = list(symbol("begin"), list(symbol("set!"), symbol("foo"), number(321)), list(symbol("+"), symbol("foo"), number(1)));

        Expression result = Evaluator.evaluate(exp, environment);

        assertThat(result, is(number(322)));

    }

    @Test
    public void define_function() {
        Environment environment = emptyEnvironment();
        ListExpression exp = list(symbol("define"), list(symbol("foo"), symbol("a"), symbol("b")), number(1));

        Evaluator.evaluate(exp, environment);

        Expression value = environment.lookup(symbol("foo"));
        assertThat(value instanceof ProcedureExpression, is(true));
    }

    private static Environment emptyEnvironment() {
//        return new Environment(new HashMap<>());
        return DefaultEnvironment.newInstance();
    }

    private static Map<SymbolExpression, Expression> bindings(Expression... keyValues) {
        HashMap<SymbolExpression, Expression> bindings = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            bindings.put((SymbolExpression) keyValues[i], keyValues[i + 1]);
        }

        return bindings;
    }
}

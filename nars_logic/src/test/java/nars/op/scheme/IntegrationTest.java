package nars.op.scheme;


import nars.op.software.scheme.DefaultEnvironment;
import nars.op.software.scheme.Evaluator;
import nars.op.software.scheme.Reader;
import nars.op.software.scheme.SchemeClosure;
import nars.op.software.scheme.exception.UnexpectedExpression;
import nars.op.software.scheme.expressions.Expression;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static nars.op.software.scheme.cons.Cons.cons;
import static nars.op.software.scheme.cons.Cons.empty;
import static nars.op.software.scheme.expressions.BooleanExpression.bool;
import static nars.op.software.scheme.expressions.ListExpression.list;
import static nars.op.software.scheme.expressions.NumberExpression.number;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IntegrationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void factorial() {
        String input = "(define factorial (lambda (n) (if (= n 1) 1 (* n (factorial (- n 1))))))";
        SchemeClosure environment = DefaultEnvironment.newInstance();

        eval(input, environment);
        Expression result = eval("(factorial 3)", environment);

        assertThat(result, is(number(6)));
    }

    @Test
    public void fibonacci() {
        String input = "(define (fib n) (if (< n 2) n (+ (fib (- n 1)) (fib (- n 2)))))";
        SchemeClosure environment = DefaultEnvironment.newInstance();

        eval(input, environment);
        Expression result = eval("(fib 10)", environment);

        assertThat(result, is(number(55)));
    }

    @Test
    public void let() {
        String input = "(let ((x 10) (y 20)) (* x y))";
        Expression result = eval(input);

        assertThat(result, is(number(200)));
    }

    @Test
    public void eval() {
        String input = "(eval (quote (+ 1 2)))";

        Expression result = eval(input);

        assertThat(result, is(number(3)));
    }

    @Test
    public void define_function_sequence() {
        String input = "(define (foo) (define a 10) a)";

        SchemeClosure environment = DefaultEnvironment.newInstance();

        eval(input, environment);
        Expression result = eval("(foo)", environment);

        assertThat(result, is(number(10)));

    }

    @Test
    public void lambda_sequence() {
        Expression result = eval("((lambda () (define a 10) a))");

        assertThat(result, is(number(10)));

    }

    @Test
    public void unexpected_expression() {
        thrown.expect(UnexpectedExpression.class);
        thrown.expectMessage("Expected expression of type 'ListExpression', got 'a'");

        eval("(lambda a b)");
    }

    @Test
    public void set_car() {
        Expression result = eval("(let ((x (cons 1 '()))) (set-car! x 2) x)");

        assertThat(result, is(list(cons(number(2), empty()))));
    }

    @Test
    public void empty_list_in_null() {
        Expression result = eval("(null? '())");

        assertThat(result, is(bool(true)));
    }

    @Test
    public void dotted_varargs() {
        Expression result = eval("((lambda (. xs) xs) 1 2 3)");

        assertThat(result, is(list(number(1), number(2), number(3))));
    }

    private Expression read(String input) {
        return Reader.read(input).iterator().next();
    }

    private Expression eval(String input, SchemeClosure environment) {
        return Evaluator.evaluate(read(input), environment);
    }

    private Expression eval(String input) {
        SchemeClosure environment = DefaultEnvironment.newInstance();

        return eval(input, environment);
    }

}

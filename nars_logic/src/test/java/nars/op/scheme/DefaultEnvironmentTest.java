package nars.op.scheme;


import nars.op.software.scheme.DefaultEnvironment;
import nars.op.software.scheme.cons.Cons;
import nars.op.software.scheme.expressions.Expression;
import nars.op.software.scheme.expressions.NumberExpression;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Function;

import static nars.op.software.scheme.cons.Cons.cons;
import static nars.op.software.scheme.expressions.BooleanExpression.bool;
import static nars.op.software.scheme.expressions.ListExpression.list;
import static nars.op.software.scheme.expressions.NumberExpression.number;
import static nars.op.software.scheme.expressions.SymbolExpression.symbol;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
public class DefaultEnvironmentTest {

    @Test
    public void addition() {
        Function<Cons<Expression>, Expression> function = lookupFunction("+");

        assertThat(number(6), is(function.apply(numberList(1, 2, 3))));
    }

    @Test
    public void subtraction() {
        Function<Cons<Expression>, Expression> function = lookupFunction("-");

        assertThat(number(5), is(function.apply(numberList(10, 2, 3))));
    }

    @Test
    public void multiplication() {
        Function<Cons<Expression>, Expression> function = lookupFunction("*");

        assertThat(number(60), is(function.apply(numberList(10, 2, 3))));
    }

    @Test
    public void division() {
        Function<Cons<Expression>, Expression> function = lookupFunction("/");

        assertThat(number(2), is(function.apply(numberList(12, 2, 3))));
    }

    @Test
    public void equality_equals() {
        Function<Cons<Expression>, Expression> function = lookupFunction("=");

        assertThat(bool(true), is(function.apply(numberList(2, 2, 2))));
    }

    @Test
    public void equality_not_equals() {
        Function<Cons<Expression>, Expression> function = lookupFunction("=");

        assertThat(bool(false), is(function.apply(numberList(12, 2, 3))));
    }

    @Test
    public void greater_than_true() {
        Function<Cons<Expression>, Expression> function = lookupFunction(">");

        assertThat(bool(true), is(function.apply(numberList(3, 2, 1))));
    }

    @Test
    public void greater_than_false() {
        Function<Cons<Expression>, Expression> function = lookupFunction(">");

        assertThat(bool(false), is(function.apply(numberList(1, 2, 1))));
    }

    @Test
    public void less_than_true() {
        Function<Cons<Expression>, Expression> function = lookupFunction("<");

        assertThat(bool(true), is(function.apply(numberList(1, 2, 3))));
    }

    @Test
    public void less_than_false() {
        Function<Cons<Expression>, Expression> function = lookupFunction("<");

        assertThat(bool(false), is(function.apply(numberList(3, 2, 3))));
    }

    @Test
    public void greater_than_or_equal_true() {
        Function<Cons<Expression>, Expression> function = lookupFunction(">=");

        assertThat(bool(true), is(function.apply(numberList(3, 2, 2))));
    }

    @Test
    public void greater_than_or_equal_false() {
        Function<Cons<Expression>, Expression> function = lookupFunction(">=");

        assertThat(bool(false), is(function.apply(numberList(3, 2, 3))));
    }

    @Test
    public void less_than_or_equal_true() {
        Function<Cons<Expression>, Expression> function = lookupFunction("<=");

        assertThat(bool(true), is(function.apply(numberList(2, 2, 3))));
    }

    @Test
    public void less_than_or_equal_false() {
        Function<Cons<Expression>, Expression> function = lookupFunction("<=");

        assertThat(bool(false), is(function.apply(numberList(2, 3, 2))));
    }

    @Test
    public void car() {
        Function<Cons<Expression>, Expression> function = lookupFunction("car");

        assertThat(function.apply(cons(list(numberList(1, 2, 3)), Cons.<Expression>empty())), is(number(1)));
    }

    private static Cons<Expression> numberList(Integer... numbers) {
        return Arrays.stream(numbers)
                .map(NumberExpression::number)
                .collect(Cons.collector());
    }

    private Function<Cons<Expression>, Expression> lookupFunction(String operator) {
        return DefaultEnvironment.newInstance().get(symbol(operator)).procedure()/*.getLambda()*/;
    }
}

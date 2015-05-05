package nars.op.scheme;

import com.google.common.collect.ImmutableList;
import nars.op.software.scheme.Reader;
import nars.op.software.scheme.exception.UnmatchedParenthesisExpection;
import nars.op.software.scheme.expressions.Expression;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Iterator;
import java.util.List;

import static nars.op.software.scheme.expressions.ListExpression.list;
import static nars.op.software.scheme.expressions.NumberExpression.number;
import static nars.op.software.scheme.expressions.SymbolExpression.symbol;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ReaderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void tokenize() {
        List<String> tokens = Reader.tokenize("foo");

        assertThat(tokens, is(listOf("foo")));
    }

    @Test
    public void tokenize2() {
        List<String> tokens = Reader.tokenize("(foo)");

        assertThat(tokens, is(listOf("(", "foo", ")")));
    }

    @Test
    public void tokenize3() {
        List<String> tokens = Reader.tokenize("    ((( ())  foo )    ");

        assertThat(tokens, is(listOf("(", "(", "(", "(", ")", ")", "foo", ")")));
    }

    @Test
    public void parse() {
        Expression exp = read("foo");

        assertThat(exp, is(symbol("foo")));
    }


    @Test
    public void parse2() {
        Expression exp = read("(foo)");

        assertThat(exp, is(list(symbol("foo"))));
    }

    @Test
    public void parse_number() {
        Expression exp = read("123");

        assertThat(exp, is(number(123)));
    }

    @Test
    public void parse_complex() {
        Expression exp = read("((lambda (a b c) (+ (- a b) c)) 1 2 3)");

        assertThat(exp, is((Expression)
                list(
                        list(symbol("lambda"),
                                list(symbol("a"), symbol("b"), symbol("c")),
                                list(symbol("+"), list(symbol("-"), symbol("a"), symbol("b")), symbol("c"))),
                        number(1), number(2), number(3))));
    }

    @Test
    public void too_many_open_parens() {
        assertThat(Reader.countOpenParens("((()"), is(2));
    }

    @Test
    public void too_many_closed_parens() {
        thrown.expect(UnmatchedParenthesisExpection.class);
        thrown.expectMessage("Too many closed parenthesis ')'");

        Reader.countOpenParens("((())()))))");
    }

    @Test
    public void foo() {
        Iterator<Expression> exps = Reader.read("1 2").iterator();
        assertThat(exps.next(), is(number(1)));
        assertThat(exps.next(), is(number(2)));
    }

    private Expression read(String foo) {
        return Reader.read(foo).iterator().next();
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return ImmutableList.copyOf(elements);
    }
}

package nars.op.software.scheme;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import nars.op.software.scheme.cons.Cons;
import nars.op.software.scheme.exception.UnmatchedDoubleQuotes;
import nars.op.software.scheme.exception.UnmatchedParenthesisExpection;
import nars.op.software.scheme.expressions.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static nars.op.software.scheme.cons.Cons.cons;
import static nars.op.software.scheme.cons.Cons.empty;


public class Reader {
    private static int eatWhiteSpace(String input, int index) {
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
            index++;
        }

        return index;
    }

    private static final Set<Character> DELIMITERS = ImmutableSet.of('(', ')', '\'', ';');

    public static List<String> tokenize(String input) {
        List<String> res = new ArrayList<>();
        for (int i = eatWhiteSpace(input, 0); i < input.length(); i = eatWhiteSpace(input, i)) {
            char c = input.charAt(i);
            if (c == ';') {
                while (i < input.length() && !(input.charAt(i) == '\n')) {
                    i++;
                }
            } else if (DELIMITERS.contains(c)) {
                res.add(Character.toString(c));
                i++;
            } else if (c == '"') {
                int stringStart = i;
                i = input.indexOf('"', i + 1);
                if (i == -1) {
                    throw new UnmatchedDoubleQuotes("");
                }

                res.add(input.substring(stringStart, ++i).replace("\\n", "\n"));
            } else {
                int stringStart = i;
                while (i < input.length() && !(Character.isWhitespace(input.charAt(i)) || DELIMITERS.contains(input.charAt(i)))) {
                    i++;
                }
                res.add(input.substring(stringStart, i));
            }
        }
        return res;
    }

    public static List<Expression> read(String input) {
        if (input.trim().isEmpty()) {
            return ImmutableList.of(Expression.none());
        }
        ImmutableList.Builder<Expression> builder = ImmutableList.builder();
        Iterator<String> iterator = tokenize(input).iterator();
        while (iterator.hasNext()) {
            builder.addAll(parseSequence(iterator));
        }

        return builder.build();
    }

    public static int countOpenParens(String input) {
        return tokenize(input).stream()
                .filter(t -> t.equals("(") || t.equals(")"))
                .map(t -> t.equals("(") ? 1 : -1)
                .reduce(0, (a, b) -> {
                    if (a + b < 0) {
                        throw new UnmatchedParenthesisExpection("Too many closed parenthesis ')'");
                    }
                    return a + b;
                });
    }

    private static Cons<Expression> parseSequence(Iterator<String> i) {
        Cons<Expression> result = empty();
        while (i.hasNext()) {
            String token = i.next();

            if ("'".equals(token)) {
                String nextToken = i.next();
                if ("(".equals(nextToken)) {
                    result = add(result, ListExpression.list(SymbolExpression.symbol("quote"), ListExpression.list(parseSequence(i))));
                } else {
                    result = add(result, ListExpression.list(SymbolExpression.symbol("quote"), symbolOrNumber(nextToken)));
                }
                continue;
            } else if (")".equals(token)) {
                return result;
            }

            if ("(".equals(token)) {
                result = add(result, ListExpression.list(parseSequence(i)));
            } else {
                result = add(result, symbolOrNumber(token));
            }
        }

        return result;
    }

    private static <T> Cons<T> add(Cons<T> list, T t) {
        if (list.isEmpty()) {
            return cons(t, empty());
        } else {
            list.append(cons(t, empty()));
        }

        return list;
    }

    private static Expression symbolOrNumber(String token) {
        if (token.startsWith("\"")) {
            return StringExpression.string(token.substring(1, token.length() - 1));
        } else if (token.equals("#t") || token.equals("true")) {
            return BooleanExpression.bool(true);
        } else if (token.equals("#f") || token.equals("false")) {
            return BooleanExpression.bool(false);
        } else {
            try {
                return NumberExpression.number(Long.parseLong(token));
            } catch (NumberFormatException e) {
                return SymbolExpression.symbol(token);
            }
        }
    }
}

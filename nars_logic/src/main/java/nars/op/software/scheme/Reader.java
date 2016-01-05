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


public enum Reader {
    ;

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
            //noinspection IfStatementWithTooManyBranches
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
        //noinspection LoopConditionNotUpdatedInsideLoop
        while (iterator.hasNext()) {
            builder.addAll(parseSequence(iterator));
        }

        return builder.build();
    }

    public static int countOpenParens(String input) {
        return tokenize(input).stream()
                .filter(t -> "(".equals(t) || ")".equals(t))
                .map(t -> "(".equals(t) ? 1 : -1)
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
                result = "(".equals(nextToken) ? add(result, ListExpression.list(SymbolExpression.symbol("quote"), ListExpression.list(parseSequence(i)))) : add(result, ListExpression.list(SymbolExpression.symbol("quote"), symbolOrNumber(nextToken)));
                continue;
            }
            if (")".equals(token)) {
                return result;
            }

            result = "(".equals(token) ? add(result, ListExpression.list(parseSequence(i))) : add(result, symbolOrNumber(token));
        }

        return result;
    }

    private static <T> Cons<T> add(Cons<T> list, T t) {
        if (list.isEmpty()) {
            return cons(t, empty());
        }
        list.append(cons(t, empty()));

        return list;
    }

    private static Expression symbolOrNumber(String token) {
        //noinspection IfStatementWithTooManyBranches
        if (token.length() > 0 && token.charAt(0) == '\"') {
            return StringExpression.string(token.substring(1, token.length() - 1));
        } else if ("#t".equals(token) || "true".equals(token)) {
            return BooleanExpression.bool(true);
        } else if ("#f".equals(token) || "false".equals(token)) {
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

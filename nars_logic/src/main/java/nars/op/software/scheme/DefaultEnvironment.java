package nars.op.software.scheme;

import com.google.common.collect.ImmutableMap;
import nars.op.software.scheme.cons.Cons;
import nars.op.software.scheme.expressions.Expression;
import nars.op.software.scheme.expressions.NumberExpression;
import nars.op.software.scheme.expressions.SymbolExpression;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static nars.op.software.scheme.Evaluator.evaluate;
import static nars.op.software.scheme.Reader.read;
import static nars.op.software.scheme.cons.Cons.cons;
import static nars.op.software.scheme.cons.Cons.empty;
import static nars.op.software.scheme.expressions.BooleanExpression.bool;
import static nars.op.software.scheme.expressions.ListExpression.Nil.nil;
import static nars.op.software.scheme.expressions.ListExpression.list;
import static nars.op.software.scheme.expressions.ProcedureExpression.procedure;
import static nars.op.software.scheme.expressions.SymbolExpression.symbol;


public enum DefaultEnvironment {
    ;
    public static final ImmutableMap<SymbolExpression, Expression> PRIMITIVES = ImmutableMap.<SymbolExpression, Expression>builder()
            .put(symbol("+"),
                    procedure(args -> longFunction(args, (a, b) -> a + b)))
            .put(symbol("-"),
                    procedure(args -> longFunction(args, (a, b) -> a - b)))
            .put(symbol("/"),
                    procedure(args -> longFunction(args, (a, b) -> a / b)))
            .put(symbol("*"),
                    procedure(args -> longFunction(args, (a, b) -> a * b)))
            .put(symbol("="),
                    procedure(args -> bool(satisfiesTransitivePredicateGeneric(args, Object::equals))))
            .put(symbol("eq?"),
                    procedure(args -> bool(satisfiesTransitivePredicateGeneric(args, Object::equals))))
            .put(symbol(">"),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a > b))))
            .put(symbol("<"),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a < b))))
            .put(symbol(">="),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a >= b))))
            .put(symbol("<="),
                    procedure(args -> bool(satisfiesTransitivePredicate(args, (a, b) -> a <= b))))
            .put(symbol("not"),
                    procedure(args -> bool(!args.car().bool().value)))
            .put(symbol("display"),
                    procedure(args -> {
                        Repl.OUTPUT_STREAM.print(args.stream().map(Expression::print).collect(Collectors.joining(" ")));
                        return Expression.none();
                    }))
            .put(symbol("boolean?"),
                    procedure(args -> bool(args.car().isBoolean())))
            .put(symbol("list?"),
                    procedure(args -> bool(args.car().isList())))
            .put(symbol("number?"),
                    procedure(args -> bool(args.car().isNumber())))
            .put(symbol("procedure?"),
                    procedure(args -> bool(args.car().isProcedure())))
            .put(symbol("string?"),
                    procedure(args -> bool(args.car().isString())))
            .put(symbol("symbol?"),
                    procedure(args -> bool(args.car().isSymbol())))
            .put(symbol("null?"),
                    procedure(args -> bool(args.car().isList() && args.car().list().value.isEmpty())))
            .put(symbol("apply"),
                    procedure(args -> args.car().procedure().apply(args.cadr().list().value)))
            .put(symbol("car"),
                    procedure(args -> args.car().list().value.car()))
            .put(symbol("cdr"),
                    procedure(args -> list(args.car().list().value.cdr())))
            .put(symbol("cons"),
                    procedure(args -> list(toList(args))))
            .put(symbol("set-car!"),
                    procedure(args -> {
                        args.car().list().value.setCar(args.cadr());
                        return Expression.none();
                    }))
            .put(symbol("set-cdr!"),
                    procedure(args -> {
                        args.car().list().value.setCdr(args.cadr().list().value);
                        return Expression.none();
                    }))
            .put(symbol("read"),
                    procedure(args ->
                        read(args.isEmpty() ? readLine() : args.car().print()).iterator().next()))
            .put(symbol("error"),
                    procedure(args -> {
                        Repl.OUTPUT_STREAM.println(args.stream().map(Expression::print).collect(Collectors.joining(" ")));
                        return Expression.none();
                    }))
            .put(symbol("eval"),
                    procedure(args -> evaluate(args.car(), Repl.ENV)))
            .put(symbol("load"),
                    procedure(args -> load(loadFile(args.car().print()), Repl.ENV)))
            .build();

    public static Expression load(String s, SchemeClosure env) {
        return StreamSupport.stream(read(s).spliterator(), false)
                .map(e -> evaluate(e, env))
                .reduce(Expression.none(), (e1, e2) -> e2);
    }

    private static String readLine() {
        try {
            InputStreamReader reader = new InputStreamReader(Repl.INPUT_STREAM);
            StringBuilder sb = new StringBuilder();
            for (int c = reader.read(); c != -1 && c != '\n' ; c = reader.read()) {
                sb.append((char) c);
            }

            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String loadFile(String print) {
        try {
            return Files.lines(Paths.get(print))
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Cons<Expression> toList(Cons<Expression> args) {
        if (args.cadr() == nil()) {
            return cons(args.car(), empty());
        }

        return cons(args.car(), args.cadr().list().value);
    }

    private static boolean satisfiesTransitivePredicate(Cons<Expression> args, BiPredicate<Long, Long> predicate) {
        Iterator<Expression> iterator = args.iterator();
        return args.stream()
                .skip(1)
                .allMatch(e -> predicate.test(iterator.next().number().value, e.number().value));
    }

    private static boolean satisfiesTransitivePredicateGeneric(Cons<Expression> args, BiPredicate<Expression, Expression> predicate) {
        Iterator<Expression> iterator = args.iterator();
        return args.stream()
                .skip(1)
                .allMatch(e -> predicate.test(iterator.next(), e));
    }

    public static SchemeClosure newInstance() {
        return new SchemeClosure(new HashMap<>(PRIMITIVES));
    }

    private static NumberExpression longFunction(Cons<Expression> args, BinaryOperator<Long> accumulator) {
        return args.stream()
                .map(a -> a.number().value)
                .reduce(accumulator)
                .map(NumberExpression::number)
                .get();
    }
}

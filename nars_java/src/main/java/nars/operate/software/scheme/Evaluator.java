package nars.operate.software.scheme;

import com.google.common.collect.ImmutableSet;
import nars.operate.software.scheme.cons.Cons;
import nars.operate.software.scheme.expressions.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nars.operate.software.scheme.expressions.ListExpression.list;
import static nars.operate.software.scheme.expressions.SymbolExpression.symbol;


public class Evaluator {

    public static Expression evaluate(Expression exp, Environment env) {
        return analyze(exp).apply(env);
    }

    public static Function<Environment, Expression> analyze(Expression exp) {
        if (isSelfEvaluating(exp)) {
            return e -> exp;
        } else if (exp.isSymbol()) {
            return env -> env.lookup(exp.symbol());
        } else if (isSpecialForm(exp)) {
            return analyzeSpecialForm(exp.list());
        } else if (isFunctionCall(exp)) {
            return analyzeFunctionCall(exp.list());
        }

        throw new IllegalArgumentException(String.format("Unable to evaluate expression '%s'", exp));
    }

    private static Function<Environment, Expression> analyzeSpecialForm(ListExpression exp) {
        Cons<Expression> exps = exp.value;
        switch (exps.car().symbol().value) {
            case "quote":
                return analyzeQuote(exps);
            case "set!":
                return analyzeSet(exps);
            case "define":
                if (isVarDefinition(exps)) {
                    return analyzeVarDefinition(exps);
                } else {
                    return analyzeFunctionDefinition(exps);
                }
            case "if":
                return analyzeIf(exps);
            case "lambda":
                return analyzeLambda(exps);
            case "begin":
                return analyzeBegin(exps);
            case "let":
                return analyzeLet(exps);
            case "cond":
                return analyzeCond(exps);
        }

        throw new IllegalArgumentException(String.format("Invalid special form expression '%s'", exp));

    }

    private static Function<Environment, Expression> analyzeQuote(Cons<Expression> exps) {
        return env -> exps.cadr();
    }

    private static Function<Environment, Expression> analyzeLambda(Cons<Expression> exps) {
        Cons<SymbolExpression> paramNames = exps.cadr().list().value.stream()
                .map(Expression::symbol)
                .collect(Cons.collector());
        return analyzeProcedure(paramNames, exps);
    }

    private static Function<Environment, Expression> analyzeSet(Cons<Expression> exps) {
        SymbolExpression symbol = exps.cadr().symbol();
        Function<Environment, Expression> valueProc = analyze(exps.cdr().cadr());

        return env -> {
            env.set(symbol, valueProc.apply(env));
            return Expression.none();
        };
    }

    private static Function<Environment, Expression> analyzeFunctionDefinition(Cons<Expression> exps) {
        SymbolExpression name = exps.cadr().list().value.car().symbol();
        Cons<SymbolExpression> paramNames = exps.cadr().list().value.cdr().stream()
                .map(Expression::symbol)
                .collect(Cons.collector());
        Function<Environment, Expression> lambda = analyzeProcedure(paramNames, exps);
        return env -> {
            env.define(name, lambda.apply(env));
            return Expression.none();
        };
    }

    private static Function<Environment, Expression> analyzeVarDefinition(Cons<Expression> exps) {
        SymbolExpression symbol = exps.cadr().symbol();
        Function<Environment, Expression> valueProc = analyze(exps.cdr().cadr());
        return env -> {
            env.define(symbol, valueProc.apply(env));
            return Expression.none();
        };
    }

    private static boolean isVarDefinition(Cons<Expression> exps) {
        return exps.cadr().isSymbol();
    }

    private static Function<Environment, Expression> analyzeFunctionCall(ListExpression exp) {
        List<Function<Environment, Expression>> map = exp.value.stream()
                .map(Evaluator::analyze)
                .collect(Collectors.toList());

        return env -> {
            Cons<Expression> list = map.stream()
                    .map(e -> e.apply(env))
                    .collect(Cons.collector());
            return list.car().procedure().lambda.apply(list.cdr());
        };
    }

    private static Function<Environment, Expression> analyzeLet(Cons<Expression> exps) {
        List<Function<Environment, Expression>> letBindingValues = letBindingValues(exps);
        Function<Environment, Expression> letBody = analyzeProcedure(letBindingSymbols(exps), exps);

        return env -> {
            Cons<Expression> letParams = letBindingValues.stream()
                    .map(a -> a.apply(env))
                    .collect(Cons.collector());
            return letBody.apply(env).procedure().lambda.apply(letParams);
        };
    }

    private static Cons<SymbolExpression> letBindingSymbols(Cons<Expression> exps) {
        return exps.cadr().list().value.stream()
                .map(e -> e.list().value.car().symbol())
                .collect(Cons.collector());
    }

    private static List<Function<Environment, Expression>> letBindingValues(Cons<Expression> exps) {
        return exps.cadr().list().value.stream()
                .map(e -> e.list().value.cadr())
                .map(Evaluator::analyze)
                .collect(Collectors.toList());
    }

    private static Function<Environment, Expression> analyzeBegin(Cons<Expression> exps) {
        return analyzeSequence(exps.cdr());
    }

    private static Function<Environment, Expression> analyzeSequence(Cons<Expression> exps) {
        List<Function<Environment, Expression>> seq = exps.stream()
                .map(Evaluator::analyze)
                .collect(Collectors.toList());

        return env -> seq.stream()
                .collect(Collectors.reducing(Expression.none(), a -> a.apply(env), (a, b) -> b));
    }

    private static Function<Environment, Expression> analyzeCond(Cons<Expression> exps) {
        return condToIf(exps.cdr());
    }

    private static Function<Environment, Expression> condToIf(Cons<Expression> exps) {
        if (exps.isEmpty()) {
            return e -> BooleanExpression.bool(false);
        } else if (exps.size() == 1) {
            Function<Environment, Expression> condition = analyze(exps.car().list().value.car());
            Function<Environment, Expression> consequent = analyze(exps.car().list().value.cadr());
            Optional<Function<Environment, Expression>> alternative = Optional.empty();

            return makeIf(condition, consequent, alternative);
        } else {
            if (exps.cadr().list().value.car().equals(symbol("else"))) {
                Function<Environment, Expression> condition = analyze(exps.car().list().value.car());
                Function<Environment, Expression> consequent = analyze(exps.car().list().value.cadr());
                Optional<Function<Environment, Expression>> alternative = Optional.of(analyze(exps.cadr().list().value.cadr()));
                return makeIf(condition, consequent, alternative);
            } else {
                Function<Environment, Expression> condition = analyze(exps.car().list().value.car());
                Function<Environment, Expression> consequent = analyze(exps.car().list().value.cadr());
                Optional<Function<Environment, Expression>> alternative = Optional.of(condToIf(exps.cdr()));

                return makeIf(condition, consequent, alternative);
            }
        }
    }

    private static Function<Environment, Expression> makeIf(Function<Environment, Expression> condition, Function<Environment, Expression> consequent, Optional<Function<Environment, Expression>> alternative) {
        return env -> {
            if (isTruthy(condition.apply(env))) {
                return consequent.apply(env);
            } else {
                return alternative.map(a -> a.apply(env)).orElse(Expression.none());
            }
        };
    }

    private static Function<Environment, Expression> analyzeIf(Cons<Expression> exps) {
        Function<Environment, Expression> condition = analyze(exps.cadr());
        Function<Environment, Expression> consequent = analyze(exps.cdr().cadr());
        Optional<Function<Environment, Expression>> alternative = exps.size() > 3 ? Optional.of(analyze(exps.cdr().cdr().cadr())) : Optional.empty();
        return makeIf(condition, consequent, alternative);
    }

    private static Function<Environment, Expression> analyzeProcedure(Cons<SymbolExpression> names, Cons<Expression> exps) {
        Function<Environment, Expression> body = analyzeSequence(exps.cdr().cdr());
        return env ->
                ProcedureExpression.procedure(args ->
                        body.apply(env.extend(makeMap(names, args, new LinkedHashMap<>()))));
    }

    private static Map<SymbolExpression, Expression> makeMap(Cons<SymbolExpression> names, Cons<Expression> args, Map<SymbolExpression, Expression> map) {
        if (names.isEmpty()) {
            return map;
        } else if (names.car().equals(symbol("."))) {
            map.put(names.cadr(), list(args));
            return map;
        } else {
            map.put(names.car(), args.car());
            return makeMap(names.cdr(), args.cdr(), map);
        }
    }

    private static boolean isSpecialForm(Expression exp) {
        return exp.isList() && exp.list().value.car().isSymbol()
                && SPECIAL_FORMS.contains(exp.list().value.car().symbol());
    }

    private static boolean isFunctionCall(Expression exp) {
        return exp.isList() && exp.list().value.size() > 0;
    }

    private static boolean isSelfEvaluating(Expression exp) {
        return exp.isNumber() || exp.isBoolean() || exp.isString() || exp == Expression.none() || (exp.isList() && exp.list().value.size() == 0);
    }

    private static boolean isTruthy(Expression exp) {
        return !BooleanExpression.bool(false).equals(exp);
    }

    private static final Set<SymbolExpression> SPECIAL_FORMS = ImmutableSet.of("quote", "set!", "define", "if", "lambda", "begin", "let", "cond").stream()
            .map(SymbolExpression::symbol)
            .collect(Collectors.toSet());
}

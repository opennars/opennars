package nars.op.software.scheme;

import java.io.Console;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

/**
 * Lispy: Small Scheme Interpreter in Java 8
 * from: https://forax.github.io/2014-06-01-e733e6af6114eff55149-lispy_in_java.html
 */
public enum Lispy {
    ;

    public interface Fun {
        Object apply(Object a);
    }

    public interface Fun2 {
        Object apply(Object a, Object b);
    }

    public interface FunAll {
        Object apply(Object[] args);
    }

    static MethodHandle mhRef(Class<?> type, String name) {
        try {
            return MethodHandles.publicLookup().unreflect(stream(type.getMethods()).filter(m -> m.getName().equals(name)).findFirst().get());
        } catch (IllegalAccessException e) {
            throw new Error(e);
        }
    }

    static <F> MethodHandle mh(Class<F> type, F fun) {
        return mhRef(type, "apply").bindTo(fun);
    }

    @SuppressWarnings("unchecked")
    static int compare(Object a, Object b) {
        return ((Comparable<Object>) a).compareTo(b);  // I hope Java will never have reified type
    }

    static List<?> list(Object o) {
        return (List<?>) o;
    }

    static String string(Object o) {
        return (String) o;
    }

    static double dbl(Object o) {
        return ((Number) o).doubleValue();
    }

    static BigInteger bigint(Object o) {
        return ((BigInteger) o);
    }

    static boolean isdbl(Object o) {
        return o instanceof Double;
    }

    static class Env {
        final HashMap<String, Object> dict = new HashMap<>();
        private final Env outer;

        Env(Env outer) {
            this.outer = outer;
        }

        Env find(String var) {
            return dict.containsKey(var) ? this : outer.find(var);
        }

        Env add(String var, Object value) {
            dict.put(var, value);
            return this;
        }

        Env addAll(List<?> vars, List<?> values) {
            range(0, vars.size()).forEach(i -> add(string(vars.get(i)), values.get(i)));
            return this;
        }
    }

    static Env globalEnv() {
        return new Env(null)
                .add("+", mh(Fun2.class, (a, b) -> (isdbl(a) || isdbl(b)) ? dbl(a) + dbl(b) : bigint(a).add(bigint(b))))
                .add("-", mh(Fun2.class, (a, b) -> (isdbl(a) || isdbl(b)) ? dbl(a) - dbl(b) : bigint(a).subtract(bigint(b))))
                .add("*", mh(Fun2.class, (a, b) -> (isdbl(a) || isdbl(b)) ? dbl(a) * dbl(b) : bigint(a).multiply(bigint(b))))
                .add("/", mh(Fun2.class, (a, b) -> (isdbl(a) || isdbl(b)) ? dbl(a) / dbl(b) : bigint(a).divide(bigint(b))))
                .add("<", mh(Fun2.class, (a, b) -> compare(a, b) < 0))
                .add("<=", mh(Fun2.class, (a, b) -> compare(a, b) <= 0))
                .add(">", mh(Fun2.class, (a, b) -> compare(a, b) > 0))
                .add(">=", mh(Fun2.class, (a, b) -> compare(a, b) >= 0))
                .add("=", mhRef(Object.class, "equals"))
                .add("equal?", mhRef(Object.class, "equals"))
                .add("eq?", mh(Fun2.class, (o, c) -> (((Class<?>) c).isInstance(o))))
                .add("length", mhRef(List.class, "size"))
                .add("cons", mh(Fun2.class, (a, l) -> concat(of(a), list(l).stream()).collect(toList())))
                .add("car", mh(Fun.class, l -> list(l).get(0)))
                .add("cdr", mh(Fun.class, l -> list(l).subList(1, list(l).size())))
                .add("append", mh(Fun2.class, (l, m) -> concat(list(l).stream(), list(m).stream()).collect(toList())))
                .add("list", mh(FunAll.class, args -> asList(args)).asVarargsCollector(Object[].class))
                .add("list?", mh(Fun.class, l -> l instanceof List))
                .add("null?", mhRef(List.class, "isEmpty"))
                .add("symbol?", mh(Fun.class, a -> a instanceof String));
    }

    static Object eval(Object x, Env env) {
        if (x instanceof String) {             // variable reference
            return env.find(string(x)).dict.get(x);
        }
        if (!(x instanceof List)) {            // constant
            return x;
        }
        List<?> l = (List<?>) x;
        String var;
        Object exp, cmd = l.get(0);
        if (cmd instanceof String) {
            switch (string(l.get(0))) {
                case "quote":                        // (quote exp)
                    return l.get(1);
                case "if":                           // (if test conseq alt)
                    return eval(((Boolean) eval(l.get(1), env)) ? l.get(2) : l.get(3), env);
                case "set!":                         // (set! var exp)
                    var = string(l.get(1));
                    env.find(var).add(var, eval(l.get(2), env));
                    return null;
                case "define":                       // (define var exp)
                    var = string(l.get(1));
                    env.add(var, eval(l.get(2), env));
                    return null;
                case "lambda":                       // (lambda (vars) exp)
                    List<?> vars = list(l.get(1));
                    exp = l.get(2);
                    return mh(FunAll.class, args -> eval(exp, new Env(env).addAll(vars, asList(args)))).asCollector(Object[].class, vars.size());
                case "begin":                        // (begin exp*)
                    return l.stream().skip(1).reduce(null, (val, e) -> eval(e, env), (__1, __2) -> null);
                default:
            }
        }
        List<?> exprs = l.stream().map(expr -> eval(expr, env)).collect(toList());
        MethodHandle proc = (MethodHandle) exprs.get(0);
        try {
            return proc.invokeWithArguments(exprs.subList(1, exprs.size()));
        } catch (Throwable e) {
            if (e instanceof Error) throw (Error) e;
            throw new Error(e);
        }
    }

    static Object parse(String s) {
        return readFrom(tokenize(s));
    }

    static Queue<String> tokenize(String text) {
        return stream(text.replace("(", "( ").replace(")", " )").split(" ")).filter(s -> !s.isEmpty()).collect(toCollection(ArrayDeque::new));
    }

    static Object readFrom(Queue<String> tokens) {
        if (tokens.isEmpty()) throw new Error("unexpected EOF while reading");
        String token = tokens.poll();
        if ("(".equals(token)) {
            ArrayList<Object> l = new ArrayList<>();
            while (!tokens.peek().equals(")")) {
                l.add(readFrom(tokens));
            }
            tokens.poll();   // pop of ")"
            return l;
        }
        if (")".equals(token)) {
            return new Error("unexpected ')'");
        }
        return atom(token);
    }

    static Object atom(String token) {
        try {
            return new BigInteger(token);
        } catch (NumberFormatException __) {
            try {
                return Double.parseDouble(token);
            } catch (NumberFormatException ___) {
                return token;
            }
        }
    }

    static String toString(Object val) {
        return (val instanceof List) ? list(val).stream().map(Lispy::toString).collect(joining(" ", "(", ")")) : String.valueOf(val);
    }

    static void repl() {
        Console console = System.console();
        Env env = globalEnv();
        for (; ; ) {
            Object val = eval(parse(console.readLine("lispy> ")), env);
            if (val != null) {
                System.out.println(toString(val));
            }
        }
    }

    public static void main(String[] args) {
        repl();
    }
}
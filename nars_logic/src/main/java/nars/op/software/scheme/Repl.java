package nars.op.software.scheme;


import nars.op.software.scheme.expressions.Expression;

import java.io.*;
import java.util.stream.IntStream;


public enum Repl {
    ;
    public static final SchemeClosure ENV = DefaultEnvironment.newInstance();
    public static final PrintStream OUTPUT_STREAM = System.out;
    public static final InputStream INPUT_STREAM = System.in;

    public static void main(String[] args) {
        repl(INPUT_STREAM, OUTPUT_STREAM);
    }

    private static void repl(InputStream in, PrintStream out) {
        repl(in, out, ENV);
    }

    public static void repl(InputStream in, PrintStream out, SchemeClosure env) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        String input = "";

        while (true) {
            prompt(out, Reader.countOpenParens(input));

            try {
                String l = readLine(bufferedReader);
                if (l == null) break;
                input += l;
                if (completeExpression(input)) {
                    Reader.read(input).stream()
                            .forEach(e -> out.print(print(Evaluator.evaluate(e, env))));

                    input = "";
                }

            } catch (Throwable t) {
                out.println("Error: " + t.getMessage());
                input = "";
            }
        }
    }

    private static String readLine(BufferedReader bufferedReader) throws IOException {
        String line = "";
        do {
            String l = bufferedReader.readLine();
            if (l == null) return null;
            line += l + '\n';
        } while (bufferedReader.ready());
        return line;
    }

    private static boolean completeExpression(String input) {
        return Reader.countOpenParens(input) == 0;
    }

    private static void prompt(PrintStream out, int openParenCount) {
        if (openParenCount == 0) {
            out.print(" > ");
        } else {
            indent(out, openParenCount);
        }
    }

    private static void indent(PrintStream out, int openParenCount) {
        out.print("... ");
        IntStream.range(0, openParenCount).forEach(i -> out.print("  "));
    }

    private static String print(Expression expression) {
        String exp = expression.print();
        if (exp.isEmpty()) {
            return "";
        }
        return String.format("-> %s\n", exp);
    }
}

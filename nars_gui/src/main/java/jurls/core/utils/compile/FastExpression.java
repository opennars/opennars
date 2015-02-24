/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.utils.compile;

import jurls.core.approximation.DiffableExpression;
import jurls.core.approximation.UnaryDoubleFunction;
import org.apache.commons.math3.stat.Frequency;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.UnitCompiler;
import org.codehaus.janino.util.ClassFile;

import java.io.ByteArrayInputStream;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * https://raw.githubusercontent.com/cdman/fast-java-expr-eval/master/src/main/java/com/blogspot/hypefree/fastexpr/JaninoFastexpr.java
 */
public final class FastExpression {

    private final static AtomicLong COMPILED_CLASS_INDEX = new AtomicLong();

    boolean debug = false;

    private final static class JaninoRestrictedClassLoader extends
            SecureClassLoader {

        Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length, new ProtectionDomain(null,
                    new Permissions(), this, null));
        }
    }

    public UnaryDoubleFunction compile(String expression) throws Exception {
        if (!java.util.regex.Pattern.matches(
                "^[a-zA-Z0-9+\\-()/\\* \t^%\\.\\?]+$", expression)) {
            throw new SecurityException();
        }

        String classPackage = getClass().getPackage().getName() + ".compiled";
        String className = "JaninoCompiledFastexpr"
                + COMPILED_CLASS_INDEX.incrementAndGet();

        //TODO use StringBuilder
        String source = "package " + classPackage + ";\n"
                + "import static java.lang.Math.*;\n" + "public final class "
                + className + " implements "
                + UnaryDoubleFunction.class.getCanonicalName() + " {\n"
                + "public double evaluate(double x) {\n"
                + "return (" + expression + ");\n" + "}\n" + "}";

        Scanner scanner = new Scanner(null, new ByteArrayInputStream(
                source.getBytes("UTF-8")), "UTF-8");

        JaninoRestrictedClassLoader cl = new JaninoRestrictedClassLoader();
        UnitCompiler unitCompiler = new UnitCompiler(
                new Parser(scanner).parseCompilationUnit(),
                new ClassLoaderIClassLoader(cl));
        ClassFile[] classFiles = unitCompiler.compileUnit(debug, debug, debug);
        Class<?> clazz = cl.defineClass(classPackage + "." + className,
                classFiles[0].toByteArray());

        return (UnaryDoubleFunction) clazz.newInstance();
    }

    @Deprecated
    public static String eliminateSubexpressions(String expression, List<String> cache, Set<String> replaced, int minSize, int maxSize) {
        //find common subexpressions
        Map<String, Integer> r = repeatingSubstrings(expression, minSize, maxSize);
        //sort by largest

        Set<String> s = new TreeSet(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return Integer.compare(o2.length(), o1.length());
            }

        });
        s.addAll(r.keySet());

        for (String k : s) {
            if (replaced.contains(k)) {
                continue;//dont reapply prevoius
            }
            final int ci = cache.size();
            String e2 = expression.replace(k, "(c" + ci + ')');

            if (!e2.equals(expression)) {
                expression = e2;
                cache.add("final double c" + ci + "=" + k + "; //#" + r.get(k) + '\n');
            }
            replaced.add(k);
        }
        return expression;
    }

    //final static Pattern paren = Pattern.compile("(\\((?>[^()]+|)*\\))", Pattern.DOTALL);
    //Pattern paren = Pattern.compile("\\((.*?)\\)",Pattern.DOTALL); <- almost works
    public static void getParens(String s, Collection<String> result, Frequency f) {

        final int minSize = 7;
        final int maxSize = 64; //auto-calculate this based on the input

        ArrayList<String> x = psplit(s);
        for (String p : x) {
            final int l = p.length();
            if ((l < minSize) || (l > maxSize)) {
                continue;
            }
            result.add(p);
            f.addValue(p);
        }

    }
    
    @Deprecated public static String simplify1(String expression) {
        //        expression = expression.replace("(1)", "1"); //HACK remove useless leading "1" factors
//        expression = expression.replace("(1*", "("); //HACK remove useless leading "1" factors
//        expression = expression.replace("(1)*", "");
//        expression = expression.replace("((-1*(", "(-1*(("); //pull negative out, allowing internal to match better
//
//        expression = expression.replaceAll("\\(\\(([^)]*)\\)\\)", "\\($1\\)");  //remove duplicate parens
        return null;
    }
    
//    public static String simplify(String expression) {
//        String[] l = expression.split(";");
//        StringBuilder result = new StringBuilder(expression.length());
//        for (String e : l) {
//
//            e = e.trim();
//            if (e.isEmpty()) continue;
//
//            String[] parts = e.split(" \\= ");
//            if (parts.length!=2) continue;
//
//            String prefix = parts[0];
//            String exp = parts[1];
//            try {
//                exp = Algebra.simplify(exp);
//                String newLine = prefix + " = " + exp + ";\n";
//                result.append(newLine);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                result.append(e);
//            }
//        }
//        return result.toString();
//    }

    public static String reduce(String expression, List<String> varCache) {
        //1. create a list of all parenthases subterms, sorted by size

        //2. substitute each one beginning with the largest
        //preprocess:

        
        
        Frequency f = new Frequency();
        Set<String> subterms = new HashSet();
        final Comparator<String> scorer = new Comparator<String>() {

            public long score(final String s) {
                return s.length() * f.getCount(s);
            }

            @Override
            public int compare(String o1, String o2) {
                int i = Long.compare(score(o2), score(o1));
                if (i == 0) {
                    return o1.compareTo(o2);
                }
                return i;
            }

        };

        getParens(expression, subterms, f);

        List<String> sl = new ArrayList(subterms);
        Collections.sort(sl, scorer);
        
        

        for (String k : sl) {
            final long repeats = f.getCount(k);

            final int ci = varCache.size();
            String e2 = expression.replace(k, "(c" + ci + ')');

            if (!e2.equals(expression)) {
                expression = e2;
                varCache.add("final double c" + ci + "=" + k + "; //#" + repeats + '\n');
            }
        }

        //expression = simplify(expression);
        

        return expression;
    }

    @Deprecated
    public static String reduce1(String expression, List<String> cache) {

        Set<String> replaced = new HashSet();

        //7 = variables
        for (int maxLen = 7, min = 5; maxLen < 256; maxLen += 24, min += 2) {
            expression = expression.replace("(1)", "1"); //HACK remove useless leading "1" factors
            expression = expression.replace("(1*", "("); //HACK remove useless leading "1" factors
            expression = expression.replace("(1)*", "");
            expression = expression.replace("((-1*(", "(-1*(("); //pull negative out, allowing internal to match better

            expression = expression.replaceAll("\\(\\(([^)]*)\\)\\)", "\\($1\\)");  //remove duplicate parens

            //"\\(\\(x\\)\\*y\\*z\\)"
            //expression = expression.replaceAll("\\(\\(([^)]*)\\)\\*([^)]*)\\*([^)]*)\\)", "\\($1\\*$2\\*$3\\)");  //mult transitivity
            //expression = expression.replaceAll("\\(([^)]*)\\*\\(([^)]*)\\*([^)]*)\\)\\)", "\\($1\\*$2\\*$3\\)");  //mult transitivity
            expression = eliminateSubexpressions(expression, cache, replaced, min, maxLen);
        }

        return expression;
    }

    public DiffableExpression compileGradientExpression(String expression) throws Exception {

        System.out.println("Compiling..");
        List<String> varCache = new ArrayList();

        int maxReductions = 4;
        boolean changed = true;
        do {
            System.out.println("Reducing..");
            String e2 = reduce(expression, varCache);
            if (e2.equals(expression)) {
                changed = false; 
            }
            else {
                expression = e2;
            }
            maxReductions--;
        } while (changed && maxReductions > 0) ;


        String classPackage = getClass().getPackage().getName() + ".compiled";
        String className = "JaninoCompiledFastexpr"
                + COMPILED_CLASS_INDEX.incrementAndGet();

        //TODO use StringBuilder
        String head = "package " + classPackage + ";\n"
                + "import static java.lang.Math.*;\n"
                + "import jurls.core.approximation.Scalar;\n"
                + "public final class "
                + className + " implements "
                + DiffableExpression.class.getCanonicalName() + " {\n"
                + "public void update(final double[] i, final double[] a, final double[] output) {\n";

        StringBuilder sb = new StringBuilder(head);
        for (String s : varCache) {
            sb.append(s);
        }

        sb.append(expression).append("\n" + "}\n" + "}");

        String source = sb.toString();

        Scanner scanner = new Scanner(null, new ByteArrayInputStream(
                source.getBytes("UTF-8")), "UTF-8");

        System.out.println(source);

        JaninoRestrictedClassLoader cl = new JaninoRestrictedClassLoader();
        UnitCompiler unitCompiler = new UnitCompiler(
                new Parser(scanner).parseCompilationUnit(),
                new ClassLoaderIClassLoader(cl));

        ClassFile[] classFiles = unitCompiler.compileUnit(debug, debug, debug);
        Class<?> clazz = cl.defineClass(classPackage + "." + className,
                classFiles[0].toByteArray());

        return (DiffableExpression) clazz.newInstance();

    }

    @Deprecated public static int commonPrefix(final String string, int x, int y) {
        final int l = string.length();
        final int oy = y;

        int n = 0;
        while (x < oy && y < l && string.charAt(x++) == string.charAt(y++)) {
            n++;
        }
        return n;
    }

    public static int occurences(String str, String needle, int afterX) {
        int count = 1; //1 for the existing one known
        int lastIndex = afterX;
        while ((lastIndex = str.indexOf(needle, lastIndex)) != -1) {
            count++;
            lastIndex++;
        }
        return count;
    }

    @Deprecated public static Map<String, Integer> repeatingSubstrings(
            final String string, final int minLength, final int maxLength) {

        final int l = string.length();
        //int fl = minLength;

        Map<String, Integer> s = new HashMap();

        for (int x = 0; x < l - minLength; x++) {

            if (string.charAt(x) != '(') {
                continue;
            }

                //fl = minLength;
            for (int y = x + 1; y < l - minLength; y++) {

                int n = commonPrefix(string, x, y);

                if ((n > minLength) && (n <= maxLength)) {
                    String v = validSubstring(string, x, x + n, minLength);
                    if (v != null && !s.containsKey(v)) {
                        int count = occurences(string, v, x);
                        if (count > 1) {
                            s.put(v, count);
                            x += v.length();
                            break;
                        }
                    }
                }

            }
        }

        return s;
    }

    public static String validSubstring(String s, int start, int end, int minLen) {
        if (s.isEmpty()) {
            return null;
        }
        int leftParen = 0, rightParen = 0;

        if (s.charAt(start) != '(') {
            return null;
        }
        if (s.charAt(end - 1) != ')') {
            return null;
        }

        int i = start;
        int cut = end;
        while (i < end) {

            final char c = s.charAt(i);
            if (c == '(') {
                leftParen++;
            } else if (c == ')') {
                rightParen++;
                if (rightParen == leftParen) {
                    cut = i + 1;
                    break;
                }
            }
            i++;
        }
        if (cut - start < minLen) {
            return null; //too short
        }
        if ((leftParen > 0) && (leftParen == rightParen)) {
            return s.substring(start, cut);
        }
        return null;
    }

    /*
     @Deprecated public static String validSubstring(String s) {
     if (s.isEmpty()) return null;
     int leftParen = 0, rightParen = 0;

     final int len = s.length();

     if (s.charAt(0)!='(') return null;
     if (s.charAt(len-1)!=')') return null;
        
     int i = 0;
     int cut = len;
     while (i < len) {
            
     final char c = s.charAt(i);
     if (c=='(')
     leftParen++;
     else if (c==')') {                
     rightParen++;
     if (rightParen == leftParen) {
     cut = i+1;
     break;
     }
     }
     i++;
     }
        
     if ((leftParen > 0) && (leftParen == rightParen)) {
     return s.substring(0, cut);
     }
     return null;
     }*/
// Standard set of braces.
    private static final String openBraces = "("; //"({[<";
// Matching close set.
    private static final String closeBraces = ")"; //")}]>";

    public static ArrayList<String> psplit(String s) {
        // Default to splitting with my standard set of braces.
        return split(s, openBraces, closeBraces);
    }

// Holds the start of an element and which brace started it.
    private static class Start {

        // The brace number from the braces string in use.

        final int brace;
        // The position in the string it was seen.
        final int pos;

        // Constructor.
        public Start(int brace, int pos) {
            this.brace = brace;
            this.pos = pos;
        }

        @Override
        public String toString() {
            return "{" + openBraces.charAt(brace) + "," + pos + "}";
        }
    }

    public static ArrayList<String> split(String s, String open, String close) {
        // The splits.
        ArrayList<String> split = new ArrayList<String>();
        // The stack.
        ArrayList<Start> stack = new ArrayList<Start>();
        // Walk the string.
        for (int i = 0; i < s.length(); i++) {
            // Get the char there.
            char ch = s.charAt(i);
            // Is it an open brace?
            int o = open.indexOf(ch);
            // Is it a close brace?
            int c = close.indexOf(ch);
            if (o >= 0) {
                // Its an open! Push it.
                stack.add(new Start(o, i));
            } else if (c >= 0 && stack.size() > 0) {
                // Pop (if matches).
                int tosPos = stack.size() - 1;
                Start tos = stack.get(tosPos);
                // Does the brace match?
                if (tos.brace == c) {
                    // Matches!
                    split.add(s.substring(tos.pos, i + 1));
                    // Done with that one.
                    stack.remove(tosPos);
                }
            }
        }
        return split;
    }
}

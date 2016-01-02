//package langdon.math;
//
//import java.lang.reflect.Array;
//import java.io.InputStreamReader;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.text.ParseException;
//
//public class Simple {
//
//    private  boolean debug = true;
//    private  String prompt = ": ";
//
//    private  boolean debugAll = false;
//    private  String interfaceStyle = "interactive";
//    private  String inputFormat = "pretty";
//    private  String outputFormat = "pretty";
//    private  boolean dispSource = false;
//
//    public static void main(String[] args) throws IOException {
//        Simple simple = new Simple();
//        simple.useInput(args);
//    }
//
//    public void useInput(String[] args) throws IOException {
//
//        // System.err.println(Arrays.toString(args));
//        ArrayList<String> argStrs = new ArrayList<String>(Arrays.asList(args));
//        ArrayList<String> toSimplify = new ArrayList<String>();
//        int argOn = 0;
//        for (; argOn < argStrs.size(); argOn++) {
//            String arg = argStrs.get(argOn);
//            if (arg.length() == 0) throw new IllegalArgumentException("unknown option \"" + arg + "\"");
//            if (arg.charAt(0) != '-') throw new IllegalArgumentException("unknown option \"" + arg + "\"");
//            String option = arg.substring(1, arg.length());
//
//            if (option.length() != 0 && option.charAt(0) == '-') {
//                if (option.equals("-")) {
//                    argOn++;
//                    break;
//                }
//                else throw new IllegalArgumentException("unknown option \"" + arg + "\"");
//            }
//            else {
//                if (option.equals("d")) {
//                    debugAll = true;
//                    // interfaceStyle = "interactive";
//                }
//                else if (option.equals("D")) {
//                    debugAll = false;
//                }
//                else if (option.equals("p")) {
//                    interfaceStyle = "pipe";
//                }
//                else if (option.equals("P")) {
//                    interfaceStyle = "interactive";
//                }
//                else if (option.equals("i")) {
//                    argOn++;
//                    if (argOn < argStrs.size()) inputFormat = argStrs.get(argOn);
//                }
//                else if (option.equals("o")) {
//                    argOn++;
//                    if (argOn < argStrs.size()) outputFormat = argStrs.get(argOn);
//                }
//                else if (option.equals("e")) {
//                    argOn++;
//                    if (argOn < argStrs.size()) toSimplify.add(argStrs.get(argOn));
//                }
//                else {
//                    if (option.length() < 2) throw new IllegalArgumentException("unknown option \"" + arg + "\"");
//                    for (int i = option.length() - 1; i >= 0; i--) {
//                        argStrs.add(argOn + 1, "-" + option.charAt(i));
//                    }
//                }
//            }
//        }
//
//        for (; argOn < Array.getLength(args); argOn++) {
//            toSimplify.add(args[argOn]);
//        }
//
//        if (!debugAll) {
//            noDebug();
//            ExprParser.noDebug();
//            Expr.noDebug();
//        }
//
//        // System.err.println(toSimplify);
//        if (toSimplify.size() > 0) {
//            if (interfaceStyle.equals("interactive")) dispSource = true;
//            for (String str : toSimplify) {
//                simplify(str);
//            }
//        }
//        else {
//            // System.err.println("please argue about math");
//            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//            String input;
//
//            if (interfaceStyle.equals("interactive")) System.out.print(prompt);
//
//            while ((input = in.readLine()) != null) {
//                simplify(input);
//                if (interfaceStyle.equals("interactive")) System.out.print(prompt);
//            }
//        }
//    }
//
//    private void simplify(String input) {
//        Context context = new Context();
//        Expr expression;
//        try {
//            String[] splitInputA = input.split(";");
//            ArrayList<String> splitInput = new ArrayList<String>();
//            for (String string : splitInputA) {
//                if (!string.equals("")) splitInput.add(string);
//            }
//
//            String complex = splitInput.remove(splitInput.size() - 1);
//
//            for (String string : splitInput) {
////                 if (string.matches("^d[a-zA-Z]/d[a-zA-Z]=.*")) {
////                     // if (debug) System.err.println("yep");
////                     char dyC = string.charAt(1);
////                     char dxC = string.charAt(4);
////                     Var dy = context.getVar(dyC);
////                     Var dx = context.getVar(dxC);
////                     dy.derivrespected.put(dx, ExprParser.parse(string.substring(6, string.length()), context));
////                     // if (debug) System.err.println("d" + dyC + "/d" + dxC + "=" + Derivative.make(dy, dx));
////                 }
//                if (string.indexOf('=') != -1) {
//                    String exprS = string.substring(0, string.indexOf('='));
//                    String subS = string.substring(string.indexOf('=') + 1, string.length());
//                    try {
//                        Expr expr = parseExpr(exprS, context);
//                        try {
//                            Expr sub = parseExpr(subS, context);
//                            context.subs.put(expr, sub);
//                        } catch (Exception e) {
//                            System.err.println("\"" + subS + "\" could not be parsed:");
//                            if (e instanceof java.text.ParseException) {
//                                String parseMsg = ExprParser.generateParseMesg(subS, (java.text.ParseException) e);
//                                System.err.println("parse error: " + e.getMessage());
//                                if (parseMsg != null) System.err.println(parseMsg);
//                            }
////                             else if (e instanceof UnsupportedOperationException) {
////                                 System.err.println(e);
////                                 if (debug) e.printStackTrace();
////                             }
//                            else {
//                                System.err.println(e);
//                                e.printStackTrace();
//                            }
//                        }
//                    } catch (Exception e) {
//                        System.err.println("\"" + exprS + "\" could not be parsed:");
//                        if (e instanceof java.text.ParseException) {
//                            String parseMsg = ExprParser.generateParseMesg(exprS, (java.text.ParseException) e);
//                            System.err.println("parse error: " + e.getMessage());
//                            if (parseMsg != null) System.err.println(parseMsg);
//                        }
////                         else if (e instanceof UnsupportedOperationException) {
////                             System.err.println(e);
////                             if (debug) e.printStackTrace();
////                         }
//                        else {
//                            System.err.println(e);
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//
//            expression = parseExpr(complex, context);
//            if (debug) System.err.println("Simple.simplify: before substitution: " + expression);
//            expression = expression.copy(context.subs);
//            if (debug) System.err.println("Simple.simplify: after substitution:  " + expression);
//
//            System.out.println((interfaceStyle.equals("interactive")?(dispSource?input+" ":"")+"-> ":"") + expression.toString(outputFormat));
//
//        } catch (Exception e) {
//            System.err.println("\"" + input + "\" could not be parsed:");
//            if (e instanceof java.text.ParseException) {
//                String parseMsg = ExprParser.generateParseMesg(input, (java.text.ParseException) e);
//                System.err.println("parse error: " + e.getMessage());
//                if (parseMsg != null) System.err.println(parseMsg);
//            }
////             else if (e instanceof UnsupportedOperationException) {
////                 System.err.println(e);
////                 if (debug) e.printStackTrace();
////             }
//            else {
//                System.err.println(e);
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void noDebug() {
//        debug = false;
//    }
//
//    private Expr parseExpr(String string, Context context) throws ParseException {
//        if (inputFormat.equals("pretty")) return ExprParser.parseExpr(string, context);
//        if (inputFormat.equals("s-expr")) return ExprParser.parseSExprExpr(string, context);
//        else return ExprParser.parseExpr(string, context);
//    }
//
//}

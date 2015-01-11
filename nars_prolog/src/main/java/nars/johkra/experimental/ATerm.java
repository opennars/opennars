//package nars.johkra;
//
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import org.apache.commons.math3.util.Pair;
//
///**
// * experiment seeing if predicate can be a generic type
// */
//public class ATerm<P extends Object> extends Pair<P,List<ATerm>>{
//
////    private P pred;
////    private List<ATerm> args;
//
//    public ATerm(P p, List<ATerm> args) throws ParseException {
//        this(p, args, true);
//    }
//    public ATerm(P p, List<ATerm> args, boolean initialize) throws ParseException {
//        super(p, args);
//        
////        if (p == null) {
////            throw (new ParseException("Predicate mustn't be empty or null", -1));
////        }
//        
//        if (!initialize)
//            return;
//        
//        if (p instanceof String) {
//            String s = (String)p;
//            Map.Entry<String, List<String>> parts = null;
//            if ((args == null)) {
//                parts = Util.splitInfix(s);
//            }
//            if (args != null) {
//                pred = (P) s;
//                this.args = args;
//            } else if (parts != null) {
//                this.pred = (P) parts.getKey();
//                this.args = new ArrayList<ATerm>();
//                for (String str : parts.getValue()) {
//                    this.args.add(new ATerm(str, null));
//                }
//            } else if (s.charAt(s.length() - 1) == ']') {
//                List<String> flds = Util.split(s.substring(1, s.length() - 1), ",", true);
//                List<String> flds2 = Util.split(s.substring(1, s.length() - 1), "|", true);
//                if (flds2.size() > 1) {
//                    this.pred = (P) ".";
//                    this.args = new ArrayList<ATerm>();
//                    for (String str : flds2) {
//                        this.args.add(new ATerm(str, null));
//                    }
//                } else {
//                    ATerm l = new ATerm(".", null);
//                    for (int i = flds.size() - 1; i >= 0; i--) {
//                        List<ATerm> temp = new ArrayList<ATerm>();
//                        temp.add(new ATerm(flds.get(i), null));
//                        temp.add(l);
//                        l = new ATerm(".", temp);
//                    }
//                    this.pred = (P) l.pred;
//                    this.args = l.args;
//                }
//            } else if (s.charAt(s.length() - 1) == ')') {
//                List<String> flds = Util.split(s, "(", false);
//                if (flds.size() != 2) {
//                    throw new ParseException("Syntax error in term: '" + s + "'", -1);
//                }
//                this.pred = (P) flds.get(0);
//                this.args = new ArrayList<ATerm>();
//                for (String str : Util.split(flds.get(1).substring(0, flds.get(1).length() - 1), ",", true)) {
//                    this.args.add(new ATerm(str, null));
//                }
//            } else {
//                this.pred = (P) s;
//                this.args = new ArrayList<ATerm>();
//            }
//        }
//        else {
//            this.pred = p;
//            this.args = args;
//        }
//    }
//
//    
//    public P getPred() {
//        return pred;
//    }
//
//    public List<ATerm> getArgs() {
//        return args;
//    }
//
//    
//    public ATerm<P> clone(boolean deep) {
//        
//        ATerm<P> clone = new ATerm<P>(
//                pred, 
//                deep ? new ArrayList<ATerm>(args) : args,
//                false
//        );
//        
//        return clone;
//    }
//
//    @Override
//    public String toString() {
//        if (this.getPred().equals(".")) {
//            if (this.getArgs().isEmpty()) {
//                return "[]";
//            }
//            ATerm nxt = this.getArgs().get(1);
//            if (nxt.getPred().equals(".") && (nxt.getArgs().isEmpty())) {
//                return "[" + this.getArgs().get(0) + "]";
//            } else if (nxt.getPred().equals(".")) {
//                return "[" + this.getArgs().get(0) + "," + nxt.toString().substring(1, nxt.toString().length() - 1) + "]";
//            } else {
//                return "[" + this.getArgs().get(0) + "|" + nxt + "]";
//            }
//        } else if (this.getArgs().size() > 0) {
//            String argsString = "";
//            if (args != null) {
//                argsString = Arrays.asList(args).toString();
//                argsString = argsString.substring(1, argsString.length() - 1);
//            }
//            return "<" + this.getPred() + "(" + argsString + ")>";
//        }
//        return this.getPred().toString();
//    }
//}

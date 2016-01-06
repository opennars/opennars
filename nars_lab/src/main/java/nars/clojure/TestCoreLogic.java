//package nars.clojure;
//
//import clojure.java.api.Clojure;
//import clojure.lang.*;
//
//import static java.lang.System.*;
//import static clojure.java.api.Clojure.*;
//
///**
// * Created by me on 2/6/15.
// */
//public class TestCoreLogic {
//
//    //http://stackoverflow.com/a/23555959
//
//    public static void main(String[] args) throws ClassNotFoundException {
//        Class.forName("clojure.java.api.Clojure");
//
//        IFn require = var("clojure.core", "require");
//        IFn use = var("clojure.core", "use");
//        IFn deref = var("clojure.core", "deref");
//        IFn macroexpand = var("clojure.core", "macroexpand");
//        IFn eval = var("clojure.core", "eval");
//
//        use.invoke(read("clojure.core.logic"));
//
//        /*
//        IFn plus = Clojure.var("clojure.core", "+");
//        out.println(plus.invoke(1, 2));
//
//        IFn map = Clojure.var("clojure.core", "map");
//        IFn inc = Clojure.var("clojure.core", "inc");
//        out.println( map.invoke(inc, Clojure.read("[1 2 3]")) );
//
//        IFn printLength = Clojure.var("clojure.core", "*print-length*");
//        out.println(deref.invoke(printLength));
//        */
//
//        IFn runStar = var("clojure.core.logic", "run*");
//        IFn lrun = var("clojure.core.logic", "run");
//
//
//        LazySeq q = (LazySeq) eval.invoke(macroexpand.invoke(read("(run* [q] (== q 1))")));
//        q.forEach(x -> {
//            System.out.println(x);
//        });
//        System.out.println();
//
//        LazySeq q2 = (LazySeq)eval.invoke(macroexpand.invoke(
//                ArraySeq.create(runStar,
//                        read("[q]"),
//                        read("(== q 1)"))
//        ));
//        q2.forEach(x -> {
//            System.out.println(x);
//        });
//        System.out.println();
//
//        LazySeq q3 = (LazySeq)eval.invoke(macroexpand.invoke(
//                ArraySeq.create(runStar,
//                        read("[q]"),
//                        read("(membero q [1 2 3])"),
//                        read("(membero q [2 3 4])"))
//        ));
//        q3.forEach(x -> {
//            System.out.println(x);
//        });
////        out.println(
////                c
//////                        lrun.invoke(
//////                        read("[q]"),
//////                        read("(== q 1)")
//////                )
////        );
//////
////        out.println(
////                lrun.invoke(
////                        read("1"),
////                        read("[q]"),
////                        read("(membero q [1 2 3])"),
////                        read("(membero q [2 3 4])")
////                )
////        );
//
//
//
//
//    }
//
// }

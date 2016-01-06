//package nars.prolog;
//
//import nars.NAR;
//import nars.tuprolog.InvalidTheoryException;
//import nars.tuprolog.SolveInfo;
//import nars.tuprolog.Term;
//import nars.tuprolog.Theory;
//
//import java.io.PrintStream;
//
//
///**
// * Wraps a Prolog instance loaded with nal.pl with some utility methods
// */
//abstract public class NARProlog {
//
//    public final NAR nar;
//
//
//
//    public NARProlog(NAR n)  {
//        super();
//        this.nar = n;
//    }
//
//    abstract public void setTheory(Theory t) throws InvalidTheoryException;
//
//    public abstract void printRules(PrintStream out);
//
//    public abstract SolveInfo query(Term s, double time);
//
//
////    public static void main(String[] args) throws Exception {
////        NAR nar = new DefaultNARBuilder().build();
////        new TextOutput(nar, System.out);
////
////        Prolog prolog = new NARProlog(nar);
////        prolog.solve("revision([inheritance(bird, swimmer), [1, 0.8]], [inheritance(bird, swimmer), [0, 0.5]], R).");
////        prolog.solve("logic([inheritance(swan, bird), [0.9, 0.8]], [inheritance(bird, swan), T]).");
////
////    }
//
//
// }

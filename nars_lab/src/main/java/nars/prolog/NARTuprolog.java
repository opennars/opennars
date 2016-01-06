//package nars.prolog;
//
//import nars.NAR;
//import nars.io.TextOutput;
//import nars.model.impl.Default;
//import nars.tuprolog.*;
//import nars.tuprolog.event.*;
//import nars.tuprolog.lib.BasicLibrary;
//
//import java.io.PrintStream;
//
///**
// * Wraps a Prolog instance loaded with nal.pl with some utility methods
// */
//public class NARTuprolog extends NARProlog implements OutputListener, WarningListener, TheoryListener, QueryListener {
//
//    public final Prolog prolog;
//
//    public NARTuprolog(NAR n)  {
//        super(n);
//        this.prolog = new Prolog();
//
//        prolog.addOutputListener(this);
//        prolog.addTheoryListener(this);
//        prolog.addWarningListener(this);
//        prolog.addQueryListener(this);
//
//
//    }
//
//    @Override
//    public void setTheory(Theory t) throws InvalidTheoryException {
//        prolog.setTheory(t);
//    }
//
//    @Override
//    public void printRules(PrintStream out) {
//        out.println( prolog.getDynamicTheoryCopy().toString() );
//    }
//
//    @Override
//    public SolveInfo query(nars.tuprolog.Term s, double time) {
//        return prolog.solve(s, time);
//    }
//
//    public NARTuprolog loadBasicLibrary() throws InvalidLibraryException {
//        prolog.loadLibrary(new BasicLibrary());
//        return this;
//    }
//
//    public NARTuprolog loadNAL() throws InvalidTheoryException {
//    /*    addTheory(getNALTheory());
//    private static Theory nalTheory;
//    static {
//        try {
//            nalTheory = new Theory(PrologContext.class.getResourceAsStream("../nal.pl"));
//        } catch (IOException ex) {
//            nalTheory = null;
//            Logger.getLogger(NARProlog.class.getName()).log(Level.SEVERE, null, ex);
//            System.exit(1);
//        }
//    }
//    public static Theory getNALTheory() { return nalTheory; }
//    */
//        return this;
//    }
//
//    @Override public void onOutput(OutputEvent e) {
//        nar.emit(Prolog.class, e.getMsg());
//    }
//
//    @Override
//    public void onWarning(WarningEvent e) {
//        nar.emit(Prolog.class, e.getMsg() + ", from " + e.getSource());
//    }
//
//    @Override
//    public void theoryChanged(TheoryEvent e) {
//        nar.emit(Prolog.class, e.toString());
//    }
//
//    @Override
//    public void newQueryResultAvailable(QueryEvent e) {
//
//        nar.emit(Prolog.class, e.getSolveInfo());
//        /*
//        //TEMPORARY
//        try {
//            SolveInfo s = e.getSolveInfo();
//            System.out.println("Question:  " + s.getQuery() + " ?");
//            System.out.println("  Answer:  " + s.getSolution());
//        } catch (NoSolutionException ex) {
//            //No solution
//            System.out.println("  Answer: none.");
//            //Logger.getLogger(NARProlog.class.getName()).log(Level.SEVERE, null, ex);
//        }
//                */
//
//    }
//
//    public static void main(String[] args) throws Exception {
//        NAR nar = new NAR(new Default());
//        new TextOutput(nar, System.out);
//
//        NARTuprolog prolog = new NARTuprolog(nar);
//        prolog.prolog.solve("revision([inheritance(bird, swimmer), [1, 0.8]], [inheritance(bird, swimmer), [0, 0.5]], R).");
//        prolog.prolog.solve("nal([inheritance(swan, bird), [0.9, 0.8]], [inheritance(bird, swan), T]).");
//
//    }
//
//
//
// }

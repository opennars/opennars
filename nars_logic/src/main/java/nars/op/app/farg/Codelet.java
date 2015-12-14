///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//
//package nars.op.app.farg;
//
//import nars.Memory;
//import nars.budget.Budget;
//import nars.budget.Item;
//import nars.term.Term;
//import nars.term.atom.Atom;
//
///**
// *
// * @author patrick.hammer
// */
//public class Codelet extends Item<Term> {
//    //Codelet is a small amount of code that has a chance to be run.
//    //since I believe NAL makes this idea obsolete to a large extent,
//    //here we concentrate on parts which are difficult for standard logic like
//    //detecting lines in a pixel-matrix:
//    //<(*,(*,1,0,0,0),(*,1,0,0,0),(*,1,0,0,0),(*,1,0,0,0)) --> viewjunk>.
//    //Linedetector codelets would then inherit from Codelet and its adding/removal
//    //would be regulated by controller
//
//    final Object args;
//    public int timestamp;
//    public Object bin=null;
//    final Term t;
//    final Memory mem;
//    public static int codeletid=0;
//
//    public Codelet(Budget budget, Memory mem, Object args) {
//        super(budget);
//        this.args=args;
//        this.mem=mem;
//        t = Atom.the("Codelet" + String.valueOf(codeletid++));
//    }
//
//
//    public boolean run(Workspace ws) { return true; }
//
//    @Override
//    public Term name() {
//        return t; //To change body of generated methods, choose Tools | Templates.
//    }
//}

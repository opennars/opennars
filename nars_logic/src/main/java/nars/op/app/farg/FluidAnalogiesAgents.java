///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//
//package nars.op.app.farg;
//
//import nars.NAR;
//import nars.nal.nal8.operator.NullOperator;
//import nars.term.Term;
//
///**
// *
// * @author tc
// */
//public class FluidAnalogiesAgents extends NullOperator {
//    public int max_codelets=100;
//    public int codelet_level=100;
//    Workspace ws;
//    final LevelBag<Term, Codelet> coderack = new LevelBag(codelet_level,max_codelets);
//
//    @Override
//    public boolean setEnabled(NAR n, boolean enabled) {
//        if(enabled) {
//            if (coderack!=null)
//                coderack.clear();
//            ws=new Workspace(this,n);
//        }
//        return true;
//    }
//
//}

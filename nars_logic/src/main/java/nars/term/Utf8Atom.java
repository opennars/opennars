//package nars.term;
//
//import nars.Op;
//import nars.term.transform.Substitution;
//
///**
// * Created by me on 12/4/15.
// */
//@Deprecated public class Utf8Atom extends AbstractUtf8Atom {
//
//    public Utf8Atom(String id) {
//        super(id);
//    }
//
//    @Override
//    public Op op() {
//        return Op.ATOM;
//    }
//
//    @Override
//    public int complexity() {
//        return 0;
//    }
//
//    @Override
//    public int structure() {
//        return 0;
//    }
//
//    @Override
//    public int varIndep() {
//        return 0;
//    }
//
//    @Override
//    public int varDep() {
//        return 0;
//    }
//
//    @Override
//    public int varQuery() {
//        return 0;
//    }
//
//    @Override
//    public int vars() {
//        return 0;
//    }
//
//    @Override
//    public Term substituted(Substitution s) {
//        return this;
//    }
//}

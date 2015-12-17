//package nars.nal.meta.pre;
//
//import nars.Op;
//import nars.nal.RuleMatch;
//import nars.term.Term;
//
//
//public final class NotImplOrEquiv extends PreCondition1 {
//
//    final static int ImplicationOrEquivalenceBits =
//            Op.or(Op.ImplicationsBits, Op.EquivalencesBits);
//
//    public NotImplOrEquiv(Term arg1) {
//        super(arg1);
//    }
//
//    @Override
//    public final boolean test(RuleMatch m, Term arg1) {
//
//        return (arg1!=null) &&
//               !arg1.op().isA(ImplicationOrEquivalenceBits);
//
////        Op o = arg1.op();
////        switch (o) {
////            case IMPLICATION:
////            case IMPLICATION_AFTER:
////            case IMPLICATION_BEFORE:
////            case IMPLICATION_WHEN:
////            case EQUIV:
////            case EQUIV_AFTER:
////            case EQUIV_WHEN:
////                return false;
////        }
////        return true;
//    }
//
//}

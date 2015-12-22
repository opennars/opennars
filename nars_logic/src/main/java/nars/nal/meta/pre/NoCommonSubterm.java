//package nars.nal.meta.pre;
//
//import nars.Global;
//import nars.nal.RuleMatch;
//import nars.term.Term;
//import nars.term.compound.Compound;
//
//import java.util.Set;
//
///** Unique subterms */
//public class NoCommonSubterm extends PreCondition2 {
//
//    /** commutivity: sort the terms */
//    public static NoCommonSubterm make(Term a, Term b) {
//        return a.compareTo(b) <= 0 ? new NoCommonSubterm(a, b) : new NoCommonSubterm(b, a);
//    }
//
//    NoCommonSubterm(Term arg1, Term arg2) {
//        super(arg1, arg2);
//    }
//
//
//
//}

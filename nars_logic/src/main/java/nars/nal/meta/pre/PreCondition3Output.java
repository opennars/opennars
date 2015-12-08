package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class PreCondition3Output extends PreCondition3 {

    public PreCondition3Output(Term var1, Term var2, Term var3) {
        super(var1, var2, var3);
    }

    @Override
    public final boolean test(RuleMatch m) {
        //these should not resolve to null
        Term a = m.apply(arg1, false);
        if (a == null) return false;
        Term b = m.apply(arg2, false);
        if (b == null) return false;

        //Term c = m.resolve(arg3);
        //if (c == null) return false;
        //Predicates which use the last variable as an output argument, similar as some Prolog predicates allow.
//        if (arg3 instanceof Variable && ((Variable) arg3).hasVarPattern()) { //wut why is this even necessary?
//            c = arg3;
//        }

        return test(m, a, b, arg3);
    }
}

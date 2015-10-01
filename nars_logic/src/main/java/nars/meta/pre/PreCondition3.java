package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.term.Term;
import nars.term.Variable;

/**
 * Created by me on 8/15/15.
 */
abstract public class PreCondition3 extends PreCondition {
    public Term arg1=null, arg2=null, arg3=null;

    public PreCondition3(){}

    /** no arguments should be null */
    public PreCondition3(Term var1, Term var2, Term var3) {
        super();
        this.arg1 = var1;
        this.arg2 = var2;
        this.arg3 = var3;
    }

    @Override
    public boolean test(RuleMatch m) {
        //these should not resolve to null
        Term a = m.resolve(arg1);
        if (a == null) return false;
        Term b = m.resolve(arg2);
        if (b == null) return false;
        Term c = m.resolve(arg3);

        //Set predicates use the last variable as an output argument, similar as some Prolog predicates do.
        //I agree that this is not beautiful, but it works
        if(this instanceof Union || this instanceof Intersection || this instanceof Intersection) {
            if (arg3 instanceof Variable && ((Variable) arg3).hasVarPattern()) { //wut why is this even necessary?
                c = arg3;
            }
        }

        if (c == null) return false;

        return test(m, a, b, c);
    }

    abstract public boolean test(RuleMatch m, Term a, Term b, Term c);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + arg1 + "," + arg2 + "," + arg3 + "]";
    }
}

package nars.meta.pre;

import nars.Op;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.term.Term;
import nars.term.Variable;


public class Substitute extends PreCondition {

    public final Term x;
    public final Variable y;
    private String str = null;

    /**
     *
     * @param x  original term
     * @param y  replacement term
     */
    public Substitute(Term x, Variable y) {
        this.x = x;
        this.y = y;

    }

    protected String id() {
        return getClass().getSimpleName() + "[" + x + "," + y + "]";
    }

    @Override
    public final String toString() {
        if (str == null) {
            this.str = id(); //must be computed outside of constructor, because of subclassing
        }
        return str;
    }

    @Override public final boolean test(RuleMatch m) {

        Term a = resolve(m, (Variable)this.x);
        if (a == null)
            return false;

        Term b = resolve(m, this.y);
        if (b == null)
            return false;


        //Map<Variable, Term> i = m.Inp;

//        if (a == null)
//            return false;

        //Term M = b; //this one got substituted, but with what?
        //Term with = m.assign.get(M); //with what assign assigned it to (the match between the rule and the premises)
        //args[0] now encodes a variable which we want to replace with what M was assigned to
        //(relevant for variable elimination rules)


        if (!a.equals(b) && substitute(m, a, b)) {
            m.Outp.put(a, b);
        }
        return true;
    }

    private final Term resolve(RuleMatch m, Variable y) {
        final Term b;
        if (y.op() == Op.VAR_PATTERN) {
            b = m.xy.get(y);
        }
        else {
            b = y;
        }
        return b;
    }

    protected boolean substitute(RuleMatch m, Term a, Term b) {
        //for subclasses to override
        return true;
    }

}

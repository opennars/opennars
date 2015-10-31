package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.term.Term;
import nars.term.Variable;

import java.util.Map;


public class Substitute extends PreCondition {

    public final Term arg1, arg2;
    private final String str;

    public Substitute(Term var1, Term var2) {
        this.arg1 = var1;
        this.arg2 = var2;
        this.str = getClass().getSimpleName() + "[" + var1 + "," + var2 + "]";
    }

    @Override
    public final String toString() {
        return str;
    }

    @Override public final boolean test(RuleMatch m) {
        Term b = m.resolve(arg2);
        if (b == null) return false;

        Map<Variable, Variable> i = m.Inp;

        Variable a = i.get(this.arg1);
        if (a == null)
            return false;

        //Term M = b; //this one got substituted, but with what?
        //Term with = m.assign.get(M); //with what assign assigned it to (the match between the rule and the premises)
        //args[0] now encodes a variable which we want to replace with what M was assigned to
        //(relevant for variable elimination rules)

        //the rule match context stores the Inp and Outp. not in this class.
        //no preconditions should store any state
        m.Outp.put(a,b);
        i.clear();
        return true;
    }

}

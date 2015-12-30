package nars.nal.meta.pre;

import nars.nal.PremiseMatch;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
public class InputPremises extends PreCondition2 {

    public InputPremises(Term var1, Term var2) {
        super(var1, var2);
    }

    @Override
    public final boolean test(PremiseMatch m, Term a, Term b) {
        return m.premise.getTask().isInput() && m.premise.getBelief() != null && m.premise.getBelief().isInput();
    }

}

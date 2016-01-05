package nars.nal;

import nars.Global;
import nars.nal.meta.BooleanCondition;
import nars.nal.meta.PostCondition;
import nars.term.Term;

import java.util.List;


public class SimpleDeriver extends Deriver  {

    private final List<List<Term>> unrolled;

    public SimpleDeriver(PremiseRuleSet rules) {
        super(rules);

        List<List<Term>> u = Global.newArrayList();
        for (PremiseRule r : rules.getPremiseRules()) {
            for (PostCondition p : r.postconditions)
                u.add( r.getConditions(p) );
        }
        this.unrolled = u;

        u.forEach(s -> System.out.println(s));
    }

    @Override
    protected void run(PremiseMatch m) {

        int now = m.now();

        for (List<Term> r : unrolled) {
            for (Term p : r) {

                if (p instanceof BooleanCondition) {

                    try {
                        if (!((BooleanCondition) p).booleanValueOf(m))
                            break;

                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }

            }

            m.revert(now);
        }

    }
}

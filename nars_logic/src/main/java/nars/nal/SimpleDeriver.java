package nars.nal;

import nars.Global;
import nars.nal.meta.PostCondition;
import nars.nal.meta.PreCondition;

import java.util.List;


public class SimpleDeriver extends Deriver  {

    private final List<List<PreCondition>> unrolled;

    public SimpleDeriver(PremiseRuleSet rules) {
        super(rules);

        List<List<PreCondition>> u = Global.newArrayList();
        for (PremiseRule r : rules) {
            for (PostCondition p : r.postconditions)
                u.add( r.getConditions(p) );
        }
        this.unrolled = u;

        u.forEach(s -> System.out.println(s));
    }

    @Override
    protected void run(RuleMatch m) {

        int now = m.now();

        for (List<PreCondition> r : unrolled) {
            for (PreCondition p : r) {
                try {
                    if (!p.test(m))
                        break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            m.revert(now);
        }

    }
}

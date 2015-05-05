package nars.nal;


import java.util.ArrayList;
import java.util.List;

/**
 * Applies a set of Rules to inputs, executing appropriate outputs
 */
public class RuleList<X> {

    public final List<LogicRule<X>> rules = new ArrayList();

    public RuleList() {
        super();
    }
    public RuleList(final LogicRule<X>[] rules) {
        if (rules!=null)
            for (LogicRule<X> l : rules)
                add(l);
    }

    public boolean add(final LogicRule l) {
        return rules.add(l);
    }

}

package nars.nal;


import java.util.ArrayList;
import java.util.List;

/**
 * Applies a set of Rules to inputs, executing appropriate outputs
 */
public class RuleList<X> {

    public final List<LogicStage<X>> rules = new ArrayList();

    public RuleList() {
        super();
    }
    public RuleList(final LogicStage<X>[] rules) {
        if (rules!=null)
            for (LogicStage<X> l : rules)
                add(l);
    }

    public boolean add(final LogicStage l) {
        return rules.add(l);
    }

}

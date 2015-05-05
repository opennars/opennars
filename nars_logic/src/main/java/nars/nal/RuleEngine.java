package nars.nal;


import java.util.ArrayList;
import java.util.List;

/**
 * Applies a set of Rules to inputs, executing appropriate outputs
 */
public class RuleEngine<X> {

    final List<LogicRule<X>> logicrules = new ArrayList();

    public RuleEngine() {
        super();
    }

    public boolean add(LogicRule l) {
        return logicrules.add(l);
    }

}

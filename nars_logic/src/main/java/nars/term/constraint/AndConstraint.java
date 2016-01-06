package nars.term.constraint;

import com.google.common.base.Joiner;
import nars.term.Term;
import nars.term.transform.FindSubst;

import java.util.Collection;

public class AndConstraint implements MatchConstraint {

    final MatchConstraint[] subConst;

    public AndConstraint(Collection<MatchConstraint> m) {
        if (m.size() < 2)
            throw new RuntimeException("invalid size");

        this.subConst = m.toArray(new MatchConstraint[m.size()]);
    }

    @Override
    public boolean invalid(Term assignee, Term value, FindSubst f) {
        for (MatchConstraint m : subConst) {
            if (m.invalid(assignee, value, f))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(&&," + Joiner.on(",").join(subConst) + ')';
    }
}

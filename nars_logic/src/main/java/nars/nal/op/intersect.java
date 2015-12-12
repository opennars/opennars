package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;

public class intersect extends ImmediateTermTransform {
    @Override public Term function(Compound x) {
        if (x.size() < 2)
            throw new RuntimeException("expects >= 2 args");

        Term a = x.term(0);
        Term b = x.term(1);
        return TermContainer.intersect(
            a.op(), (Compound) a, (Compound) b
        );
    }
}

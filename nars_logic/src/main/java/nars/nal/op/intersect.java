package nars.nal.op;

import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;

import static nars.term.compound.GenericCompound.COMPOUND;

public class intersect extends ImmediateTermTransform {
    @Override public Term function(Compound x) {
        if (x.size()!=2)
            throw new RuntimeException("expects 2 args");

        return COMPOUND(x.term(0).op(),
            TermContainer.intersect(
                (Compound) x.term(0), (Compound) x.term(1)
            )
        );
    }
}

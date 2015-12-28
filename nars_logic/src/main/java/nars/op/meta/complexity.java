package nars.op.meta;

import nars.nal.nal8.operator.TermFunction;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;

/**
 * Created by me on 3/6/15.
 */
public class complexity extends TermFunction<Integer> {

    @Override
    public Integer function(Compound x, TermIndex i) {
        return x.term(0).complexity();
    }
}

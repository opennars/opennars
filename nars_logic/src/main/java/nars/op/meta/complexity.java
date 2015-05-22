package nars.op.meta;

import nars.nal.nal8.operator.TermFunction;
import nars.nal.term.Term;

/**
 * Created by me on 3/6/15.
 */
public class complexity extends TermFunction<Integer> {

    @Override
    public Integer function(Term... x) {
        return (int)x[0].getComplexity();
    }
}

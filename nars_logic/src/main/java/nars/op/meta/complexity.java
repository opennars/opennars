package nars.op.meta;

import nars.nal.nal8.operator.TermFunction;
import nars.term.Term;

/**
 * Created by me on 3/6/15.
 */
public class complexity extends TermFunction<Integer> {

    @Override
    public Integer function(Term... x) {
        return x[0].complexity();
    }
}

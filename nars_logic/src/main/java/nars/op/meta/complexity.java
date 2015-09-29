package nars.op.meta;

import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.TermFunction;

/**
 * Created by me on 3/6/15.
 */
public class complexity extends TermFunction<Integer> {

    @Override
    public Integer function(Operation x) {
        return x.arg(0).complexity();
    }
}

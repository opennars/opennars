package nars.nal.meta;

import com.gs.collections.api.block.function.primitive.BooleanFunction;
import nars.term.Term;

import java.util.List;

/**
 * Created by me on 12/31/15.
 */
public interface BooleanCondition<C> extends Term, BooleanFunction<C> {
    void addConditions(List<Term> l);
}

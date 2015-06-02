package nars.nal.transform;

import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Variable;
import nars.nal.transform.CompoundTransform;

/**
 * Created by me on 6/1/15.
 */
public interface VariableTransform extends CompoundTransform<Compound, Variable> {

    @Override
    default boolean test(Term possiblyAVariable) {
        return (possiblyAVariable instanceof Variable);
    }
}

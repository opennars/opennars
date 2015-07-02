package nars.term.transform;

import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;

/**
 * Created by me on 6/1/15.
 */
public interface VariableTransform extends CompoundTransform<Compound, Variable> {

    @Override
    default boolean test(Term possiblyAVariable) {
        return (possiblyAVariable instanceof Variable);
    }

    default boolean testSuperTerm(Compound t) {
        //prevent executing on any superterms that contain no variables, because this would have no effect
        return t.hasVar();
    }
}

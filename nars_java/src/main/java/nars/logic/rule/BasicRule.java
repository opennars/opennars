package nars.logic.rule;

import nars.logic.LogicRule;
import org.drools.rule.BooleanCondition;

/** a rule which executes according to the result of a boolean predicate.
 *  it will create a new column in the rule solver, so it should be avoided
 *  when common element expressions will unify
 * */
@Deprecated abstract public class BasicRule<X> extends BooleanCondition<X> implements LogicRule<X> {

    @Override
    public BooleanCondition<X> condition() {
        return this;
    }

}

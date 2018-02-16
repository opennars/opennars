package nars.imagination;

import nars.entity.TruthValue;
import nars.language.Conjunction;
import nars.operator.Operator;

/**
 *
 * @author Patrick
 */
public interface ImaginationSpace {
    //a group of operations that when executed add up to a certain declarative "picture".
    public TruthValue AbductionOrComparisonTo(final ImaginationSpace obj, boolean comparison);
    //attaches an imagination space to the conjunction that is constructed
    //by starting with the leftmost element of the conjunction
    //and then gradually moving to the right
    public ImaginationSpace ConstructSpace(Conjunction program);
    //Has to return a new instance, not changing "this"!
    public ImaginationSpace ProgressSpace(Operator op, ImaginationSpace B);
}
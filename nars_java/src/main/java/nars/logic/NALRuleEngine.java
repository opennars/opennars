package nars.logic;

import nars.core.Memory;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.logic.rule.concept.*;

/**
 * General class which has all NAL Rules
 */
public class NALRuleEngine extends RuleEngine {

    public NALRuleEngine(Memory memory) {
        super();

        add(new FilterEqualSubtermsInRespectToImageAndProduct());

        add(new FilterMatchingTaskAndBelief());

        add(new TemporalInductionChain());

        add(new DeduceSecondaryVariableUnification());

        add(new DeduceConjunctionByQuestion());

        add(new MonolithicRuleTables());

    }

    public void fire(FireConcept fireConcept) {
        base.fire(FireConcept.class, fireConcept);
    }

}

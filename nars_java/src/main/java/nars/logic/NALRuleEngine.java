package nars.logic;

import nars.core.Memory;
import nars.logic.reason.ConceptFire;
import nars.logic.reason.concept.*;

/**
 * General class which has all NAL Rules
 */
public class NALRuleEngine extends RuleEngine {

    public NALRuleEngine(Memory memory) {
        super();


        //concept fire tasklink derivation
        add(new TransformTask());
        add(new Contraposition());

        //concept fire tasklink termlink (pre-filter)
        add(new FilterEqualSubtermsInRespectToImageAndProduct());
        add(new FilterMatchingTaskAndBelief());

        //concept fire tasklink termlink derivation
        add(new TemporalInductionChain());
        add(new DeduceSecondaryVariableUnification());
        add(new DeduceConjunctionByQuestion());
        add(new MonolithicRuleTables());

    }

    public void fire(ConceptFire fireConcept) {
        base.fire(ConceptFire.class, fireConcept);
    }

}

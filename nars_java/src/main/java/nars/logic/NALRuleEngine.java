package nars.logic;

import nars.core.Memory;
import nars.logic.reason.ConceptFire;
import nars.logic.reason.concept.*;
import nars.logic.reason.filter.FilterBelowBudget;
import nars.logic.reason.filter.FilterBelowConfidence;
import nars.logic.reason.filter.FilterOperationWithSubjOrPredVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * General class which has all NAL Rules
 */
public class NALRuleEngine extends RuleEngine {

    private List<NAL.DerivationFilter> derivationFilters = new ArrayList();

    public NALRuleEngine(Memory memory) {
        super();

        initConceptFireRules();
        initDerivationFilters();

    }

    void initConceptFireRules() {

        //concept fire tasklink derivation
        {
            add(new TransformTask());
            add(new Contraposition());
        }

        //concept fire tasklink termlink (pre-filter)
        {
            add(new FilterEqualSubtermsInRespectToImageAndProduct());
            add(new MatchTaskBelief());
        }

        //concept fire tasklink termlink derivation
        {
            add(new ForwardImplicationProceed());
            add(new TemporalInductionChain2()); //add(new TemporalInductionChain());
            add(new DeduceSecondaryVariableUnification());
            add(new DeduceConjunctionByQuestion());
            add(new TableDerivations());
        }
    }

    void initDerivationFilters() {
        derivationFilters.add(new FilterBelowBudget());
        derivationFilters.add(new FilterBelowConfidence());
        derivationFilters.add(new FilterOperationWithSubjOrPredVariable());
        //derivationFilters.add(new FilterCyclic());
    }



    public void fire(ConceptFire fireConcept) {
        base.fire(ConceptFire.class, fireConcept);
    }

    public List<NAL.DerivationFilter> getDerivationFilters() {
        return derivationFilters;
    }

}

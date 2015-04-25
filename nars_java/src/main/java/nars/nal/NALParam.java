package nars.nal;

import nars.Memory;
import nars.nal.rule.*;
import nars.nal.filter.FilterBelowBudget;
import nars.nal.filter.FilterBelowConfidence;
import nars.nal.filter.FilterOperationWithSubjOrPredVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * General class which specifies NAL rules, their ordering, and other parameters applied during inference processes
 */
public class NALParam extends RuleEngine<NAL> {

    private List<NAL.DerivationFilter> derivationFilters = new ArrayList();

    public NALParam(Memory memory) {
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

    public void fire(ConceptProcess fireConcept) {
        int n = logicrules.size();
        for (int l = 0; l < n; l++) {
            LogicRule<NAL> r = logicrules.get(l);
            if (!r.accept(fireConcept))
                break;
        }
    }

    public List<NAL.DerivationFilter> getDerivationFilters() {
        return derivationFilters;
    }

    /** tests validity of a derived task; if valid returns null, else returns a String rule explaining why it is invalid */
    public String derivationRejected(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {

        List<NAL.DerivationFilter> derivationFilters = getDerivationFilters();

        if (derivationFilters != null) {
            for (int i = 0; i < derivationFilters.size(); i++) {
                NAL.DerivationFilter d = derivationFilters.get(i);
                String rejectionReason = d.reject(nal, task, solution, revised, single, currentBelief, currentTask);
                if (rejectionReason != null) {
                    return rejectionReason;
                }
            }
        }
        return null;
    }
}

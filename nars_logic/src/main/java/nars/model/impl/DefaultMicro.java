package nars.model.impl;

import nars.Memory;

/**
 * Created by me on 5/5/15.
 */
public class DefaultMicro extends Default {

    public DefaultMicro() {
        super();

        setInternalExperience(null);

        setConceptBagSize(128);
        setSubconceptBagSize(16);

        setTaskLinkBagSize(16);

        setTermLinkBagSize(16);

        setNovelTaskBagSize(16);


        //Runtime Initial Values

        param.confidenceThreshold.set(0.05);

        param.temporalRelationsMax.set(4);


        param.conceptBeliefsMax.set(7);
        param.conceptGoalsMax.set(5);
        param.conceptQuestionsMax.set(3);


        param.termLinkMaxReasoned.set(3);
        param.termLinkMaxMatched.set(5);
        param.termLinkRecordLength.set(6);
        param.noveltyHorizon.set(6);

        param.setForgetting(Memory.Forgetting.Periodic);
        param.setTiming(Memory.Timing.Cycle);
        param.outputVolume.set(100);

        param.reliance.set(0.9f);

        param.decisionThreshold.set(0.60);

        //add derivation filters here:
        //param.getDefaultDerivationFilters().add(new BeRational());

    }

}

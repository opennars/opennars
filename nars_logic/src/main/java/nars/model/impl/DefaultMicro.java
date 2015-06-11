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

        confidenceThreshold.set(0.05);

        temporalRelationsMax.set(4);


        conceptBeliefsMax.set(7);
        conceptGoalsMax.set(5);
        conceptQuestionsMax.set(3);


        termLinkMaxReasoned.set(3);
        termLinkMaxMatched.set(5);
        termLinkRecordLength.set(6);
        noveltyHorizon.set(6);

        setForgetting(Memory.Forgetting.Periodic);
        setTiming(Memory.Timing.Cycle);
        outputVolume.set(100);

        reliance.set(0.9f);

        executionThreshold.set(0.60);

        //add derivation filters here:
        //param.getDefaultDerivationFilters().add(new BeRational());

    }

}

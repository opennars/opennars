package nars.model.impl;

/**
 * Created by me on 5/5/15.
 */
public class DefaultMicro extends Default {

    public DefaultMicro() {
        super();

        setInternalExperience(null);

        setActiveConcepts(128);

        setTaskLinkBagSize(16);

        setTermLinkBagSize(16);

        setNovelTaskBagSize(16);


        //Runtime Initial Values

        confidenceThreshold.set(0.02);

        temporalRelationsMax.set(4);


        conceptBeliefsMax.set(7);
        conceptGoalsMax.set(5);
        conceptQuestionsMax.set(3);


        termLinkMaxReasoned.set(3);
        termLinkMaxMatched.set(5);
        termLinkRecordLength.set(6);


        //add derivation filters here:
        //param.getDefaultDerivationFilters().add(new BeRational());

    }

}

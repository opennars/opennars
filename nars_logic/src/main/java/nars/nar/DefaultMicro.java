package nars.nar;

/**
 * Created by me on 5/5/15.
 */
public class DefaultMicro extends Default {

    public DefaultMicro() {
        super();

        setInternalExperience(null);

        setActiveConcepts(256);

        setTaskLinkBagSize(16);

        setTermLinkBagSize(16);

        setNovelTaskBagSize(16);


        //Runtime Initial Values

        temporalRelationsMax.set(4);


        conceptBeliefsMax.set(7);
        conceptGoalsMax.set(5);
        conceptQuestionsMax.set(3);

        duration.set(20); //slower forgets

        conceptTaskTermProcessPerCycle.set(3);
        termLinkMaxMatched.set(5);


        //add derivation filters here:
        //param.getDefaultDerivationFilters().add(new BeRational());

    }

}

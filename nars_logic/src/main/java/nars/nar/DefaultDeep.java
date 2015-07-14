package nars.nar;

/**
 * More complete processing
 */
public class DefaultDeep extends Default {

    public DefaultDeep() {
        super();

        conceptsFiredPerCycle.set(3);
        termLinkMaxReasoned.set(8);
        termLinkMaxMatched.set(16);
        termLinkRecordLength.set(6);


    }

}

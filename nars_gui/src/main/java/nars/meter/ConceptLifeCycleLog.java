package nars.meter;

import nars.NAR;
import nars.event.ConceptReaction;
import nars.concept.Concept;

/**
 * Created by me on 5/8/15.
 */
public class ConceptLifeCycleLog extends ConceptReaction {

    int active = 0;
    int deleted = 0;

    public ConceptLifeCycleLog(NAR nar) {
        super(nar);
    }

    public void printStat() {
        int bagActive = memory.getControl().size();
        System.out.print(bagActive + "," + active + "-" + deleted + ":\t");
    }

    @Override
    public void onConceptActive(Concept c) {
        printStat();
        System.out.println("active: " + c.toInstanceString());
        active++;
    }

    @Override
    public void onConceptForget(Concept c) {
        printStat();
        System.out.println("forget: " + c.toInstanceString());
        active--;
    }

    @Override
    public void onConceptDelete(Concept c) {
        printStat();
        System.out.println("delete: " + c.toInstanceString());
        deleted++;
    }

}

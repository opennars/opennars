package nars.meter;

import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.process.ConceptProcess;

/**
 * Meter utility for analyzing useless inference processes
 * --no derivations
 * --derivations which are rejected by memory
 * --...
 */
public class UselessProcess extends NARReaction {

    private final NAR nar;

    public UselessProcess(NAR n) {
        super(n, Events.ConceptProcessed.class);
        this.nar = n;
    }

    @Override
    public void event(Class event, Object... args) {
        if (event == Events.ConceptProcessed.class) {
            onConceptProcessed((ConceptProcess)args[0]);
        }
    }

    void onConceptProcessed(ConceptProcess arg) {
        int numDerived = arg.getDerived().size();
        if (numDerived == 0) {
            System.err.println(nar.time() + ": " +  arg + " no derivations" );
        }
        else {
            if (arg.getTaskLink().type==0) {
                System.err.println(nar.time() + " type 0 tasklink caused derivations: " + arg);
            }
            System.err.println(nar.time() + ": " +  arg + " OK derived=" + numDerived );
        }
    }
}

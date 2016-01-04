package nars.nar;

import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.term.Term;
import nars.time.FrameClock;

/**
 * Terminal only executes commands and does not
 * reason.  however it will produce an event
 * stream which can be delegated to other
 * components like other NAR's
 *
 * TODO extend AbstractNAR, not Default
 */
public class Terminal extends Default {

    public Terminal() {
        super(0,0,0,0, new FrameClock());
    }

    @Override
    public Concept apply(Term t) {
        return new DefaultConcept(null, t, memory);
    }


}

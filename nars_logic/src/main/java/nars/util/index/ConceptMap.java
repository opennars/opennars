package nars.util.index;

import nars.NAR;
import nars.concept.Concept;
import nars.util.event.Topic;

/**
 * Created by me on 4/16/15.
 */
abstract public class ConceptMap  {

    public final NAR nar;

    Topic.Registrations regs;
    int frame = -1;
    protected int cycleInFrame = -1;

    public int frame() {
        return frame;
    }

    public void reset() { }

    public ConceptMap(NAR nar) {
        super();

        regs = new Topic.Registrations(
        nar.memory.eventReset.on(n -> {
            frame = 0;
            reset();
        }),
        nar.memory.eventFrameEnd.on(n -> {
            frame++;
            onFrame();
            cycleInFrame = 0;
        }),
        nar.memory.eventConceptActivated.on(c -> {
            onConceptActive(c);
        }),
        nar.memory.eventConceptForget.on(c -> {
            onConceptForget(c);
        }),
        nar.memory.eventCycleEnd.on(m -> {
            cycleInFrame++;
            onCycle();
        }) );
        this.nar = nar;

    }

    public void off() {

    }

    protected void onFrame() { }

    protected void onCycle() { }


    abstract public boolean contains(Concept c);


    /** returns true if the concept was successfully removed (ie. it was already present and not permanently included) */
    protected abstract boolean onConceptForget(Concept c);

    /** returns true if the concept was successfully added (ie. it was not already present) */
    protected abstract boolean onConceptActive(Concept c);


}

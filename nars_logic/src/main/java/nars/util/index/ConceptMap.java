package nars.util.index;

import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.nal.concept.Concept;

/**
 * Created by me on 4/16/15.
 */
abstract public class ConceptMap extends NARReaction {

    public final NAR nar;
    int frame = -1;
    protected int cycleInFrame = -1;

    public int frame() {
        return frame;
    }

    public void reset() { }

    public ConceptMap(NAR nar) {
        super(nar, Events.ConceptActive.class,
                Events.ConceptForget.class, Events.CycleEnd.class, Events.FrameEnd.class, Events.ResetStart.class);

        this.nar = nar;

    }

    protected void onFrame() { }

    protected void onCycle() { }


    abstract public boolean contains(Concept c);

    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.CycleEnd.class) {
            cycleInFrame++;
            onCycle();
        }
        if (event == Events.FrameEnd.class) {
            frame++;
            onFrame();
            cycleInFrame = 0;
        }
        if (event == Events.ResetStart.class) {
            frame = 0;
            reset();
        } else if (event == Events.ConceptActive.class) {
            Concept c = (Concept) args[0];
            if (contains(c))
                onConceptActive(c);
        } else if (event == Events.ConceptForget.class) {
            Concept c = (Concept) args[0];
            if (contains(c))
                onConceptForget(c);
        }

    }

    /** returns true if the concept was successfully removed (ie. it was already present and not permanently included) */
    protected abstract boolean onConceptForget(Concept c);

    /** returns true if the concept was successfully added (ie. it was not already present) */
    protected abstract boolean onConceptActive(Concept c);


}

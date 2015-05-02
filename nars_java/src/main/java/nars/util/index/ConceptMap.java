package nars.util.index;

import nars.Events;
import nars.Global;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import java.util.*;

/**
 * Created by me on 4/16/15.
 */
abstract public class ConceptMap extends AbstractReaction {

    int frame = -1;
    protected int cycleInFrame = -1;

    public int frame() {
        return frame;
    }

    public void reset() { }

    public ConceptMap(NAR nar) {
        super(nar, Events.ConceptNew.class, Events.ConceptRemember.class,
                Events.ConceptForget.class, Events.CycleEnd.class, Events.FrameEnd.class, Events.ResetStart.class);
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
        } else if ((event == Events.ConceptNew.class) || (event == Events.ConceptRemember.class)) {
            Concept c = (Concept) args[0];
            if (contains(c))
                onConceptNew(c);
        } else if (event == Events.ConceptForget.class) {
            Concept c = (Concept) args[0];
            if (contains(c))
                onConceptForget(c);
        }

    }

    protected abstract void onConceptForget(Concept c);

    protected abstract void onConceptNew(Concept c);


}

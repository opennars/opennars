package vnc;

import nars.Events;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import java.util.Set;

/**
 * Created by me on 4/16/15.
 */
abstract public class ConceptMap extends AbstractReaction {

    int frame = -1;
    int cycleInFrame = -1;

    public int frame() {
        return frame;
    }

    public void reset() { }

    public ConceptMap(NAR nar) {
        super(nar, Events.ConceptNew.class, Events.ConceptForget.class, Events.CycleEnd.class, Events.FrameEnd.class, Events.ResetStart.class);
    }

    abstract protected void onFrame();

    abstract protected void onCycle();


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
        } else if (event == Events.ConceptNew.class) {
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

    /** uses a predefined set of terms that will be mapped */
    abstract public static class SeededConceptMap extends ConceptMap {

        public final Set<Term> terms;

        public SeededConceptMap(NAR nar, Set<Term> terms) {
            super(nar);
            this.terms = terms;
        }


        @Override
        public boolean contains(Concept c) {
            Term s = c.getTerm();
            return terms.contains(s);
        }

        public boolean contains(Term t) { return terms.contains(t); }
    }
}

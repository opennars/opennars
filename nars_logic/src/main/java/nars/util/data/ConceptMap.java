package nars.util.data;

import nars.NAR;
import nars.concept.Concept;
import nars.util.event.Active;

/**
 * Created by me on 4/16/15.
 */
public abstract class ConceptMap {

	public final NAR nar;

	Active regs;
	int frame = -1;
	protected int cycleInFrame = -1;

	public int frame() {
		return frame;
	}

	public void reset() {
	}

	protected ConceptMap(NAR nar) {

        regs = new Active(
        nar.memory.eventReset.on(n -> {
            frame = 0;
            reset();
        }),
        nar.memory.eventFrameStart.on(n -> {
            frame++;
            onFrame();
            cycleInFrame = 0;
        }),
        //nar.memory.eventConceptActivated.on(this::onConceptActive),
//        nar.memory.eventConceptForget.on(c -> {
//            onConceptForget(c);
//        }),
        nar.memory.eventCycleEnd.on(m -> {
            cycleInFrame++;
            onCycle();
        }) );
        this.nar = nar;

    }
	public void off() {

	}

	protected void onFrame() {
	}

	protected void onCycle() {
	}

	public abstract boolean contains(Concept c);

	/**
	 * returns true if the concept was successfully removed (ie. it was already
	 * present and not permanently included)
	 */
	protected abstract boolean onConceptForget(Concept c);

	/**
	 * returns true if the concept was successfully added (ie. it was not
	 * already present)
	 */
	protected abstract boolean onConceptActive(Concept c);

}

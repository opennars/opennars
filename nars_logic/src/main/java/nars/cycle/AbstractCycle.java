package nars.cycle;

import nars.AbstractMemory;
import nars.Memory;
import nars.concept.ConceptActivator;
import nars.io.DefaultPerception;
import nars.io.Perception;
import nars.io.in.Input;
import nars.process.CycleProcess;
import nars.task.Task;

/**
 * Basic CycleProcess that can buffer perceptions
 */
public abstract class AbstractCycle extends ConceptActivator implements CycleProcess<Memory> {

    final protected Perception percepts = new DefaultPerception();

    protected Memory memory;

    @Override
    public void reset(Memory m) {

        /* If the doors of perception were cleansed every thing would
        appear to NARS as it is, Infinite */
        percepts.clear();

        memory = m;

    }

    @Override
    public Memory getMemory() {
        return memory;
    }

    @Override
    public void onInput(Input i) {

        percepts.accept(i);
    }

    /** attempts to perceive the next input from perception, and
     *  handle it by immediately acting on it, or
     *  adding it to the new tasks queue for future reasoning.
     * @return how many tasks were generated as a result of perceiving (which can be zero), or -1 if no percept is available */
    protected int inputNextPerception() {
        if (!memory.isInputting()) return -1;

        Task t = percepts.get();
        if (t != null)
            return memory.add(t) ? 1 : 0;

        return -1;
    }



    /** attempts to perceive at most N perceptual tasks.
     *  this allows Attention to regulate input relative to other kinds of mental activity
     *  if N == -1, continue perceives until perception buffer is emptied
     *  @return how many tasks perceived
     */
    public int inputNextPerception(int maxPercepts) {
        //if (!perceiving()) return 0;

        boolean inputEverything;

        if (maxPercepts == -1) { inputEverything = true; maxPercepts = 1; }
        else inputEverything = false;

        int perceived = 0;
        while (perceived < maxPercepts) {
            int p = inputNextPerception();
            if (p == -1) break;
            else if (!inputEverything) perceived += p;
        }
        return perceived;
    }
}

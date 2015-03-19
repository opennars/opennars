package nars.gui.output.graph.nengo;

import ca.nengo.model.StepListener;

/**
* Created by you on 19.3.15.
*/
abstract public class SubCycle implements StepListener {

    float lastStep = 0;
    long lastStepReal = System.currentTimeMillis();

    @Override
    public void stepStarted(float time) {
        double interval = getTimePerCycle();
        float dt = time - lastStep;
        int numCycles = (int) (Math.floor(dt / interval));

        if (numCycles > 0) {

            long now = System.currentTimeMillis();
            run(numCycles, time, now - lastStepReal);

            lastStep = time;
            lastStepReal = now;
        }

        //System.out.println(this + " run: " + time + " waiting since " + lastStep);
    }

    /** last millisecond-resolution step time */
    public long getLastStepRealMS() { return lastStepReal; }

    abstract public double getTimePerCycle();

    abstract public void run(int count, float endTime, long deltaMS);
}

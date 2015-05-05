package nars.gui;

import automenta.vivisect.Video;
import nars.Memory;
import nars.NAR;
import nars.NARSeed;
import nars.Param;
import nars.model.impl.Default;

/**
 * NARSwing for a Real-time NAR
 */
public class NARSwingRT {

    public static void main(String[] args) {
        Video.themeInvert();

        NARSeed d = new Default();
        d.setTiming(Memory.Timing.RealMS);
        d.duration.set(50);
        d.outputVolume.set(50);

        NAR n = new NAR(d);

        NARSwing s = new NARSwing(n);
        s.newKeyboardInput();

        s.setSpeed(0.5f);
    }
}

package nars.gui;

import nars.Video;
import nars.NAR;
import nars.NARSeed;
import nars.clock.RealtimeMSClock;
import nars.nar.Default;

/**
 * NARSwing for a Real-time NAR
 */
public class NARSwingRT {

    public static void main(String[] args) {
        Video.themeInvert();

        NARSeed d = new Default();
        d.setClock(new RealtimeMSClock(false));
        d.duration.set(50);
        d.outputVolume.set(50);

        NAR n = new NAR(d);

        NARSwing s = new NARSwing(n);
        s.newKeyboardInput();

        s.setSpeed(0.5f);
    }
}

package nars.gui;

import nars.Video;
import nars.NAR;
import nars.NARSeed;
import nars.nar.NewDefault;

/**
 * temporary NARSwing launcher for New Rule engine
 */
public class NARSwingRules {

    public static void main(String[] args) {

        Video.themeInvert();

        NARSeed d = new NewDefault();

        NAR n = new NAR(d);

        NARSwing s = new NARSwing(n);

    }

}

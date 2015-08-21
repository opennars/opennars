package nars.util.java;

import nars.Global;
import nars.NAR;
import nars.io.out.TextOutput;
import nars.nar.experimental.Equalized;
import org.junit.Test;

import java.util.Random;

/**
 * Created by me on 8/20/15.
 */
public class ThermostatTest  {

    public static class Thermostat {
        public int target;
        public int current;
        public int increment;
        public boolean log = false;

        public void reset(int current, int target) {
            if (log) System.out.println("reset: " + current + " " + target);
            this.current = current;
            this.target = target;
        }

        public boolean valid() {
            return Math.abs(current-target) <= increment;
        }
        public boolean above() { return target < current-increment; }
        public boolean below() { return target > current+increment; }

        public void up() {
            if (log) System.out.println("  up @ " + current + " -> " + target);
            current += increment;

        }
        public void down() {
            if (log) System.out.println("down @ " + current + " -> " + target);
            current -= increment;
        }

    }

    Random rng = new Random();
    public void demonstrate(NAR n, Thermostat t, int maxRange) {

        t.increment = 1;

        t.reset(rng.nextInt(maxRange), rng.nextInt(maxRange));


        while (!t.valid()) {
            n.frame();
            if (t.above()) t.down();
            n.frame();
            if (t.below()) t.up();
            n.frame();
        }

    }

    //@Test
    public void testThermostat1() throws Exception {

        Global.DEBUG = true;
        Global.EXIT_ON_EXCEPTION = true;

        NAR n = new NAR(new Equalized(1024, 10, 3).setInternalExperience(null));
        //NAR n = new NAR(new Default().setInternalExperience(null));

        NALObjects nobj = new NALObjects(n);
        Thermostat tc = nobj.build("t", Thermostat.class);

        nobj.setGoalInvoke(false);


        TextOutput.out(n);
        //NARTrace.out(n);

        for (int i = 0; i < 100; i++) {
            demonstrate(n, tc, 10);
            n.frame(100);

            //n.memory.getControl().iterator().forEachRemaining(c -> System.out.println(c));
        }

        tc.log = true;
        nobj.setGoalInvoke(true);


        n.frame(1000);



    }
}

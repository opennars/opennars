package nars.java;

import nars.Global;
import nars.NAR;
import nars.nar.Default;

import java.util.Random;

/**
 * Created by me on 8/20/15.
 */
public enum ThermostatTest  {
    ;

    public static class Thermostat {
        public int target;
        public int current;
        public int increment;
        public boolean log = false;


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

    static final Random rng = new Random();
    public static void demonstrate(NAR n, Thermostat t, int maxRange) {

        t.increment = 1;

        reset(t, maxRange);


        while (!t.valid()) {
            if (t.above()) t.down();
            if (t.below()) t.up();
            n.frame(10);
        }

    }

    private static void reset(Thermostat t, int maxRange) {
        do {
            t.current = rng.nextInt(maxRange);
            t.target = rng.nextInt(maxRange);
        } while ( Math.abs(t.current - t.target) < maxRange/3 );

        if (t.log) System.out.println("reset: " + t.current + ' ' + t.target);
    }

    //@Test public void testThermostat1() throws Exception {
    public static void main(String[] arg) throws Exception {

        Global.DEBUG = true;
        Global.EXIT_ON_EXCEPTION = true;

        NAR n = new Default(512, 1, 2, 3);
        //NAR n = new NAR(new Default().setInternalExperience(null));

        NALObjects nobj = new NALObjects(n);
        Thermostat tc = nobj.wrap("t", new Thermostat());

        nobj.setGoalInvoke(false);


        //TextOutput.out(n);
        //TextOutput.out(n).setOutputPriorityMin(0.5f);
        //NARTrace.out(n);

        int range = 20;

        //n.trace(System.out);
         n.log();

        //            @Override
//            protected boolean output(Channel channel, Class event, Object... args) {
//                if (event == Events.EXE.class) {
//                    ExecutionResult t = (ExecutionResult)args[0];
//                    System.out.println( t.getTask().getExplanation() );
//                    return super.output(channel, event, args);
//
//                }
//                return false;
//            }

        for (int i = 0; i < 2; i++) {
            demonstrate(n, tc, range);
            //n.memory.getControl().iterator().forEachRemaining(c -> System.out.println(c));
            n.frame(1000);
        }

        n.forEachConcept(System.out::println);

        tc.log = true;
        nobj.setGoalInvoke(true);




        //TextOutput.out(n);

        /*n.input("Thermostat_valid(t, #1)! :|: %0.50;0.99%");
        n.input("Thermostat_up(t, #1)! :|: %0.50;0.99%");
        n.input("Thermostat_down(t, #1)! :|: %0.50;0.99%");*/

        for (int i = 0; i < 1; i++) {
            n.input("<true --> (/, ^Thermostat_valid, t, _)>!");

            reset(tc, range);


            //n.input("<(--,true) --> (/, ^Thermostat_valid, t, _)>! %0%");
            n.frame(1000);
            System.out.println(tc.valid());
        }



    }
}

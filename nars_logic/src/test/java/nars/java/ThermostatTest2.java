package nars.java;

import nars.Global;
import nars.NAR;
import nars.nar.Default;
import nars.nar.Default2;
import nars.task.Task;
import nars.task.Tasked;
import nars.util.data.random.XORShiftRandom;
import nars.util.meter.TaskRemovalReasons;

import java.util.Random;

/**
 * Created by me on 8/20/15.
 */
public class ThermostatTest2 {

    /** number of steps in total range */
    static int range = 16;

    /** # steps it can move per invocation (+/-) */
    static int maxStep = 2;
    static public int tolerance = 1;

    public static class Model {
        public int target;
        public int current;

        public boolean log = true;


        public boolean valid() {
            return Math.abs(current-target) <= tolerance;
        }

        public boolean above() { return target < current-tolerance; }
        public boolean below() { return target > current+tolerance; }

        public int go(int speed, boolean upOrDown) {
            if (log) System.out.println("  go @ " + current + " (" + speed +  "," + upOrDown + ") TO " + target);
            current += speed * (upOrDown ? +1 : -1);
            return (int)Math.signum(target-current);
        }

    }

    static final Random rng = new XORShiftRandom(1);

    public static void teach(NAR n, Model t, int maxRange) {

        int minDelay = 10;
        int delayVariation = 10;

        reset(t, maxRange);

        for (int i= 1; i <= maxStep; i++) {


            for (int j = 0; j < 10; j++) {

                if (j%2 == 0)
                    adjust(t, 5);

                //n.frame((int)(minDelay + delayVariation * rng.nextFloat()));

                if (t.above()) t.go(i, false);

                //n.frame((int)(minDelay + delayVariation * rng.nextFloat()));

                if (t.below()) t.go(i, true);

                n.frame((int)(minDelay + delayVariation * rng.nextFloat()));
            }
            n.frame(100);
        }

    }

    private static void reset(Model t, int maxRange) {
        do {
            t.current = rng.nextInt(maxRange);
            t.target = rng.nextInt(maxRange);
        } while ( Math.abs(t.current - t.target) < maxRange/3 );

        if (t.log) System.out.println("reset: " + t.current + " " + t.target);
    }
    private static void adjust(Model t, int scale) {

        t.target += (rng.nextBoolean() ? +1 : -1) * (1+rng.nextInt(scale));
        t.target = Math.max(0, Math.min(t.target, range));

        if (t.log) System.out.println("adjust: " + t.current + " " + t.target);

    }

    //@Test public void testThermostat1() throws Exception {
    public static void main(String[] arg) throws Exception {

        Global.DEBUG = false;
        Global.EXIT_ON_EXCEPTION = true;

        Default n = new Default2(1024, 1, 2, 2);
        n.memory.duration.set(1);

        //NAR n = new NAR(new Default().setInternalExperience(null));

        NALObjects nobj = new NALObjects(n);
        String id = "T";
        Model tc = nobj.build(id, Model.class);

        nobj.setGoalInvoke(false);


        //TextOutput.out(n);
        //TextOutput.out(n).setOutputPriorityMin(0.5f);
        //NARTrace.out(n);


        //n.trace(System.out);
         //n.log();

        {

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
        }

//        for (int i = 0; i < 2; i++) {
//            teach(n, tc, range);
//            //n.memory.getControl().iterator().forEachRemaining(c -> System.out.println(c));
//            n.frame(200);
//        }
        //n.forEachConcept(System.out::println);

        tc.log = true;
        nobj.setGoalInvoke(true);




        n.log(System.out, v -> {

            Task t = Tasked.the(v);
            if (t == null) return false;

            //if (t.isJudgmentOrGoal()) return true;

            //return t.getPriority() > 0.25;
            return t.getQuality() > 0.05;

        });

        TaskRemovalReasons taskStats = new TaskRemovalReasons(n);

        /*n.input("Thermostat_valid(t, #1)! :|: %0.50;0.99%");
        n.input("Thermostat_up(t, #1)! :|: %0.50;0.99%");
        n.input("Thermostat_down(t, #1)! :|: %0.50;0.99%");*/
        //n.log();

        //teach actions/sensors
        tc.valid(); n.frame();
        tc.go(1, true); n.frame();
        tc.go(1, false); n.frame();

        for (int i = 0; i < 1; i++) {

            n.input("$1.0;0.9;0.95$ <true --> (/, ^Model_valid, t, _)>!");

            tc.valid();

            reset(tc, range);


            //n.input("<(--,true) --> (/, ^Thermostat_valid, t, _)>! %0%");
            n.frame(1000);
            System.out.println(tc.valid() + " " + tc.current + " ... " + tc.target  );
        }


        System.out.println(taskStats);

    }
}

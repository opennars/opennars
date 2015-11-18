package nars.java;

import nars.Global;
import nars.concept.Concept;
import nars.nar.Default2;
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
            boolean b = Math.abs(current-target) <= tolerance;

            if (log) System.out.println("\n\tvalid? " + b + " @ " + current + " TO " + target + "\n");

            return b;
        }

//        public boolean above() { return target < current-tolerance; }
//        public boolean below() { return target > current+tolerance; }

        public int go(int speed, boolean upOrDown) {
            if (log) System.out.println("\n\tgo @ " + current + " (" + speed +  "," + upOrDown + ") TO " + target + "\n");
            current += speed * (upOrDown ? +1 : -1);
            current = Math.min(Math.max(0, current), range);
            return (int)Math.signum(target-current);
        }

    }

    static final Random rng = new XORShiftRandom(1);


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

        Default2 n = new Default2(1024, 1, 1, 2);
        n.memory.duration.set(5);
        n.getInput().inputPerCycle.set(4);

        //NAR n = new NAR(new Default().setInternalExperience(null));

        NALObjects nobj = new NALObjects(n);

        String id = "T";
        Model tc = nobj.wrap(id, Model.class);

        //nobj.setGoalInvoke(false);


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
        ///nobj.setGoalInvoke(true);




        //n.log();

        //n.trace();

//        n.log(System.out, v -> {
//
//
//            Task t = Tasked.the(v);
//            if (t == null)
//                return false;
//
//            //if (t.isJudgmentOrGoal()) return true;
//
//            return !(t.isJudgment() && t.getPriority() < 0.1);
//            //return t.getQuality() > 0.05;
//            //return true;
//
//        });


        TaskRemovalReasons taskStats = new TaskRemovalReasons(n);

        /*n.input("Thermostat_valid(t, #1)! :|: %0.50;0.99%");
        n.input("Thermostat_up(t, #1)! :|: %0.50;0.99%");
        n.input("Thermostat_down(t, #1)! :|: %0.50;0.99%");*/
        //n.log();

        //teach actions/sensors
        tc.valid(); n.frame(100);
        tc.go(1, true); n.frame(100);
        tc.go(1, false); n.frame(100);




        //begin invalid
        tc.current = range/4;
        tc.target = range/2+range/4;

        tc.valid();


        String isValid = "<true --> (/, ^Model_valid, T, (), _)>";
        String notValid = "<(--,true) --> (/, ^Model_valid, T, (), _)>";





        String up = "Model_go(T, (1, true), #x)";
        String down = "Model_go(T, (1, (--,true)), #x)";

        for (int i = 0; i < 100; i++) {

            {
                tc.log = false;
                n.input(up + "@ :|:");
                n.input(down + "@ :|:");
                n.input(isValid + "!");
                n.input(notValid + "! %0%");
                tc.valid();

                n.frame(50);

                tc.log = true;
            }

            //$0.8;0.5;0.95$
            //n.input("<true --> (/, ^Model_valid, T, (), _)>!");
            //n.input("<(--,true) --> (/, ^Model_valid, T, (), _)>!");

            //n.input("Model_go(T, (1, true), #1)!");
            //n.input("Model_go(T, (1, false), #1)!");

            //n.input("<#x --> (/, ^Model_valid, T, #y, _)>?");

            //tc.valid();


            //n.input("<(--,true) --> (/, ^Thermostat_valid, t, _)>! %0%");
            n.frame(1500);
            //System.out.println(tc.valid() + " " + tc.current + " ... " + tc.target  );

            reset(tc, range);

            Concept upConcept = n.concept(up);
            if (upConcept!=null) {
                //isValidConcept.print(System.out);
                System.err.println(upConcept + ": " + upConcept.getDesireExpectation() + " " + upConcept.getSuccess());
            }
        }


        //System.out.println(taskStats);


        n.forEachConcept(c -> {
            if (c.getTerm().volume() < 9)
                if (/*c.hasBeliefs() ||*/ c.hasGoals())
                    c.print(System.out);
        });

        System.out.println(n.concepts().size() + " total concepts cached");

        System.out.println(taskStats);
    }
}

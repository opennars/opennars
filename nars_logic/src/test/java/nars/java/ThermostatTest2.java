package nars.java;

import com.gs.collections.api.tuple.Twin;
import nars.Global;
import nars.nar.Default;
import nars.task.Task;
import nars.task.Tasked;
import nars.util.data.random.XORShiftRandom;

import java.util.Random;

/**
 * Created by me on 8/20/15.
 */
public enum ThermostatTest2 {
    ;

    /** number of steps in total range */
    static int range = 4;

    /** # steps it can move per invocation (+/-) */

    public static class Model {
        public int target;
        public int current;

        public boolean log = true;

//
//        public boolean valid() {
//            boolean b = Math.abs(current-target) <= tolerance;
//
//            if (log) System.out.println("\n\tvalid? " + b + " @ " + current + " TO " + target + "\n");
//
//            return b;
//        }

//        public boolean above() { return target < current-tolerance; }
//        public boolean below() { return target > current+tolerance; }

        public int go(/*int speed,*/ boolean upOrDown) {
            int speed = 1;

            if (log) System.out.println("\tgo @ " + current + " (" + speed + ',' + upOrDown + ") TO " + target + " (" + (Math.abs(target-current)) + " dist)\n");

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

        if (t.log) System.out.println("reset: " + t.current + ' ' + t.target);
    }
    private static void adjust(Model t, int scale) {

        t.target += (rng.nextBoolean() ? +1 : -1) * (1+rng.nextInt(scale));
        t.target = Math.max(0, Math.min(t.target, range));

        if (t.log) System.out.println("adjust: " + t.current + ' ' + t.target);

    }

    //@Test public void testThermostat1() throws Exception {
    public static void main(String[] arg) throws Exception {

        Global.DEBUG = false;
        Global.EXIT_ON_EXCEPTION = true;

        int dur = 5;

        Default n = new Default(1024, 4, 2, 3);
        n.memory.duration.set(dur);
        //n.getInput().inputPerCycle.set(2);


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

        boolean trace = false;
        if (trace) {
            n.log(System.out, v -> {

                if (v instanceof Twin) return true; //Q&A

                Task t = Tasked.the(v);
                if (t == null)
                    return false;


                //if (t.isJudgmentOrGoal()) return true;

                return t.getBudget().summary() > 0.05;
                //return t.getQuality() > 0.05;
                //return true;

            });
        }


        //TaskRemovalReasons taskStats = new TaskRemovalReasons(n);

        /*n.input("Thermostat_valid(t, #1)! :|: %0.50;0.99%");
        n.input("Thermostat_up(t, #1)! :|: %0.50;0.99%");
        n.input("Thermostat_down(t, #1)! :|: %0.50;0.99%");*/

        //n.log();

        //teach actions/sensors
        //tc.valid(); n.frame(dur*4);




        //begin invalid
        tc.current = range/4;
        tc.target = range/2+range/4;



        /*String isValid = "<true --> (/, ^Model_valid, T, (), _)>";
        String notValid = "<(--,true) --> (/, ^Model_valid, T, (), _)>";*/

        String isValid = "<0 --> (/, ^Model_go, T, ?x, _)>";
//        String notValid = "<(--,true) --> (/, ^Model_valid, T, (), _)>";
//
//        String up = "Model_go(T, (1, true), #x)";
//        String down = "Model_go(T, (1, (--,true)), #x)";
//
//        Function<Term,Implication> isValidThen = (t) -> {
//            return $.implForward($(isValid), t);
//        };
//        Function<Term,Implication> notValidThen = (t) -> {
//            return $.implForward($(notValid), t);
//        };


//                n.input(up + "@ :|:");
//                n.input(down + "@ :|:");

        n.input(isValid + '?');
//        n.should(isValidThen.apply($(up)));
//        n.should(isValidThen.apply($(down)));
//        n.should(notValidThen.apply($(up)));
//        n.should(notValidThen.apply($(down)));


//                n.input(notValid + "! %0%");

        for (int i = 0; i < 5; i++) {

            reset(tc, range);
            tc.go(true); n.frame(dur*2); ///*tc.valid();*/ n.frame(dur*4);
            tc.go(false); n.frame(dur*2); ///*tc.valid();*/ n.frame(dur*4);

            //tc.log = false;


            //tc.valid();

            //n.frame(50);
            //tc.log = true;

            //$0.8;0.5;0.95$
            //n.input("<true --> (/, ^Model_valid, T, (), _)>!");
            //n.input("<(--,true) --> (/, ^Model_valid, T, (), _)>!");

            //n.input("Model_go(T, (1, true), #1)!");
            //n.input("Model_go(T, (1, false), #1)!");

            //n.input("<#x --> (/, ^Model_valid, T, #y, _)>?");

            //tc.valid();


            //n.input("<(--,true) --> (/, ^Thermostat_valid, t, _)>! %0%");
            n.frame(1000*dur);
            //System.out.println(tc.valid() + " " + tc.current + " ... " + tc.target  );


//            Concept upConcept = n.concept(up);
//            if (upConcept!=null) {
//                //isValidConcept.print(System.out);
//                System.err.println(upConcept + ": " + upConcept.getDesireExpectation() + " " + upConcept.getSuccess());
//            }
        }


        //System.out.println(taskStats);


//        n.forEachConcept(c -> {
//            if (c.getTerm().volume() < 9)
//                if (/*c.hasBeliefs() ||*/ c.hasGoals())
//                    c.print(System.out);
//        });

        System.out.println(n.index().size() + " total concepts cached");

        //System.out.println(taskStats);
    }
}
package nars.rover.run;

import nars.Video;
import nars.Global;
import nars.NAR;
import nars.NARSeed;
import nars.clock.SimulatedClock;
import nars.event.CycleReaction;
import nars.io.out.TextOutput;
import nars.nar.Default;
import nars.nar.experimental.Equalized;
import nars.nar.experimental.Solid;
import nars.rover.Sim;
import nars.rover.robot.CarefulRover;
import nars.rover.robot.Rover;
import nars.rover.robot.Spider;
import nars.rover.robot.Turret;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by me on 6/20/15.
 */
public class SomeRovers {

    private static SimulatedClock clock = new SimulatedClock();


    static {
        Video.themeInvert();
    }

    public NARSeed newSolid() {
        Default d = new Solid(32, 599, 1, 1, 1, 3 ) {


        }.setInternalExperience(null).setClock(clock);

        //TextOutput.out(nar).setShowInput(true).setShowOutput(false);

        //N/A for solid
        //nar.param.inputsMaxPerCycle.set(32);
        //nar.param.conceptsFiredPerCycle.set(4);


        d.conceptBeliefsMax.set(16);
        d.conceptGoalsMax.set(12);


        return d;
    }
    public static Default newDefault() {

        Default d = new Equalized(1024, 16, 8);
//
//
//            @Override
//            public Concept newConcept(final Term t, final Budget b, final Memory m) {
//
//                Bag<Sentence, TaskLink> taskLinks =
//                        new CurveBag(rng, /*sentenceNodes,*/ getConceptTaskLinks());
//                        //new ChainBag(rng,  getConceptTaskLinks());
//
//                Bag<TermLinkKey, TermLink> termLinks =
//                        new CurveBag(rng, /*termlinkKeyNodes,*/ getConceptTermLinks());
//                        //new ChainBag(rng, /*termlinkKeyNodes,*/ getConceptTermLinks());
//
//                return newConcept(t, b, taskLinks, termLinks, m);
//            }
//
//        };
        //d.setInternalExperience(null);
        d.setClock(clock);

        d.conceptTaskTermProcessPerCycle.set(4);

        int cycPerFrame = 8;

        d.setCyclesPerFrame(cycPerFrame);
        d.duration.set(5 * cycPerFrame);


        //TextOutput.out(nar).setShowInput(true).setShowOutput(false);

        d.setTermLinkBagSize(64);
        d.setTaskLinkBagSize(64);

        //N/A for solid
        //nar.param.inputsMaxPerCycle.set(32);
        //nar.param.conceptsFiredPerCycle.set(4);

        d.conceptCreationExpectation.set(0);
        d.conceptBeliefsMax.set(16);
        d.conceptGoalsMax.set(8);
        //d.termLinkForgetDurations.set(4);



        return d;
    }

    public static void main(String[] args) {

        Global.DEBUG = Global.EXIT_ON_EXCEPTION = false;


        float fps = 90;
        boolean cpanels = true;

        final Sim game = new Sim(clock);


        game.add(new Turret("turret"));
        game.add(new Spider("spider",
                3, 3, 0.618f, 30, 30));


        {
            Default e = newDefault();


            NAR nar = new NAR(e);
            //TextOutput.out(nar).setOutputPriorityMin(0.5f);

            //TextOutput.out(nar);

            game.add(new Rover("r1", nar));

            //new UselessProcess(nar);

        }
      {

            NAR nar;

            //NARSeed d = newSolid();
            Default d = newDefault();
            nar = new NAR(d);



            //new InputActivationController(nar);



            //TextOutput.out(nar).setOutputPriorityMin(0.5f);


            //nar.param.shortTermMemoryHistory.set(3);

            (nar.param).outputVolume.set(0);
            //nar.param.budgetThreshold.set(0.02);
            //nar.param.confidenceThreshold.set(0.02);

            //(nar.param).conceptForgetDurations.set(2f);
            /*
            (nar.param).taskLinkForgetDurations.set(10f);
            (nar.param).termLinkForgetDurations.set(10f);
            (nar.param).novelTaskForgetDurations.set(10f);
            */



            game.add(new CarefulRover("r2", nar));

            /*if (cpanels) {
                SwingUtilities.invokeLater(() -> {
                    new NARSwing(nar, false);
                });
            }*/
        }

        game.run(fps);
    }

    private static class InputActivationController extends CycleReaction {

        private final NAR nar;

        final int windowSize;

        final DescriptiveStatistics busyness;

        public InputActivationController(NAR nar) {
            super(nar);
            this.nar = nar;
            this.windowSize = nar.memory.duration();
            this.busyness = new DescriptiveStatistics(windowSize);
        }

        @Override
        public void onCycle() {

            final float bInst = nar.memory.emotion.busy();
            busyness.addValue(bInst);

            float bAvg = (float)busyness.getMean();

            float busyMax = 3f;

            double a = nar.param.inputActivationFactor.get();
            if (bAvg > busyMax) {
                a -= 0.01f;
            }
            else  {
                a += 0.01f;
            }

            final float min = 0.01f;
            if (a < min) a = min;
            if (a > 1f) a = 1f;

            //System.out.println("act: " + a + " (" + bInst + "," + bAvg);

            nar.param.inputActivationFactor.set(a);
            nar.param.conceptActivationFactor.set( 0.5f * (1f + a) /** half as attenuated */ );
        }
    }
}

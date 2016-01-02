package nars.rover.run;

import nars.Global;
import nars.NAR;
import nars.NARSeed;
import nars.Video;
import nars.nar.experimental.Equalized;
import nars.rover.RoverWorld;
import nars.rover.Sim;
import nars.rover.robot.CarefulRover;
import nars.rover.robot.Rover;
import nars.rover.robot.Spider;
import nars.rover.robot.Turret;
import nars.rover.world.FoodSpawnWorld1;
import nars.time.SimulatedClock;
import nars.util.event.CycleReaction;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Created by me on 6/20/15.
 */
public class SomeRovers {

    private static SimulatedClock clock = new SimulatedClock();


    static {
        Video.themeInvert();
    }

    public static NARSeed newDefault(int threads) {

        int cycPerFrame = 5;

        //Alann d = new ParallelAlann(64, threads);
        //DefaultAlann d = new DefaultAlann(32);
        //d.tlinkToConceptExchangeRatio = 1f;

        Equalized d = new Equalized(1000, 32, 4);

        //d.param.conceptActivationFactor.set(0.25f);
        //d.param.inputsMaxPerCycle.set(4);

        //Default d = new Equalized(1024, 16, 10);
        //d.setTermLinkBagSize(16);
        //d.setTaskLinkBagSize(16);

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
        //d.param.setClock(clock);
        d.setClock(clock);

        d.param.conceptTaskTermProcessPerCycle.set(4);


        //d.param.setCyclesPerFrame(cycPerFrame);
        d.setCyclesPerFrame(cycPerFrame);
        d.param.duration.set(cycPerFrame);
        d.param.conceptBeliefsMax.set(16);
        d.param.conceptGoalsMax.set(8);

        //TextOutput.out(nar).setShowInput(true).setShowOutput(false);


        //N/A for solid
        //nar.param.inputsMaxPerCycle.set(32);
        //nar.param.conceptsFiredPerCycle.set(4);

        d.param.conceptCreationExpectation.set(0);

        //d.termLinkForgetDurations.set(4);



        return d;
    }

    public static void main(String[] args) {

        Global.DEBUG = Global.EXIT_ON_EXCEPTION = false;


        float fps = 60;
        boolean cpanels = true;

        RoverWorld world;

        //world = new ReactorWorld(this, 32, 48, 48*2);

        world = new FoodSpawnWorld1(128, 48, 48, 0.5f);

        //world = new GridSpaceWorld(GridSpaceWorld.newMazePlanet());


        final Sim game = new Sim(clock, world);


        game.add(new Turret("turret"));
        game.add(new Spider("spider",
                3, 3, 0.618f, 30, 30));


        {
            NARSeed e = newDefault(3);

            NAR nar = new NAR(e);

            game.add(new Rover("r1", nar));

        }
        {
            NAR nar = new NAR( newDefault(1) );

            //nar.param.outputVolume.set(0);

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

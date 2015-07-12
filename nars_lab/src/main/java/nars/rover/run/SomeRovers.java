package nars.rover.run;

import automenta.vivisect.Video;
import nars.Global;
import nars.NAR;
import nars.clock.SimulatedClock;
import nars.gui.NARSwing;
import nars.nar.Solid;
import nars.process.TaskProcess;
import nars.rover.RoverEngine;
import nars.rover.robot.RoverModel;
import nars.task.Task;
import nars.task.filter.ConstantDerivationLeak;

import javax.swing.*;

/**
 * Created by me on 6/20/15.
 */
public class SomeRovers {

    static {
        Video.themeInvert();
    }

    public static void main(String[] args) {
        Global.DEBUG = true;
        Global.EXIT_ON_EXCEPTION = true;


        float fps = 60;
        boolean cpanels = true;

        final RoverEngine game = new RoverEngine();


        int rovers = 1;

        for (int i = 0; i < rovers; i++)  {

            NAR nar;
            SimulatedClock clock;
            nar = new NAR(new Solid(32, 855, 1, 1, 1, 3 ) {

                protected void initDerivationFilters() {
                    final float DERIVATION_PRIORITY_LEAK=0.6f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
                    final float DERIVATION_DURABILITY_LEAK=0.7f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
                    getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(DERIVATION_PRIORITY_LEAK, DERIVATION_DURABILITY_LEAK));
                }

            }.setInternalExperience(null).setClock(clock = new SimulatedClock())
            ) {
                @Override
                public TaskProcess inputDirect(Task t) {

                    TaskProcess tp = super.inputDirect(t);
                    //System.out.println(tp);
                    return tp;
                }

            };

            //TextOutput.out(nar).setShowInput(true).setShowOutput(false);

            //N/A for solid
            //nar.param.inputsMaxPerCycle.set(32);
            //nar.param.conceptsFiredPerCycle.set(4);

            nar.param.conceptBeliefsMax.set(12);
            nar.param.conceptGoalsMax.set(10);

            int nc = 1;
            nar.setCyclesPerFrame(nc);
            (nar.param).duration.set(1);

            //nar.param.shortTermMemoryHistory.set(3);

            (nar.param).outputVolume.set(3);
            //nar.param.budgetThreshold.set(0.02);
            //nar.param.confidenceThreshold.set(0.02);

            /*
            (nar.param).conceptForgetDurations.set(15f);
            (nar.param).taskLinkForgetDurations.set(10f);
            (nar.param).termLinkForgetDurations.set(10f);
            (nar.param).novelTaskForgetDurations.set(10f);
            */



            game.add(new RoverModel("r" + i, nar, game));

            if (cpanels) {
                SwingUtilities.invokeLater(() -> {
                    new NARSwing(nar, false);
                });
            }
        }

        game.run(fps);
    }
}

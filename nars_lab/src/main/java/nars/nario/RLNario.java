package nars.nario;

import automenta.vivisect.Video;
import nars.NAR;
import nars.gui.NARSwing;
import nars.prototype.Default;
import nars.rl.*;
import nars.rl.example.QVis;

import javax.swing.*;

/**
 * Created by me on 4/26/15.
 */
public class RLNario extends NARio  {

    private final QVis mi;

    public RLNario(NAR nar, Perception... p) {
        super(nar);

        float fps = 30f;
        gameRate = 1.0f / fps;

        QLAgent agent = new QLAgent(nar, "act", "<nario --> [good]>", this, p);

        agent.brain.setEpsilon(0.05);

        //agent.setqAutonomicGoalConfidence(0.1f);

        mi = new QVis(agent);


        Video.themeInvert();
        new NARSwing(nar);
    }

    @Override
    protected void input(String sensed) {
        //ignore this input
    }


    @Override
    public void frame() {
        super.frame();
        SwingUtilities.invokeLater(mi::run);
    }

    public static void main(String[] args) {


        NAR nar = new NAR(new Default().setInternalExperience(null)
                .simulationTime().setConceptBagSize(3500));

        nar.param.duration.set(memoryCyclesPerFrame * 3);
        nar.setCyclesPerFrame(memoryCyclesPerFrame);

        nar.param.outputVolume.set(0);
        nar.param.decisionThreshold.set(0.55);
        nar.param.conceptsFiredPerCycle.set(50);
        nar.param.shortTermMemoryHistory.set(5);

        new RLNario(nar,
                new RawPerception("r", 0.8f),
                new HaiSOMPerception("s", 3, 0.4f),
                new AEPerception("a", 0.5f, 5, 1)
        );

    }

}

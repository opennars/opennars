package nars.nario;

import automenta.vivisect.Video;
import nars.Global;
import nars.NAR;
import nars.gui.NARSwing;
import nars.model.impl.Default;
import nars.rl.*;
import nars.rl.example.QVis;

import java.awt.*;

/**
 * Created by me on 4/26/15.
 */
public class RLNario extends NARio  {

    private final QVis mi;

    public RLNario(NAR nar, Perception... p) {
        super(nar);

        float fps = 20f;
        gameRate = 1.0f / fps;

        QLAgent agent = new QLAgent(nar, "act", "<nario --> [good]>", this, p);

        agent.brain.setEpsilon(0.1);
        agent.brain.setAlpha(0.1);

        mi = new QVis(agent);


        Video.themeInvert();
        new NARSwing(nar);
    }

    @Override
    @Deprecated synchronized public double[] observe() {


        o.clear();

        o.addAll(dx/6.0, dy/6.0); //higher resolution velocity
        o.addAll(dx/12.0, dy/12.0); //coarse velocity

        for (boolean b : mario.keys)
            o.add(b ? 1 : -1);


        //o.addAll(radar);

        Image screen = ((LevelScene) scene).layer.image;



        return o.toArray();
    }

    @Override
    protected void input(String sensed) {
        //ignore this input
    }


    @Override
    public void frame() {
        super.frame();
        mi.frame();
    }

    public static void main(String[] args) {


        NAR nar = new NAR(new Default(2000, 50, 3).setInternalExperience(null) );

        nar.param.duration.set(memoryCyclesPerFrame * 3);
        nar.setCyclesPerFrame(memoryCyclesPerFrame);

        nar.param.outputVolume.set(0);
        nar.param.decisionThreshold.set(0.51);
        nar.param.shortTermMemoryHistory.set(3);

        new RLNario(nar,
                //new RawPerception("r", 0.1f)
                new RawPerception.BipolarDirectPerception("r", 0.3f),
                new HaiSOMPerception("s", 2, 0.3f)
                //new AEPerception("a", 0.2f, 15, 2).setLearningRate(0.02).setSigmoid(true)
                //new AEPerception("b", 0.2f, 15).setLearningRate(0.02).setSigmoid(false)
        );

    }

}

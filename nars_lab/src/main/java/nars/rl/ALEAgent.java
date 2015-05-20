package nars.rl;

import automenta.vivisect.Video;
import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.arcade.agents.AbstractAgent;
import jurls.reinforcementlearning.domains.arcade.io.Actions;
import jurls.reinforcementlearning.domains.arcade.io.ConsoleRAM;
import jurls.reinforcementlearning.domains.arcade.io.RLData;
import jurls.reinforcementlearning.domains.arcade.screen.ScreenMatrix;
import nars.NAR;
import nars.gui.NARSwing;
import nars.model.impl.Default;
import nars.nario.ShapePerception;
import nars.rl.example.QVis;
import nars.util.signal.Autoencoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Created by me on 5/20/15.
 */
public class ALEAgent extends AbstractAgent implements RLEnvironment {


    final static String romPath = "/home/me/roms";
    final static String alePath = "/home/me/neuro/ale_0.4.4/ale_0_4";


    final static String namedPipesName = alePath + "/ale_fifo_";
    private BufferedImage buffer;
    private NAR nar;
    private int nextAction = 0;
    private double[] obs = new double[0];
    private int reward;
    private double[] screen = new double[33600];
    private QVis mi;

    public static void main(String[] args) throws IOException, InterruptedException {
        new ALEAgent(
                "space_invaders"
                //"seaquest"
        );
    }

    public ALEAgent(String rom) throws IOException, InterruptedException {
        super();


        String aleCommand = alePath + "/ale -game_controller fifo_named " + romPath + "/" + rom + ".bin";

        System.out.println(aleCommand);

        Process proc = Runtime.getRuntime().exec(aleCommand, new String[] {} , new File(alePath));


        initNARS();

        init(true, namedPipesName);

        run();

        proc.waitFor();

    }

    protected void initNARS() {
        this.nar = new NAR(new Default(2000, 20, 4).setInternalExperience(null) );

        int memoryCyclesPerFrame = 4;

        nar.param.duration.set(memoryCyclesPerFrame * 3);
        nar.setCyclesPerFrame(memoryCyclesPerFrame);

        nar.param.outputVolume.set(0);
        nar.param.decisionThreshold.set(0.65);
        nar.param.shortTermMemoryHistory.set(3);


        QLAgent agent = new QLAgent(nar, "act", "<nario --> good>", this);

        agent.brain.setEpsilon(0.15);
        agent.brain.setAlpha(0.1);

        mi = new QVis(agent);

        /*agent.add(new ShapePerception(new Supplier<BufferedImage>() {
            @Override
            public BufferedImage get() {
                return buffer;
            }
        }));*/
        agent.add(new AEPerception("AE", 0.25f, 64).setLearningRate(0.3));

        Video.themeInvert();
        NARSwing s = new NARSwing(nar);

    }



    @Override
    public long getPauseLength() {
        return 0;
    }

    @Override
    public int selectAction() {
        return nextAction;
    }

    public BufferedImage convert(ScreenMatrix m, BufferedImage img) {
        // Create a new image, of the same width and height as the screen matrix
        if ((img == null) || (img.getWidth()!=m.width) || (img.getHeight()!=m.height)) {
            img = new BufferedImage(m.width, m.height, BufferedImage.TYPE_INT_RGB);
        }

        // Map each pixel
        for (int x = 0; x < m.width; x++)
            for (int y = 0; y < m.height; y++) {
                int index = m.matrix[x][y];
                Color c = converter.colorMap.get(index);
                img.setRGB(x, y, c.getRGB());
            }

        return img;
    }


    public static double[] m(ScreenMatrix screen, double[] s) {
        if (s == null)
            s = new double[screen.width * screen.height];
        int k = 0;
        for (int i = 0; i < screen.width; i++)
            for (int j = 0; j < screen.height; j++)
                s[k++] = screen.matrix[i][j] / 256.0f;
        return s;
    }

    @Override
    public void observe(ScreenMatrix screen, ConsoleRAM ram, RLData rlData) {
        this.reward = rlData.reward;

        this.buffer = convert(screen, this.buffer);
        this.screen = m(screen, this.screen);

        nar.frame(1);
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

    @Override
    public boolean wantsScreenData() {
        return true;
    }

    @Override
    public boolean wantsRamData() {
        return false;
    }

    @Override
    public boolean wantsRLData() {
        return true;
    }

    @Override
    public double[] observe() {
        return screen;
    }

    @Override
    public double getReward() {
        return reward;
    }

    @Override
    public boolean takeAction(int action) {
        nextAction = action;
        return true;
    }

    @Override
    public void frame() {
        mi.frame();
    }

    @Override
    public int numActions() {
        return Actions.numPlayerActions;
    }

    @Override
    public Component component() {
        return null;
    }
}

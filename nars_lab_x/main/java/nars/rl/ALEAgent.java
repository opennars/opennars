package nars.rl;

import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.arcade.agents.AbstractAgent;
import jurls.reinforcementlearning.domains.arcade.io.Actions;
import jurls.reinforcementlearning.domains.arcade.io.ConsoleRAM;
import jurls.reinforcementlearning.domains.arcade.io.RLData;
import jurls.reinforcementlearning.domains.arcade.rl.FeatureMap;
import jurls.reinforcementlearning.domains.arcade.rl.FrameHistory;
import jurls.reinforcementlearning.domains.arcade.screen.ScreenMatrix;
import nars.NAR;
import nars.Video;
import nars.nar.Default;
import nars.rl.example.QVis;
import nars.task.Task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * https://github.com/mgbellemare/Arcade-Learning-Environment/tree/master/src/games/supported
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
    private ScreenMatrix currentFrame;

    public static void main(String[] args) throws IOException, InterruptedException {
        new ALEAgent(
                //"space_invaders"
                //"seaquest"
                //"chopper_command"
                //"enduro"
                //"berzerk"
                "assault"
                //"asteroids"
                //"alien"
                //"atlantis"
                //"battle_zone"
                //"kung_fu_master"
                //"montezuma_revenge"
                //"pitfall"
                //"video_pinball"
                //"zaxxon"
                //"yars_revenge"
                //"wizard_of_wor"
                //"air_raid"
                //"star_gunner"
                //"river_raid"


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

        this.nar = new NAR(new Default(3000, 10, 3).setInternalExperience(null) );
        int memoryCyclesPerFrame = 2;

        nar.param.duration.set(memoryCyclesPerFrame * 3);
        nar.setCyclesPerFrame(memoryCyclesPerFrame);

        nar.param.outputVolume.set(0);
        nar.param.executionThreshold.set(0.65);
        nar.param.shortTermMemoryHistory.set(3);

        nar.input("schizo(I)!");


        QLAgent agent = new QLAgent(nar, "a", "<be --> good>", this);

        agent.ql.brain.setEpsilon(0.1);

        mi = new QVis(agent);

        /*agent.add(new ShapePerception(new Supplier<BufferedImage>() {
            @Override
            public BufferedImage get() {
                return buffer;
            }
        }));*/
        //agent.add(new AEPerception("AE", 0.25f, 64).setLearningRate(0.1));
        //agent.add(new ALEFeaturePerception(0.75f));
        agent.add(new AEALEFeaturePerception(0.75f));

        Video.themeInvert();
        //NARSwing s = new NARSwing(nar);

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
        this.currentFrame = screen;

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
        if (mi!=null)
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

    private class ALEFeaturePerception extends RawPerception implements Perception {

        final FrameHistory h = new FrameHistory(1);
        final FeatureMap m = new FeatureMap(9, 8, 8);

        public ALEFeaturePerception(float conf) {
            super("ALE", conf);
        }


        @Override
        public Iterable<Task> perceive(NAR nar, double[] input, double t) {
            h.addFrame(currentFrame);
            return super.perceive(nar, m.getFeatures(h), t);
        }

    }

    private class AEALEFeaturePerception extends AEPerception implements Perception {

        final FrameHistory h = new FrameHistory(1);
        final FeatureMap m = new FeatureMap(9, 8, 8);

        public AEALEFeaturePerception(float conf) {
            super("ALE", conf, 128, 1);
            setLearningRate(0.01f);
        }

        @Override
        public int numStates() {
            return 576;
        }

        @Override
        public Iterable<Task> perceive(NAR nar, double[] input, double t) {
            h.addFrame(currentFrame);
            double[] vvf = m.getFeatures(h);
            //System.out.println(vvf.length);
            return super.perceive(nar, vvf, t);
        }

    }
}

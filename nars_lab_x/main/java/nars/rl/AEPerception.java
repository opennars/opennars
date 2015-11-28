package nars.rl;

import automenta.vivisect.swing.NWindow;
import jurls.core.utils.MatrixImage;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.task.Task;
import nars.util.signal.Autoencoder;


/**
 * denoising autoencoder
 */
public class AEPerception extends RawPerception {

    private MatrixImage vis;
    private final int history;
    private Autoencoder ae = null;
    private final int nodes;
    private QLAgent agent;

    double noise = 0.001;
    double learningRate = 0.05;
    private double[] ii;
    private int frameDimension;
    boolean sigmoid = true;
    private RLEnvironment env;

    /**
     * present and history input buffer
     */

    public AEPerception(String id, float conf, int nodes) {
        this(id, conf, nodes, 1);
    }

    /**
     * history=1 means no history, just present
     */
    public AEPerception(String id, float conf, int nodes, int history) {
        super(id, conf);

        this.nodes = nodes;
        this.history = history;

        setMin(0);
        setMax(1.0);

        vis = null;
    }

    protected AEPerception vis() {

        new NWindow("AE",
                vis = new MatrixImage(400, 400)
        ).show(400, 400);

        return this;

    }

    public int numStates() {
        return env.numStates();
    }


    @Override
    public void init(RLEnvironment env, QLAgent agent) {
        this.env = env;
        frameDimension = numStates();
        if (history > 1)
            ii = new double[frameDimension * history];
        else
            ii = new double[frameDimension * 1]; //env.observe();

        this.agent = agent;

        ae = new Autoencoder(ii.length, nodes);
    }


    @Override
    public Iterable<Task> perceive(NAR nar, double[] input, double t) {

        if (history > 1) {

            //subtract old input from current input
            for (int i = 0; i < input.length; i++) {
                ii[i] = input[i] - ii[i];
            }

            //shift over
            System.arraycopy(ii, 0, ii, frameDimension, ii.length - frameDimension);

            //copy new input to first frame
            System.arraycopy(input, 0, ii, 0, input.length);
        } else {
            ii = input;
        }

        //System.out.println(Arrays.toString(ii));

        double error = ae.train(ii, learningRate, 0, noise, sigmoid);

        //float conf = (float) (1.0f / (1.0f + error)); //TODO normalize against input mag?

        float conf = (float)(1.0f - error);

        //agent.learn(ae.getOutput(), reward, conf);


        //perception input

        if (vis != null) {

            vis.draw(new MatrixImage.Data2D() {
                @Override
                public double getValue(int x, int y) {
                    return ae.W[y][x];
                }
            }, ae.W.length, ae.W[0].length, -1, 1);
            vis.repaint();
        }

        //System.out.println(error + " " + conf + " " + Arrays.toString(ae.getOutput()));

        return super.perceive(nar, ae.getOutput(), t);

    }

    public AEPerception setLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public AEPerception setSigmoid(boolean sigmoid) {
        this.sigmoid = sigmoid;
        return this;
    }
}

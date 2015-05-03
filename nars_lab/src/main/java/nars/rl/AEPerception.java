package nars.rl;

import automenta.vivisect.swing.NWindow;
import jurls.core.learning.Autoencoder;
import jurls.core.utils.MatrixImage;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.NAR;
import nars.nal.Task;
import nars.nal.term.Term;


/** denoising autoencoder */
    public class AEPerception extends RawPerception {

        private final MatrixImage vis;
        private final int history;
        private Autoencoder ae = null;
        private final int nodes;
        private QLAgent agent;

        double noise = 0.001;
        double learningRate = 0.05;
        private double[] ii;
        private int frameDimension;

        /** present and history input buffer */

        public AEPerception(String id, float conf, int nodes) {
            this(id, conf, nodes, 1);
        }

        /** history=1 means no history, just present */
        public AEPerception(String id, float conf, int nodes, int history) {
            super(id, conf);

            this.nodes = nodes;
            this.history = history;

            new NWindow("AE",
                    vis = new MatrixImage(400, 400)
            ).show(400, 400);
        }

        @Override
        public void init(RLEnvironment env, QLAgent agent) {
            frameDimension = env.numStates();
            if (history > 1)
                ii = new double[frameDimension * history];
            else
                ii = env.observe();

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
            }
            else {
                ii = input;
            }

            //System.out.println(Arrays.toString(ii));

            double error = ae.train(ii, learningRate, 0, noise, true);

            float conf = (float) (1.0f / (1.0f + error)); //TODO normalize against input mag?


            //agent.learn(ae.getOutput(), reward, conf);


            //perception input

            if (vis!=null) {

                vis.draw(new MatrixImage.Data2D() {
                    @Override
                    public double getValue(int x, int y) {
                        return ae.W[y][x];
                    }
                }, ae.W.length, ae.W[0].length, -1, 1);
                vis.repaint();
            }

            return super.perceive(nar, ae.getOutput(), t);

        }
    }

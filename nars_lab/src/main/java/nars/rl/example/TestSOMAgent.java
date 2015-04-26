package nars.rl.example;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import com.google.common.collect.Iterables;
import jurls.core.utils.MatrixImage;
import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.wander.Curiousbot;
import nars.Global;
import nars.NAR;
import nars.ProtoNAR;
import nars.gui.NARSwing;
import nars.nal.term.Term;
import nars.prototype.Default;
import nars.rl.HaiSOMPerception;
import nars.rl.NARQLAgent;
import nars.rl.Perception;
import nars.rl.RawPerception;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * TODO add parameters determining what sensor input is exposed to NARS
 * --how many SOM nodes (# closest), confidence inverse proportional to distance
 * --raw input?  if so, allow segmentation (ex: columns size to form products for each row)
 * TODO measure action coherence as % similarlity of chosen execution to rejected executions
 * TODO use concept desire in accumulating votes\
 * TODO abstract the voting execution into an abstract Operator
 */
public class TestSOMAgent extends JPanel {


    private final NARQLAgent agent;


    //    /** growing neural gas */
//    public static class GNGPerception implements Perception {
//
//        private NeuralGasNet som = null;
//        private final int nodes;
//        private QLAgent agent;
//
//        public GNGPerception(int nodes) {
//            this.nodes = nodes;
//        }
//
//        @Override
//        public int init(RLEnvironment env, QLAgent agent) {
//
//            this.agent = agent;
//
//            som = new NeuralGasNet(env.inputDimension(), nodes);
//            return nodes;
//        }
//
//
//        @Override
//        public void perceive(double[] input, double t) {
//            Node closest = som.learn(input);
//            double d = closest.getLocalDistance();
//            float conf = (float) (1.0f / (1.0f + d)); //TODO normalize against input mag?
//
//            //perception input
//
//            //System.out.println(closest.id + "<" + Texts.n4(closest.getLocalError()) + ">: " + Arrays.toString(input) + " -> " + Arrays.toString(closest.getDataRef()));
//
//            agent.learn(closest.id, reward, conf);
//        }
//
//        /*
//        public void newVisWindow() {
//
//        new GraphPanelNengo<Node,Connection>(ql.som) {
//
//            @Override
//            public Color getEdgeColor(UIEdge<? extends UIVertex> v) {
//
//                Node a = (Node)v.getSource().vertex;
//                Node b = (Node)v.getTarget().vertex;
//                float dist = (float)a.getDistance(b);
//                float o = 1f / (1f + dist);
//                return new Color(o, 0.25f, 1f - o, 0.25f + 0.75f * o);
//            }
//
//        }.newWindow(800, 600);
//
//        }*/
//    }
//
//    /** denoising autoencoder */
//    public static class AEPerception implements Perception {
//
//        private final MatrixImage vis;
//        private final int history;
//        private Autoencoder ae = null;
//        private final int nodes;
//        private QLAgent agent;
//
//        double noise = 0.001;
//        double learningRate = 0.05;
//        private double[] ii;
//        private int frameDimension;
//
//        /** present and history input buffer */
//
//        public AEPerception(int nodes) {
//            this(nodes, 1);
//        }
//
//        /** history=1 means no history, just present */
//        public AEPerception(int nodes, int history) {
//            this.nodes = nodes;
//            this.history = history;
//
//            new NWindow("AE",
//                    vis = new MatrixImage(400, 400)
//            ).show(400, 400);
//        }
//
//        @Override
//        public int init(RLEnvironment env, QLAgent agent) {
//            frameDimension = env.inputDimension();
//            if (history > 1)
//                ii = new double[frameDimension * history];
//            else
//                ii = env.observe();
//
//            this.agent = agent;
//
//            ae = new Autoencoder(ii.length, nodes);
//            return nodes;
//        }
//
//
//
//        @Override
//        public void perceive(double[] input, double t) {
//
//            if (history > 1) {
//
//                //subtract old input from current input
//                for (int i = 0; i < input.length; i++) {
//                    ii[i] = input[i] - ii[i];
//                }
//
//                //shift over
//                System.arraycopy(ii, 0, ii, frameDimension, ii.length - frameDimension);
//
//                //copy new input to first frame
//                System.arraycopy(input, 0, ii, 0, input.length);
//            }
//            else {
//                ii = input;
//            }
//
//            //System.out.println(Arrays.toString(ii));
//
//            double error = ae.train(ii, learningRate, 0, noise, true);
//
//            float conf = (float) (1.0f / (1.0f + error)); //TODO normalize against input mag?
//
//
//            //agent.learn(ae.getOutput(), reward, conf);
//
//
//            //perception input
//
//            if (vis!=null) {
//
//                vis.draw(new Data2D() {
//                    @Override
//                    public double getValue(int x, int y) {
//                        return ae.W[y][x];
//                    }
//                }, ae.W.length, ae.W[0].length, -1, 1);
//                vis.repaint();
//            }
//
//        }
//    }

    //DBSCAN?
    //...
    //Embedded NAR?


    public final NAR nar;



    public TestSOMAgent(RLEnvironment d, ProtoNAR dd, float qLearnedConfidence, Perception... p) {
        super();

        double[] exampleObs = d.observe();



        nar = new NAR(dd);
        nar.param.duration.set(5 * cyclesPerFrame);         //nar.param.duration.setLinear


        agent = new NARQLAgent(nar, d, p) {

            private final MatrixImage mi = new MatrixImage(400, 400);
            private final NWindow nmi = new NWindow("Q", mi).show(400, 400);

            final java.util.List<Term> xstates = new ArrayList();
            final java.util.List<Term> xactions = new ArrayList();


            final Runnable swingUpdate = new Runnable() {

                @Override
                public void run() {
                    repaint();

                    d.component().repaint();


                    if (xstates.size() != states.size()) {
                        xstates.clear();
                        Iterables.addAll(xstates, states);
                    }
                    if (xactions.size() != actions.size()) {
                        xactions.clear();
                        Iterables.addAll(xactions, actions);
                    }

                    repaint();


                    mi.draw(new MatrixImage.Data2D() {
                        @Override
                        public double getValue(int y, int x) {
                            return q(xstates.get(x), xactions.get(y));
                        }
                    }, xstates.size(), xactions.size(), -1, 1);


//                    mi.draw(new Data2D() {
//                        @Override
//                        public double getValue(int x, int y) {
//                            return q(y, x);
//                        }
//                    }, nstates, nactions, -1, 1);
                }
            };


            @Override
            public void onFrame() {
                super.onFrame();

                SwingUtilities.invokeLater(swingUpdate);
            }

        };
        agent.init();



        nar.setCyclesPerFrame(cyclesPerFrame);
        nar.param.shortTermMemoryHistory.set(3);
        nar.param.decisionThreshold.set(0.65);
        nar.param.outputVolume.set(5);




//        new AbstractReaction(nar, Events.CycleEnd.class) {
//            @Override
//            public void event(Class event, Object[] args) {
//                cycle();
//            }
//        };

//        new AbstractReaction(nar, Events.FrameEnd.class) {
//            @Override
//            public void event(Class event, Object[] args) {
//
//            }
//        };

        Video.themeInvert();

        NARSwing s = new NARSwing(nar);

        new Frame().setVisible(true);
        this.setIgnoreRepaint(true);



    }


    private final int cyclesPerFrame = 100;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        /* Create and display the form */
        //RLEnvironment d = new PoleBalancing2D();
        //RLEnvironment d = new Follow1D();
        RLEnvironment d = new Curiousbot();
        //RLEnvironment d = new Tetris(10, 14);
        //RLEnvironment d = new Tetris(10, 8);

        d.newWindow();

        Global.DEBUG = false;
        //Global.TRUTH_EPSILON = 0.01f;
        //Global.BUDGET_EPSILON = 0.02f;
        Global.DERIVATION_PRIORITY_LEAK = 0.85f;
        Global.DERIVATION_DURABILITY_LEAK = 0.85f;
        int concepts = 2000;
        int conceptsPerCycle = 25;

        float qLearnedConfidence = 0.7f; //0.85f; //0 to disable

        //Perception p = new GNGPerception(64);
        //Perception p = new AEPerception(18,2);


        Default dd = new Default(concepts, conceptsPerCycle, 4);
        dd.setTaskLinkBagSize(24);
        dd.setInternalExperience(null);

        TestSOMAgent a = new TestSOMAgent(d, dd, qLearnedConfidence,
                new RawPerception("L", 0.8f),
                /*new RawPerception("P", 0.8f) {
                    @Override
                    public float getFrequency(double d) {
                        // pos or negative binary
                        if (d > 0) return 1;
                        return 0;
                    }
                },*/
                new HaiSOMPerception("A", 4, 0.8f)
                //new HaiSOMPerception("B", 2, 0.8f)
        );



        a.agent.setEpsilon(0.10);

    }


//    protected void train2() {
//        for (int i= 0; i < initialTrainingCycles; i++) {
//            nar.input("move(SELF)!");
//            /*nar.input("move(left)!");
//            nar.input("move(right)!");*/
//        }
//    }
//
//    protected void train(int periods) {
//        int delay = trainingActionPeriod;
//        String t = "";
//        for (int i = 0; i < periods; i++) {
//            String dir = Math.random() < 0.5 ? "left" : "right";
//            t += "move(" + dir + ")! %1.00;" + initialDesireConf + "% \n" +
//                    //delay + "\n" +
//                    //"move(right)! :|: %1.00;" + initialDesireConf + "%\n" +
//                    (int)(Math.random()*delay) + "\n";
//        }
//        nar.input(t);
//    }

//    public void beGood() {
//
//        nar.input("<" + reward + " --> " + goodness + ">! :|:");
//
//    }

}
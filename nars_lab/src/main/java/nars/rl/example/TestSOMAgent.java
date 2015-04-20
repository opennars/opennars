package nars.rl.example;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import jurls.core.learning.Autoencoder;
import jurls.core.utils.MatrixImage;
import jurls.core.utils.MatrixImage.Data2D;
import jurls.reinforcementlearning.domains.PoleBalancing2D;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.event.FrameReaction;
import nars.gui.NARSwing;
import nars.io.Symbols;
import nars.io.Texts;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;
import nars.prototype.Discretinuous;
import nars.rl.BaseQLAgent;
import nars.rl.gng.NeuralGasNet;
import nars.rl.gng.Node;
import nars.rl.hai.Hsom;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import javax.swing.*;
import java.awt.*;

/**
 * TODO add parameters determining what sensor input is exposed to NARS
 * --how many SOM nodes (# closest), confidence inverse proportional to distance
 * --raw input?  if so, allow segmentation (ex: columns size to form products for each row)
 * TODO measure action coherence as % similarlity of chosen execution to rejected executions
 * TODO use concept desire in accumulating votes\
 * TODO abstract the voting execution into an abstract Operator
 */
public class TestSOMAgent extends JPanel {



    /* dimension reduction / input processing implementations */
    public interface Perception {

        /** initialize for a QLAgent that will use it
         *  returns the # dimensions the processed perception
         *  will be represented in
         * */
        public int setEnvironment(RLEnvironment env);

        public void setAgent(QLAgent agent);

        /** process the next vector of input; should call methods (ex: input) on the agent provided by reset */
        public void perceive(double[] input, double reward, double t);
    }


    public static class HaiSOMPerception implements Perception {

        private Hsom som = null;
        private QLAgent agent;
        private RLEnvironment env;

        @Override
        public int setEnvironment(RLEnvironment env) {
            this.env = env;

            som = new Hsom(env.inputDimension()+1, env.inputDimension());
            return som.width()*som.width();
        }

        @Override
        public void setAgent(QLAgent agent) {
            this.agent = agent;
        }

        @Override
        public void perceive(double[] input, double reward, double t) {

            som.learn(input);
            int s = som.winnerx * env.inputDimension() + som.winnery;
            //System.out.println(Arrays.toString(input) + " " + reward );
            //System.out.println(som.winnerx + " " + som.winnery + " -> " + s);

            //System.out.println(Arrays.deepToString(q));
            agent.learn(s, reward, 1f);
        }
    }

    /** growing neural gas */
    public static class GNGPerception implements Perception {

        private NeuralGasNet som = null;
        private final int nodes;
        private QLAgent agent;

        public GNGPerception(int nodes) {
            this.nodes = nodes;
        }

        @Override
        public int setEnvironment(RLEnvironment env) {
            som = new NeuralGasNet(env.inputDimension(), nodes);
            return nodes;
        }

        @Override
        public void setAgent(QLAgent agent) {
            this.agent = agent;
        }

        @Override
        public void perceive(double[] input, double reward, double t) {
            Node closest = som.learn(input);
            double d = closest.getLocalDistance();
            float conf = (float) (1.0f / (1.0f + d)); //TODO normalize against input mag?

            //perception input

            //System.out.println(closest.id + "<" + Texts.n4(closest.getLocalError()) + ">: " + Arrays.toString(input) + " -> " + Arrays.toString(closest.getDataRef()));

            agent.learn(closest.id, reward, conf);
        }
    }

    /** denoising autoencoder */
    public static class AEPerception implements Perception {

        private final MatrixImage vis;
        private Autoencoder ae = null;
        private final int nodes;
        private QLAgent agent;

        double noise = 0.001;
        double learningRate = 0.01;

        public AEPerception(int nodes) {
            this.nodes = nodes;

            new NWindow("AE",
                    vis = new MatrixImage(400, 400)
            ).show(400, 400);
        }

        @Override
        public int setEnvironment(RLEnvironment env) {
            ae = new Autoencoder(env.inputDimension(), nodes);
            return nodes;
        }

        @Override
        public void setAgent(QLAgent agent) {
            this.agent = agent;
        }

        @Override
        public void perceive(double[] input, double reward, double t) {
            double error = ae.train(input, learningRate, 0, noise, false);
            int closest = ae.getMax();

            float conf = (float) (1.0f / (1.0f + error)); //TODO normalize against input mag?

            //perception input

            //System.out.println(closest.id + "<" + Texts.n4(closest.getLocalError()) + ">: " + Arrays.toString(input) + " -> " + Arrays.toString(closest.getDataRef()));

            if (vis!=null) {

                vis.draw(new Data2D() {
                    @Override
                    public double getValue(int x, int y) {
                        return ae.W[y][x];
                    }
                }, ae.W.length, ae.W[0].length, -1, 1);
                vis.repaint();
            }

            agent.learn(closest, reward, conf);
        }
    }
    //Autoencoder
    //GNG
    //DBSCAN?
    //...
    //Embedded NAR?


    abstract public static class QLAgent extends BaseQLAgent {

        private final Perception perception;
        private final RLEnvironment env;
        private final RL rl;

        private final ArrayRealVector actByExpectation;
        private final ArrayRealVector actByPriority;
        //private final ArrayRealVector actByQ;

        public int actByQStrongest = -1; //default q-action if NARS does not specify one by the next frame
        double lastReward = 0;


        /**
         *
         * @param nar
         * @param env
         * @param p
         * @param epsilon randomness factor
         */
        public QLAgent(NAR nar, RLEnvironment env, Perception p, double epsilon) {
            super(nar, p.setEnvironment(env), env.numActions());

            this.env = env;
            this.perception = p;
            this.rl = new RL();

            this.actByPriority = new ArrayRealVector(env.numActions());
            this.actByExpectation = new ArrayRealVector(env.numActions());

            setEpsilon(epsilon);

            nar.on(new Operator("^move") {


                @Override
                protected java.util.List<Task> execute(Operation operation, Term[] args, Memory memory) {

                    if (args.length != 2) { // || args.length==3) { //left, self
                        //System.err.println(this + " ?? " + Arrays.toString(args));
                        return null;
                    }

                    Term ta = ((Operation) operation.getTerm()).getArgument(0);
                    try {
                        int a = Integer.parseInt(ta.toString());
                        rl.desire(a, operation);

                    } catch (NumberFormatException e) {

                    }

                    return null;
                }
            });

            p.setAgent(this);
        }

        @Override
        public Operation getActionOperation(int s) {
            return (Operation) nar.term("move(" + s + ")");
        }

        @Override
        protected void initializeQ(int s, int a) {
            System.out.println(qterm(s, a));
        }

        @Override
        public Term getStateTerm(int s) {
            //return nar.term("<state --> [s" + s + "]>");
            //int row = s / dimensions;
            //int column = s % dimensions;

            return nar.term("{s" + s + "}");
            //return nar.term("s" + s);
        }

        @Override
        public int learn(int state, double reward, int nextAction, float confidence) {

            float freq = 1.0f;
            nar.input(getStateTerm(state) + ". :|: %" + freq + ";" + confidence + "%");


            return super.learn(state, reward, nextAction, confidence);
        }



        /** interface to an RL world/experiment/etc..
         * implements a per-frame reaction in which an action
         * is decided either by NARS or an Agent, the world updated,
         * and the new input processed by the Agent's perception interface */
        class RL extends FrameReaction {

            public RL() {
                super(nar);

            }

            public void desire(int action, Operation operation) {
                desire(action, operation.getTask().getPriority(), operation.getTaskExpectation());
            }

            protected void desire(int action, float priority, float expectation) {
                actByPriority.addToEntry(action, priority);
                actByExpectation.addToEntry(action, expectation);
            }



            @Override
            public void onFrame() {
                QLAgent.this.onFrame();
            }


        };

        /** decides which action, TODO make this configurable */
        public synchronized int decide() {

            double m = actByExpectation.getL1Norm();
            if (m == 0) return -1;

            int winner = actByExpectation.getMaxIndex();
            if (winner == -1) return -1; //no winner?

            RealVector normalized = actByExpectation.unitVector();
            double alignment = normalized.dotProduct(actByExpectation);

            System.out.print("NARS act: '" + winner + "' (from " + actByExpectation + " total executions) vs. '" + actByQStrongest + "' qAct");
            System.out.println("  volition_coherency: " + Texts.n4(alignment * 100.0) + "%" );

            actByExpectation.mapMultiplyToSelf(0); //zero
            actByPriority.mapMultiplyToSelf(0); //zero

            return winner;
        }

        protected void onFrame() {
            long now = nar.time();

            int action = decide();

            if (action==-1) {
                action = actByQStrongest;
                System.out.print("QL act: " + action);

                if (action == -1) {
                    //no qAction specified either, choose random
                    action = (int)(Math.random() * env.numActions());
                }

                /** introduce belief or goal for the QL action */
                act(action, Symbols.GOAL);
            }

            env.takeAction(action);

            env.worldStep();


            double r = env.reward();

            //double dr = r - lastReward;

            lastReward = r;

            double[] o = env.observe();

            //System.out.println(Arrays.toString(o) + " " + r);

            System.out.println("  reward=" + Texts.n4(r));

            perception.perceive(o, r, nar.time());
            actByQStrongest = getNextAction();


        }
    }

    private final QLAgent ql;
    private final MatrixImage mi;


    private final int cyclesPerFrame = 50;


    public final NAR nar;





    public TestSOMAgent(RLEnvironment d, Perception p) {
        super();

        double[] exampleObs = d.observe();

        nar = new NAR(new Discretinuous(2000, 10, 4).setInternalExperience(null));


        final Runnable swingUpdate = new Runnable() {

            @Override
            public void run() {
                repaint();

                d.component().repaint();

                mi.draw(new Data2D() {
                    @Override
                    public double getValue(int x, int y) {
                        return ql.q(y, x);
                    }
                }, ql.nstates, ql.nactions, -1, 1);
            }
        };


        ql = new QLAgent(nar, d, p, 0.02) {
            @Override
            public void onFrame() {
                super.onFrame();

                SwingUtilities.invokeLater(swingUpdate);
            }

        };
        ql.init();



        nar.setCyclesPerFrame(cyclesPerFrame);
        nar.param.shortTermMemoryHistory.set(1);
        nar.param.duration.set(5);         //nar.param.duration.setLinear
        nar.param.decisionThreshold.set(0.7);
        nar.param.outputVolume.set(5);


        new AbstractReaction(nar, Events.CycleEnd.class) {
            @Override
            public void event(Class event, Object[] args) {
                cycle();
            }
        };

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

        /*
        new GraphPanelNengo<Node,Connection>(ql.som) {

            @Override
            public Color getEdgeColor(UIEdge<? extends UIVertex> v) {

                Node a = (Node)v.getSource().vertex;
                Node b = (Node)v.getTarget().vertex;
                float dist = (float)a.getDistance(b);
                float o = 1f / (1f + dist);
                return new Color(o, 0.25f, 1f - o, 0.25f + 0.75f * o);
            }

        }.newWindow(800, 600);
        */

        new NWindow("Q",
                mi = new MatrixImage(400, 400)
        ).show(400, 400);

    }




    protected void cycle() {


    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        Global.DEBUG = false;

        /* Create and display the form */
        RLEnvironment d = new PoleBalancing2D();
        //RLEnvironment d = new Follow1D();
        //RLEnvironment d = new Curiousbot();
        //RLEnvironment d = new Tetris(10, 14);

        d.newWindow();

        //Perception p = new GNGPerception(64);
        //Perception p = new HaiSOMPerception();
        Perception p = new AEPerception(8);

        new TestSOMAgent(d, p);


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
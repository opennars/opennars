package nars.rl.example;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import jurls.core.utils.MatrixImage;
import jurls.core.utils.MatrixImage.Data2D;
import jurls.reinforcementlearning.domains.RLEnvironment;
import jurls.reinforcementlearning.domains.wander.Curiousbot;
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
import nars.rl.HaiQNAR;
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


    private final RLEnvironment environment;
    private final RL rl;

    abstract public static class HsomQNAR extends HaiQNAR {

        private final Hsom som;
        private final int dimensions;

        public HsomQNAR(NAR nar, int dimensions, int actions) {
            super(nar, dimensions * dimensions, actions);

            this.dimensions = dimensions;
            som = new Hsom(dimensions, dimensions);
        }

        @Override
        protected void initializeQ(int s, int a) {
            System.out.println(qterm(s, a));
        }

        @Override
        public Term getStateTerm(int s) {
            //return nar.term("<state --> [s" + s + "]>");
            int row = s / dimensions;
            int column = s % dimensions;
            return nar.term("<state --> [(s" + row + ",s" + column + ")]>");
        }

        public void learn(double[] input, double reward) {
            som.learn(input);
            int s = som.winnerx * dimensions + som.winnery;
            //System.out.println(Arrays.toString(input) + " " + reward );
            //System.out.println(som.winnerx + " " + som.winnery + " -> " + s);

            //System.out.println(Arrays.deepToString(q));
            super.learn(s, reward);
        }
    }

    abstract public static class HgngQNAR extends HaiQNAR {

        private final NeuralGasNet som;
        private final int dimensions;

        public HgngQNAR(NAR nar, int dimensions, int actions) {
            this(nar, dimensions, dimensions * dimensions /* * dimensions */, actions);
        }

        public HgngQNAR(NAR nar, int dimensions, int somSize, int actions) {
            super(nar, somSize, actions);

            setEpsilon(0); //no randomness

            this.dimensions = dimensions;
            som = new NeuralGasNet(dimensions, somSize);
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

        public int learn(double[] input, double reward) {

            Node closest = som.learn(input);
            double d = closest.getLocalDistance();

            //perception input

            //System.out.println(closest.id + "<" + Texts.n4(closest.getLocalError()) + ">: " + Arrays.toString(input) + " -> " + Arrays.toString(closest.getDataRef()));

            float freq = 1.0f;
            float conf = (float) (1.0f / (1.0f + d)); //TODO normalize against input mag?
            nar.input(getStateTerm(closest.id) + ". :|: %" + freq + ";" + conf + "%");


            return super.learn(closest.id, reward);

        }
    }

    private final HgngQNAR ql;
    private final MatrixImage mi;


    private final int cyclesPerFrame = 50;


    public final NAR nar;



    /** interface to an RL world/experiment/etc..
     * implements a per-frame reaction in which an action
     * is decided either by NARS or an Agent, the world updated,
     * and the new input processed by the Agent's perception interface */
    public static class RL extends FrameReaction {

        private final NAR nar;
        private final RLEnvironment environment;
        private final ArrayRealVector actByExpectation;
        private final ArrayRealVector actByPriority;
        private final HgngQNAR agent;
        //private final ArrayRealVector actByQ;

        public int actByQStrongest = -1; //default q-action if NARS does not specify one by the next frame
        double lastReward = 0;

        public RL(HgngQNAR agent, RLEnvironment environment) {
            super(agent.nar);

            this.nar = agent.nar;
            this.agent = agent;
            this.environment = environment;


            this.actByPriority = new ArrayRealVector(environment.numActions());
            this.actByExpectation = new ArrayRealVector(environment.numActions());
        }

        public void desire(int action, Operation operation) {
            desire(action, operation.getTask().getPriority(), operation.getTaskExpectation() );
        }

        protected void desire(int action, float priority, float expectation) {
            actByPriority.addToEntry(action, priority);
            actByExpectation.addToEntry(action, expectation);
        }

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

        @Override
        public void onFrame() {
            long now = nar.time();

            int action = decide();

            if (action==-1) {
                action = actByQStrongest;
                System.out.print("QL act: " + action);

                if (action == -1) {
                    //no qAction specified either, choose random
                    action = (int)(Math.random() * environment.numActions());
                }

                /** introduce belief or goal for the QL action */
                agent.act(action, Symbols.GOAL);
            }

            environment.takeAction(action);

            environment.worldStep();


            double r = environment.reward();

            //double dr = r - lastReward;

            lastReward = r;

            double[] o = environment.observe();

            //System.out.println(Arrays.toString(o) + " " + r);

            System.out.println("  reward=" + Texts.n4(r));

            actByQStrongest = agent.learn(o, r);



        }


    };


    public TestSOMAgent(RLEnvironment d, int somSize) {
        super();

        this.environment = d;
        double[] exampleObs = d.observe();

        nar = new NAR(new Discretinuous(2000, 10, 4).setInternalExperience(null));



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

        ql = new HgngQNAR(nar, exampleObs.length, somSize, environment.numActions()) {
            @Override
            public Operation getActionOperation(int s) {
                return (Operation) nar.term("move(" + s + ")");
            }
        };
        ql.init();



        rl = new RL(ql, environment) {

            @Override
            public void onFrame() {
                super.onFrame();

                SwingUtilities.invokeLater(swingUpdate);
            }
        };



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


    final Runnable swingUpdate = new Runnable() {

        @Override
        public void run() {
            repaint();

            environment.component().repaint();

            mi.draw(new Data2D() {
                @Override
                public double getValue(int x, int y) {
                    return ql.q(y, x);
                }
            }, ql.nstates, ql.nactions, -1, 1);
        }
    };

    protected void cycle() {


    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        Global.DEBUG = true;

        /* Create and display the form */
        //RLEnvironment d = new PoleBalancing2D();
        //RLEnvironment d = new Follow1D();
        RLEnvironment d = new Curiousbot();
        //RLEnvironment d = new Tetris(10, 14);

        d.newWindow();

        new TestSOMAgent(d, 16);


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
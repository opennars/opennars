package nars.rl.example;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import jurls.core.utils.MatrixImage;
import jurls.core.utils.MatrixImage.Data2D;
import jurls.reinforcementlearning.domains.RLDomain;
import jurls.reinforcementlearning.domains.tetris.Tetris;
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


    private final RLDomain domain;

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


    Task strongest = null; //current action
    int exeCount = 0;

    public TestSOMAgent(RLDomain d, int somSize) {
        super();

        this.domain = d;
        double[] exampleObs = d.observe();

        nar = new NAR(new Discretinuous(2000, 10, 4).setInternalExperience(null));

        AbstractReaction rl = new FrameReaction(nar) {

            public int nextQAction = -1; //default q-action if NARS does not specify one by the next frame
            double lastReward = 0;

            @Override
            public void onFrame() {
                long now = nar.time();

                int action = -1;

                if (strongest!=null) {
                    Term ta = ((Operation) strongest.getTerm()).getArgument(0);
                    action = Integer.parseInt(ta.toString());
                    System.out.print("NARS act: '" + action + "' (from " + exeCount + " total executions) vs. '" + nextQAction + "' qAct");
                }
                if (action == -1) {
                    action = nextQAction;
                    System.out.print("QL act: " + action);

                    if (action == -1) {
                        //no qAction specified either, choose random
                        action = (int)(Math.random() * domain.numActions());
                    }

                    ql.act(action, Symbols.JUDGMENT);
                }

                domain.takeAction(action);

                domain.worldStep();


                double r = domain.reward();

                //double dr = r - lastReward;

                lastReward = r;

                double[] o = domain.observe();

                //System.out.println(Arrays.toString(o) + " " + r);

                System.out.println("  reward=" + Texts.n4(r));

                nextQAction = ql.learn(o, r);

                exeCount = 0;
                strongest = null;

                SwingUtilities.invokeLater(swingUpdate);


            }
        };
        nar.on(new Operator("^move") {



            long lastWorldStep = 0;



            @Override
            protected java.util.List<Task> execute(Operation operation, Term[] args, Memory memory) {

                if (args.length != 2) { // || args.length==3) { //left, self
                    //System.err.println(this + " ?? " + Arrays.toString(args));
                    return null;
                }

                Term ta = ((Operation) operation.getTerm()).getArgument(0);
                try {
                    Integer.parseInt(ta.toString());

                    exeCount++;

                    Task newTask = operation.getTask();
                    if (strongest == null) strongest = newTask;
                    else {
                        if (strongest.getDesire().getExpectation() > newTask.getDesire().getExpectation())
                            strongest = newTask;
                    }

                } catch (NumberFormatException e) {

                }

                return null;
            }
        });


        ql = new HgngQNAR(nar, exampleObs.length, somSize, domain.numActions()) {
            @Override
            public Operation getActionOperation(int s) {
                return (Operation) nar.term("move(" + s + ")");
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


    final Runnable swingUpdate = new Runnable() {

        @Override
        public void run() {
            repaint();

            domain.component().repaint();

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

        Global.DEBUG = false;

        /* Create and display the form */
        //RLDomain d = new PoleBalancing2D();
        //RLDomain d = new Follow1D();
        //RLDomain d = new Curiousbot();
        RLDomain d = new Tetris(10, 14);

        d.newWindow();

        new TestSOMAgent(d, 100);


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
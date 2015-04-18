package nars.rl.example;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import jurls.core.utils.MatrixImage;
import jurls.core.utils.MatrixImage.Data2D;
import jurls.reinforcementlearning.domains.RLDomain;
import jurls.reinforcementlearning.domains.wander.Curiousbot;
import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.gui.NARSwing;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;
import nars.prototype.Default;
import nars.rl.HaiQNAR;
import nars.rl.gng.NeuralGasNet;
import nars.rl.gng.Node;
import nars.rl.hai.Hsom;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @author patrick.hammer
 */
public class TestSOMAgent extends JPanel {

    static {
        Global.DEBUG = true;
    }

    private final RLDomain domain;

    abstract public static class HsomQNAR extends HaiQNAR {

        private final Hsom som;
        private final int dimensions;

        public HsomQNAR(NAR nar, int dimensions, int actions) {
            super(nar, dimensions*dimensions, actions);

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
            return nar.term("<state --> s" + s + ">");
        }

        public void learn(double[] input, double reward) {

            Node closest = som.learn(input);
            //System.out.println(closest.id + " :: " + Arrays.toString(input) + " -> " + Arrays.toString(closest.getDataRef()));
            super.learn(closest.id, reward);

        }
    }

    private final HgngQNAR ql;
    private final MatrixImage mi;


    private final int cyclesPerFrame = 30;


    public final NAR nar;


    public TestSOMAgent(RLDomain d) {
        super();

        this.domain = d;
        double[] exampleObs = d.observe();

        nar = new NAR(new Default(2000,10,4).setInternalExperience(null));

        nar.on(new Operator("^move") {


            double lastReward= 0;

            long lastWorldStep = 0;

            @Override
            protected java.util.List<Task> execute(Operation operation, Term[] args, Memory memory) {

                if (args.length != 2) { // || args.length==3) { //left, self
                    System.err.println(this + " ?? " + Arrays.toString(args));
                    return null;
                }

                try {
                    int action = Integer.parseInt(args[0].toString());

                    domain.takeAction(action);

                    long now = nar.time();
                    long dt = now - lastWorldStep;

                    for (int i = 0; i < dt; i++)
                        domain.worldStep();

                    lastWorldStep = now;

                    double r = domain.reward();

                    //double dr = r - lastReward;




                    lastReward = r;

                    double[] o = domain.observe();

                    System.out.println(Arrays.toString(o) + " " + r);


                    ql.learn(o, r);

                }
                catch (NumberFormatException e) {

                }


                return null;
            }
        });



        ql = new HgngQNAR(nar, exampleObs.length, exampleObs.length * 6, domain.numActions()) {
            @Override public Operation getActionOperation(int s) {
                return (Operation)nar.term("move(" + s + ")");
            }
        };
        ql.init();

        nar.setCyclesPerFrame(cyclesPerFrame);
        nar.param.shortTermMemoryHistory.set(1);
        nar.param.duration.set(5);         //nar.param.duration.setLinear
        nar.param.decisionThreshold.set(0.75);
        nar.param.outputVolume.set(5);


        new AbstractReaction(nar, Events.CycleEnd.class) {
            @Override public void event(Class event, Object[] args) {
                cycle();
            }
        };

        new AbstractReaction(nar, Events.FrameEnd.class) {
            @Override public void event(Class event, Object[] args) {

                //repaint();

                d.component().repaint();

                mi.draw(new Data2D() {
                    @Override
                    public double getValue(int x, int y) {
                        return ql.q(y, x);
                    }
                }, ql.nstates, ql.nactions, -1, 1);
            }
        };

        Video.themeInvert();

        NARSwing s = new NARSwing(nar);

        new Frame().setVisible(true);
        this.setIgnoreRepaint(true);


        new NWindow("Q",
                mi = new MatrixImage(400,400)
        ).show(400, 400);

    }



    protected void cycle() {


    }





    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Create and display the form */
        //RLDomain d = new PoleBalancing2D();
        //RLDomain d = new Follow1D();
        RLDomain d = new Curiousbot();
        //RLDomain d = new Tetris(4,8);

        d.newWindow();

        new TestSOMAgent(d);



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
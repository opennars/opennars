package nars.rl.example;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import jurls.core.utils.MatrixImage;
import jurls.core.utils.MatrixImage.Data2D;
import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.gui.NARSwing;
import nars.nal.*;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;
import nars.prototype.Default;
import nars.rl.BaseQLAgent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

/**
 * @author patrick.hammer
 */
public class TwoPointRegulatorAgent extends JPanel {

    static {
        Global.DEBUG = true;
    }

    private final BaseQLAgent ql;
    private final MatrixImage mi;


    private final int cyclesPerFrame = 50;
    float drawXScale = 0.5f;
    int historySize = (int)(800 / drawXScale);

    String target = "target";

    int speed = 5;
    boolean speedProportionalToExpectation = false;


    static int targetX = 0; //80 230
    float x = 30;

    Deque<State> history = new ArrayDeque<>();



    public class Move extends Operator {


        private float closeEnough = speed; //tolerance threshold
        private float distance;

        public Move() {
            super("^move");
        }

        @Override
        protected java.util.List<Task> execute(Operation operation, Term[] args, Memory memory) {

            if (args.length == 3) {
                throw new RuntimeException("how did this get derived");
            }

            if (args.length == 1) {
                String t = Math.random() < 0.5 ? ("left") : ("right");
                nar.input("move(" + t + ")!");
                return null;
            }
            else if (args.length != 2) { // || args.length==3) { //left, self
                System.err.println(this + " ?? " + Arrays.toString(args));
                return null;
            }

            long now = nar.time();


            float dx;
            float exp = operation.getTask().sentence.truth.getExpectation();
            if (speedProportionalToExpectation) {
                dx = speed * exp;
            } else {
                dx = speed;
            }


            if (args[0].toString().equals("left")) {
                x -= dx;
            } else if (args[0].toString().equals("right")) {
                x += dx;
            } else if (args[0].toString().equals("center")) {
                //nothing
            } else {
                System.err.println(this + " ?? " + Arrays.toString(args));
                return null;
            }


            System.out.println(operation.getTask().getExplanation());

            double oldDistance = distance;
            distance = Math.abs(x - targetX);
            boolean here = distance < closeEnough;
            double delta = distance - oldDistance;

            boolean closer = delta < 0;

            boolean further = delta > 0;
            int state;
            double reward;
            if (here) {
                state = 0;
                reward = 1.0;
            } else if (x > targetX) {
                state = 1;
                reward = closer ?  0.5 : -1.0;
            } else { // if (x < targetX)
                state = 2;
                reward = closer ?  0.5 : -1.0;
            }

            ql.learn(state, reward, 1f);

            return null;

        }
    }




    private String state(boolean left, boolean right) {

        String x;

        if ((!left && !right) || (left && right))
            x = "center";

        else if (left)
            x = "left";

        else //if(right)
            x = "right";

        return "<" + target + " --> " + x + ">";

    }


    public final NAR nar;


    public TwoPointRegulatorAgent() {
        super();

        nar = new NAR(new Default().setInternalExperience(null));
        nar.on(new Move());

        ql = new BaseQLAgent(nar, 3, 3) {


            @Override public Term getStateTerm(int s) {
                switch (s) {
                    case 0: return nar.term(state(false,false));
                    case 1: return nar.term(state(true,false));
                    case 2: return nar.term(state(false,true));
                }
                return null;
            }

            @Override public Operation getActionOperation(int s) {
                switch(s) {
                    case 0: return (Operation)nar.term("move(center)");
                    case 1: return (Operation)nar.term("move(left)");
                    case 2: return (Operation)nar.term("move(right)");
                    //case 3: return (Operation)nar.term("move(SELF)"); //random
                }
                return null;
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

                repaint();

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
        history(x, nar.memory.emotion.happy(), nar.memory.emotion.busy());
    }



    public static class State {
        public final float happiness;
        public final float x;
        private final float busy;

        public State(float x, float happiness, float busy) {
            this.happiness = happiness;
            this.busy = busy;
            this.x = x;
        }

    }


    private void history(float x, float happiness, float busy) {
        synchronized (history) {
            while (history.size() >= historySize)
                history.removeFirst();

            history.add(new State(x, happiness, busy));
        }

    }

    private void doDrawing(Graphics g) {


        int dy = getHeight() / 2;

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.gray);
        g2d.fillRect(0, dy + targetX - speed / 4, getWidth(), speed / 2);

        int i = 0;
        synchronized (history) {
            for (State s : history) {
                int x = Math.round(s.x);
                float happiness = s.happiness;
                g2d.setColor(new Color(Video.colorHSB(happiness, 0.75f, 0.75f, 0.5f)));
                int r = (int) (speed * (0.2f + 0.8f * s.busy));
                g2d.fillRect((int) (i * drawXScale), dy + x - r / 2, 1, r);
                i++;
            }
        }

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }


    public class Frame extends JFrame implements KeyListener {


        /**
         * Creates new form twoPointRegulator
         */
        public Frame() {
            initComponents();
            this.setTitle("Experience Predictive Control");
            this.setSize(1400, 400);
            this.addKeyListener(this);
        }

        /**
         * This method is called from within the constructor to initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is always
         * regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

            Container drawPanel1 = TwoPointRegulatorAgent.this;

            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            GroupLayout drawPanel1Layout = new GroupLayout(drawPanel1);
            drawPanel1.setLayout(drawPanel1Layout);
            drawPanel1Layout.setHorizontalGroup(
                    drawPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGap(0, 380, Short.MAX_VALUE)
            );
            drawPanel1Layout.setVerticalGroup(
                    drawPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGap(0, 278, Short.MAX_VALUE)
            );

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(drawPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addContainerGap())
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(drawPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addContainerGap())
            );

            pack();
        }// </editor-fold>//GEN-END:initComponents


        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                targetX += 10;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                targetX -= 10;
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                targetX -= 10;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                targetX += 10;
            }
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Create and display the form */
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TwoPointRegulatorAgent();
            }
        });
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
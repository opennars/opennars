//package nars.rl.example;
//
//import automenta.vivisect.Video;
//import nars.Events;
//import nars.Global;
//import nars.NAR;
//import nars.event.NARReaction;
//import nars.gui.NARSwing;
//import nars.nal.Task;
//
//import nars.nal.nal8.Operator;
//import nars.nal.term.Term;
//import nars.model.impl.Default;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Deque;
//
///**
// * @author patrick.hammer
// */
//public class TwoPointRegulator extends JPanel {
//
//    static {
//        Global.DEBUG = true;
//    }
//
//    final int targetCyclesMin = 100;
//    final int targetCyclesMax = targetCyclesMin;
//    int targetCycles = targetCyclesMin;
//
//    final int beGoodPeriod = targetCyclesMin;
//
//    private final int cyclesPerFrame = 1;
//    float drawXScale = 0.5f;
//    int historySize = (int)(800 / drawXScale);
//
//    final int trainingActionPeriod = targetCyclesMin;
//    final int initialTrainingCycles = 25;
//
//    float initialDesireConf = 0.7f;
//    int speed = 15;
//
//    //String self = "SELF";
//    String target = "good";
//    String reward = "good";
//    String goodness = "reward";
//    //String goodness = "[good]";
//
//
//    int movement = 0;
//    int lastMovement = 0;
//    Deque<State> history = new ArrayDeque<>();
//    boolean speedProportionalToExpectation = true;
//    private float closeEnough = 0.5f; //tolerance threshold
//    private float distance;
//    private boolean here;
//
//    static int setpoint = 0; //80 230
//    float x = 30;
//
//    public class move extends Operator {
//
//        public move() {
//            super("^move");
//        }
//
//        @Override
//        protected java.util.List<Task> execute(Operation operation, Term[] args) {
//
//            if (args.length == 3) {
//                throw new RuntimeException("how did this get derived");
//            }
//
//            if (args.length == 1) {
//                String t = Math.random() < 0.5 ? ("left") : ("right");
//                nar.input("move(" + t + ")!");
//                return null;
//            }
//            else if (args.length != 2) { // || args.length==3) { //left, self
//                System.err.println(this + " ?? " + Arrays.toString(args));
//                return null;
//            }
//
//            long now = nar.time();
//               /* if (now - lastMovementAt < minCyclesPerMovement) {
//                    moving();
//                    return null;
//                }*/
//
//            float dx;
//            if (speedProportionalToExpectation) {
//                dx = speed * 0.5f + 0.5f * operation.getTask().sentence.truth.getExpectation();
//                //dx = speed * operation.getTask().sentence.truth.getExpectation();
//                //dx = speed * operation.getDesire(memory);
//            } else {
//                dx = speed;
//            }
//
//
//            if (args[0].toString().equals("left")) {
//                x -= dx;
//            } else if (args[0].toString().equals("right")) {
//                x += dx;
//            } else {
//                System.err.println(this + " ?? " + Arrays.toString(args));
//                return null;
//            }
//
//            movement++;
//            // lastMovementAt = now;
//
//            double delta = updateDistance();
//
//            boolean closer = delta < 0;
//
//            if (here || closer) {
//                System.out.println("GOOD:\n" + operation.getTask().getExplanation());
//                return Arrays.asList(good(here ? 0.95f : 0.85f));
//            } else {
//                if (x > setpoint) {
//                    System.out.println("BAD:\n" + operation.getTask().getExplanation());
//                    bad(true, false);
//
//                } else { // (x > setpoint - thresh)
//                    System.out.println("BAD:\n" + operation.getTask().getExplanation());
//                    bad(false, true);
//                }
//            }
//            return null;
//
//        }
//    }
//
//
//    public void beGood() {
//
//        nar.input("<" + reward + " --> " + goodness + ">! :|:");
//
//        //nar.input("move(SELF)! :|:");
//
//        /*if (!here) {
//            nar.input("<" + reward + " --> " + goodness + ">. :|: %0.05;0.65%");
//        }*/
//        //nar.input("move(left)@");
//        //nar.input("move(right)@");
//    }
//
////    public void moving() {
////        nar.input("<" + self + " --> [moving]>. :|:");
////    }
////
////    public void beGoodNow() {
////        nar.input("<" + self + " --> [good]>! :|:");
////    }
//
//    public Task good(float conf) {
//        return nar.task("<" + reward + " --> " + goodness + ">. :|: %0.90;" + conf + "%");
//    }
//
//    public void bad() {
//        String b = "<" + reward + " --> " + goodness + ">" + ". :|: %0.05;0.90%"; //punishment
//        nar.input(b);
//    }
//    public void bad(boolean left, boolean right) {
//        bad();
//        java.util.List<String> cb = getTargetTerm2(left, right);
//        for (String tt : cb)
//            nar.input(tt + ". :|: %0.95;0.90%");
//    }
//
//    public void target(boolean left, boolean right) {
//        for (String tt : getTargetTerm2(left, right))
//            nar.input(tt + ". :|: %0.95;0.90%");
//    }
//
//    private java.util.List<String> getTargetTerm2(boolean left, boolean right) {
//
//        java.util.List<String> l = new ArrayList(3);
//
//        if (left)
//            l.add("<left --> " + target + ">");
//        else
//            l.add("(--,<left --> " + target + ">)");
//
//
//
//        if (right)
//            l.add("<right --> " + target + ">");
//        else
//            l.add("(--,<right --> " + target + ">)");
//
//
//        return l;
//
//    }
//    private String getTargetTerm1(boolean left, boolean right, String... additional) {
//
//        String term = "(&|,";
//        if (left)
//            term += "<move(left) --> " + target + ">";
//        else
//            term += "<(--,move(left)) -->" + target + ">";
//
//        term += ",";
//
//        if (right)
//            term += "<move(right) --> " + target + ">";
//        else
//            term += "<(--,move(right)) -->" + target + ">";
//
//        for (String s : additional) {
//            term += "," + s;
//        }
//
//        term += ")";
//
//        return term;
//    }
//
//    protected double updateDistance() {
//        double oldDistance = distance;
//        distance = Math.abs(x - setpoint);
//        here = distance < closeEnough;
//        return distance - oldDistance;
//    }
//
//    NAR nar;
//
//    public TwoPointRegulator() {
//        super();
//
//        updateDistance();
//
//        new Frame().setVisible(true);
//
//        this.setIgnoreRepaint(true);
//
//        //Global.TEMPORAL_INDUCTION_SAMPLES=0;
//        ////   Parameters.DERIVATION_DURABILITY_LEAK=0.1f;
//        //// Parameters.DERIVATION_PRIORITY_LEAK=0.1f;
//        //Global.CURIOSITY_ALSO_ON_LOW_CONFIDENT_HIGH_PRIORITY_BELIEF = false;
//
//        //Parameters.DEFAULT_JUDGMENT_CONFIDENCE = 0.95f; //made this non-final
//
//        nar = new NAR(new Default().setInternalExperience(null));
//
//        //nar.on(new DeriveOnlyDesired());
//
//        nar.on(new move());
//
//        nar.setCyclesPerFrame(cyclesPerFrame);
//        nar.param.shortTermMemoryHistory.set(2);
//        nar.param.duration.set(cyclesPerFrame*5);
//        //nar.param.duration.setLinear
//
//
//        nar.param.decisionThreshold.set(0.6);
//
//        nar.param.outputVolume.set(5);
//
//        Video.themeInvert();
//        NARSwing s = new NARSwing(nar);
//
//
//        new NARReaction(nar, Events.CycleEnd.class) {
//
//            @Override
//            public void event(Class event, Object[] args) {
//
//                boolean hasMoved = (movement != lastMovement);
//                lastMovement = movement;
//
//                history(x, nar.memory.emotion.happy(), nar.memory.emotion.busy());
//
//                updateDistance();
//
////                if (nar.time() % feedbackCycles == 0) {
////                    if (here)
////                        nar.input(good());
////                }
//
//                if (distance > speed * 10) {
//                    //most often, farthest
//                    targetCycles = targetCyclesMin;
//                } else if (distance > speed * 5) {
//                    targetCycles = (targetCyclesMin + targetCyclesMax) / 2;
//                } else {
//                    //least often, closest
//                    targetCycles = targetCyclesMax;
//                }
//
//                if (nar.time() % targetCycles == 0) {
//                    if (here) {
//                        //target("here", 1.0f);
//                        target(false, false);
//                    } else if (x > setpoint) {
//                        //target("here", 0.0f);
//                        target(true, false);
//                    } else { // if (x < setpoint)
//                        //target("here", 0.0f);
//                        target(false, true);
//                    }
//
//                }
//
//                if (nar.time() % beGoodPeriod == 0) {
//                    beGood();
//                }
//
//            }
//        };
//        new NARReaction(nar, Events.FrameEnd.class) {
//
//            @Override
//            public void event(Class event, Object[] args) {
//                repaint();
//            }
//
//        };
//
//
//        //train(initialTrainingCycles);
//        train2();
//
//    }
//
//
//    protected void train2() {
//        for (int i= 0; i < initialTrainingCycles; i++)
//            nar.input("move(SELF)!");
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
//
//    public static class State {
//        public final float happiness;
//        public final float x;
//        private final float busy;
//
//        public State(float x, float happiness, float busy) {
//            this.happiness = happiness;
//            this.busy = busy;
//            this.x = x;
//        }
//
//    }
//
//
//    private void history(float x, float happiness, float busy) {
//        synchronized (history) {
//            while (history.size() >= historySize)
//                history.removeFirst();
//
//            history.add(new State(x, happiness, busy));
//        }
//
//    }
//
//    private void doDrawing(Graphics g) {
//
//
//        int dy = getHeight() / 2;
//
//        Graphics2D g2d = (Graphics2D) g;
//
//        g2d.setColor(Color.black);
//        g2d.fillRect(0, 0, getWidth(), getHeight());
//
//        g2d.setColor(Color.gray);
//        g2d.fillRect(0, dy + setpoint - speed / 4, getWidth(), speed / 2);
//
//        int i = 0;
//        synchronized (history) {
//            for (State s : history) {
//                int x = Math.round(s.x);
//                float happiness = s.happiness;
//                g2d.setColor(new Color(Video.colorHSB(happiness, 0.75f, 0.75f, 0.5f)));
//                int r = (int) (speed * (0.2f + 0.8f * s.busy));
//                g2d.fillRect((int) (i * drawXScale), dy + x - r / 2, 1, r);
//                i++;
//            }
//        }
//
//    }
//
//    @Override
//    public void paintComponent(Graphics g) {
//
//        super.paintComponent(g);
//        doDrawing(g);
//    }
//
//
//    public class Frame extends javax.swing.JFrame implements KeyListener {
//
//
//        /**
//         * Creates new form twoPointRegulator
//         */
//        public Frame() {
//            initComponents();
//            this.setTitle("Experience Predictive Control");
//            this.setSize(1400, 400);
//            this.addKeyListener(this);
//        }
//
//        /**
//         * This method is called from within the constructor to initialize the form.
//         * WARNING: Do NOT modify this code. The content of this method is always
//         * regenerated by the Form Editor.
//         */
//        @SuppressWarnings("unchecked")
//        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
//        private void initComponents() {
//
//            Container drawPanel1 = TwoPointRegulator.this;
//
//            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
//
//            javax.swing.GroupLayout drawPanel1Layout = new javax.swing.GroupLayout(drawPanel1);
//            drawPanel1.setLayout(drawPanel1Layout);
//            drawPanel1Layout.setHorizontalGroup(
//                    drawPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                            .addGap(0, 380, Short.MAX_VALUE)
//            );
//            drawPanel1Layout.setVerticalGroup(
//                    drawPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                            .addGap(0, 278, Short.MAX_VALUE)
//            );
//
//            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
//            getContentPane().setLayout(layout);
//            layout.setHorizontalGroup(
//                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                            .addGroup(layout.createSequentialGroup()
//                                    .addContainerGap()
//                                    .addComponent(drawPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                    .addContainerGap())
//            );
//            layout.setVerticalGroup(
//                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                            .addGroup(layout.createSequentialGroup()
//                                    .addContainerGap()
//                                    .addComponent(drawPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                    .addContainerGap())
//            );
//
//            pack();
//        }// </editor-fold>//GEN-END:initComponents
//
//
//        @Override
//        public void keyTyped(KeyEvent e) {
//            if (e.getKeyCode() == KeyEvent.VK_UP) {
//                setpoint += 10;
//            }
//            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                setpoint -= 10;
//            }
//        }
//
//        @Override
//        public void keyPressed(KeyEvent e) {
//            if (e.getKeyCode() == KeyEvent.VK_UP) {
//                setpoint -= 10;
//            }
//            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                setpoint += 10;
//            }
//            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//        @Override
//        public void keyReleased(KeyEvent e) {
//            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//    }
//
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[]) {
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new TwoPointRegulator();
//            }
//        });
//    }
//
//}
//package nars.rl.example;
//
//import automenta.vivisect.Video;
//import automenta.vivisect.swing.NWindow;
//import jurls.core.utils.MatrixImage;
//import jurls.core.utils.MatrixImage.Data2D;
//import nars.Events;
//import nars.Global;
//import nars.Memory;
//import nars.NAR;
//import nars.event.AbstractReaction;
//import nars.gui.NARSwing;
//import nars.nal.Task;
//
//import nars.nal.nal8.Operator;
//import nars.nal.term.Term;
//import nars.prototype.Default;
//import nars.rl.QLTermMatrix;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.util.*;
//import java.util.List;
//
///**
// * @author patrick.hammer
// */
//public class TwoPointRegulatorAgent extends JPanel {
//
//    static {
//        Global.DEBUG = true;
//    }
//
//    private final QLTermMatrix<Term,Term> ql;
//    private final MatrixImage mi;
//
//
//    private final int cyclesPerFrame = 15;
//    float drawXScale = 0.5f;
//    int historySize = (int)(800 / drawXScale);
//
//    String target = "target";
//
//    int speed = 5;
//    boolean speedProportionalToExpectation = false;
//
//
//    static int targetX = 0; //80 230
//    float x = 30;
//
//    Deque<State> history = new ArrayDeque<>();
//
//    Term center, left, right;
//
//    public class Move extends Operator {
//
//
//        private float closeEnough = speed; //tolerance threshold
//        private float distance;
//
//        public Move() {
//            super("^move");
//        }
//
//
//
//        @Override
//        protected java.util.List<Task> execute(Operation operation, Term[] args, Memory memory) {
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
//
//
//            float dx;
//            float exp = operation.getTask().sentence.truth.getExpectation();
//            if (speedProportionalToExpectation) {
//                dx = speed * exp;
//            } else {
//                dx = speed;
//            }
//
//
//            if (args[0].toString().equals("left")) {
//                x -= dx;
//            } else if (args[0].toString().equals("right")) {
//                x += dx;
//            } else if (args[0].toString().equals("center")) {
//                //nothing
//            } else {
//                System.err.println(this + " ?? " + Arrays.toString(args));
//                return null;
//            }
//
//
//
//            double oldDistance = distance;
//            distance = Math.abs(x - targetX);
//            boolean here = distance < closeEnough;
//            double delta = distance - oldDistance;
//
//            boolean closer = delta < 0;
//
//            boolean further = delta > 0;
//            Term state;
//            double reward;
//            if (here) {
//                state = center;
//                reward = 1.0;
//            } else if (x > targetX) {
//                state = left;
//                reward = closer ?  0.5 : -1.0;
//            } else { // if (x < targetX)
//                state = right;
//                reward = closer ?  0.5 : -1.0;
//            }
//
//
//            System.out.println(state + " " + reward);
//            System.out.println(ql.q);
//            System.out.println(ql.e);
//            System.out.println(operation.getTask().getExplanation());
//
//
//
//            ql.brain.learn(state, reward, 1);
//
//            return null;
//
//        }
//    }
//
//
//    public final NAR nar;
//
//
//    public TwoPointRegulatorAgent() {
//        super();
//
//        nar = new NAR(new Default().setInternalExperience(null));
//
//        center = nar.term("<state --> [center]>");
//        left = nar.term("<state --> [left]>");
//        right = nar.term("<state --> [right]>");
//
//
//        nar.on(new Move());
//
//        Set<Term> actions = new HashSet();
//        actions.add(nar.term("move(center)"));
//        actions.add(nar.term("move(left)"));
//        actions.add(nar.term("move(right)"));
//
//        Set<Term> states = new HashSet();
//        states.add(center);
//        states.add(left);
//        states.add(right);
//
//        ql = new QLTermMatrix(nar) {
//
//
//
//            @Override
//            public boolean isRow(Term s) {
//                return rows.contains(s);
//            }
//
//            @Override
//            public boolean isCol(Term a) {
//                return cols.contains(a);
//            }
//
//            @Override
//            public void init() {
//                super.init();
//
//                ql.possibleDesire(cols, 0.9f);
//
//                //setqAutonomicGoalConfidence(0.55f);
//            }
//        };
//
//
//        nar.setCyclesPerFrame(cyclesPerFrame);
//        nar.param.shortTermMemoryHistory.set(2);
//        nar.param.duration.set(5 * cyclesPerFrame / 2);         //nar.param.duration.setLinear
//        nar.param.decisionThreshold.set(0.5);
//        nar.param.outputVolume.set(5);
//
//
//        new AbstractReaction(nar, Events.CycleEnd.class) {
//            @Override public void event(Class event, Object[] args) {
//                cycle();
//            }
//        };
//
//        new AbstractReaction(nar, Events.FrameEnd.class) {
//
//            final List<Term> xstates = new ArrayList();
//            final List<Term> xactions = new ArrayList();
//
//            @Override
//            public void event(Class event, Object[] args) {
//
//                if (xstates.size() != states.size()) {
//                    xstates.clear();
//                    for (Term s : states)
//                        xstates.add(s);
//                }
//                if (xactions.size() != actions.size()) {
//                    xactions.clear();
//                    for (Term a : actions)
//                        xactions.add(a);
//                }
//
//                repaint();
//
//
//                mi.draw(new Data2D() {
//                    @Override
//                    public double getValue(int x, int y) {
//
//                        return ql.q(xstates.get(x), xactions.get(y));
//                    }
//                }, states.size(), actions.size(), -1, 1);
//            }
//        };
//
//        Video.themeInvert();
//
//        NARSwing s = new NARSwing(nar);
//
//        new Frame().setVisible(true);
//        this.setIgnoreRepaint(true);
//
//
//        new NWindow("Q",
//                mi = new MatrixImage(200,200)
//        ).show(400, 400);
//
//
//
//
//    }
//
//
//
//    protected void cycle() {
//        history(x, nar.memory.emotion.happy(), nar.memory.emotion.busy());
//    }
//
//
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
//        g2d.fillRect(0, dy + targetX - speed / 4, getWidth(), speed / 2);
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
//    public class Frame extends JFrame implements KeyListener {
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
//            Container drawPanel1 = TwoPointRegulatorAgent.this;
//
//            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//
//            GroupLayout drawPanel1Layout = new GroupLayout(drawPanel1);
//            drawPanel1.setLayout(drawPanel1Layout);
//            drawPanel1Layout.setHorizontalGroup(
//                    drawPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addGap(0, 380, Short.MAX_VALUE)
//            );
//            drawPanel1Layout.setVerticalGroup(
//                    drawPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addGap(0, 278, Short.MAX_VALUE)
//            );
//
//            GroupLayout layout = new GroupLayout(getContentPane());
//            getContentPane().setLayout(layout);
//            layout.setHorizontalGroup(
//                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addGroup(layout.createSequentialGroup()
//                                    .addContainerGap()
//                                    .addComponent(drawPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                    .addContainerGap())
//            );
//            layout.setVerticalGroup(
//                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addGroup(layout.createSequentialGroup()
//                                    .addContainerGap()
//                                    .addComponent(drawPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
//                targetX += 10;
//            }
//            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                targetX -= 10;
//            }
//        }
//
//        @Override
//        public void keyPressed(KeyEvent e) {
//            if (e.getKeyCode() == KeyEvent.VK_UP) {
//                targetX -= 10;
//            }
//            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                targetX += 10;
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
//        EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new TwoPointRegulatorAgent();
//            }
//        });
//    }
//
//
////    protected void train2() {
////        for (int i= 0; i < initialTrainingCycles; i++) {
////            nar.input("move(SELF)!");
////            /*nar.input("move(left)!");
////            nar.input("move(right)!");*/
////        }
////    }
////
////    protected void train(int periods) {
////        int delay = trainingActionPeriod;
////        String t = "";
////        for (int i = 0; i < periods; i++) {
////            String dir = Math.random() < 0.5 ? "left" : "right";
////            t += "move(" + dir + ")! %1.00;" + initialDesireConf + "% \n" +
////                    //delay + "\n" +
////                    //"move(right)! :|: %1.00;" + initialDesireConf + "%\n" +
////                    (int)(Math.random()*delay) + "\n";
////        }
////        nar.input(t);
////    }
//
////    public void beGood() {
////
////        nar.input("<" + reward + " --> " + goodness + ">! :|:");
////
////    }
//
//}
package nars.rl;

import automenta.vivisect.Video;
import automenta.vivisect.swing.NWindow;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import jurls.core.utils.MatrixImage;
import jurls.core.utils.MatrixImage.Data2D;
import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.gui.NARSwing;
import nars.io.Texts;
import nars.nal.*;
import nars.nal.nal5.Implication;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;
import nars.prototype.Default;
import nars.rl.hai.AbstractHaiQBrain;
import vnc.ConceptMap;

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

//    abstract public static class NALAgent extends Operator {
//        public final NAR nar;
//        private final AbstractHaiQBrain rl;
//        private final FrameReaction cycle;
//        private int nextAgentAction;
//
//        public NALAgent(String id, NAR n, int inputs, int actions) {
//            super("^" + id);
//
//            this.nar = n;
//            nar.on(this);
//
//            this.rl = new AbstractHaiQBrain(inputs, actions);
//            this.cycle = new FrameReaction(nar) {
//                @Override public void onFrame() {
//                    frame();
//                }
//            };
//        }
//
//        public void input(double[] i, float reward) {
//
//            nextAgentAction = rl.act(i, reward); //agent first
//            act(nextAgentAction);
//
//            Term t = getInputTerm(i);
//            nar.believe(t.toString(), Tense.Present, 1.0f, 0.9f);
//        }
//
//        protected void frame() {
//
//
//
//            nextAgentAction = -1;
//        }
//
//        abstract public Term getInputTerm(double[] input);
//
//        /** called by the agent when it decides */
//        protected void agentExecute(int action) {
//            nar.input(this.toString() + "(" + "a" + action + ")!");
//        }
//
//        /** called when the agent or NARS executes */
//        abstract public void act(int action);
//
//        @Override
//        protected List<Task> execute(Operation operation, Term[] args, Memory memory) {
//            if (args.length == 2) {
//                try {
//                    int a = Integer.parseInt(args[0].toString().substring(1));
//                    if (nextAgentAction!=-1)
//                        nextNARAction = a;
//                    act(a);
//                }
//                catch (NumberFormatException e) { }
//            }
//            return null;
//        }
//    }

    final int targetCyclesMin = 200;
    final int targetCyclesMax = targetCyclesMin;
    private final HaiQNAR ql;
    private final MatrixImage mi;
    int targetCycles = targetCyclesMin;

    final int beGoodPeriod = targetCyclesMin;

    private final int cyclesPerFrame = 50;
    float drawXScale = 0.5f;
    int historySize = (int)(800 / drawXScale);

    final int trainingActionPeriod = targetCyclesMin;
    final int initialTrainingCycles = 1;

    float initialDesireConf = 0.75f;
    int speed = 5;

    //String self = "SELF";
    String target = "good";
    static String reward = "SELF";
    static String goodness = "good";
    //String goodness = "[good]";


    int movement = 0;
    int lastMovement = 0;
    Deque<State> history = new ArrayDeque<>();
    boolean speedProportionalToExpectation = false;
    private float closeEnough = speed; //tolerance threshold
    private float distance;
    private boolean here, closer;

    static int setpoint = 0; //80 230
    float x = 30;

    public class move extends Operator {



        public move() {
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
               /* if (now - lastMovementAt < minCyclesPerMovement) {
                    moving();
                    return null;
                }*/

            float dx;
            if (speedProportionalToExpectation) {
                dx = speed * 0.5f + 0.5f * operation.getTask().sentence.truth.getExpectation();
                //dx = speed * operation.getTask().sentence.truth.getExpectation();
                //dx = speed * operation.getDesire(memory);
            } else {
                dx = speed;
            }

            int a = -1;

            if (args[0].toString().equals("left")) {
                x -= dx;
                a = 1;
            } else if (args[0].toString().equals("right")) {
                x += dx;
                a = 2;
            } else if (args[0].toString().equals("center")) {
                a = 0;
            } else {
                System.err.println(this + " ?? " + Arrays.toString(args));
                return null;
            }

            movement++;
            // lastMovementAt = now;

            System.out.println(operation.getTask().getExplanation());


            acted(a);


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
            return null;

        }
    }

    private void acted(int a) {
        double delta = updateDistance();

        closer = delta < 0;

        boolean further = delta > 0;
        int nextAct = -1;
        if (here) {
            nextAct = ql.act(0, 1.0, a);
        } else if (x > setpoint) {
            nextAct = ql.act(1, closer ?  0.5 : -1.0 ); //further ? -0.5 : -0.1, a);
        } else { // if (x < setpoint)
            nextAct = ql.act(2, closer ?  0.5 : -1.0 ); //further ? -0.5 : -0.1, a);
        }

        if (a == -1) {
            ql.act(nextAct);
        }
        System.out.println("Qact=" + nextAct + " " + closer);

    }


    public void beGood() {

        nar.input("<" + reward + " --> " + goodness + ">! :|:");

        //nar.input("move(SELF)! :|:");

        /*if (!here) {
            nar.input("<" + reward + " --> " + goodness + ">. :|: %0.05;0.65%");
        }*/
        //nar.input("move(left)@");
        //nar.input("move(right)@");
    }

//    public void moving() {
//        nar.input("<" + self + " --> [moving]>. :|:");
//    }
//
//    public void beGoodNow() {
//        nar.input("<" + self + " --> [good]>! :|:");
//    }

/*    public Task good(float conf) {
        return nar.task("<" + reward + " --> " + goodness + ">. :|: %0.90;" + conf + "%");
    }*/

//    public void bad() {
//        String b = "<" + reward + " --> " + goodness + ">" + ". :|: %0.05;0.90%"; //punishment
//        nar.input(b);
//    }
//    public void bad(boolean left, boolean right) {
//        bad();
//        String tt = state(left, right);
//        nar.input(tt + ". :|: %0.95;0.90%");
//    }

//    public void target(boolean left, boolean right) {
//        nar.input( state(left, right) + ". :|: %0.95;0.90%");
//    }

    private String state(boolean left, boolean right) {

        java.util.List<String> l = new ArrayList(3);

        String x;

        if ((!left && !right) || (left && right))
            x = "center";

        else if (left)
            x = "left";

        else //if(right)
            x = "right";



        return ("<" + target + " --> " + x + ">");
        //return "<<" + reward + "-->" + goodness + "> ==> <" + x + " --> " + target + ">>";

    }

    private String getTargetTerm1(boolean left, boolean right, String... additional) {

        String term = "(&|,";
        if (left)
            term += "<move(left) --> " + target + ">";
        else
            term += "<(--,move(left)) -->" + target + ">";

        term += ",";

        if (right)
            term += "<move(right) --> " + target + ">";
        else
            term += "<(--,move(right)) -->" + target + ">";

        for (String s : additional) {
            term += "," + s;
        }

        term += ")";

        return term;
    }

    protected double updateDistance() {
        double oldDistance = distance;
        distance = Math.abs(x - setpoint);
        here = distance < closeEnough;
        return distance - oldDistance;
    }

    NAR nar;

    abstract public static class HaiQNAR extends AbstractHaiQBrain {

        private final NAR nar;
        ConceptMap.SeededConceptMap c;
        BiMap<Term,Integer> states;
        BiMap<Operation, Integer> actions;
        private final Concept[][] q;

        public HaiQNAR(NAR nar, int nstates, int nactions) {
            super(nstates, nactions);

            setAlpha(0.25f);
            setEpsilon(0);
            setLambda(0.5f);

            this.nar = nar;
            states = HashBiMap.create(nstates);
            actions = HashBiMap.create(nactions);

            Set<Term> qseeds = new HashSet();
            for (int s = 0; s < nstates; s++) {

                states.put(getStateTerm(s), s);

                for (int a = 0; a < nactions; a++) {
                    if (s == 0) {
                        actions.put(getActionOperation(a), a);

                    }

                    qseeds.add(qterm(s, a));
                }
            }

            System.out.println("states:\n" + states);
            System.out.println("actions:\n" + actions);

            for (int s = 0; s < nstates; s++) {
                for (int a = 0; a < nactions; a++) {
                    conceptualize(s, a);
                }
            }

            c = new ConceptMap.SeededConceptMap(nar, qseeds) {

                @Override
                protected void onFrame() {

                }

                @Override
                protected void onCycle() {

                }

                @Override
                protected void onConceptForget(Concept c) {
                    Implication t = (Implication) c.getTerm();
                    int[] x = qterm(t);
                    if (x!=null) {
                        int s = x[0];
                        int a = x[1];
                        q[s][a] = null;
                        conceptualize(s,a);
                    }
                }

                @Override
                protected void onConceptNew(Concept c) {
                    Implication t = (Implication) c.getTerm();
                    int[] x = qterm(t);
                    if (x!=null)
                        q[x[0]][x[1]] = c;

                    //System.out.println( Arrays.deepToString(q) );
                }
            };

            this.q = new Concept[nstates][nactions];
        }

        @Override
        public int act(int state, double r, int action) {

            String b = "<" + reward + " --> " + goodness + ">" + ". :|: %" + Texts.n2(r/2.0+0.5f) + ";0.90%"; //punishment
            nar.input(b);

            return super.act(state, r, action);
        }

        private int[] qterm(Implication t) {
            Term s = t.getSubject();
            Operation p = (Operation)t.getPredicate();

            int state = states.get(s);
            int action = actions.get(p);

            return new int[] { state, action };
        }

        private void conceptualize(int s, int a) {
            Term t = qterm(s, a);
            nar.input(t + ". %0.50;0.50%");
        }

        public Term qterm(int s, int a) {
            return nar.term("<" + getStateTerm(s)  + "=/>" + getActionOperation(a) + ">");
        }

        abstract public Term getStateTerm(int s);
        abstract public Operation getActionOperation(int s);

        @Override
        public void qAdd(int state, int action, double dq) {
            float thresh = 0.01f;
            if (Math.abs(dq) < thresh) return;

            Concept c = q[state][action];
            double q = q(state, action);
            double nq = q + dq;
            if (nq > 1d) nq = 1d;
            if (nq < -1d) nq = -1d;


            //float conf = (float)Math.abs(dq)/2.0f + 0.5f; //confidence of each update
            float conf = 0.5f;

            System.out.println(c + " qUpdate: " + Texts.n4(q) + " + " + dq + " -> " + " (" + Texts.n4(nq) + ")");

            double nextFreq = (nq/2) + 0.5f;

            //String updatedGoal = c.getTerm() + "! :|: %" + Texts.n2(nextFreq) + ";" + Texts.n2(conf) + "%";
            String updatedBelief = c.getTerm() + ". :|: %" + Texts.n2(nextFreq) + ";" + Texts.n2(conf) + "%";


            //c.beliefs.clear();
            //c.goals.clear();

            //new DirectProcess(nar.memory, nar.task(updatedGoal)).run();
            //new DirectProcess(nar.memory, nar.task(updatedBelief)).run();
            //System.out.println("  " + c.goals.size() + " " + c.goals );

            nar.input(updatedBelief);

            c.print(System.out, true, false, false, false);
        }

        @Override
        public double q(int state, int action) {
//            Concept c = q[state][action];
//            TruthValue t = c.getDesire();
//            if (t == null) return 0.5;
//            return t.getFrequency(); // (t.getFrequency() - 0.5f) * 2f * t.getConfidence();

            Concept c = q[state][action];
            if (c == null) return 0f;
            Sentence s = c.getBestBelief();
            if (s == null) return 0f;
            TruthValue t = s.truth;
            if (t == null) return 0f;
            return ((t.getFrequency() - 0.5f)*2.0f); // (t.getFrequency() - 0.5f) * 2f * t.getConfidence();

        }

        public void act(int nextAct) {
            Operation a = actions.inverse().get(nextAct);
            nar.input(a + "! :|:");
        }
    }


    public TwoPointRegulatorAgent() {
        super();

        nar = new NAR(new Default().setInternalExperience(null));
        nar.on(new move());

        ql = new HaiQNAR(nar,3, 3) {
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


        nar.setCyclesPerFrame(cyclesPerFrame);
        nar.param.shortTermMemoryHistory.set(1);
        nar.param.duration.set(5);
        //nar.param.duration.setLinear
        nar.param.decisionThreshold.set(0.75);
        nar.param.outputVolume.set(5);

        //nar.on(new DeriveOnlyDesired());

        new AbstractReaction(nar, Events.CycleEnd.class) {

            @Override
            public void event(Class event, Object[] args) {

                cycle();

            }
        };
        new AbstractReaction(nar, Events.FrameEnd.class) {

            @Override
            public void event(Class event, Object[] args) {


                acted(-1);

                repaint();

                mi.draw(new Data2D() {
                    @Override
                    public double getValue(int x, int y) {
                        return ql.q(y, x);
                    }
                }, ql.states.size(), ql.actions.size(), -1, 1);
            }
        };

        Video.themeInvert();

        NARSwing s = new NARSwing(nar);

        new Frame().setVisible(true);
        this.setIgnoreRepaint(true);


        new NWindow("Q",
                mi = new MatrixImage(400,400)
        ).show(400, 400);
        init();

    }



    protected void cycle() {
        boolean hasMoved = (movement != lastMovement);
        lastMovement = movement;

        history(x, nar.memory.emotion.happy(), nar.memory.emotion.busy());

        updateDistance();

//                if (nar.time() % feedbackCycles == 0) {
//                    if (here)
//                        nar.input(good());
//                }

//        if (distance > speed * 10) {
//            //most often, farthest
//            targetCycles = targetCyclesMin;
//        } else if (distance > speed * 5) {
//            targetCycles = (targetCyclesMin + targetCyclesMax) / 2;
//        } else {
//            //least often, closest
//            targetCycles = targetCyclesMax;
//        }

        /*if (nar.time() % targetCycles == 0) {
            if (here) {
                //target("here", 1.0f);
                target(false, false);
            } else if (x > setpoint) {
                //target("here", 0.0f);
                target(true, false);
            } else { // if (x < setpoint)
                //target("here", 0.0f);
                target(false, true);
            }

        }*/

        if (nar.time() % beGoodPeriod == 0) {
            beGood();
        }
    }

    protected void init() {
        updateDistance();

        //train(initialTrainingCycles);
        train2();
    }

    protected void train2() {
        for (int i= 0; i < initialTrainingCycles; i++) {
            nar.input("move(SELF)!");
            /*nar.input("move(left)!");
            nar.input("move(right)!");*/
        }
    }

    protected void train(int periods) {
        int delay = trainingActionPeriod;
        String t = "";
        for (int i = 0; i < periods; i++) {
            String dir = Math.random() < 0.5 ? "left" : "right";
            t += "move(" + dir + ")! %1.00;" + initialDesireConf + "% \n" +
                    //delay + "\n" +
                    //"move(right)! :|: %1.00;" + initialDesireConf + "%\n" +
                    (int)(Math.random()*delay) + "\n";
        }
        nar.input(t);
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
        g2d.fillRect(0, dy + setpoint - speed / 4, getWidth(), speed / 2);

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
                setpoint += 10;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                setpoint -= 10;
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                setpoint -= 10;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                setpoint += 10;
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

}
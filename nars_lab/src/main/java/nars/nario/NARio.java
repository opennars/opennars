package nars.nario;

import automenta.vivisect.Video;
import com.gs.collections.impl.list.mutable.primitive.DoubleArrayList;
import jurls.reinforcementlearning.domains.RLEnvironment;
import nars.Events;
import nars.Global;
import nars.NAR;
import nars.gui.NARSwing;
import nars.io.ChangedTextInput;
import nars.model.impl.Default;
import nars.nal.Task;
import nars.nal.nal8.NullOperator;
import nars.nal.nal8.Operation;
import nars.nal.term.Term;
import nars.nario.level.Level;
import nars.nario.level.LevelGenerator;
import nars.nario.sprites.*;
import nars.util.event.Reaction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.log;
import static java.lang.Math.signum;

/**
 * @author me
 */
public class NARio extends Run implements RLEnvironment {

    static int memoryCyclesPerFrame = 2;

    int movementStatusPeriod = 1;
    int commandPeriod = 50;
    int radarPeriod = 2;

    private int[] keyTime = new int[256];
    private float lastMX;
    private float lastMY;

    //boolean representation_simple = false;
    public int t = 0;

    private final NAR nar;

    private float lastX = -1;
    private float lastY = -1;
    int cycle = 0;
    double bonus = 0;
    float dx, dy;
    /*int mx, my;
    float dmx, dmy;*/

    protected Mario mario;
    static double gameRate;

    boolean offKeys = false;

    private ChangedTextInput moveInput;
    private ChangedTextInput velInput;
    private LevelScene level;

    public NARio(NAR n) {
        super();
        this.nar = n;
        //start();
        run();
    }


    /** should not be used if RL has its own keyboard operation that calls takeAction */
    protected void initKeyboardOperators() {
        for (final int kk : keys) {
            String ko = "^keyboard" + kk;
            if (nar.memory.operator(ko) == null) {
                nar.on(new NullOperator("^" + "keyboard" + kk) {

                    @Override
                    protected List<Task> execute(Operation operation, Term[] args) {

                        String state = args[0].toString();

                        Task task = operation.getTask();
                        Task parent = task.getParentTask();
                        Task root = task.getRootTask();


                        mario.keys[kk] = state.equals("on");

                        return super.execute(operation, args);
                    }

                });
            }

            int currentKeyTime, nextKeyTime;
            currentKeyTime = nextKeyTime = keyTime[kk];
            boolean wasPressed = currentKeyTime > 0;
            boolean pressed;

            if (!mario.keys[kk]) {
                nextKeyTime = 0;
                pressed = false;
            } else {
                nextKeyTime++;
                pressed = true;
            }

            if (pressed != wasPressed) {
                            /*String budget = (nextKeyTime > 0) ?
                                    "$" + (1.0 / (1.0 + nextKeyTime)) + "$" :
                                    "";*/
                String state = nextKeyTime > 0 ? "on" : "off";
                //String budget = "$0.8;0.1$";
                String budget = "";
                //nar.addInput(budget + "(^" + ko + "," + state + ")!");
            }

            keyTime[kk] = nextKeyTime;
        }


    }
    public static String n(int x) {
        if (x == 0) return "z";
        if (x < 0) return "n" + (-x);
        else return "p" + x;
    }

    public static String direction(int i, int j) {

        return "(*," + n(i) + "," + n(j) + ")";

    }

    final DoubleArrayList o = new DoubleArrayList();

    final int rad = 2;
    final DoubleArrayList radar = DoubleArrayList.newWithNValues((rad*2+1)*(rad*2+1), 0);


    @Override
    public double[] observe() {
        return new double[0];
    }

    @Override
    public double getReward() {

        double horizontalMotionReward = 0.35;
        double verticalMotionReward =   0.04;

        double adx = Math.abs(dx);
        double ady = Math.abs(dy);

        double r = -1.0 +
                adx * horizontalMotionReward +
                ady * verticalMotionReward +
                bonus;

        bonus *= 0.95; //decay

        return r;
    }

    @Override
    public boolean takeAction(int action) {
        boolean pressed;
        if (action > 5) { pressed = false; action -=5; }
        else pressed = true;


        /*if (mario.keys[action] == pressed)
            return false;*/

        //System.out.println('@' + nar.time() + " " + Arrays.toString(mario.keys));
        mario.keys[action] = pressed;
        //System.out.println("  " + action + " " + Arrays.toString(mario.keys));
        return true;
    }

    @Override
    public void frame() {
        //nar.memory.timeSimulationAdd(1);

        cycle(gameRate);

        float x = level.mario.x;
        float y = level.mario.y;

        dx = (x - lastX);
        dy = (y - lastY);

        lastX = x;
        lastY = y;



//                    //if no movement, decrease priority of sense
//                    if ((dx == 0) && (dy == 0)) {
//                        //sightPriority/=2.0f;
//                        //movementPriority/=2.0f;
//                    } else {
//                        movement = true;
//                    }

//
//                    if (movement || (www % movementStatusPeriod == 0)) {
//
////                            if (!((mx==0) && (my==0))) {
////                                String dir=direction(mx,my);
////                                if (right!=false) { // && moveInput.set(/*"$" + movementPriority + "$"*/"<"+dir+" --> moved>. :|:")) {
////                                    //if significantly changed block position, record it for next difference
////                                    nar.input("<right --> moved>. :|:");
////                                    lastMX = x;
////                                    lastMY = y;
////                                }
////                                else {
////                                    nar.input("(--, <right --> moved>). :|:");
////                                }
////                            }
//
////                        updateMovement("nowhere", (mx == 0 && my == 0));
////                        updateMovement("left", (mx > 0));
////                        updateMovement("right", (mx < 0));
//
//                        //this one is wrong like when getting stuck indicates:
//                        //  velInput.set(/*"$" + movementPriority + "$"*/"<(*," + slog(dx) + "," + slog(dy) + ") --> velocity>. :|:");
//
//                    } else {
//                        //if (moveInput.set("<"+direction(0,0)" --> moved>. :|:")) { //stopped
//                        //}
//
//                    }
//


        if (t % movementStatusPeriod == 0) {

            if ((dx == 0) && (dy == 0)) {
                updateMovement(direction(0, 0), 1.0f);
            } else {
                //4 basis vectors
                int maxVelocity = 64;
                updateMovement(dx, dy, 0, -maxVelocity);
                updateMovement(dx, dy, 0, maxVelocity);
                updateMovement(dx, dy, -maxVelocity, 0);
                updateMovement(dx, dy, maxVelocity, 0);
            }
        }

                    /*if (movement)*/
        {
            //predict next type of block at next current position


            int rr = 0;

            for (int i = -rad; i <= rad; i++) {
                for (int j = -rad; j <= rad; j++) {

                    if ((i == 0) && (j == 0)) continue;

                    int block = level.level.getBlock(x + i * 16f - 8, y + j * 16f - 8);
                    int data = level.level.getData(x + i * 16 - 8, y + j * 16 - 8);

                    boolean blocked
                            = ((block & Level.BIT_BLOCK_ALL) > 0)
                            || ((block & Level.BIT_BLOCK_LOWER) > 0)
                            || ((block & Level.BIT_BLOCK_UPPER) > 0);

//                            String s = " <(*," +
//                                            (blocked ? "solid" : "empty") +
//                                            "," + data + ",(*," + i + "," + j +
//                                            ")) --> space>. :|:";
                    String direction = direction(i, j);

                    char datachar = (char) ('r' + data);
                    //String s = "<" + direction + " --> [solid]>. :|: %" + (blocked ? 1.0 : 0.0) + ";0.90%";


                    float sightPriority = (float) (4.0 / (4.0 + Math.sqrt(i * i + j * j)));

                    if (t % radarPeriod == 0) {
                        String s2 = "$" + sightPriority + "$" + "<" + direction + "--> [" + datachar + "]>. :|:";
                        input(s2);
                    }

                    radar.set(rr++, blocked ? -1 : 1);

                    //System.out.println(i + " " + j + " " +  s2);


//                            if ((sight[k] != null) && (sight[k].equals(s))) {
//                                continue;
//                            }
//
//                            sight[k] = s;


                    //chg.set(/*"$" + sightPriority + "$" +*/s);


                }
            }

            ArrayList<Sprite> sprites = new ArrayList(level.sprites);
            for (Sprite s : sprites) {
                if (s instanceof Mario) {
                    continue;
                }
                if ((s instanceof Sparkle) || (s instanceof Particle)) {
                    continue;
                    //priority/=2f;
                }

                double senseRadius = 15;
                double dist = Math.sqrt((x - s.x) * (x - s.x) + (y - s.y) * (y - s.y)) / 16.0;
                if (dist <= senseRadius) {
                    double priority = 0.5f + 0.5f * (senseRadius - dist) / senseRadius;

                    //sparkles are common and not important
                    String type = s.getClass().getSimpleName();
                    if (s instanceof Enemy) {
                        type = s.toString();
                    }

                    int dx = Math.round((x - s.x) / 16f);
                    int dy = Math.round((y - s.y) / 16f);

                    float sightPriority = (float) (4.0 / (4.0 + Math.sqrt(dx * dx + dy * dy)));

                    nar.input("$" + sightPriority + "$" +
                            " <{" + type + "} --> " + direction(dx, dy) + ">. :|:");

                    //nar.addInput("$" + sv.toString() + "$ <(*,<(*," + dx +"," + dy + ") --> localPos>," + type + ") --> feel>. :|:");
                }

            }

        }

        if (t % commandPeriod == 0) {
            goals();
        }



        t++;
        cycle++;

    }

    @Override
    public Component component() {
        return null;
    }

    @Override
    public int numActions() {
        return 5*2;
    }




    protected void goals() {

        //move, mostly to the right
        //nar.input("<SELF --> [" + direction(+1, 0) + "]>! %1.0;0.2%");

        //dont remain still
        //nar.input("<SELF --> [" + direction(0, 0) + "]>! :|: %0.05;0.6%");

        nar.input("<SELF --> [died]>! %0.00;0.95%");
        nar.input("<SELF --> [died]>. :|: %0.00;0.9%");
        nar.input("<SELF --> [stomp]>! :|: %1.00;0.5%");
        nar.input("<SELF --> [coin]>! :|: %1.00;0.45%");

//        for (int i= 0; i < 5; i++) {
//            nar.input("keyboard" + i + "(on)! :|: %0.75;0.5%");
//            nar.input("keyboard" + i + "(off)! :|: %0.75;0.5%");
//        }

    }

    public void coin() {
        nar.input("<SELF --> [coin]>. :|:");
        System.out.println("MONEY");
        bonus += 1;
    }

    protected void hurt() {
        nar.input("<SELF --> [died]>. :|:");
        goals();
        System.out.println("OUCH");
        bonus -= 10;
    }

    public void stomp() {
        nar.input("<SELF --> [stomp]>. :|:");
        System.out.println("KILL");
        bonus += 1;
    }

    public void trip() {
        nar.input("<SELF --> [tripping]>. :|:");
        System.out.println("TRIPPING");
        bonus += 2;
    }


    @Override
    public void levelFailed() {

        int type = Math.random() > 0.5 ? LevelGenerator.TYPE_UNDERGROUND : LevelGenerator.TYPE_OVERGROUND;
        startLevel((long) (Math.random() * 8000), 1, type);
        bonus -= 2;
    }

    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            double ai = vectorA[i];
            double bi = vectorB[i];
            normA += ai * ai;
            normB += bi * bi;
        }
        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);
        return dotProduct / (normA * normB);
    }

    public static double cosineSimilarityScaled(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            double ai = vectorA[i];
            double bi = vectorB[i];
            normA += ai * ai;
            normB += bi * bi;
        }
        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);
        return dotProduct / (normA * normB) * (normA / normB);
    }

    public static void main(String[] arg) {
        //NAR nar = new Default().realtime().build();

        NAR nar = new NAR(new Default().setInternalExperience(null)
                .setConceptBagSize(3500));

        Global.EXIT_ON_EXCEPTION = true;
        //Global.TRUTH_EPSILON = 0.01f;

        //nar.on(new TemporalParticlePlanner());

        // NAR nar = new CurveBagNARBuilder().simulationTime().build();
        /*nar.param().termLinkRecordLength.set(4);
         nar.param().beliefCyclesToForget.set(30);
         nar.param().conceptCyclesToForget.set(7);
         nar.param().taskCyclesToForget.set(22);
         nar.param().termLinkMaxReasoned.set(6);
        
         nar.param().cycleInputTasks.set(1);
         nar.param().cycleMemory.set(1);*/

        // nar.param().conceptForgetDurations.set(99.0f);

        // nar.param().termLinkForgetDurations.set(99.0f);

        //new TextOutput(nar, System.out).setShowInput(true);

        nar.param.duration.set(memoryCyclesPerFrame * 3);
        nar.setCyclesPerFrame(memoryCyclesPerFrame);

        nar.param.outputVolume.set(0);
        nar.param.decisionThreshold.set(0.75);
        nar.param.conceptsFiredPerCycle.set(200);
        nar.param.shortTermMemoryHistory.set(5);

        float fps = 70f;
        gameRate = 1.0f / fps;


        Video.themeInvert();
        NARSwing sw = new NARSwing(nar);

        //nar.start(((long)(1000f/fps)));//, memCyclesPerFrame, 1f);

        NARio nario = new NARio(nar);
        //sw.setSpeed(0.95f);

    }

    ChangedTextInput chg;
    String[] sight = new String[9];

    protected void axioms() {

        //System.out.println("Inports: " + nar.getInPorts().size());
        //System.out.println("Flushed: " + flushed);

        //NAR.DEBUG = true;

        chg = new ChangedTextInput(nar);
        moveInput = new ChangedTextInput(nar);
        velInput = new ChangedTextInput(nar);

        //nar.addInput("<(*,?m,(*,?x,?y)) --> space>? :/:");
                       /* nar.addInput("<?y --> space>? :/:");
                        nar.addInput("<{(*,0,0),(*,0,1),(*,1,0),(*,-1,0),(*,0,-1)} <-> direction>. %1.00;0.99%");
                        
                        nar.addInput("{solid,empty,gotCoin} <-> spatial>.  %1.00;0.99%");
                        nar.addInput("<solid <-> empty>.  %0.00;0.99%");
                        nar.addInput("<(*,0,0) <-> center>.  %1.00;0.99%");
                        nar.addInput("<(*,-1,0) <-> left>.  %1.00;0.99%");
                        nar.addInput("<(*,-2,0) <-> left>.  %1.00;0.50%");
                        nar.addInput("<(*,1,0) <-> right>.  %1.00;0.99%");
                        nar.addInput("<(*,0,1) <-> up>.  %1.00;0.99%");
                        nar.addInput("<(*,0,2) <-> up>.  %1.00;0.50%");
                        nar.addInput("<(*,0,2) <-> right>.  %1.00;0.50%");
                        nar.addInput("<(*,0,-1) <-> down>.  %1.00;0.99%");
                        nar.addInput("<(*,0,-2) <-> down>.  %1.00;0.50%");
                        nar.addInput("<{on,off} <-> activation>. %1.00;0.99%");
                        nar.addInput("<{-5,-4,-3,-2,-1,0,1,2,3,4,5} <-> integers>. %1.00;0.99%");
                        nar.addInput("<{0,(*,0,0)} <-> zeroish>.  %1.00;0.99%");      
                        */
        //nar.addInput("<solid <-> empty>. %0.00;0.99%");
        //nar.addInput("<up <-> down>. %0.00;0.99%");
        //nar.addInput("<left <-> right>. %0.00;0.99%");


//        {
//            //teach keys
//            int[] ev2 = new int[]{Mario.KEY_DOWN, Mario.KEY_JUMP, Mario.KEY_JUMP, Mario.KEY_LEFT, Mario.KEY_RIGHT, Mario.KEY_SPEED, Mario.KEY_UP};
//            for (boolean b : new boolean[]{true, false}) {
//                for (int i : ev2) {
//                    setKey(i, b, true);
//                }
//            }
//        }
    }

    ChangedTextInput[] keyInput = new ChangedTextInput[6];

//    protected void setKey(int k, boolean pressed) {
//        setKey(k, pressed, true);
//    }
//
//    protected void setKey(int k, boolean pressed, boolean goal) {
//        if (keyInput[k] == null && pressed)
//            keyInput[k] = new ChangedTextInput(nar);
//        nar.input("(^keyboard" + k + "," + (pressed ? "on" : "off") + ")" + (goal ? '!' : '.') + " :|:");
//    }

//    @Override
//    protected void toggleKey(int keyCode, boolean isPressed) {
//        if (keyCode == KeyEvent.VK_LEFT) {
//            setKey(0, isPressed);
//            scene.toggleKey(Mario.KEY_LEFT, isPressed);
//        }
//        if (keyCode == KeyEvent.VK_RIGHT) {
//            setKey(1, isPressed);
//            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
//        }
//        if (keyCode == KeyEvent.VK_DOWN) {
//            setKey(2, isPressed);
//            scene.toggleKey(Mario.KEY_DOWN, isPressed);
//        }
//        if (keyCode == KeyEvent.VK_UP) {
//            setKey(3, isPressed);
//            scene.toggleKey(Mario.KEY_UP, isPressed);
//            setKey(1, isPressed);
//            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
//        }
//        if (keyCode == KeyEvent.VK_S) {
//            setKey(4, isPressed);
//            scene.toggleKey(Mario.KEY_JUMP, isPressed);
//            setKey(1, isPressed);
//            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
//        }
//        if (keyCode == KeyEvent.VK_A) {
//            setKey(5, isPressed);
//            scene.toggleKey(Mario.KEY_SPEED, isPressed);
//        }
//    }

    protected int slog(int x) {
        if (x == 0) return 0;
        int sign = (int) signum(x);
        x = Math.abs(x);
        return sign * (int) Math.ceil(log((1 + x)));
    }

    @Override
    protected LevelScene newLevel(long seed, int difficulty, int type) {
        return new LevelScene(graphicsConfiguration, this, seed, 1, Math.random() < 0.5 ? LevelGenerator.TYPE_OVERGROUND : LevelGenerator.TYPE_UNDERGROUND) {
            @Override
            protected Mario newMario(LevelScene level) {
                return new Mario(level) {
                    @Override
                    public void getCoin() {
                        super.getCoin();
                        coin();
                    }

                    @Override
                    public void die() {
                        super.die();
                        hurt();
                    }

                    @Override
                    public void getMushroom() {
                        super.getMushroom();
                        trip();
                    }

                    @Override
                    public void getHurt() {
                        super.getHurt();
                        hurt();
                    }



                    @Override
                    public void stomp(BulletBill bill) {
                        super.stomp(bill);
                        NARio.this.stomp();
                    }

                    @Override
                    public void stomp(Enemy enemy) {
                        super.stomp(enemy);
                        NARio.this.stomp();
                    }

                    @Override
                    public void stomp(Shell shell) {
                        super.stomp(shell);
                        NARio.this.stomp();
                    }
                };
            }
        };
    }

    final int[] keys = new int[]{Mario.KEY_LEFT, Mario.KEY_RIGHT, Mario.KEY_UP, Mario.KEY_DOWN, Mario.KEY_JUMP, Mario.KEY_SPEED};

    @Override
    public void ready() {
        //super.ready();

        scene = newLevel((int) (Math.random() * 1000), 1, LevelGenerator.TYPE_OVERGROUND);

        level = (LevelScene) scene;

        level.setSound(sound);
        level.init();

        mario = level.mario;
        mario.setInvincible(false);


        axioms();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //new Window("Implications", new SentenceGraphPanel(nar, new ImplicationGraph(nar))).show(500,500);
                //new Window("Inheritance", new SentenceGraphPanel(nar, new InheritanceGraph(nar))).show(500,500);

            }

        });

        nar.memory.event.on(Events.FrameEnd.class, new Reaction() {



            @Override
            public void event(Class event, Object... arguments) {

                //frame();

            }


        });

    }





    protected void updateMovement(String direction, float freq) {
        String s = "<SELF --> [" + direction + "]>. :|: %" +
                freq + ";0.80%";
        input(s);
    }


    protected void updateMovement(float cx, float cy, int tx, int ty) {
        double f = cosineSimilarityScaled(new double[]{cx, cy}, new double[]{tx, ty});
        float ff = (float) (f / 2f + 0.5f);
        updateMovement(direction(-tx, -ty), ff); //for some reason the sign needs negated
    }



    protected void input(String sensed) {
        nar.input(sensed);
    }



}

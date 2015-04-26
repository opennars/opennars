package nars.nario;

import automenta.vivisect.Video;
import nars.Events;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.event.Reaction;
import nars.gui.NARSwing;
import nars.io.ChangedTextInput;
import nars.io.TextOutput;
import nars.nal.Task;
import nars.nal.nal8.NullOperator;
import nars.nal.nal8.Operation;
import nars.nal.term.Term;
import nars.nario.level.Level;
import nars.nario.level.LevelGenerator;
import nars.nario.sprites.*;
import nars.prototype.Default;
import org.apache.commons.math3.linear.ArrayRealVector;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.log;
import static java.lang.Math.signum;

/**
 * @author me
 */
public class NARio extends Run {

    static int memoryCyclesPerFrame = 40;

    int movementStatusPeriod = 1;
    int commandPeriod = 50;
    //int keyStatePeriod = 25;
    int radarPeriod = 3;

    private final NAR nar;

    private float lastX = -1;
    private float lastY = -1;
    int cycle = 0;

    private Mario mario;
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

    public static String n(int x) {
        if (x == 0) return "z";
        if (x < 0) return "n" + (-x);
        else return "p" + x;
    }

    public static String direction(int i, int j) {

        return "(*," + n(i) + "," + n(j) + ")";

    }

    protected void goals() {

        //move, mostly to the right
        nar.input("<moved --> [" + direction(+1, 0) + "]>! %1.0;0.2%");
        //nar.input("<moved --> [" + direction(-1, 0) + "]>! %1.0;0.2%");
        //nar.input("<moved --> [" + direction(0, +1) + "]>! %1.0;0.2%");
        //nar.input("<moved --> [" + direction(0, -1) + "]>! %1.0;0.2%");

        //dont remain still
        nar.input("<moved --> [" + direction(0, 0) + "]>! %0.0;0.2%");

        nar.input("(--,<nars --> died>)! :|: %1.00;0.9%");
        nar.input("<nars --> stomp>! :|: %1.00;0.5%");
        nar.input("<nars --> coin>! :|: %1.00;0.45%");

    }

    public void coin() {
        nar.input("<nars --> coin>. :|:");
        System.out.println("MONEY");
    }
    protected void hurt() {
        nar.input("<nars --> died>. :|:");
        goals();
        System.out.println("OUCH");
    }
    public void stomp() {
        nar.input("<nars --> stomp>. :|:");
        System.out.println("KILL");
    }
    public void trip() {
        nar.input("<nars --> trip>. :|:");
        System.out.println("TRIP");
    }



    @Override
    public void levelFailed() {

        int type = Math.random() > 0.5 ? LevelGenerator.TYPE_UNDERGROUND : LevelGenerator.TYPE_OVERGROUND;
        startLevel((long) (Math.random() * 8000), 1, type);
    }

    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            double ai = vectorA[i];
            double bi = vectorB[i];
            normA += ai*ai;
            normB += bi*bi;
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
            normA += ai*ai;
            normB += bi*bi;
        }
        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);
        return dotProduct / (normA * normB) * (normA / normB);
    }

    public static void main(String[] arg) {
        //NAR nar = new Default().realtime().build();

        NAR nar = new NAR(new Default().setInternalExperience(null).simulationTime().setConceptBagSize(2500));

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

        nar.param.duration.set(memoryCyclesPerFrame);
        nar.setCyclesPerFrame(memoryCyclesPerFrame);

        nar.param.outputVolume.set(0);
        nar.param.decisionThreshold.set(0.75);
        nar.param.conceptsFiredPerCycle.set(1000);
        nar.param.shortTermMemoryHistory.set(5);

        float fps = 20f;
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

    protected void setKey(int k, boolean pressed) {
        setKey(k, pressed, true);
    }

    protected void setKey(int k, boolean pressed, boolean goal) {
        if (keyInput[k] == null && pressed)
            keyInput[k] = new ChangedTextInput(nar);
        nar.input("(^keyboard" + k + "," + (pressed ? "on" : "off") + ")" + (goal ? '!' : '.') + " :|:");
    }

    @Override
    protected void toggleKey(int keyCode, boolean isPressed) {
        if (keyCode == KeyEvent.VK_LEFT) {
            setKey(0, isPressed);
            scene.toggleKey(Mario.KEY_LEFT, isPressed);
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            setKey(1, isPressed);
            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
        }
        if (keyCode == KeyEvent.VK_DOWN) {
            setKey(2, isPressed);
            scene.toggleKey(Mario.KEY_DOWN, isPressed);
        }
        if (keyCode == KeyEvent.VK_UP) {
            setKey(3, isPressed);
            scene.toggleKey(Mario.KEY_UP, isPressed);
            setKey(1, isPressed);
            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
        }
        if (keyCode == KeyEvent.VK_S) {
            setKey(4, isPressed);
            scene.toggleKey(Mario.KEY_JUMP, isPressed);
            setKey(1, isPressed);
            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
        }
        if (keyCode == KeyEvent.VK_A) {
            setKey(5, isPressed);
            scene.toggleKey(Mario.KEY_SPEED, isPressed);
        }
    }

    protected int slog(int x) {
        if (x == 0) return 0;
        int sign = (int) signum(x);
        x = Math.abs(x);
        return sign * (int) Math.ceil(log((1 + x)));
    }

    @Override
    protected LevelScene newLevel(long seed, int difficulty, int type) {
        return new LevelScene(graphicsConfiguration, this, 4, 1, LevelGenerator.TYPE_OVERGROUND) {
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

    @Override
    public void ready() {
        //super.ready();

        scene = newLevel((int)(Math.random() * 1000), 1, LevelGenerator.TYPE_OVERGROUND);

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
            private int[] keyTime = new int[256];
            private float lastMX;
            private float lastMY;

            //boolean representation_simple = false;
            boolean right = false;




            protected void updateMovement(String direction, float freq) {
                String s = "<moved --> [" + direction + "]>. :|: %" +
                        freq + ";0.90%";
                nar.input(s);
            }


            protected void updateMovement(int cx, int cy, int tx, int ty) {
                double f = cosineSimilarityScaled(new double[]{cx, cy}, new double[]{tx, ty});
                float ff = (float)(f / 2f + 0.5f);
                updateMovement(direction(-tx, -ty), ff); //for some reason the sign needs negated
            }

            int tt = 0;
            int dx, dy, mx, my;

            @Override
            public void event(Class event, Object... arguments) {

                nar.memory.timeSimulationAdd(1);

                {
                    //                int ji = 10;
                    //                System.out.print("CONCEPTS: ");
                    //                for (Concept cc : nar.memory.conceptProcessor) {
                    //                    System.out.print(cc.getPriority() + "=" + cc.toString() + " ");
                    //                    ji--;
                    //                    if (ji == 0)
                    //                        break;
                    //                }
                    //                System.out.println();
                }

                tt++;
                /*
                if(tt%19==0) {
                    int[] ev2=new int[]{Mario.KEY_DOWN,Mario.KEY_JUMP,Mario.KEY_JUMP,Mario.KEY_LEFT,Mario.KEY_RIGHT,Mario.KEY_SPEED,Mario.KEY_UP};
                    for(int i : ev2) {
                        scene.toggleKey(i, false);
                    }
                }*/

                //if(Memory.randomNumber.nextDouble()<1.0/10.0 && tt<200) {

                //boolean keyState = www % keyStatePeriod == 0;


//                    boolean isPressed=true; //Memory.randomNumber.nextBoolean();
                //boolean isPressed = offKeys ? Memory.randomNumber.nextBoolean() : true;


//                    int[] ev=new int[]{KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_UP,KeyEvent.VK_S};
//                    int keyCode=ev[Memory.randomNumber.nextInt(ev.length)];
//                    if (keyCode == KeyEvent.VK_LEFT)
//                    {
//                        //if (keyState) setKey(0, isPressed);
//                        scene.toggleKey(Mario.KEY_LEFT, isPressed);
//                    }
//                    if (keyCode == KeyEvent.VK_RIGHT)
//                    {
//                        //if (keyState) setKey(1, isPressed);
//                        scene.toggleKey(Mario.KEY_RIGHT, isPressed);
//                    }
//                    if (keyCode == KeyEvent.VK_DOWN)
//                    {
//                        //if (keyState) setKey(2, isPressed);
//                        scene.toggleKey(Mario.KEY_DOWN, isPressed);
//                    }
//                    if (keyCode == KeyEvent.VK_UP)
//                    {
//                        //if (keyState) setKey(3, isPressed);
//                        scene.toggleKey(Mario.KEY_JUMP, isPressed);
//
//                    }
//                    if (keyCode == KeyEvent.VK_S)
//                    {
//                        //if (keyState) setKey(4, isPressed);
//                        scene.toggleKey(Mario.KEY_UP, isPressed);
//                    }
//                    if (keyCode == KeyEvent.VK_A) //wat
//                    {
//                        //if (keyState) setKey(5, isPressed);
//                        scene.toggleKey(Mario.KEY_DOWN, isPressed);
//                    }
                //}
                    
               /* if (cycle % cyclesPerMario == 0)*/
                {
                    cycle(gameRate);
                }


//                if (cycle % 100 == 1) {
//                    System.out.println("Inports: " + nar.getInPorts().size());
//                }


                //float sightPriority = 0.75f;
                //float movementPriority = 0.60f;
                float x = level.mario.x;
                float y = level.mario.y;

                boolean movement = false;

                if (lastX != -1) {
                    dx = Math.round((x - lastX) / 1);
                    dy = Math.round((y - lastY) / 1);

                    mx = Math.round((x - lastMX) / 16);
                    my = Math.round((y - lastMY) / 16);

                    //if no movement, decrease priority of sense
                    if ((dx == 0) && (dy == 0)) {
                        //sightPriority/=2.0f;
                        //movementPriority/=2.0f;
                    } else {
                        movement = true;
                    }

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
                }

                lastMX = x;
                lastMY = y;

                if (www % movementStatusPeriod == 0) {

                    if ((dx == 0) && (dy == 0)) {
                        updateMovement(direction(0,0), 1.0f);
                    }
                    else {
                        //4 basis vectors
                        int maxVelocity = 64;
                        updateMovement(dx, dy, 0, -maxVelocity);
                        updateMovement(dx, dy, 0, maxVelocity);
                        updateMovement(dx, dy, -maxVelocity, 0);
                        updateMovement(dx, dy, maxVelocity, 0);
                    }
                }

                    /*if (movement)*/
                if (www % radarPeriod == 0)  {
                    //predict next type of block at next current position

                    int rad = 4;
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



                            float sightPriority = (float)(4.0 / (4.0 + Math.sqrt(i*i+j*j)));

                            String s2 = "$" + sightPriority + "$" + "<" + direction + "--> [" + datachar + "]>. :|:";
                            nar.input(s2);



                                //System.out.println(i + " " + j + " " +  s2);


//                            if ((sight[k] != null) && (sight[k].equals(s))) {
//                                continue;
//                            }
//
//                            sight[k] = s;


                            //chg.set(/*"$" + sightPriority + "$" +*/s);


                        }
                    }





                    int[] keys = new int[]{Mario.KEY_LEFT, Mario.KEY_RIGHT, Mario.KEY_UP, Mario.KEY_DOWN, Mario.KEY_JUMP, Mario.KEY_SPEED};
                    for (final int kk : keys) {
                        String ko = "^keyboard" + kk;
                        if (nar.memory.operator(ko) == null) {
                            nar.on(new NullOperator("^" + "keyboard" + kk) {

                                @Override
                                protected List<Task> execute(Operation operation, Term[] args, Memory memory) {

                                    String state = args[0].toString();

                                    Task task = operation.getTask();
                                    //if ((task.getParentTask()!=null) && (task.getParentBelief()!=null)) {
                                    Task parent = task.getParentTask();
                                    Task root = task.getRootTask();

                                    //System.out.print(nar.getTime() + ": " + operation.getTask() + " caused by " + task.getParentBelief() + ", parent=" + parent);
                                        
                                        /*if (parent!=root) {
                                            System.out.println(", root=" + root);
                                        }
                                        else {
                                            System.out.println();
                                        }*/
                                    if (Mario.KEY_UP == kk || Mario.KEY_JUMP == kk) {
                                        mario.keys[Mario.KEY_RIGHT] = state.equals("on");
                                    }
                                    mario.keys[kk] = state.equals("on");
                                    //}

                                    return super.execute(operation, args, memory);
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

                            int dx = Math.round((x - s.x) / 16);
                            int dy = Math.round((y - s.y) / 16);

                            float sightPriority = (float)(4.0 / (4.0 + Math.sqrt(dx*dx+dy*dy)));

                            nar.input("$" + sightPriority + "$" +
                                    " <{" + type + "} --> " + direction(dx, dy) + ">. :|:");

                            //nar.addInput("$" + sv.toString() + "$ <(*,<(*," + dx +"," + dy + ") --> localPos>," + type + ") --> feel>. :|:");
                        }

                    }

                    //keyTime = mario.keys.clone();

                    lastX = x;
                    lastY = y;
                }

                if (www % commandPeriod == 0) {
                    goals();
                }
                www++;
                cycle++;
            }


        });

    }

    public int www = 0;

    @Override
    protected void update() {

    }

}

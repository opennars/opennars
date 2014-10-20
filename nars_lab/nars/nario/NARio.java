package nars.nario;

import java.awt.event.KeyEvent;
import static java.lang.Math.log;
import static java.lang.Math.signum;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DiscretinuousBagNARBuilder;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.io.ChangedTextInput;
import nars.language.Term;
import nars.nario.level.Level;
import nars.nario.level.LevelGenerator;
import nars.nario.sprites.Enemy;
import nars.nario.sprites.Mario;
import nars.nario.sprites.Particle;
import nars.nario.sprites.Sparkle;
import nars.nario.sprites.Sprite;
import nars.operator.NullOperator;
import nars.operator.Operation;

/**
 *
 * @author me
 */
public class NARio extends Run {

    private final NAR nar;
    private LevelScene level;
    private float lastX = -1;
    private float lastY = -1;
    int cycle = 0;
    int gotCoin = 0;
    private Mario mario;    
    static double gameRate;

    private ChangedTextInput moveInput;
    private ChangedTextInput velInput;

    public NARio(NAR n) {
        super();
        this.nar = n;
        //start();
        run();
    }

    public static void main(String[] arg) {
        //NAR nar = new DefaultNARBuilder().realtime().build();
        
        NAR nar = new DiscretinuousBagNARBuilder().setConceptBagSize(1024).simulationTime().build();
        
        //NAR nar = new ContinuousBagNARBuilder().build();
        /*nar.param().termLinkRecordLength.set(4);
         nar.param().beliefCyclesToForget.set(30);
         nar.param().conceptCyclesToForget.set(7);
         nar.param().taskCyclesToForget.set(22);
         nar.param().termLinkMaxReasoned.set(6);
        
         nar.param().cycleInputTasks.set(1);
         nar.param().cycleMemory.set(1);*/
        
       // nar.param().conceptForgetDurations.set(99.0f);

       // nar.param().beliefForgetDurations.set(99.0f);
        
        //new TextOutput(nar, System.out).setShowInput(true);
        
        nar.param().decisionThreshold.set(0.3);
        nar.param().noiseLevel.set(0);
        nar.param().conceptForgetDurations.set(10);
        nar.param().duration.set(500); //??
        float fps = 20f;
        gameRate = 1.0f / fps;

        NARio nario = new NARio(nar);

        new NARSwing(nar);
        nar.startFPS(fps, 500, 1f);
    }

    String[] sight = new String[9];

    protected void axioms() {
                        
                        //System.out.println("Inports: " + nar.getInPorts().size()); 
                        //System.out.println("Flushed: " + flushed);

                        //NAR.DEBUG = true;
                        

                        moveInput = new ChangedTextInput(nar);
                        velInput = new ChangedTextInput(nar);
                        
                    //nar.addInput("<(*,?m,(*,?x,?y)) --> space>? :/:");                
                        nar.addInput("<?y --> space>? :/:");
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
                        //nar.addInput("<solid <-> empty>. %0.00;0.99%");
                        //nar.addInput("<up <-> down>. %0.00;0.99%");
                        //nar.addInput("<left <-> right>. %0.00;0.99%");
        
    }
    
    ChangedTextInput[] keyInput = new ChangedTextInput[6];
    
    protected void setKey(int k, boolean pressed) {
        if (keyInput[k] == null)
            keyInput[k] = new ChangedTextInput(nar);
        
        if (keyInput[k].set("(^keyboard" + k + "," + (pressed ? "on" : "off") + ")!")) {            
            //input the opposite keypress as a back-dated input from between last frame and this
//            long dd = nar.param().duration.get();
//            long lastFrame = nar.getTime() - dd + dd/2;
//            nar.addInput("(^keyboard" + k + "," + (!pressed ? "on" : "off") + "). :|:", lastFrame);
        }
    }
    
    @Override protected void toggleKey(int keyCode, boolean isPressed)
    {
        if (keyCode == KeyEvent.VK_LEFT)
        {
            setKey(0, isPressed);
            scene.toggleKey(Mario.KEY_LEFT, isPressed);
        }
        if (keyCode == KeyEvent.VK_RIGHT)
        {
            setKey(1, isPressed);
            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
        }
        if (keyCode == KeyEvent.VK_DOWN)
        {
            setKey(2, isPressed);
            scene.toggleKey(Mario.KEY_DOWN, isPressed);
        }
        if (keyCode == KeyEvent.VK_UP)
        {
            setKey(3, isPressed);
            scene.toggleKey(Mario.KEY_UP, isPressed);
        }
        if (keyCode == KeyEvent.VK_S)
        {
            setKey(4, isPressed);
            scene.toggleKey(Mario.KEY_JUMP, isPressed);
        }
        if (keyCode == KeyEvent.VK_A)
        {
            setKey(5, isPressed);
            scene.toggleKey(Mario.KEY_SPEED, isPressed);
        }
    }

    protected int slog(int x) {
        if (x == 0) return 0;
        int sign = (int)signum(x);        
        x = Math.abs(x);
        return sign * (int)Math.ceil(log((1+x)));
    }
    
    @Override
    public void ready() {
        //level = startLevel(0, 1, LevelGenerator.TYPE_OVERGROUND);

        scene = level = new LevelScene(graphicsConfiguration, this, 4,1, LevelGenerator.TYPE_OVERGROUND) {
            @Override
            protected Mario newMario(LevelScene level) {
                return new Mario(level) {
                    @Override
                    public void getCoin() {
                        super.getCoin();
                        gotCoin++;
                    }
                };
            }
        };
        level.setSound(sound);
        level.init();

        mario = level.mario;
        mario.setInvincible(true);
                   
        axioms();        
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //new Window("Implications", new SentenceGraphPanel(nar, new ImplicationGraph(nar))).show(500,500);
                //new Window("Inheritance", new SentenceGraphPanel(nar, new InheritanceGraph(nar))).show(500,500);
                
            }
            
        });
                
        nar.memory.event.on(Events.FrameEnd.class, new Observer() {
            private int[] keyTime = new int[256];
            private float lastMX;
            private float lastMY;

            @Override
            public void event(Class event, Object... arguments) {

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
                
                    
               /* if (cycle % cyclesPerMario == 0)*/ {
                    cycle(gameRate);
                }

                {
//                if (cycle % 100 == 1) {
//                    System.out.println("Inports: " + nar.getInPorts().size());
//                }
 

                //float sightPriority = 0.75f;
                    //float movementPriority = 0.60f;
                    float x = level.mario.x;
                    float y = level.mario.y;

                    boolean movement = false;

                    if (lastX != -1) {
                        int dx = Math.round((x - lastX) / 1);
                        int dy = Math.round((y - lastY) / 1);
                        
                        int mx = (int)Math.round((x-lastMX)/16);
                        int my = (int)Math.round((y-lastMY)/16);

                        //if no movement, decrease priority of sense
                        if ((dx == 0) && (dy == 0)) {
                        //sightPriority/=2.0f;
                            //movementPriority/=2.0f;
                        } else {
                            movement = true;
                        }

                        if (movement) {

                            if (!((mx==0) && (my==0))) {
                                if (moveInput.set(/*"$" + movementPriority + "$"*/"<(*," + mx + "," + my + ") --> moved>. :|:")) {
                                    //if significantly changed block position, record it for next difference
                                    lastMX = x;
                                    lastMY = y;
                                }
                            }
                            velInput.set(/*"$" + movementPriority + "$"*/"<(*," + slog(dx) + "," + slog(dy) + ") --> velocity>. :|:");
                            
                        }
                        else {
                            if (moveInput.set("<(*,0,0) --> moved>. :|:")) { //stopped
                                lastMX = x;
                                lastMY = y;
                            }
                
                        }

                    }

                    /*if (movement)*/ {
                    //predict next type of block at next current position

                        int k = -1; //cycle through a different seeing each cycle

                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {

                                k++;
                                
                                int block = level.level.getBlock(x + i*16f-8, y + j*16f-8);
                                int data = level.level.getData(x + i*16-8, y + j*16-8);

                                boolean blocked
                                        = ((block & Level.BIT_BLOCK_ALL) > 0)
                                        || ((block & Level.BIT_BLOCK_LOWER) > 0)
                                        || ((block & Level.BIT_BLOCK_UPPER) > 0);

//                            String s = " <(*," + 
//                                            (blocked ? "solid" : "empty") +
//                                            "," + data + ",(*," + i + "," + j + 
//                                            ")) --> space>. :|:";
                                String direction = "(*," + i + "," + j + ")";
                                /*if ((i == 0) && (j == -1)) {
                                    direction = "up";
                                } else if ((i == 0) && (j == 1)) {
                                    direction = "down";
                                } else if ((i == -1) && (j == 0)) {
                                    direction = "left";
                                } else if ((i == 1) && (j == 0)) {
                                    direction = "right";
                                } else */{
                                    direction = "(*," + i + "," + j + ")";
                                }
                                
                                char datachar = (char)('r' + data);
                                String s = "<" + direction + " --> " + (blocked ? "solid" : "empty") + ">. :|:";
                                s += "\n";
                                s +=  "<" + direction + " --> d" + datachar + ">. :|:";

                                if ((sight[k] != null) && (sight[k].equals(s))) {
                                    continue;
                                }

                                sight[k] = s;

                                nar.addInput(/*"$" + sightPriority + "$" +*/
                                        s);
                            }
                        }
                    }

                    if (gotCoin > 0) {
                        nar.addInput("<(*,0,0) --> gotCoin>. :|:");
                    }

                        
                    int[] keys = new int[] { Mario.KEY_LEFT,Mario.KEY_RIGHT, Mario.KEY_UP, Mario.KEY_DOWN, Mario.KEY_JUMP, Mario.KEY_SPEED };
                    for (final int k : keys) {
                        String ko = "keyboard" + k;
                        if (nar.memory.getOperator(ko) == null) {
                            nar.memory.addOperator(new NullOperator("^" + "keyboard" + k) {

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
                                        
                                        mario.keys[k] = state.equals("on");
                                    //}

                                    return super.execute(operation, args, memory);
                                }

                            });
                        }

                        int currentKeyTime, nextKeyTime;
                        currentKeyTime = nextKeyTime = keyTime[k];
                        boolean wasPressed = currentKeyTime > 0;
                        boolean pressed;

                        if (!mario.keys[k]) {
                            nextKeyTime = 0;
                            pressed = false;
                        }
                        else {
                            nextKeyTime++;
                            pressed = true;
                        }

                        if (pressed!=wasPressed) {
                            /*String budget = (nextKeyTime > 0) ? 
                                    "$" + (1.0 / (1.0 + nextKeyTime)) + "$" :
                                    "";*/
                            String state = nextKeyTime > 0 ? "on" : "off";
                            //String budget = "$0.8;0.1$";
                            String budget = "";
                            //nar.addInput(budget + "(^" + ko + "," + state + ")!");
                        }

                        keyTime[k] = nextKeyTime;
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

                        double senseRadius = 7;
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

                            nar.addInput(/*"$" + sightPriority + "$" +*/
                                    " <(*," + dx + "," + dy + ") --> " + type + ">. :|:");

                            //nar.addInput("$" + sv.toString() + "$ <(*,<(*," + dx +"," + dy + ") --> localPos>," + type + ") --> feel>. :|:");
                        }

                    }

                    //keyTime = mario.keys.clone();

                    lastX = x;
                    lastY = y;
                    gotCoin = 0;
                }
                
                cycle++;
            }
            

        });

    }

    @Override
    protected void update() {

    }

}

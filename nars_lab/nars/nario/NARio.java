package nars.nario;

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
import nars.io.Output;
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
    int cyclesPerMario = 4;
    int initCycles = 1000;

    public NARio(NAR n) {
        super();
        this.nar = n;
        //start();
        run();
    }

    public static void main(String[] arg) {
        NAR nar = new DiscretinuousBagNARBuilder().setConceptBagSize(1024).build();
        //NAR nar = new ContinuousBagNARBuilder().build();
        /*nar.param().termLinkRecordLength.set(4);
         nar.param().beliefCyclesToForget.set(30);
         nar.param().conceptCyclesToForget.set(7);
         nar.param().taskCyclesToForget.set(22);
         nar.param().termLinkMaxReasoned.set(6);
        
         nar.param().cycleInputTasks.set(1);
         nar.param().cycleMemory.set(1);*/

        //new TextOutput(nar, System.out).setShowInput(true);
        nar.param().duration.set(10);
        nar.param().decisionThreshold.set(0.1);
        nar.param().noiseLevel.set(0);
        nar.param().shortTermMemorySize.set(35);


        NARio nario = new NARio(nar);

        new NARSwing(nar);
        nar.start(50, 30);
    }

    String[] sight = new String[9];

    protected void axioms() {
                        int flushed = nar.flushInput(Output.NullOutput);
                        //System.out.println("Inports: " + nar.getInPorts().size()); 
                        //System.out.println("Flushed: " + flushed);

                        //NAR.DEBUG = true;
                        
                        
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
    
    @Override
    public void ready() {
        //level = startLevel(0, 1, LevelGenerator.TYPE_OVERGROUND);

        scene = level = new LevelScene(graphicsConfiguration, this, 0, 1, LevelGenerator.TYPE_OVERGROUND) {
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
  
        nar.finish(initCycles);
        
        
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //new Window("Implications", new SentenceGraphPanel(nar, new ImplicationGraph(nar))).show(500,500);
                //new Window("Inheritance", new SentenceGraphPanel(nar, new InheritanceGraph(nar))).show(500,500);
                
            }
            
        });
                
        nar.memory.event.on(Events.FrameEnd.class, new Observer() {
            private int[] keyTime = new int[256];

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
                
                    
                /*if (cycle % cyclesPerMario == 0)*/ {
                    cycle(0.05);
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
                        int dx = Math.round((x - lastX) / 16);
                        int dy = Math.round((y - lastY) / 16);

                        //if no movement, decrease priority of sense
                        if ((dx == 0) && (dy == 0)) {
                        //sightPriority/=2.0f;
                            //movementPriority/=2.0f;
                        } else {
                            movement = true;
                        }

                        if (movement) {
                            nar.addInput(/*"$" + movementPriority + "$"*/"<(*," + dx + "," + dy + ") --> moved>. :\\:");
                        }

                    }

                    /*if (movement)*/ {
                    //predict next type of block at next current position

                        int k = -1; //cycle through a different seeing each cycle

                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {

                                k++;

                                int block = level.level.getBlock(x, y);
                                int data = level.level.getData(x, y);

                                boolean blocked
                                        = ((block & Level.BIT_BLOCK_ALL) > 0)
                                        || ((block & Level.BIT_BLOCK_LOWER) > 0)
                                        || ((block & Level.BIT_BLOCK_UPPER) > 0);

//                            String s = " <(*," + 
//                                            (blocked ? "solid" : "empty") +
//                                            "," + data + ",(*," + i + "," + j + 
//                                            ")) --> space>. :|:";
                                String direction = "(*," + i + "," + j + ")";
                                if ((i == 0) && (j == -1)) {
                                    direction = "down";
                                } else if ((i == 0) && (j == 1)) {
                                    direction = "up";
                                } else if ((i == -1) && (j == 0)) {
                                    direction = "left";
                                } else if ((i == 1) && (j == 0)) {
                                    direction = "right";
                                } else {
                                    continue; //ignore diagonal for now
                                }
                                String s = "<" + (blocked ? "solid" : "empty") + " --> " + direction +">. :|:";

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
                                    if ((task.getParentTask()!=null) && (task.getParentBelief()!=null)) {
                                        Task parent = task.getParentTask();
                                        Task root = task.getRootTask();
                                        
                                        System.out.print(nar.getTime() + ": " + operation.getTask() + " caused by " + task.getParentBelief() + ", parent=" + parent);
                                        
                                        if (parent!=root) {
                                            System.out.println(", root=" + root);
                                        }
                                        else {
                                            System.out.println();
                                        }
                                        
                                        mario.keys[k] = state.equals("on");
                                    }

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
                            nar.addInput(budget + "(^" + ko + "," + state + ")!");
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

                        double senseRadius = 4;
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

package nars.nario;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DiscretinuousBagNARBuilder;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.Window;
import nars.gui.output.graph.SentenceGraphPanel;
import nars.inference.GraphExecutive;
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
    int gotCoin = 0;
    private Mario mario;
    int cyclesPerMario = 1;
    final boolean random = false;
    int healthStatusCycle = 512;
    final float gameTimePerMemoryCycle = 0.02f;

    public static void main(String[] arg) {
        NAR nar = new DiscretinuousBagNARBuilder(true).setConceptBagSize(1024).build();
        //NAR nar = new DefaultNARBuilder().build();
        /*nar.param().termLinkRecordLength.set(4);
         nar.param().beliefCyclesToForget.set(30);
         nar.param().conceptCyclesToForget.set(7);
         nar.param().taskCyclesToForget.set(22);
         nar.param().termLinkMaxReasoned.set(6);
        
         nar.param().cycleInputTasks.set(1);
         nar.param().cycleMemory.set(1);*/

        //new TextOutput(nar, System.out).setShowInput(true);
        nar.param().duration.set(10);
        nar.param().noiseLevel.set(10);
        nar.param().shortTermMemorySize.set(1);

        NARio nario = new NARio(nar);
        new NARSwing(nar);
        //nar.start(10);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);
        if (e.getKeyChar() == '[') {
            System.out.println("Autonomy Disabled; Learning");
            nar.param().decisionThreshold.set(1.0);
        }
        if (e.getKeyChar() == ']') {
            System.out.println("Autonomy Enabled");
            nar.param().decisionThreshold.set(0.3);
        }
        if (e.getKeyChar() == 'i') {
            //new Window("Implications", new JGraphXGraphPanel(nar.memory.executive.graph.implication)).show(500,500);            
            new Window("Implications", new SentenceGraphPanel(nar, nar.memory.executive.graph.implication)).show(500,500);
        }
    }


    
    
    public NARio(NAR n) {
        super();
        this.nar = n;

        new GraphExecutive(nar.memory);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //new Window("Implications", new SentenceGraphPanel(nar, new ImplicationGraph(nar))).show(500,500);
                //new Window("Inheritance", new SentenceGraphPanel(nar, new InheritanceGraph(nar))).show(500,500);

            }

        });

        
        
        nar.memory.event.on(Events.CycleEnd.class, new Observer() {
            private int[] keyTime = new int[256];
            private int cyclesWithoutXMove;
            private int cyclesWithoutMove;

            @Override
            public void event(Class event, Object... arguments) {

                
                
                if (nar.getTime() % cyclesPerMario == 0) {
                    if (scene != null) {
                        try {
                            
                                cycle(gameTimePerMemoryCycle);
                        } catch (NullPointerException n) {
                            //HACK sick of debugging mario brothers
                            return;
                        }
                    } else {
                        return;
                    }

                    if (level == null)                         return;
                    if (level.mario == null)                        return;
                    
//                if (cycle % 100 == 1) {
//                    System.out.println("Inports: " + nar.getInPorts().size());
//                }
                    //float sightPriority = 0.75f;
                    //float movementPriority = 0.60f;
                    float x = level.mario.x;
                    float y = level.mario.y;

                    boolean movement = false;

                    if (lastX != -1) {
                        float dx = ((x - lastX) / 16.0f);
                        float dy = ((y - lastY) / 16.0f);

                        float minMove = 0.5f; //in blocks
                        
                        //if no movement, decrease priority of sense
                        if ((dx < minMove) && (dy < minMove)) {
                            //sightPriority/=2.0f;
                            //movementPriority/=2.0f;
                            cyclesWithoutMove++;
                        } else {
                            movement = true;
                            cyclesWithoutMove = 0;
                        }
                        
                        if (dx == 0) {
                            cyclesWithoutXMove++;
                        }
                        else {
                            cyclesWithoutXMove = 0;
                        }

                        int idx = (int)Math.round(dx);
                        int idy = (int)Math.round(dy);
                        int idist = (int)Math.round(Math.sqrt(dx*dx+dy*dy));
                        if (movement) {
//                            if ((idx==0) && (idy==0)) {
//                                //create a fractional movement for <1 nudges
//                                idx = (int)Math.ceil(dx);
//                                idy = (int)Math.ceil(dy);
//                                int conf = (int)(100.0 * 0.9 * Math.sqrt( dx*dx + dy*dy ));
//                                if (conf > 99) conf = 99;
//                                nar.addInput("<(*," + idx + "," + idy + ") --> moved>. :|: %1.00;0." + conf + "%");
//                            }
//                            else {                                
                                nar.addInput(/*"$" + movementPriority + "$"*/
                                        "<(*," + idx + "," + idy + "," + idist + ") --> moved>. :|:");
//                            }
                        }

                    }
    
                    boolean statusUpdate = nar.getTime() % healthStatusCycle == 0;
                    //health status report
                    if (statusUpdate) {
                        float dead = 0.0f;
                        //consider self dead when no movement
                        if (cyclesWithoutMove > 0) {
                            dead += 0.5;
                        }
                        //consider self dead when no X movement
                        if (cyclesWithoutXMove > 8) {
                            dead += 0.25;
                        }
                        
                        if (dead == 0) {
                            //nar.addInput("<nario --> alive>. :|: %1.00;0.50%");
                        }
                        else {
                            nar.addInput("<nario --> alive>. :|: %0.00;" + dead + "%");
                            nar.addInput("<nario --> alive>!");
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
                                /*String s = "<" + (blocked ? "solid" : "empty") + " --> " + 
                                        direction + ">. :|:";*/
                                String s = "<" + direction + " --> " + 
                                        (blocked ? "solid" : "empty") + ">. :|:";

                                if (((sight[k] != null) && (sight[k].equals(s))) && (!statusUpdate)) {
                                    continue;
                                }

                                sight[k] = s;

                                nar.addInput(/*"$" + sightPriority + "$" +*/
                                        s);
                            }
                        }
                    }

                    if (gotCoin > 0) {
                        nar.addInput("<(*,0,0,0) --> gotCoin>. :|:");
                        nar.addInput("<nario --> alive>. :|:");
                    }

                    int[] keys = new int[]{Mario.KEY_LEFT, Mario.KEY_RIGHT, Mario.KEY_UP, Mario.KEY_DOWN, Mario.KEY_JUMP, Mario.KEY_SPEED};
                    for (final int k : keys) {
                        String ko = "keyboard" + k;
                        if (nar.memory.getOperator(ko) == null) {
                            nar.memory.addOperator(new NullOperator("^" + "keyboard" + k) {

                                @Override
                                protected List<Task> execute(Operation operation, Term[] args, Memory memory) {

                                    String state = args[0].toString();

                                    Task task = operation.getTask();
                                    if ((task.getParentTask() != null) && (task.getParentBelief() != null)) {
                                        Task parent = task.getParentTask();
                                        Task root = task.getRootTask();

                                        //System.out.print(nar.getTime() + ": " + operation.getTask() + " caused by " + task.getParentBelief().toString(nar, true) + ", parent=" + parent);

                                        /*
                                        if (parent != root) {
                                            System.out.println(", root=" + root);
                                        } else {
                                            System.out.println();
                                        }
                                        */

                                        if (random) {
                                            mario.keys[(int) (Math.random() * 6)] = Math.random() < 0.5 ? false : true;
                                        } else {
                                            mario.keys[k] = state.equals("on");
                                        }
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
                        int idist = (int)Math.round(dist);
                        if (dist <= senseRadius) {
                            double sightPriority = 0.5f + 0.5f * (senseRadius - dist) / senseRadius;

                            //sparkles are common and not important
                            String type = s.getClass().getSimpleName();
                            if (s instanceof Enemy) {
                                type = s.toString();
                            }

                            int dx = Math.round((x - s.x) / 16);
                            int dy = Math.round((y - s.y) / 16);

                            //prevent repeat inputs
                            if ((s.ix == Integer.MAX_VALUE) || ((s.ix != dx) || (s.iy != dy))) {
                                s.ix = dx;
                                s.iy = dy;
                                nar.addInput("$" + sightPriority + "$" +
                                        " <(*," + dx + "," + dy + "," + idist + ") --> " + type + ">. :|:");
                            }

                            //nar.addInput("$" + sv.toString() + "$ <(*,<(*," + dx +"," + dy + ") --> localPos>," + type + ") --> feel>. :|:");
                        }
                        else {
                            s.ix = s.iy = Integer.MAX_VALUE;
                        }

                    }

                    //keyTime = mario.keys.clone();
                    lastX = x;
                    lastY = y;
                    gotCoin = 0;
                }

                
            }

        });

        //start();
        run();
    }

    String[] sight = new String[9];

    protected void axioms() {
        int flushed = nar.flushInput(Output.NullOutput);
                        //System.out.println("Inports: " + nar.getInPorts().size()); 
        //System.out.println("Flushed: " + flushed);

                        //NAR.DEBUG = true;
        //nar.addInput("<(*,?m,(*,?x,?y)) --> space>? :/:");                
        nar.addInput("$0.99;0.99$ <nario --> alive>!");
        nar.addInput("$0.99;0.99$ (--,<nario --> dead>)!");
        nar.addInput("<alive <-> dead>. %0.00;0.99%");        
        nar.addInput("<?y --> space>? :/:");
        //nar.addInput("<{(*,0,0),(*,0,1),(*,1,0),(*,-1,0),(*,0,-1)} <-> direction>.");
        nar.addInput("<(*,0,0,0) <-> center>. %1.00;0.99%");
        nar.addInput("<(*,-1,0,1) <-> left>. %1.00;0.99%");
        nar.addInput("<(*,1,0,1) <-> right>. %1.00;0.99%");
        nar.addInput("<(*,0,1,1) <-> up>. %1.00;0.99%");
        nar.addInput("<(*,0,-1,1) <-> down>. %1.00;0.99%");
                        //nar.addInput("<solid <-> empty>. %0.00;0.99%");
        //nar.addInput("<up <-> down>. %0.00;0.99%");
        //nar.addInput("<left <-> right>. %0.00;0.99%");

    }

    @Override
    public void ready() {
        //level = startLevel(0, 1, LevelGenerator.TYPE_OVERGROUND);

        //reset keys
        if (mario!=null)
            if (mario.keys!=null)
                Arrays.fill(mario.keys, false);
        
        axioms();

        
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
        
        //mario.setInvincible(true);

    }

    @Override
    public void levelWon() {
        nar.addInput("<nario --> win>. :|:");
        scene = null;
        ready();
    }

    @Override
    public void lose() {
        //not sure if this will ever be called
        ready();
    }

    @Override
    public void levelFailed() {
        nar.addInput("<nario --> dead>. :|:");

        scene = null;
        ready();
        /*
         scene = mapScene;
         mapScene.startMusic();
         Mario.lives--;
         if (Mario.lives == 0)
         {
         lose();
         }
         */
    }

    @Override
    protected void update() {

    }


}

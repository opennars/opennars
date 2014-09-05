package nars.nario;

import nars.core.EventEmitter.Observer;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.ShortFloat;
import nars.gui.NARSwing;
import nars.io.TextOutput;
import nars.nario.level.LevelGenerator;
import nars.nario.sprites.Enemy;
import nars.nario.sprites.Mario;
import nars.nario.sprites.Sparkle;
import nars.nario.sprites.Sprite;

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
    int cycleSamples = 8;
    int gotCoin = 0;
    private Mario mario;

    public NARio(NAR n) {
        super();
        this.nar = n;               
    }

    @Override
    public void ready() {
        //level = startLevel(0, 1, LevelGenerator.TYPE_OVERGROUND);

        
        scene = level = new LevelScene(graphicsConfiguration, this, 0, 1, LevelGenerator.TYPE_OVERGROUND) {
            @Override protected Mario newMario(LevelScene level) {
                return new Mario(level) {
                    @Override public void getCoin() {
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
        
        nar.memory.event.on(Memory.Events.CycleStop.class, new Observer() {
            private boolean[] lastKeys;

            @Override public void event(Class event, Object... arguments) {
                
                if (((cycle++) % cycleSamples) != 0) {
                    return;
                }
                
                float sightPriority = 0.75f;
                float movementPriority = 0.60f;

                float x = level.mario.x;
                float y = level.mario.y;

                //predict next type of block at next current position
                nar.addInput("<(*,<(*,?x,?y) --> material>,<(*,0,0) --> localPos>) --> see>? :/:");
                nar.addInput("<<(*,?x,?y) --> localPos> --> ?s>? :/:");
                
                boolean movement = false;
                
                if (lastX != -1) {
                    int dx = Math.round((x - lastX)/16);
                    int dy = Math.round((y - lastY)/16);

                    //if no movement, decrease priority of sense
                    if ((dx==0) && (dy==0)) {
                        sightPriority/=2.0f;
                        movementPriority/=2.0f;
                    }
                    else
                        movement = true;
                    
                    if (movement)
                        nar.addInput(/*"$" + movementPriority + "$"*/ "<<(*," + dx +"," + dy + ") --> localPos> --> move>. :|:");
                    
                }

                if (movement) {
                    for (int i= -1; i <=1; i++)
                        for (int j= -1; j <= 1; j++) {
                            int block = 127 + level.level.getBlock(x, y);
                            int data = level.level.getData(x, y);
                            nar.addInput(/*"$" + sightPriority + "$" +*/ " <(*,<(*," + block +"," + data + ") --> material>,<(*," + i + "," + j + ") --> localPos> ) --> feel>. :|:");
                        }
                }
                

                if (gotCoin > 0) {
                    nar.addInput("<gotCoin --> feel>. :|:");
                }
                
        
                if (lastKeys!=null) {
                    if (lastKeys[Mario.KEY_LEFT]!=mario.keys[Mario.KEY_LEFT])
                        nar.addInput("<moveLeft --> " + (mario.keys[Mario.KEY_LEFT] ? "on" : "off") + ">. :|:");
                    if (lastKeys[Mario.KEY_RIGHT]!=mario.keys[Mario.KEY_RIGHT])
                        nar.addInput("<moveRight --> " + (mario.keys[Mario.KEY_RIGHT] ? "on" : "off") + ">. :|:");
                }
                
                for (Sprite s : level.sprites) {
                    if (s instanceof Mario) continue;
                    
                    double senseRadius = 15;
                    double dist = Math.sqrt( (x-s.x)*(x-s.x) + (y-s.y)*(y-s.y) )/16.0;
                    if (dist <= senseRadius) {
                        double priority = 0.5f + 0.5f * (senseRadius - dist) / senseRadius;
                        
                        //sparkles are common and not important
                        if (s instanceof Sparkle)
                            priority/=2f;
                        
                        ShortFloat sv = new ShortFloat((float)priority);

                        
                        String type = s.getClass().getSimpleName();
                        if (s instanceof Enemy)
                            type = s.toString();
                        
                        int dx = Math.round((x - s.x)/16);
                        int dy = Math.round((y - s.y)/16);
                                                
                        nar.addInput("$" + sv.toString() + "$ <(*,<(*," + dx +"," + dy + ") --> localPos>," + type + ") --> feel>. :|:");
                    }
                    
                    
                }
                
                lastKeys = mario.keys.clone();
                
                lastX = x;
                lastY = y;
                gotCoin = 0;
            }
            
        });
                
    }

    
    @Override
    protected void update() {
        
    }
    
    
    public static void main(String[] arg) {
        //NAR nar = new ContinuousBagNARBuilder(true).setConceptBagSize(2048).build();
        NAR nar = new DefaultNARBuilder().build();
        nar.param().cycleInputTasks.set(32);
        nar.param().cycleMemory.set(23);
        
        new NARSwing(nar); 
        new TextOutput(nar, System.out).setShowInput(false);
        nar.start(100);

        NARio nario = new NARio(nar);
        nario.TICKS_PER_SECOND = 12;
        

        nar.param().noiseLevel.set(50);
    }
}

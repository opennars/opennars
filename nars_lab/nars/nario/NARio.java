package nars.nario;

import nars.core.EventEmitter.Observer;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.entity.ShortFloat;
import nars.gui.NARSwing;
import nars.nario.level.Level;
import nars.nario.level.LevelGenerator;
import nars.nario.sprites.Enemy;
import nars.nario.sprites.Mario;
import nars.nario.sprites.Particle;
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
    int gotCoin = 0;
    private Mario mario;

    public NARio(NAR n) {
        super();
        this.nar = n;               
    }

    String[] sight = new String[9];
    
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
                
//                if (((cycle++) % cycleSamples) != 0) {
//                    return;
//                }
                if (cycle == 0) {
                    nar.addInput("<(*,?m,(*,?x,?y)) --> space>? :/:");                
                    nar.addInput("<?y --> space>? :/:");
                }
                
                //float sightPriority = 0.75f;
                //float movementPriority = 0.60f;

                float x = level.mario.x;
                float y = level.mario.y;

                
                boolean movement = false;
                
                if (lastX != -1) {
                    int dx = Math.round((x - lastX)/16);
                    int dy = Math.round((y - lastY)/16);

                    //if no movement, decrease priority of sense
                    if ((dx==0) && (dy==0)) {
                        //sightPriority/=2.0f;
                        //movementPriority/=2.0f;
                    }
                    else
                        movement = true;
                    
                    if (movement)
                        nar.addInput(/*"$" + movementPriority + "$"*/ "<(*," + dx +"," + dy + ") --> moved>. :\\:");
                    
                }
                
                /*if (movement)*/ {
                    //predict next type of block at next current position
                
                    
                
                    
                    int k = -1; //cycle through a different seeing each cycle
                    
                    for (int i= -1; i <=1; i++)
                        for (int j= -1; j <= 1; j++) {
                            
                            k++;                            
                            
                            int block = level.level.getBlock(x, y);
                            int data = level.level.getData(x, y);
                            
                            boolean blocked = 
                                    ((block & Level.BIT_BLOCK_ALL) > 0) ||
                                    ((block & Level.BIT_BLOCK_LOWER) > 0) ||
                                    ((block & Level.BIT_BLOCK_UPPER) > 0);
                            
                            String s = " <(*," + 
                                            (blocked ? "solid" : "empty") +
                                            "," + data + ",(*," + i + "," + j + 
                                            ")) --> space>. :|:";
                            
                            if ((sight[k]!=null) && (sight[k].equals(s)))
                                continue;
                                    
                            sight[k] = s;
                            
                            nar.addInput(/*"$" + sightPriority + "$" +*/ 
                                    s);
                        }
                }
                
                

                if (gotCoin > 0) {
                    nar.addInput("<gotCoin --> space>. :|:");
                }
                
        
                if (lastKeys!=null) {
                    if (lastKeys[Mario.KEY_LEFT]!=mario.keys[Mario.KEY_LEFT])
                        nar.addInput("<(*,left," + (mario.keys[Mario.KEY_LEFT] ? "on" : "off") + ") --> input>. :|:");
                    if (lastKeys[Mario.KEY_RIGHT]!=mario.keys[Mario.KEY_RIGHT])
                        nar.addInput("<(*,right," + (mario.keys[Mario.KEY_RIGHT] ? "on" : "off") + ") --> input>. :|:");
                }
                
                if (Math.random() < 0.2) {
                    for (Sprite s : level.sprites) {
                        if (s instanceof Mario) continue;
                        if ((s instanceof Sparkle) || (s instanceof Particle)) {
                            continue;
                            //priority/=2f;
                        }

                        double senseRadius = 4;
                        double dist = Math.sqrt( (x-s.x)*(x-s.x) + (y-s.y)*(y-s.y) )/16.0;
                        if (dist <= senseRadius) {
                            double priority = 0.5f + 0.5f * (senseRadius - dist) / senseRadius;

                            //sparkles are common and not important

                            ShortFloat sv = new ShortFloat((float)priority);


                            String type = s.getClass().getSimpleName();
                            if (s instanceof Enemy)
                                type = s.toString();

                            int dx = Math.round((x - s.x)/16);
                            int dy = Math.round((y - s.y)/16);

                            nar.addInput(/*"$" + sightPriority + "$" +*/ 
                                    " <(*," + 
                                            type +
                                            ",(*," + dx + "," + dy + 
                                            ")) --> space>. :|:");

                            //nar.addInput("$" + sv.toString() + "$ <(*,<(*," + dx +"," + dy + ") --> localPos>," + type + ") --> feel>. :|:");
                        }


                    }
                }
                
                lastKeys = mario.keys.clone();
                
                lastX = x;
                lastY = y;
                gotCoin = 0;
                cycle++;
            }
            
        });
                
    }

    
    @Override
    protected void update() {
        
    }
    
    
    public static void main(String[] arg) {
        //NAR nar = new ContinuousBagNARBuilder(true).setConceptBagSize(2048).build();
        NAR nar = new DefaultNARBuilder().build();
        /*nar.param().termLinkRecordLength.set(4);
        nar.param().beliefCyclesToForget.set(30);
        nar.param().conceptCyclesToForget.set(7);
        nar.param().taskCyclesToForget.set(22);
        nar.param().termLinkMaxReasoned.set(6);
        
        nar.param().cycleInputTasks.set(1);
        nar.param().cycleMemory.set(1);*/
        
        new NARSwing(nar); 
        //new TextOutput(nar, System.out).setShowInput(true);
        nar.start(50);

        NARio nario = new NARio(nar);
        nario.TICKS_PER_SECOND = 12;
        

        //nar.param().noiseLevel.set(50);
    }
}

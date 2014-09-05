package nars.nario;

import nars.core.EventEmitter.Observer;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.build.ContinuousBagNARBuilder;
import nars.gui.NARSwing;
import nars.io.TextOutput;
import nars.nario.level.LevelGenerator;

/**
 *
 * @author me
 */


public class NARio extends Run {
    private final NAR nar;
    private LevelScene level;
    private float lastX = -1;
    private float lastY = -1;

    public NARio(NAR n) {
        super();
        this.nar = n;
        
    }

    @Override
    public void ready() {
        level = startLevel(0, 1, LevelGenerator.TYPE_OVERGROUND);
        level.mario.setInvincible(true);
        nar.memory.event.on(Memory.Events.MemoryCycleStart.class, new Observer() {

            @Override public void event(Class event, Object... arguments) {
                float sightPriority = 0.1f;
                float movementPriority = 0.1f;

                float x = level.mario.x;
                float y = level.mario.y;
                for (int i= -1; i <=1; i++)
                    for (int j= -1; j <= 1; j++) {
                        int block = 127 + level.level.getBlock(x, y);
                        int data = level.level.getData(x, y);
                        nar.addInput("$" + sightPriority + " $ <(*,<(*," + block +"," + data + ") --> material>,<(*," + i + "," + j + ") --> localPos> ) --> see>. :|:");

                        if (lastX != -1) {
                            int dx = Math.round((x - lastX)/16);
                            int dy = Math.round((y - lastY)/16);
                            nar.addInput("$" + movementPriority + " $ <<(*," + dx +"," + dy + ") --> localPos> --> move>. :|:");
                        }
                    }
                lastX = x;
                lastY = y;
            }
            
        });
                
    }

    
    @Override
    protected void update() {
        
    }
    
    
    public static void main(String[] arg) {
        NAR nar = new ContinuousBagNARBuilder(true).setConceptBagSize(2048).build();
        nar.param().cycleInputTasks.set(32);
        nar.param().cycleMemory.set(32);
        
        new NARSwing(nar); 
        new TextOutput(nar, System.out);
        nar.start(1000);

        new NARio(nar);

        nar.param().noiseLevel.set(50);
    }
}

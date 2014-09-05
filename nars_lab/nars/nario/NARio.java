package nars.nario;

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
    }

    int cycle=0, updateEvery = 150;
    
    @Override
    protected void update() {
        if (cycle++ % updateEvery != 0)
            return;
        
        float x = level.mario.x;
        float y = level.mario.y;
        for (int i= -1; i <=1; i++)
            for (int j= -1; j <= 1; j++) {
                int block = 127 + level.level.getBlock(x, y);
                int data = level.level.getData(x, y);
                nar.addInput("<(*,<(*," + block +"," + data + ") --> material>,<(*," + i + "," + j + ") --> relPos> ) --> see>. :|:");
                
                if (lastX != -1) {
                    int dx = Math.round((x - lastX)/16);
                    int dy = Math.round((y - lastY)/16);
                    nar.addInput("<(*," + dx +"," + dy + ") --> movement>. :|:");
                }
            }
        System.out.println("update " + cycle);
        lastX = x;
        lastY = y;
    }
    
    
   
    
    public static void main(String[] arg) {
        NAR nar = new ContinuousBagNARBuilder(true).setConceptBagSize(2048).build();
        nar.param().cycleInputTasks.set(32);
        nar.param().cycleMemory.set(32);
        nar.param().noiseLevel.set(10);
        
        new NARio(nar);
        new NARSwing(nar); 
        new TextOutput(nar, System.out);
        nar.start(250);
    }
}

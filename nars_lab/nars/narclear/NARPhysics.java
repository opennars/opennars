package nars.narclear;

import nars.NARGame;
import nars.core.NAR;

/**
 *
 * @author me
 */


public class NARPhysics extends NARGame {
    private final PhysicsModel model;
    private final PhysicsRun phy;

    public NARPhysics(NAR nar, PhysicsModel model) {
        super(nar);
        this.model = model;
        this.phy = new PhysicsRun(model);
    }

    @Override
    public void start(float fps) {
        phy.controller.setFrameRate((int)fps);        
        super.start(fps);        
    }

    @Override
    public void stop() {
        super.stop();
    }

    
    @Override
    public void init() {
    }

    @Override
    public void cycle() {
        phy.cycle();
    }
    
}

package nars.narclear;

import nars.NARGame;
import nars.core.NAR;

/**
 *
 * @author me
 */


public class NARPhysics<P extends PhysicsModel> extends NARGame {
    public final P model;
    public final PhysicsRun phy;

    public NARPhysics(NAR nar, P model) {
        super(nar);
        this.model = model;
        this.phy = new PhysicsRun(model);
    }

    @Override
    public void start(float fps) {
        phy.controller.setFrameRate((int)fps);        
        super.start(fps);        
    }
    
    public P getModel() { return model; }
    

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

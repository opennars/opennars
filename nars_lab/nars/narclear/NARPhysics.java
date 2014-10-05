package nars.narclear;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.NARGame;
import nars.core.NAR;



public class NARPhysics<P extends PhysicsModel> extends NARGame implements Runnable {
    public final P model;
    public final PhysicsRun phy;
    ExecutorService physExe = Executors.newFixedThreadPool(1);
    private Future<?> phyCycle;

    public NARPhysics(NAR nar, P model) {
        super(nar);
        this.model = model;
        this.phy = new PhysicsRun(model);
    }

    @Override
    public void start(float fps, int cyclesPerFrame) {
        phy.controller.setFrameRate((int)fps);        
        super.start(fps, cyclesPerFrame);        
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
        if (phy!=null) {
            
            //wait for prevoius cycle to finish if it hasnt
            if (phyCycle!=null) {
                try {
                    phyCycle.get();
                } catch (Exception ex) {
                    Logger.getLogger(NARPhysics.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            phyCycle = physExe.submit(this);            
        }
    }

    @Override
    public void run() {
        phy.cycle();        
    }
    
}

package nars.lab.narclear;

import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.lab.launcher.NARGame;
import nars.core.NAR;



public class NARPhysics<P extends PhysicsModel> extends NARGame implements Runnable {
    public final P model;
    public final PhysicsRun phy;
    ExecutorService physExe = Executors.newFixedThreadPool(1);
    private Future<?> phyCycle;

    public NARPhysics(NAR nar, float simulationRate, P model) {
        super(nar);
        this.model = model;
        this.phy = new PhysicsRun(nar,simulationRate, model) {

            @Override
            public void keyPressed(KeyEvent e) {
                NARPhysics.this.keyPressed(e);
            }
          
            
        };
        
    }
    public void keyPressed(KeyEvent e) { }

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
            
            //wait for previous cycle to finish if it hasnt
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

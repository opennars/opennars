/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.perf;

import nars.core.EventEmitter.Observer;
import nars.core.Events.FrameStart;
import nars.core.NAR;
import nars.core.build.NeuromorphicNARBuilder;
import nars.io.TextOutput;

/**
 *
 * @author me
 */
public class Predict1D {
    private final NAR nar;
 
    float t = 0;
    float dt = 0.1f;
    int discretization = 4;
    
    float nextSample() {        
        float v = (float)Math.sin(t)/2f+0.5f;
        t += dt;
        return v;        
    }
    
    /** discretize float (0..1.0) to a term */
    public String f(float p) {
        if (p < 0) {
            p = 0;
            //p = 0;
        }
        if (p > 1f) {
            p = 1f;
        }
        int i = (int) (p * discretization);        
        return Integer.toString(i);
    }
    
    public void observe(String channel, float v) {
        nar.addInput("<" + f(v) + " --> " + channel + ">. :|:");
        for (int i = 0; i < discretization; i++) {
            nar.addInput("<" + i + " --> " + channel + ">? :/:");
        }
    }
    
    public Predict1D() {
        //577.0 [1, 259.0, 156.0, 2.0, 101.0, 4.0, 16.0, 2.0, 3.0, 1.0]
        
        this.nar = new NeuromorphicNARBuilder(4).
                setTaskLinkBagSize(4).
                setTermLinkBagSize(100).   
                simulationTime().                
                build();
        nar.param().conceptForgetDurations.set(4);
        nar.param().beliefForgetDurations.set(16);
        nar.param().taskForgetDurations.set(2);
        
        new TextOutput(nar, System.out, 0.8f);
        
        nar.on(FrameStart.class, new Observer() {
            @Override public void event(Class event, Object[] arguments) {
                observe("x", nextSample());
            }            
        });
        nar.startFPS(1, 100, 1.0f);
        
    }
    
    public static void main(String[] args) {
        new Predict1D();
    }
}

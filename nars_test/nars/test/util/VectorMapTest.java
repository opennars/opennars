package nars.test.util;

import nars.core.EventEmitter.Observer;
import nars.core.Events.CycleStart;
import nars.core.NAR;
import nars.core.build.Default;
import nars.util.signal.DenoisingAutoencoder;
import nars.util.signal.UniformVector;
import nars.util.signal.VectorMap;
import org.junit.Test;

/**
 *
 * @author me
 */


public class VectorMapTest {
 
    @Test 
    public void testUniformVector() {
        NAR n = new Default().build();
        
        double[] d = new double[3];
        
        UniformVector v = new UniformVector(n, "d", d);                
        v.update();
        
        //new TextOutput(n, System.out);
        
        n.finish(16);
        
        d[1] = 1.0f;
        d[2] = 0.5f;
        
        v.update();

        n.finish(16);
        
        //TODO assert that NAR has > 1 concepts
    }
    
    @Test
    public void testAE() {
        NAR n = new Default().build();
        
        
        
        
        VectorMap v = new VectorMap(n, "d", 8, 0.25f, 2, 0.75f) {
        
            DenoisingAutoencoder d;
            
            @Override protected void map(double[] in, double[] out) {
                if (d == null)
                    d = new DenoisingAutoencoder(in.length, out.length);
                
                d.train(in, 0.05, 0);
                d.encode(in, out, true, true);                
            }          
        };
        
        
        //new TextOutput(n, System.out);
        
        v.update();
        
        n.finish(16);
        
        n.on(CycleStart.class, new Observer() {

            @Override public void event(Class event, Object[] arguments) {
                
                long t = n.time();
                if (t % 100 != 0)  return;
                
                
                for (int i = 0; i < v.input.data.length; i++)
                    v.input.data[i] = 0.5 * (1.0 + Math.sin((t+i)/20f));
                v.update();
            }
            
        });
        
        v.update();
        
        n.finish(16);
        
        //new NARSwing(n);

    }
    
    public static void main(String[] args) {
        new VectorMapTest().testAE();
    }
}

package nars.util;

import nars.NAR;
import nars.event.CycleReaction;
import nars.io.signal.UniformVector;
import nars.io.signal.VectorMap;
import nars.nar.Default;
import nars.util.event.Reaction;
import nars.util.signal.Autoencoder;
import org.junit.Test;

/**
 *
 * @author me
 */


public class VectorMapTest {
 
    @Test 
    public void testUniformVector() {
        NAR n = new NAR(new Default());
        
        double[] d = new double[3];
        
        UniformVector v = new UniformVector(n, "d", d);                
        v.update();
        
        //new TextOutput(n, System.out);
        
        n.runWhileNewInput(16);
        
        d[1] = 1.0f;
        d[2] = 0.5f;
        
        v.update();

        n.runWhileNewInput(16);
        
        //TODO assert that NAR has > 1 concepts
    }
    
    @Test
    public void testAE() {
        NAR n = new NAR(new Default());
        
        
        
        
        VectorMap v = new VectorMap(n, "d", 8, 0.25f, 2, 0.75f) {
        
            Autoencoder d;
            
            @Override protected void map(double[] in, double[] out) {
                if (d == null)
                    d = new Autoencoder(in.length, out.length);
                
                d.train(in, 0, 0.05, 0, true);
                d.encode(in, out, true, true);                
            }          
        };
        
        
        //new TextOutput(n, System.out);
        
        v.update();
        
        n.runWhileNewInput(16);

        new CycleReaction(n) {

            @Override
            public void onCycle() {

                long t = n.time();
                if (t % 100 != 0)  return;


                for (int i = 0; i < v.input.data.length; i++)
                    v.input.data[i] = 0.5 * (1.0 + Math.sin((t+i)/20f));
                v.update();
            }
        };

        v.update();
        
        n.runWhileNewInput(16);
        
        //new NARSwing(n);

    }
    
    public static void main(String[] args) {
        new VectorMapTest().testAE();
    }
}

package nars.util;

import nars.NAR;
import nars.nar.Default;
import nars.util.data.UniformVector;
import nars.util.data.VectorMap;
import nars.util.event.CycleReaction;
import nars.util.signal.Autoencoder;
import org.junit.Test;

/**
 *
 * @author me
 */


public class VectorMapTest {

    NAR n = new Default(100, 1, 1, 1);

    @Test 
    public void testUniformVector() {
        
        double[] d = new double[3];
        
        UniformVector v = new UniformVector(n, "d", d);                
        v.update();
        
        //n.log();

        n.frame(16);

        d[1] = 1.0f;
        d[2] = 0.5f;
        
        v.update();

        n.frame(16);


        //TODO assert that NAR has > 1 concepts
    }
    
    @Test
    public void testAE() {


        
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

        n.frame(16);

        new CycleReaction(n) {

            @Override
            public void onCycle() {

                long t = n.time();
                if (t % 100 != 0)  return;


                for (int i = 0; i < v.input.data.length; i++)
                    v.input.data[i] = 0.5 * (1.0 + Math.sin((t+i)/ 20.0f));
                v.update();
            }
        };

        v.update();

        n.log();

        n.frame(256);



        //new NARSwing(n);

    }

//    public static void main(String[] args) {
//        new VectorMapTest().testAE();
//    }
}

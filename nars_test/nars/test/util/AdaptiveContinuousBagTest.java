package nars.test.util;

import static java.lang.Math.exp;
import java.util.Arrays;
import nars.core.Memory;
import nars.entity.Item;
import static nars.io.Texts.n2;
import nars.storage.ContinuousBag2;
import org.junit.Test;

/**
 *
 * @author me
 */
public class AdaptiveContinuousBagTest {

/**
 *  1. there is an input curve which maps a linear X value to the exponential curve i sent before
 this is to approximate the priority -> probabilty assumption
 2. the output value of this curve is used to find an index in the sorted item list
 according to a set of control points, in this case i used 10
 this is an array of values representing the priority at indexes
 so each point is 100 indexes away from another, when the bag is full
 it can interpolate these to find an approximate index of the sorted bag where the expected priority value (output from the first step) should be found
then it removes the item at this index
* 
 * @param <I>
 * @param <K> 
 */    
    public static class AdaptiveContinuousBag<I extends Item<K>, K> extends ContinuousBag2<I, K> {

        final int resolution = 10;
        
        double[] index = new double[resolution];

        public AdaptiveContinuousBag(int capacity) {
            super(capacity, null, true);
            
            reset();
        }
        
        public int posToIndex(final int res) {
            return posToIndex( ((double)res)/((double)resolution)  );
        }
        
        public int posToIndex(final double p) {            
            return (int)(p * ((double)size()));
        }
        
        public void reset() {
            for (int i = 0; i < resolution; i++)
                index[i] = i * (((double)getCapacity()) / resolution);
        }

        public void update() {
            for (int i = 0; i < resolution; i++) {
                index[i] = items.exact(posToIndex(i)).getPriority();
            }
            //System.out.println(Arrays.toString(index));
        }
        
        //probability curve that weights priority to proportion
        double p(final double x) {            
            //ex:   1-e^(-5*x)            
            return 1 - exp(-5 * x);
        }
        
        int c(double x) {
            double y = p(x);
            int i = closestIndex(y);            
            return i;
        }
        
        public int closestIndex(double p) {
            //TODO interpolate between two indexes
            int r;
            int s = size();
            for (r = 0; r < resolution; r++) {                
                if ( index[r] > p) {
                    
                    break;                    
                }
            }  
            if (r == resolution) r = resolution-1;
            
            int i = (int)Math.round(r * ((double)s) / (resolution-1));
            if (i < 0) i = 0;
            if (i >= s) i = s-1;
            //System.out.println(/*"x=" + n2(x) + */" ..  y=" + p + "   r=" + r + " i[r]=" + index[r] + "  d=" + (p - index[r]) + " @ i=" + i);
            return i;
        }

        @Override
        public int nextRemovalIndex() {
            update();
            
            final int s = size();
            if (randomRemoval) {
                //uniform random distribution on 0..1.0
                x = Memory.randomNumber.nextFloat();
            } else {
                x += scanningRate * 1.0f / (1 + s);
                if (x >= 1.0f) {
                    x = x - 1.0f;
                }
                if (x <= 0.0f) {
                    x = x + 1.0f;
                }
            }

            return c(x);
        }

    }

    @Test
    public void test() {

    }

}

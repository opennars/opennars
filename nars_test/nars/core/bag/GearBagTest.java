/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.bag;

import nars.storage.GearBag;
import org.junit.Test;

/**
 *
 * @author me
 */
public class GearBagTest {
    
    @Test
    public void testGearBagDistribution() {
        GearBag g = new GearBag(100,1000);
        int[] r;
        //r = CurveBagTest.testRemovalPriorityDistribution(4, 5000, 0.2f, 0.2f, g);
        r = CurveBagTest.testRemovalPriorityDistribution(2, 500, 0.8f, 0.6f, g);
        /*System.out.println(Arrays.toString(r));
        System.out.println(g.size());
        System.out.println(Iterators.toString(g.iterator()));*/
        
        
        
        
    }
    
}

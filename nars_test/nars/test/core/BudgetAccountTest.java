/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.core;

import nars.core.Parameters;
import nars.entity.BudgetAccount;
import nars.entity.BudgetValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class BudgetAccountTest {
    
    final float epsilon = Parameters.BUDGET_EPSILON;
    
    @Test
    public void testBudgetAccounting() {
        BudgetValue a = new BudgetValue(1f,0,0);
        BudgetValue b = new BudgetValue(0.5f,0,0);
        BudgetAccount x = new BudgetAccount(0f);
        
        x.transfer(a, 0.75f, b, 0.75f, false);
        assertEquals(0.75f, a.getPriority(), epsilon);
        assertEquals(0.75f, b.getPriority(), epsilon);
        assertEquals(0f, x.getBalance(), epsilon);
        
        x.transfer(a, 0.5f, b, 0.5f, true);
        assertEquals(0.5f, a.getPriority(), epsilon);
        assertEquals(0.5f, b.getPriority(), epsilon);
        assertEquals(0.5f, x.getBalance(), epsilon);
        
        try {
            x.transfer(a, 1.0f, b, 0.8f, true);
            assertTrue(false);
        }
        catch (RuntimeException e) {
            assertTrue(true);
        }
        
        assertEquals(0.5f, a.getPriority(), epsilon);
        
        float spent = x.invest(a, 0.7f);
        assertEquals(0.2f, spent, epsilon);
        assertEquals(0.7f, a.getPriority(), epsilon);
        assertEquals(0.3f, x.getBalance(), epsilon);
        
        //has some but not enough
        spent = x.invest(b, 1.0f);
        assertEquals(0.3f, spent, epsilon);
        assertEquals(0.8f, b.getPriority(), epsilon);
        
        assertEquals(1.5f, a.getPriority() + b.getPriority() + x.getBalance(), epsilon);
        
        x.absorb(a, 0f);
        assertEquals(0f, a.getPriority(), epsilon);
        assertEquals(0.7f, x.getBalance(), epsilon);
        
        /*x.invest(a, 0.6f, b, 0.6f, true);
        assertEquals(0.6f, a.getPriority(), epsilon);
        assertEquals(0.6f, b.getPriority(), epsilon);
        assertEquals(0.3f, x.getBalance(), epsilon);*/
    }
    
}

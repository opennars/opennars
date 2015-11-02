package nars.budget;

import nars.meter.bag.NullItem;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


/**
 * tests the merging operators in Budget.{plus,max,average}
 */
public class BudgetMergeTest {


    @Test public void testMax() {
        ItemAccumulator<CharSequence,NullItem> a = new ItemAccumulator(8);
        NullItem x0 = new NullItem(0.5f, "x");
        NullItem x1 = new NullItem(0.1f, "x");
        NullItem y = new NullItem(0.3f, "y");
        NullItem z = new NullItem(0.1f, "z");

        a.put(x0);
        assertEquals(1, a.size());
        assertEquals(0.5f, a.pop().getPriority(), 0.001);
        a.put(x0);

        //new value with same key does not affect the higher existing value w/ max
        a.put(x1);
        assertEquals(1, a.size());
        assertEquals(0.5f, a.pop().getPriority(), 0.001);

        a.put(x0);

        //unaffected
        assertEquals(1, a.size());
        assertEquals(0.5f, a.pop().getPriority(), 0.001);

        assertEquals(0, a.size());

        a.put(z);
        a.put(y);
        assertEquals(2, a.size());
    }

    @Test public void testMean() {
        ItemAccumulator<CharSequence, NullItem> a = new ItemAccumulator(2);
        NullItem x0 = new NullItem(0.3f, "x");
        NullItem x1 = new NullItem(0.1f, "x");

        a.put(x0);
        a.put(x1);

        //new value is the average
        assertEquals(1, a.size());
        assertEquals(0.25f, a.pop().getPriority(), 0.001);

    }

    @Test
    public void testPlus() throws Exception {

        ItemAccumulator<CharSequence, NullItem> a = new ItemAccumulator(3);

        NullItem x = new NullItem(0.5f, "x");
        NullItem y = new NullItem(0.3f, "y");
        NullItem z = new NullItem(0.1f, "z");

        a.put(x);
        assertEquals(1, a.size());

        a.put(y);
        assertEquals(2, a.size());

        a.put(z);
        assertEquals(3, a.size());



        assertEquals(x, a.highest());
        assertEquals(z, a.lowest());



        a.put(new NullItem(0.25f, "x"));

        //System.out.println(a);

        assertEquals(3, a.size());
        assertEquals(0.75f, a.highest().getPriority()); //new instance but equal, does cause merging


        assertEquals(x, a.pop());
        assertEquals(y, a.pop());
        assertEquals(z, a.pop());

    }
}
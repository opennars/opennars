package nars.budget;

import nars.analyze.experimental.NullItem;
import org.junit.Test;

import java.util.Iterator;

import static junit.framework.TestCase.assertEquals;


/**
 * tests the merging operators in Budget.{plus,max,average}
 */
public class BudgetMergeTest {


    @Test public void testMax() {
        ItemAccumulator<NullItem> a = new ItemAccumulator(Budget.max);
        NullItem x0 = new NullItem(0.5f, "x");
        NullItem x1 = new NullItem(0.1f, "x");
        NullItem y = new NullItem(0.3f, "y");
        NullItem z = new NullItem(0.1f, "z");

        a.add(x0);
        a.add(x1);

        //the new value does not affect the higher existing value w/ max
        assertEquals(1, a.size());
        assertEquals(0.5f, a.removeHighest().getPriority(), 0.001);

        a.add(x0);

        //unaffected
        assertEquals(1, a.size());
        assertEquals(0.5f, a.removeHighest().getPriority(), 0.001);

        assertEquals(0, a.size());

        a.add(z);
        a.add(y);
        assertEquals(2, a.size());
    }

    @Test public void testMean() {
        ItemAccumulator<NullItem> a = new ItemAccumulator(Budget.average);
        NullItem x0 = new NullItem(0.3f, "x");
        NullItem x1 = new NullItem(0.1f, "x");

        a.add(x0);
        a.add(x1);

        //new value is the average
        assertEquals(1, a.size());
        assertEquals(0.2f, a.removeHighest().getPriority(), 0.001);

    }

    @Test
    public void testPlus() throws Exception {

        ItemAccumulator<NullItem> a = new ItemAccumulator(Budget.plus);

        NullItem x = new NullItem(0.5f, "x");
        NullItem y = new NullItem(0.3f, "y");
        NullItem z = new NullItem(0.1f, "z");

        a.add(x);
        assertEquals(1, a.size());

        a.add(y);
        assertEquals(2, a.size());

        a.add(z);
        assertEquals(3, a.size());



        assertEquals(x, a.highest());
        assertEquals(z, a.lowest());



        a.add(new NullItem(0.25f, "x"));

        //System.out.println(a);

        assertEquals(3, a.size());
        assertEquals(0.75f, a.highest().getPriority()); //new instance but equal, does cause merging


        Iterator<NullItem> ii = a.iterateHighestFirst();
        assertEquals(x, ii.next());
        assertEquals(y, ii.next());
        assertEquals(z, ii.next());

    }
}
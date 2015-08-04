package nars.budget;

import nars.analyze.experimental.NullItem;
import org.junit.Test;

import java.util.Iterator;

import static org.jgroups.util.Util.assertEquals;

/**
 * TODO test for correct accumulation of durability and quality
 */
public class ItemAccumulatorTest {

    @Test
    public void testAdd() throws Exception {

        ItemAccumulator<NullItem> a = new ItemAccumulator(new ItemComparator.Plus());

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
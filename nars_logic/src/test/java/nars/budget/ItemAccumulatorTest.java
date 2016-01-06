package nars.budget;

import nars.$;
import nars.NAR;
import nars.bag.BLink;
import nars.nar.Default;
import nars.task.Task;
import nars.util.data.MutableDouble;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;


public class ItemAccumulatorTest {

    NAR n = new Default();

    @Test
    public void testAccumulatorDeduplication() {
        TaskAccumulator ii = new TaskAccumulator(
                2 //capacity = 2 but this test will only grow to size 1 if successful
        );

        ii.getArrayBag().mergePlus();

        assertEquals(0, ii.getArrayBag().size());

        Task t = n.task("$0.1$ <a --> b>. %1.00;0.90%");
        assertEquals(0.1f, t.getPriority(), 0.001);

        ii.getArrayBag().put(t);
        assertEquals(1, ii.getArrayBag().size());

        ii.getArrayBag().put(t);
        assertEquals(1, ii.getArrayBag().size());

        ii.getArrayBag().commit();

        ii.getArrayBag().forEach(c -> System.out.println(c));

        //mergePlus:
        assertEquals(0.1f+0.1f, ii.getArrayBag().sample().getPriority(), 0.001f);

    }

    /** test batch dequeing the highest-ranking tasks from an ItemAccumulator
     *
     * */
    @Test public void testAccumulatorBatchDeque() {

        int capacity = 4;

        TaskAccumulator ii = new TaskAccumulator(
                capacity
        );


        String s = ". %1.00;0.90%";
        Task n1 = n.task("$0.05$ <z-->x>" + s);

        ii.getArrayBag().put(n1);
        ii.getArrayBag().put(n.task("$0.09$ <a-->x>" + s ));
        ii.getArrayBag().put(n.task("$0.1$ <b-->x>" + s ));
        ii.getArrayBag().put(n.task("$0.2$ <c-->x>" + s ));
        ii.getArrayBag().put(n.task("$0.3$ <d-->x>" + s ));
        ii.getArrayBag().commit();
        assertEquals(4, ii.getArrayBag().size());

        //z should be ignored
        //List<Task> buffer = Global.newArrayList();


        assertEquals(capacity, ii.getArrayBag().size());

        assertTrue(ii.getArrayBag().isSorted());

        //System.out.println(ii);
        ii.getArrayBag().top(x -> System.out.println(x));

        BLink<Task> oneLink = ii.getArrayBag().pop();
        Task one = oneLink.get();
        assertEquals("$.30;.50;.95$ <d-->x>. :0: %1.0;.90%", one.toString());

        List<Task> two = new ArrayList();
        two.add(ii.getArrayBag().pop().get());
        two.add(ii.getArrayBag().pop().get());
        assertEquals("[$.20;.50;.95$ <c-->x>. :0: %1.0;.90%, $.10;.50;.95$ <b-->x>. :0: %1.0;.90%]", two.toString());

        assertEquals(1, ii.getArrayBag().size());

//        ii.update(capacity, buffer);
//        System.out.println(buffer);
//        System.out.println(ii.size());
//        assertEquals(ii.size(), capacity);

        //batch remove should return these in order: (d,c,b|a,)

    }


    @Test public void testForEachOrder() {

        //highest first

        int capacity = 8;

        TaskAccumulator ii = new TaskAccumulator(capacity);
        assertTrue(ii.getArrayBag().isSorted());

        for (int i = 0; i < capacity - 1; i++) {
            ii.getArrayBag().put($.$("a:" + i, '?').budget( (float)Math.random() * 0.95f, 0.5f, 0.5f));
        }

        ii.getArrayBag().commit();

        MutableDouble prev = new MutableDouble(Double.POSITIVE_INFINITY);

        ii.getArrayBag().forEach( (Budgeted t) -> {
            float p = t.getBudget().getPriority();
            assertTrue(p <= prev.floatValue()); //decreasing
            prev.set(p);
        });

        //this will use an Iterator to determine sorting
        assertTrue(ii.getArrayBag().isSorted());
    }

    @Test public void testRankDurQuaForEqualPri() {   }

    @Test public void testRankQuaForEqualPriDur() {   }

    @Test public void testRankDurForEqualPriQua() {

        int capacity = 8;

        TaskAccumulator ii = new TaskAccumulator(capacity);

        for (int i = 0; i < capacity-1; i++) {
            float dur = i * 0.05f;
            ii.getArrayBag().put($.$("a:" + i, '?').budget(0.5f, dur, 0.5f));
        }

        assertTrue(ii.getArrayBag().isSorted());

        ii.print(System.out);

    }
}
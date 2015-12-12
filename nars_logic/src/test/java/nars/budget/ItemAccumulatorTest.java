package nars.budget;

import nars.$;
import nars.NAR;
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
        ii.mergePlus();
        assertEquals(0, ii.size());

        Task t = n.task("$0.1$ <a --> b>. %1.00;0.90%");
        assertEquals(0.1f, t.getPriority(), 0.001);

        ii.put(t);
        assertEquals(1, ii.size());

        ii.put(t);
        assertEquals(1, ii.size());

        //mergePlus:
        assertEquals(0.1f+0.1f, ii.peekNext().getPriority(), 0.001f);

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
        ii.put(n.task("$0.05$ <z-->x>" + s ));
        ii.put(n.task("$0.09$ <a-->x>" + s ));
        ii.put(n.task("$0.1$ <b-->x>" + s ));
        ii.put(n.task("$0.2$ <c-->x>" + s ));
        ii.put(n.task("$0.3$ <d-->x>" + s ));
        assertEquals(4, ii.size());

        //z should be ignored
        //List<Task> buffer = Global.newArrayList();


        assertEquals(capacity, ii.size());

        Task one = ii.pop();
        assertEquals("$.30;.50;.95$ <d-->x>. :0: %1.0;.90%", one.toString());

        List<Task> two = new ArrayList();
        two.add(ii.pop());
        two.add(ii.pop());
        assertEquals("[$.20;.50;.95$ <c-->x>. :0: %1.0;.90%, $.10;.50;.95$ <b-->x>. :0: %1.0;.90%]", two.toString());

        assertEquals(1, ii.size());

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
        assertTrue(ii.isSorted());

        for (int i = 0; i < capacity - 1; i++) {
            ii.put($.$("a:" + i, '?').budget( (float)Math.random() * 0.95f, 0.5f, 0.5f));
        }

        MutableDouble prev = new MutableDouble(Double.POSITIVE_INFINITY);
        ii.forEach( (Budgeted t) -> {
            float p = t.getBudget().getPriority();
            assertTrue(p <= prev.floatValue()); //decreasing
            prev.set(p);
        });

        //this will use an Iterator to determine sorting
        assertTrue(ii.isSorted());
    }

    @Test public void testRankDurQuaForEqualPri() {   }

    @Test public void testRankQuaForEqualPriDur() {   }

    @Test public void testRankDurForEqualPriQua() {

        int capacity = 8;

        TaskAccumulator ii = new TaskAccumulator(capacity);

        for (int i = 0; i < capacity-1; i++) {
            float dur = i * 0.05f;
            ii.put($.$("a:" + i, '?').budget(0.5f, dur, 0.5f));
        }

        assertTrue(ii.isSorted());

        ii.print(System.out);

    }
}
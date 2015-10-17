package nars.budget;

import nars.NAR;
import nars.nar.Terminal;
import nars.task.Task;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by me on 10/16/15.
 */
public class ItemAccumulatorTest {
    NAR n = new Terminal();

    @Test
    public void testAccumulatorDeduplication() {
        ItemAccumulator<Task> ii = new ItemAccumulator(
            Budget.plus
        );
        assertEquals(0, ii.size());

        Task t = n.task("$0.1$ <a --> b>. %1.00;0.90%");
        assertEquals(0.1f, t.getPriority(), 0.001);

        ii.add(t);
        assertEquals(1, ii.size());

        ii.add(t);
        assertEquals(1, ii.size());

        //plus:
        assertEquals(0.1f+0.1f, ii.items.values().iterator().next().getPriority(), 0.001f);

    }

    /** test batch dequeing the highest-ranking tasks from an ItemAccumulator
     *
     * */
    @Test public void testAccumulatorBatchDeque() {

        ItemAccumulator<Task> ii = new ItemAccumulator(
                Budget.plus
        );

        final int capacity = 4;

        String s = ". %1.00;0.90%";
        ii.add(n.task("$0.05$ <z --> x>" + s ));
        ii.add(n.task("$0.1$ <a --> x>" + s ));
        ii.add(n.task("$0.1$ <b --> x>" + s ));
        ii.add(n.task("$0.2$ <c --> x>" + s ));
        ii.add(n.task("$0.3$ <d --> x>" + s ));
        assertEquals(ii.size(), 5);

        //z should be ignored
        //List<Task> buffer = Global.newArrayList();

        ii.limit(capacity);
        assertEquals(capacity, ii.size());

        List<Task> one = new ArrayList();
        ii.next(1, x-> {
            one.add(x);
        });
        assertEquals("[<d --> x>. %1.00;0.90%]", one.toString());

        List<Task> two = new ArrayList();
        ii.next(2, x-> {
            two.add(x);
        });
        assertEquals("[<c --> x>. %1.00;0.90%, <b --> x>. %1.00;0.90%]", two.toString());

        assertEquals(1, ii.size());

//        ii.update(capacity, buffer);
//        System.out.println(buffer);
//        System.out.println(ii.size());
//        assertEquals(ii.size(), capacity);

        //batch remove should return these in order: (d,c,b|a,)

    }
}
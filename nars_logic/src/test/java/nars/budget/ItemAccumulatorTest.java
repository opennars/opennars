package nars.budget;

import nars.NAR;
import nars.nar.Terminal;
import nars.task.Task;
import org.junit.Test;

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

    /** test batch dequeing the highest-ranking tasks from an ItemAccumulator */
    @Test public void testAccumulatorBatchDeque() {
        ItemAccumulator<Task> ii = new ItemAccumulator(
                Budget.plus
        );

    }
}
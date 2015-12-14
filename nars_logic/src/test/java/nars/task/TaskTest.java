package nars.task;

import com.google.common.collect.Lists;
import nars.$;
import nars.truth.DefaultTruth;
import org.junit.Test;

import java.util.List;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 11/3/15.
 */
public class TaskTest {




    @Test public void testTruthHash() {
        //for TRUTH EPSILON 0.01:

        DefaultTruth dt = new DefaultTruth(0, 0.1f);
        assertEquals(9, dt.hashCode());

        DefaultTruth du = new DefaultTruth(1, 1.0f);
        assertEquals(6553700, du.hashCode());
    }

    /** tests the ordering of tasks that differ by truth values,
     * which is determined by directly comparing their int hashcode
     * representation (which is perfect and lossless hash if truth epsilon
     * is sufficiently large) */
    @Test public void testTaskOrderByTruthViaHash() {
        TreeSet<Task> t = new TreeSet<>();
        int count = 0;
        for (float f = 0; f < 1.0f; f += 0.3f)
            for (float c = 0; c < 1.0f; c += 0.3f) {
                t.add(
                    $.$("a:b", '.').truth(f, c)
                );
                count++;
            }
        assertEquals(count, t.size());

        List<Task> l = Lists.newArrayList(t);
        //l.forEach(System.out::println);
        int last = l.size() - 1;
        assertTrue(l.get(0).toString().contains("<b-->a>. :-: %.90;.90%"));
        assertTrue(l.get(last).toString().contains("<b-->a>. :-: %0.0;0.0%"));

        //test monotonically decreasing
        Task y = null;
        for (Task x : l) {
            if (y!=null) {
                assertTrue( x.getFrequency() <= y.getFrequency() );
                float c = y.getConfidence();
                if (x.getConfidence() != 0.9f) //wrap around only time when it will decrease
                    assertTrue( x.getConfidence() <= c);
            }
            y = x;
        }
    }
}

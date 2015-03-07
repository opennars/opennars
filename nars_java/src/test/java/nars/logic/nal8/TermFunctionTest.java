package nars.logic.nal8;

import nars.build.Default;
import nars.core.NAR;
import org.junit.Test;

/**
 * Created by me on 3/6/15.
 */
public class TermFunctionTest {
    public final NAR n = new NAR(new Default());

    @Test
    public void testRecursiveEvaluation1() {
        //count({ count({a,b}), 2})!
        n.input("count({ count({a,b}), 2})!");

          //      reflect(js("Math.random()"))!
    }

}

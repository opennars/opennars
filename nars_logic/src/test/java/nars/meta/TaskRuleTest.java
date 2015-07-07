package nars.meta;

import junit.framework.TestCase;
import nars.NAR;
import nars.nar.Default;
import nars.term.Term;
import org.junit.Test;

/**
 * Created by me on 7/7/15.
 */
public class TaskRuleTest extends TestCase {

    @Test
    public void testParser() {

        //NarseseParser p = NarseseParser.newMetaParser();
        NAR p = new NAR(new Default());

        Term a = p.term("<A --> b>");
        System.out.println(a);

        {
            TaskRule x = p.term("< <A --> B>, <B --> A> |- <A <-> B> >");

            assertEquals("((<B --> A>, <A --> B>), <A <-> B>)", x.toString());
            assertEquals(11, x.getMass());
        }

        {
            TaskRule x = p.term("<<A --> b> |- (X & y)>");
            assertEquals("((<A --> b>), (&, X, y))", x.toString());
            assertEquals(8, x.getMass());
        }




    }

}
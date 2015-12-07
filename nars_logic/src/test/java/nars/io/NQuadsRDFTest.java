package nars.io;

import nars.NAR;
import nars.nar.Default;
import nars.task.in.NQuadsRDF;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by me on 10/11/15.
 */
public class NQuadsRDFTest {

    @Test
    public void test1() throws Exception {
        NAR n = new Default();
        //n.stdout();
        NQuadsRDF.input(n, "<http://example.org/#spiderman> <http://xmlns.com/foaf/0.1/name> \"Человек-паук\"@ru .");
        n.frame(1);
        assertTrue(n.memory.index.size() > 2);
    }


}
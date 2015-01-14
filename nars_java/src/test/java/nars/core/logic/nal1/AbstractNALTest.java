package nars.core.logic.nal1;

import nars.core.Build;
import org.junit.After;
import org.junit.Before;


abstract public class AbstractNALTest {

    TestNAR n;

    abstract public Build build();

    @Before
    public void setup() {
        n = new TestNAR(build());
    }

    @After
    public void test() {
        n.run();
    }


}

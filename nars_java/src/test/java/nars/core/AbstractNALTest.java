package nars.core;

import org.junit.After;
import org.junit.Before;


abstract public class AbstractNALTest {

    public TestNAR n;

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

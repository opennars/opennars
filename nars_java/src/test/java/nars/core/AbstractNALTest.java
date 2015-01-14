package nars.core;

import org.junit.After;
import org.junit.Before;


abstract public class AbstractNALTest {

    public static final long randomSeed = 1;

    public TestNAR n;

    abstract public Build build();

    @Before
    public void setup() {
        n = new TestNAR(build());
    }

    @After
    public void test() {
        Memory.resetStatic(randomSeed);
        Parameters.DEBUG = true;
        n.run();
    }


}

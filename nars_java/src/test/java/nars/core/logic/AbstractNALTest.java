package nars.core.logic;

import junit.framework.TestCase;
import nars.core.Build;
import nars.core.Memory;
import nars.core.Parameters;
import nars.core.TestNAR;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AbstractNALTest extends TestCase {

    public static final long randomSeed = 1;

    public TestNAR n;


    public AbstractNALTest(Build b) {
        this.n = new TestNAR(b);
    }

    @After
    public void test() {
        Memory.resetStatic(randomSeed);
        Parameters.DEBUG = true;
        n.run();
    }

}

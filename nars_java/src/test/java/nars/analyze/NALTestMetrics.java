package nars.analyze;


import nars.build.Curve;
import nars.build.Default;
import nars.core.NewNAR;
import nars.logic.ScriptNALTest;
import org.junit.Ignore;
import org.junit.runners.Parameterized;

import java.util.Collection;

@Ignore
public class NALTestMetrics extends ScriptNALTest {

    public NALTestMetrics(NewNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(
                new String[] { "test2", "test3", "test4" },
                new Default(),
                new Default().setInternalExperience(null),
                new Curve() );
    }

    public int getMaxCycles() { return 200; }


    public static void main(String[] args) {

    }

//    @After
//    public void test() {
//        super.test();
//    }
}


package nars.logic.nal5;


import nars.build.Curve;
import nars.build.Default;
import nars.core.NewNAR;
import nars.logic.ScriptNALTest;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.ExampleFileInput.getParams;

public class NAL5ScriptTests extends ScriptNALTest {

    public NAL5ScriptTests(NewNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test5"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(5),
                new Curve());
    }


    //some of the tests will fail if given too long because their test probably depends on the initial results
    //so keep this value low, like ~500
    public int getMaxCycles() { return 300; }


}


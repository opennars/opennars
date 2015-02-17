package nars.logic.nal5;


import nars.build.Curve;
import nars.build.Default;
import nars.core.NewNAR;
import nars.logic.ScriptNALTest;
import org.junit.runners.Parameterized;

import java.util.Collection;

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

    public int getMaxCycles() { return 900; }


}


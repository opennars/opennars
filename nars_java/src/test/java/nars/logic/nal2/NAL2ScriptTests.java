package nars.logic.nal2;


import nars.build.Curve;
import nars.build.Default;
import nars.build.Discretinuous;
import nars.core.NewNAR;
import nars.logic.ScriptNALTest;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.ExampleFileInput.getParams;

public class NAL2ScriptTests extends ScriptNALTest {

    public NAL2ScriptTests(NewNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[] { "test2"},
                new Default(),
                new Default().setInternalExperience(null),
                new Default().setInternalExperience(null).level(3), //needs 3 for sets
                new Curve(),
                new Default.DefaultMicro(),
                new Discretinuous()
        );
    }

    public int getMaxCycles() { return 2500; }


}


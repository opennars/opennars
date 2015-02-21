package nars.logic.nal7;


import nars.build.Default;
import nars.core.NewNAR;
import nars.logic.ScriptNALTest;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static nars.io.ExampleFileInput.getParams;

public class NAL7ScriptTests extends ScriptNALTest {

    public NAL7ScriptTests(NewNAR b, String input) {
        super(b, input);
    }

    @Parameterized.Parameters(name= "{1} {0}")
    public static Collection configurations() {
        return getParams(new String[]{"test7"},
                new Default(),
                new Default().setInternalExperience(null));
    }

    public int getMaxCycles() { return 500; }


}

